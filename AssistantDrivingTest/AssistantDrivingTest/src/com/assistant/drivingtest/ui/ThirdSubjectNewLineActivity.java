package com.assistant.drivingtest.ui;import com.assistant.drivingtest.R;import android.os.Bundle;import android.support.v4.app.FragmentActivity;import android.support.v4.app.FragmentTransaction;public class ThirdSubjectNewLineActivity extends FragmentActivity {	private ThirdSubjectNewLineFragment mFragment;	@Override	protected void onCreate(Bundle arg0) {		super.onCreate(arg0);		setContentView(R.layout.fragment_container);		mFragment = new ThirdSubjectNewLineFragment();		FragmentTransaction transaction = getSupportFragmentManager()				.beginTransaction();		transaction.replace(R.id.frame_container, mFragment);		transaction.commitAllowingStateLoss();	}}