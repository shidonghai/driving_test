package com.assistant.drivingtest.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.assistant.drivingtest.R;

public class ThirdSubjectSettingActivity extends FragmentActivity {

	private ThirdSubjectSettingFragment mFragment;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.fragment_container);

		mFragment = new ThirdSubjectSettingFragment();
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.frame_container, mFragment);
		transaction.commitAllowingStateLoss();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

		setResult(Activity.RESULT_OK);
	}
}
