package com.assistant.drivingtest.ui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.assistant.drivingtest.R;
import com.assistant.drivingtest.db.DBManager;
import com.assistant.drivingtest.domain.Deduction;
import com.assistant.drivingtest.domain.ThirdTestItem;
import com.assistant.drivingtest.location.LocationTask;
import com.assistant.drivingtest.location.LocationTask.LocationListener;
import com.assistant.drivingtest.logic.ThirdSubjectTestManager;
import com.assistant.drivingtest.logic.ThirdSubjectTestManager.ITestItemListener;
import com.assistant.drivingtest.logic.ThirdTestItemManager;
import com.assistant.drivingtest.logic.ThirdTestItemManager.ILightTest;
import com.assistant.drivingtest.sensor.GravitySensor;
import com.assistant.drivingtest.sensor.GravitySensorObserver;
import com.assistant.drivingtest.sensor.GyroscopeSensor;
import com.assistant.drivingtest.sensor.GyroscopeSensorObserver;
import com.assistant.drivingtest.sensor.MagneticSensor;
import com.assistant.drivingtest.sensor.MagneticSensorObserver;
import com.assistant.drivingtest.sensor.filter.MeanFilter;
import com.assistant.drivingtest.utils.Constant;
import com.assistant.drivingtest.utils.MapUtil;
import com.baidu.mapapi.map.LocationData;

