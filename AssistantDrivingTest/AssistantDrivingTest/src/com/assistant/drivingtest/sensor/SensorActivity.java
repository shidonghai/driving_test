package com.assistant.drivingtest.sensor;

import java.util.Arrays;

import android.app.Activity;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import com.assistant.drivingtest.R;
import com.assistant.drivingtest.sensor.filter.MeanFilter;

public class SensorActivity extends Activity implements GravitySensorObserver,
		MagneticSensorObserver, GyroscopeSensorObserver {

	private static final float NS2S = 1.0f / 1000000000.0f;

	private static final int MEAN_FILTER_WINDOW = 10;

	private static final int MIN_SAMPLE_COUNT = 30;

	public static final float EPSILON = 0.000000001f;

	private final String TAG = "sensor";

	private GravitySensor mGravitySensor;

	private GyroscopeSensor mGyroscopeSensor;

	private MagneticSensor mMagneticSensor;

	private int accelerationSampleCount = 0;

	private int magneticSampleCount = 0;

	private boolean hasInitialOrientation = false;
	private boolean stateInitializedAndroid = false;

	private MeanFilter gravityFilter;

	private MeanFilter magneticFilter;

	// accelerometer vector
	private float[] gravity;

	// magnetic field vector
	private float[] magnetic;

	// accelerometer and magnetometer based rotation matrix
	private float[] initialRotationMatrix;

	// Calibrated maths.
	private float[] currentRotationMatrixAndroid;
	private float[] deltaRotationMatrixAndroid;
	private float[] deltaRotationVectorAndroid;
	private float[] gyroscopeOrientationAndroid;

	private long timestampOldCalibrated = 0;

	private TextView mAzimuthView;

	private double mAzimuth;

	private double mChange;

	private int mStraight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sensor_test);

		mAzimuthView = (TextView) findViewById(R.id.azimuth);

		initMaths();
		initSensor();
		initFilters();
	}

	@Override
	protected void onStart() {
		super.onStart();

		mGravitySensor.registerGravityObserver(this);
		mMagneticSensor.registerMagneticObserver(this);
		mGyroscopeSensor.registerGyroscopeObserver(this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		mGravitySensor.removeGravityObserver(this);
		mMagneticSensor.removeMagneticObserver(this);
		mGyroscopeSensor.removeGyroscopeObserver(this);

		initMaths();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Initialize the data structures required for the maths.
	 */
	private void initMaths() {
		gravity = new float[3];
		magnetic = new float[3];

		initialRotationMatrix = new float[9];

		deltaRotationVectorAndroid = new float[4];
		deltaRotationMatrixAndroid = new float[9];
		currentRotationMatrixAndroid = new float[9];
		gyroscopeOrientationAndroid = new float[3];

		// Initialize the current rotation matrix as an identity matrix...
		currentRotationMatrixAndroid[0] = 1.0f;
		currentRotationMatrixAndroid[4] = 1.0f;
		currentRotationMatrixAndroid[8] = 1.0f;
	}

	private void initSensor() {
		mGravitySensor = new GravitySensor(this);
		mGyroscopeSensor = new GyroscopeSensor(this);
		mMagneticSensor = new MagneticSensor(this);

	}

	/**
	 * Initialize the mean filters.
	 */
	private void initFilters() {
		gravityFilter = new MeanFilter();
		gravityFilter.setWindowSize(MEAN_FILTER_WINDOW);

		magneticFilter = new MeanFilter();
		magneticFilter.setWindowSize(MEAN_FILTER_WINDOW);
	}

	@Override
	public void onGravitySensorChanged(float[] gravity, long timeStamp) {

		// Get a local copy of the raw magnetic values from the device sensor.
		System.arraycopy(gravity, 0, this.gravity, 0, gravity.length);

		// Use a mean filter to smooth the sensor inputs
		this.gravity = gravityFilter.filterFloat(this.gravity);

		// Count the number of samples received.
		accelerationSampleCount++;

		// Only determine the initial orientation after the acceleration sensor
		// and magnetic sensor have had enough time to be smoothed by the mean
		// filters. Also, only do this if the orientation hasn't already been
		// determined since we only need it once.
		if (accelerationSampleCount > MIN_SAMPLE_COUNT
				&& magneticSampleCount > MIN_SAMPLE_COUNT
				&& !hasInitialOrientation) {
			calculateOrientation();
		}
	}

	@Override
	public void onMagneticSensorChanged(float[] magnetic, long timeStamp) {
		// Get a local copy of the raw magnetic values from the device sensor.
		System.arraycopy(magnetic, 0, this.magnetic, 0, magnetic.length);

		// Use a mean filter to smooth the sensor inputs
		this.magnetic = magneticFilter.filterFloat(this.magnetic);

		// Count the number of samples received.
		magneticSampleCount++;
	}

	/**
	 * Calculates orientation angles from accelerometer and magnetometer output.
	 * Note that we only use this *once* at the beginning to orient the
	 * gyroscope to earth frame. If you do not call this, the gyroscope will
	 * orient itself to whatever the relative orientation the device is in at
	 * the time of initialization.
	 */
	private void calculateOrientation() {
		hasInitialOrientation = SensorManager.getRotationMatrix(
				initialRotationMatrix, null, gravity, magnetic);

		// Remove the sensor observers since they are no longer required.
		if (hasInitialOrientation) {
			mGravitySensor.removeGravityObserver(this);
			mMagneticSensor.removeMagneticObserver(this);

			Log.d("zxh", Arrays.toString(initialRotationMatrix));
		}
	}

	@Override
	public void onGyroscopeSensorChanged(float[] gyroscope, long timestamp) {
		// don't start until first accelerometer/magnetometer orientation has
		// been acquired
		if (!hasInitialOrientation) {
			return;
		}

		// Initialization of the gyroscope based rotation matrix
		if (!stateInitializedAndroid) {
			currentRotationMatrixAndroid = matrixMultiplication(
					currentRotationMatrixAndroid, initialRotationMatrix);

			stateInitializedAndroid = true;
		}

		// This timestep's delta rotation to be multiplied by the current
		// rotation after computing it from the gyro sample data.
		if (timestampOldCalibrated != 0 && stateInitializedAndroid) {
			final float dT = (timestamp - timestampOldCalibrated) * NS2S;

			// Axis of the rotation sample, not normalized yet.
			float axisX = gyroscope[0];
			float axisY = gyroscope[1];
			float axisZ = gyroscope[2];

			// Calculate the angular speed of the sample
			float omegaMagnitude = (float) Math.sqrt(axisX * axisX + axisY
					* axisY + axisZ * axisZ);

			// Normalize the rotation vector if it's big enough to get the axis
			if (omegaMagnitude > EPSILON) {
				axisX /= omegaMagnitude;
				axisY /= omegaMagnitude;
				axisZ /= omegaMagnitude;
			}

			// Integrate around this axis with the angular speed by the timestep
			// in order to get a delta rotation from this sample over the
			// timestep. We will convert this axis-angle representation of the
			// delta rotation into a quaternion before turning it into the
			// rotation matrix.
			float thetaOverTwo = omegaMagnitude * dT / 2.0f;

			float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
			float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);

			deltaRotationVectorAndroid[0] = sinThetaOverTwo * axisX;
			deltaRotationVectorAndroid[1] = sinThetaOverTwo * axisY;
			deltaRotationVectorAndroid[2] = sinThetaOverTwo * axisZ;
			deltaRotationVectorAndroid[3] = cosThetaOverTwo;

			SensorManager.getRotationMatrixFromVector(
					deltaRotationMatrixAndroid, deltaRotationVectorAndroid);

			currentRotationMatrixAndroid = matrixMultiplication(
					currentRotationMatrixAndroid, deltaRotationMatrixAndroid);

			SensorManager.getOrientation(currentRotationMatrixAndroid,
					gyroscopeOrientationAndroid);
		}

		timestampOldCalibrated = timestamp;

		float azimuth = (float) (Math.toDegrees(gyroscopeOrientationAndroid[0]) + 360) % 360;

		double b = azimuth - mAzimuth;
		mAzimuth = azimuth;
		if (Math.abs(b) > 180) {
			return;
		}

		if (Math.abs(b) < 0.01) {
			mStraight = mStraight + 1;
			if (mStraight > 30) {
				mChange = 0;
				mStraight = 0;
			}
		} else {
			mChange = mChange + b;
		}
		Log.d("zxh", b + " " + mChange);

		if (Math.abs(mChange) > 150) {
			mAzimuthView.setText("掉头");
		} else if (mChange > 2) {
			mAzimuthView.setText(" 右转");
		} else if (mChange < -2) {
			mAzimuthView.setText(" 左转");
		} else {
			mAzimuthView.setText(" 直行");
		}
		
		mAzimuthView.setText(mAzimuthView.getText() + "\n" + b + "  " + mAzimuth);

		// mAzimuth.setText(String.valueOf(azimuth));
		// Log.d(TAG, "azimuth:" + azimuth);
		//
		// Log.d("zxh",
		// gyroscopeOrientationAndroid[0] + "  "
		// + Math.toDegrees(gyroscopeOrientationAndroid[0]) + "  "
		// + azimuth);
	}

	/**
	 * Multiply matrix a by b. Android gives us matrices results in
	 * one-dimensional arrays instead of two, so instead of using some (O)2 to
	 * transfer to a two-dimensional array and then an (O)3 algorithm to
	 * multiply, we just use a static linear time method.
	 * 
	 * @param a
	 * @param b
	 * @return a*b
	 */
	private float[] matrixMultiplication(float[] a, float[] b) {
		float[] result = new float[9];

		result[0] = a[0] * b[0] + a[1] * b[3] + a[2] * b[6];
		result[1] = a[0] * b[1] + a[1] * b[4] + a[2] * b[7];
		result[2] = a[0] * b[2] + a[1] * b[5] + a[2] * b[8];

		result[3] = a[3] * b[0] + a[4] * b[3] + a[5] * b[6];
		result[4] = a[3] * b[1] + a[4] * b[4] + a[5] * b[7];
		result[5] = a[3] * b[2] + a[4] * b[5] + a[5] * b[8];

		result[6] = a[6] * b[0] + a[7] * b[3] + a[8] * b[6];
		result[7] = a[6] * b[1] + a[7] * b[4] + a[8] * b[7];
		result[8] = a[6] * b[2] + a[7] * b[5] + a[8] * b[8];

		return result;
	}
}
