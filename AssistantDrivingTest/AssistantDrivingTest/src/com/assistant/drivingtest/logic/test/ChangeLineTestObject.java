package com.assistant.drivingtest.logic.test;import android.content.Context;import com.assistant.drivingtest.R;import com.assistant.drivingtest.domain.Deduction;import com.assistant.drivingtest.domain.ThirdTestItem;import com.assistant.drivingtest.logic.ThirdSubjectTestManager;import com.assistant.drivingtest.utils.Constant;import com.assistant.drivingtest.utils.MapUtil;import com.baidu.mapapi.map.LocationData;public class ChangeLineTestObject extends TestObject {	public ChangeLineTestObject(Context context,			ThirdSubjectTestManager testManager, ThirdTestItem item) {		super(context, testManager, item);		mKeepDirection = true;		checkDirection();	}	@Override	public void setAzimuth(double azimuth) {		if (Math.abs(azimuth) > Constant.CHANGE_LINE) {			if (mKeepDirection) {				Deduction deduction = new Deduction();				deduction.name = mTestItem.name;				deduction.reason = mContext						.getString(R.string.deduction_message_keep_direction);				deduction.scores = -10;				mTestManager.addDeduction(deduction);				mKeepDirection = false;			} else {				mHandler.removeMessages(FAIL);				mTestManager.testSuccess();			}		}	}	@Override	public void setLocation(LocationData locationData) {		int dis = MapUtil.getDistanceInt(locationData.latitude,				locationData.longitude, mTestItem.voiceLatitude,				mTestItem.voiceLongitude);		int disEnd = MapUtil.getDistanceInt(locationData.latitude,				locationData.longitude, mTestItem.endLatitude,				mTestItem.endLongitude);		if (dis > mTestItem.distance || disEnd <= Constant.DISTANCE_OK) {			mHandler.removeMessages(FAIL);			Deduction deduction = new Deduction();			deduction.name = mTestItem.name;			deduction.reason = mContext.getString(R.string.deduction_message);			deduction.scores = -100;			mTestManager.testFail(deduction);		}	}	@Override	public void setSpeed(double speed) {	}}