package com.assistant.drivingtest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.assistant.drivingtest.R;
import com.assistant.drivingtest.db.DBManager;
import com.assistant.drivingtest.ui.LocationOverlay.locationOverlay;
import com.assistant.drivingtest.utils.LogUtil;

public class ThirdSubjectItemActivity extends FragmentActivity {

	private ThirdSubjectItemFragment mFragment;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.fragment_container);

		mFragment = new ThirdSubjectItemFragment();

		Bundle bundle = new Bundle();
		Intent intent = getIntent();
		bundle.putLong("id", intent.getLongExtra("id", -1));
		bundle.putString("name", intent.getStringExtra("name"));
		mFragment.setArguments(bundle);

		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.frame_container, mFragment);
		transaction.commitAllowingStateLoss();
	}
}
