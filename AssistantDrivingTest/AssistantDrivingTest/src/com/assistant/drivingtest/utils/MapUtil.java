package com.assistant.drivingtest.utils;

import android.location.Location;

public class MapUtil {
	public static int getDistanceInt(double lat1, double lng1, double lat2,
			double lng2) {
		LogUtil.d("distance", lat1 + "," + lng1 + "|" + lat2 + "," + lng2);
		float[] result = new float[1];
		Location.distanceBetween(lat1, lng1, lat2, lng2, result);
		return (int) result[0];
	}

	public static String getDistance(int distance) {
		int unit = 1;
		if (distance > 1000) {
			distance /= 1000;
			unit++;
		}
		String unitString = null;
		switch (unit) {
		case 1:
			unitString = "m";
			break;
		case 2:
			unitString = "km";
			break;
		}
		return distance + unitString;
	}

	public static String getDistance(double lat1, double lng1, double lat2,
			double lng2) {
		LogUtil.d("distance", lat1 + "," + lng1 + "|" + lat2 + "," + lng2);
		int distance = getDistanceInt(lat1, lng1, lat2, lng2);
		return getDistance(distance);
	}
}
