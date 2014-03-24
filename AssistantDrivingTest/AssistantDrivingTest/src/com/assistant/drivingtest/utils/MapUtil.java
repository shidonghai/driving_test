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

	// 计算方位角pab。
	public static double gps2d(double lat_a, double lng_a, double lat_b,
			double lng_b) {
		double d = 0;
		lat_a = lat_a * Math.PI / 180;
		lng_a = lng_a * Math.PI / 180;
		lat_b = lat_b * Math.PI / 180;
		lng_b = lng_b * Math.PI / 180;

		d = Math.sin(lat_a) * Math.sin(lat_b) + Math.cos(lat_a)
				* Math.cos(lat_b) * Math.cos(lng_b - lng_a);
		d = Math.sqrt(1 - d * d);
		d = Math.cos(lat_b) * Math.sin(lng_b - lng_a) / d;
		d = Math.asin(d) * 180 / Math.PI;

		// d = Math.round(d*10000);
		return d;
	}
}