public class ThirdSubjectItemFragment extends Fragment implements
		OnClickListener, GravitySensorObserver, MagneticSensorObserver,
		GyroscopeSensorObserver, LocationListener {

	private static final String TAG = "zxh";

	private static final int START_SPACING = 10;

	private static final int LOAD_SUCCESS = 0;

	private static final float NS2S = 1.0f / 1000000000.0f;

	private static final int MEAN_FILTER_WINDOW = 10;

	private static final int MIN_SAMPLE_COUNT = 30;

	public static final float EPSILON = 0.000000001f;

	private ThirdSubjectAdapter mAdapter;

	private DeductionAdapter mDeductionAdapter;

	private ListView mDeductionListView;

	private TextView mTitle;

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

	private long timestampOldCalibrated = 0;

	private double mAzimuth;

	// Calibrated maths.
	private float[] currentRotationMatrixAndroid;
	private float[] deltaRotationMatrixAndroid;
	private float[] deltaRotationVectorAndroid;
	private float[] gyroscopeOrientationAndroid;

	private TextView mAngleView;

	private TextView mDistance;

	private TextView mSpeed;

	private int mCurretItemPosition;

	private ThirdTestItem mCureentTestItem;

	private LocationTask mLocationTask;

	private LocationData mLocationData;

	private boolean mStartTest = true;

	private boolean mInProcessing;

	private ThirdSubjectTestManager mTestManager;

	private List<Deduction> mDeductions = new ArrayList<Deduction>();

	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {

			case LOAD_SUCCESS:
				List<ThirdTestItem> items = (List<ThirdTestItem>) msg.obj;
				mCurretItemPosition = 0;
				if (items.size() > mCurretItemPosition) {
					mCureentTestItem = items.get(mCurretItemPosition);
				}

				mAdapter.setList(items);
				break;

			default:
				break;
			}
		};
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.third_subject_test_layout, container,
				false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		initView(view);
		Bundle bundle = getArguments();
		long id = bundle.getLong("id");
		String name = bundle.getString("name");
		mTitle.setText(name);
		Log.d(TAG, "ThirdSubjectItemFragment:" + id);
		loadLines(id);

		Toast.makeText(getActivity(), R.string.night_driving,
				Toast.LENGTH_SHORT).show();

		ThirdTestItemManager thirdTestItemManager = ThirdTestItemManager
				.getInstace();
		// thirdTestItemManager.startLightTest(getActivity());
		thirdTestItemManager.setLightTestListener(new LightTestListerner());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initMaths();
		initSensor();
		initFilters();

		mLocationTask = new LocationTask(getActivity());
		mLocationTask.setLocationListener(this);

		mTestManager = new ThirdSubjectTestManager(getActivity());
		mTestManager.setListener(new TestItemListener());
	}

	@Override
	public void onResume() {
		super.onResume();

		mLocationTask.start();

		mGravitySensor.registerGravityObserver(this);
		mMagneticSensor.registerMagneticObserver(this);
		mGyroscopeSensor.registerGyroscopeObserver(this);
	}

	@Override
	public void onPause() {
		super.onPause();

		mLocationTask.stop();

		mGravitySensor.removeGravityObserver(this);
		mMagneticSensor.removeMagneticObserver(this);
		mGyroscopeSensor.removeGyroscopeObserver(this);

		initMaths();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mTestManager.onDestroy();
		ThirdTestItemManager.getInstace().stopLightTest();
	}

	private void initView(View view) {
		ImageView back = (ImageView) view.findViewById(R.id.back);
		back.setOnClickListener(this);

		mTitle = (TextView) view.findViewById(R.id.title);
		mAngleView = (TextView) view.findViewById(R.id.angle);
		mDistance = (TextView) view.findViewById(R.id.distance);
		mSpeed = (TextView) view.findViewById(R.id.speed);

		mAngleView.setText(getString(R.string.angle, 0));
		mDistance.setText(getString(R.string.distance, MapUtil.getDistance(0)));
		mSpeed.setText(getString(R.string.speed, 0));

		mAdapter = new ThirdSubjectAdapter();
		GridView gridView = (GridView) view.findViewById(R.id.item_grid);
		gridView.setAdapter(mAdapter);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));

		mDeductionAdapter = new DeductionAdapter();
		mDeductionListView = (ListView) view.findViewById(R.id.deduction_list);
		mDeductionListView.setAdapter(mDeductionAdapter);
		mDeductionListView.setDividerHeight(0);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.back:
			getActivity().finish();
			break;

		default:
			break;
		}
	}

	private void loadLines(final long id) {
		new Thread() {
			public void run() {
				DBManager dbManager = DBManager.getInstance();
				List<ThirdTestItem> list = dbManager.getThirdTestItems(id);
				mHandler.sendMessage(mHandler.obtainMessage(LOAD_SUCCESS, list));
			};
		}.start();
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
		mGravitySensor = new GravitySensor(getActivity());
		mGyroscopeSensor = new GyroscopeSensor(getActivity());
		mMagneticSensor = new MagneticSensor(getActivity());

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

		// double b = azimuth - mAzimuth;
		mAzimuth = azimuth;
		// if (Math.abs(b) > 180) {
		// return;
		// }

		mTestManager.setAzimuth(mAzimuth);

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

	@Override
	public void onSuccess(LocationData location) {
		if (null == location) {
			return;
		}

		mLocationData = location;
		mTestManager.setLocationData(mLocationData);
		Log.d(TAG, "speed:" + location.speed);

		if (null != mCureentTestItem) {
			int distance = MapUtil.getDistanceInt(mLocationData.latitude,
					mLocationData.longitude, mCureentTestItem.latitude,
					mCureentTestItem.longitude);
			mDistance.setText(getString(R.string.distance,
					MapUtil.getDistance(distance)));

			if (!mStartTest || mInProcessing || !hasInitialOrientation) {
				return;
			}

			if (distance < START_SPACING) {
				mInProcessing = true;
				mCureentTestItem.azimuth = mAzimuth;
				mTestManager.setTestItem(mCureentTestItem);
			}

		}

	}

	@Override
	public void onFail() {

	}

	private class LightTestListerner implements ILightTest {

		@Override
		public void onLightComplete() {
			mStartTest = true;
		}

	}

	private class TestItemListener implements ITestItemListener {

		@Override
		public void onItemComplete() {

			mCurretItemPosition = mCurretItemPosition + 1;
			if (mCurretItemPosition == mAdapter.getCount()) {
				mCureentTestItem = null;

				mTestManager.setFinish(true);
				mTestManager
						.play(mTestManager.getResult() < 0 ? Constant.THIRD_TEST_FAIL_VOICE
								: Constant.THIRD_TEST_SUCCESS_VOICE);
				// getActivity().finish();
			} else {
				mCureentTestItem = mAdapter.getItem(mCurretItemPosition);
				mAdapter.notifyDataSetChanged();
			}

			mInProcessing = false;

		}

		@Override
		public void setAngle(double angle) {
			DecimalFormat decimalFormat = new DecimalFormat("0.00");
			mAngleView.setText(getString(R.string.angle,
					decimalFormat.format(angle)));
		}

		@Override
		public void testFail(Deduction deduction) {

			mCurretItemPosition = mCurretItemPosition + 1;
			if (mCurretItemPosition == mAdapter.getCount()) {
				mCureentTestItem = null;
				mTestManager.setFinish(true);
				mTestManager
						.play(mTestManager.getResult() < 0 ? Constant.THIRD_TEST_FAIL_VOICE
								: Constant.THIRD_TEST_SUCCESS_VOICE);

			} else {
				mCureentTestItem = mAdapter.getItem(mCurretItemPosition);
				mAdapter.notifyDataSetChanged();
			}

			if (null != deduction) {
				mDeductions.add(deduction);
				mDeductionAdapter.notifyDataSetChanged();
			}

			mInProcessing = false;
			mDeductionListView.setSelection(mDeductionAdapter.getCount());

		}

	}

	private class ThirdSubjectAdapter extends BaseAdapter {

		private List<ThirdTestItem> mList;

		@Override
		public int getCount() {
			return mList == null ? 0 : mList.size();
		}

		@Override
		public ThirdTestItem getItem(int arg0) {
			return mList == null ? null : mList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public boolean isEnabled(int position) {
			return false;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			if (null == arg1) {
				arg1 = LayoutInflater.from(getActivity()).inflate(
						R.layout.third_subject_item, arg2, false);
			}

			TextView name = (TextView) arg1.findViewById(R.id.name);
			name.setText(mList.get(arg0).name);

			arg1.setBackgroundResource(mCurretItemPosition == arg0 ? R.drawable.third_item_select
					: R.drawable.third_item);
			name.setTextColor(mCurretItemPosition == arg0 ? getResources()
					.getColor(R.color.white) : getResources().getColor(
					R.color.third_item));

			return arg1;
		}

		public void setList(List<ThirdTestItem> list) {
			mList = list;
			notifyDataSetChanged();
		}

	}

	private class DeductionAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mDeductions.size();
		}

		@Override
		public Deduction getItem(int arg0) {
			return mDeductions.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public boolean isEnabled(int position) {
			return false;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			ViewHolder holder;
			if (null == arg1) {
				arg1 = LayoutInflater.from(getActivity()).inflate(
						R.layout.deduction_layout, arg2, false);

				holder = new ViewHolder();
				holder.name = (TextView) arg1.findViewById(R.id.deduction_name);
				holder.reason = (TextView) arg1
						.findViewById(R.id.deduction_reason);
				holder.scores = (TextView) arg1
						.findViewById(R.id.deduction_scores);
				arg1.setTag(holder);
			} else {
				holder = (ViewHolder) arg1.getTag();
			}

			Deduction deduction = mDeductions.get(arg0);
			if (null != deduction) {
				holder.name.setText(deduction.name);
				holder.reason.setText(deduction.reason);
				holder.scores.setText(String.valueOf(deduction.scores));
			}

			return arg1;
		}

		private class ViewHolder {
			TextView name;
			TextView reason;
			TextView scores;
		}

	}

}
