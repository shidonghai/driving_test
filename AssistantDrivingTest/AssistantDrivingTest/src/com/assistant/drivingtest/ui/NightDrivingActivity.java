package com.assistant.drivingtest.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.assistant.drivingtest.R;

public class NightDrivingActivity extends FragmentActivity {

	private NightDrivingFragment mFragment;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.fragment_container);

		mFragment = new NightDrivingFragment();
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.frame_container, mFragment);
		transaction.commitAllowingStateLoss();
	}
}
