package com.assistant.drivingtest.location;

import android.content.Context;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.LocationData;

public class LocationTask {
	private LocationClient mLocClient;
	private LocationData locData;

	private LocationListener mLocationListener;

	public void setLocationListener(LocationListener listener) {
		this.mLocationListener = listener;
	}

	private BDLocationListener mListener = new BDLocationListener() {

		@Override
		public void onReceiveLocation(BDLocation location) {

			// Warning: At the beginning, location listener will receive several
			// "null" location data, and then the correct data will come, so, we
			// should not depend on this callback which only be called for one
			// time, what we should do is to listen for a period of time if
			// "correct data" comes in this period, we say it got location
			// succeeded, no matter whether if it received null location.

			int waitTime = 0;

			if (location == null) {
				if (waitTime >= 5) {

					Log.w("WiFi", getClass().getName() + "wait time = "
							+ waitTime);
					if (mLocationListener != null) {
						mLocationListener.onFail();
					}

					mLocClient.unRegisterLocationListener(mListener);
					mLocClient.stop();
				}

				waitTime++;
				return;
			}

			locData.latitude = location.getLatitude();
			locData.longitude = location.getLongitude();
			locData.accuracy = location.getRadius();
			locData.direction = location.getDerect();
			locData.speed = location.getSpeed();

			if (mLocationListener != null) {
				mLocationListener.onSuccess(locData);
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {

			if (poiLocation == null) {
				return;
			}
		}
	};

	public LocationTask(Context ctx) {
		mLocClient = new LocationClient(ctx);
		locData = new LocationData();
		mLocClient.registerLocationListener(mListener);

		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000);
		option.setPriority(LocationClientOption.GpsFirst);
		mLocClient.setLocOption(option);
	}

	public void start() {
		if (mLocClient.isStarted()) {
			mLocClient.stop();
		}
		mLocClient.start();
	}

	public void stop() {
		if (null != mLocClient && mLocClient.isStarted()) {
			mLocClient.stop();
		}
	}

	public static interface LocationListener {
		public void onSuccess(LocationData location);

		public void onFail();
	}

}
