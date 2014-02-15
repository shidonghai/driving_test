package com.assistant.drivingtest.logic.test;import android.content.Context;import com.assistant.drivingtest.R;import com.assistant.drivingtest.domain.Deduction;import com.assistant.drivingtest.domain.ThirdTestItem;import com.assistant.drivingtest.logic.ThirdSubjectTestManager;import com.assistant.drivingtest.utils.Constant;import com.baidu.mapapi.map.LocationData;public class DrivingStraightTestObject extends TestObject {	public DrivingStraightTestObject(Context context,			ThirdSubjectTestManager testManager, ThirdTestItem item) {		super(context, testManager, item);		mHandler.sendEmptyMessageDelayed(SUCCESS, 3000);	}	@Override	public void setAzimuth(double azimuth) {		if (Math.abs(azimuth) > Constant.CHANGE_LINE) {			mHandler.removeMessages(SUCCESS);			Deduction deduction = new Deduction();			deduction.name = mTestItem.name;			deduction.reason = mContext.getString(R.string.deduction_message);			deduction.scores = -100;			mTestManager.testFail(deduction);		}	}	@Override	public void setLocation(LocationData locationData) {	}	@Override	public void setSpeed(double speed) {			}}