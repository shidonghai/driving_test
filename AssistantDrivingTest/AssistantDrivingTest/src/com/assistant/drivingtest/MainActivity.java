package com.assistant.drivingtest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.assistant.drivingtest.db.DBManager;
import com.assistant.drivingtest.logic.ThirdTestItemManager;
import com.assistant.drivingtest.ui.AboutActivity;
import com.assistant.drivingtest.ui.CriteriaActivity;
import com.assistant.drivingtest.ui.SecondSubjectActivity;
import com.assistant.drivingtest.ui.ThirdSubjectActivity;

public class MainActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initView();
		new InitThread().start();
	}

	private void initView() {
		RelativeLayout secondSubject = (RelativeLayout) findViewById(R.id.second_subject);
		secondSubject.setOnClickListener(this);

		RelativeLayout thirdSubject = (RelativeLayout) findViewById(R.id.third_subject);
		thirdSubject.setOnClickListener(this);

		RelativeLayout about = (RelativeLayout) findViewById(R.id.about);
		about.setOnClickListener(this);

		LinearLayout secondCriteria = (LinearLayout) findViewById(R.id.second_criteria);
		secondCriteria.setOnClickListener(this);

		LinearLayout thirdCriteria = (LinearLayout) findViewById(R.id.third_criteria);
		thirdCriteria.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		DBManager.getInstance().close();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.second_subject:
			Intent intent = new Intent(this, SecondSubjectActivity.class);
			startActivity(intent);
			break;

		case R.id.third_subject:
			Intent third = new Intent(this, ThirdSubjectActivity.class);
			startActivity(third);
			break;

		case R.id.about:
			Intent about = new Intent(this, AboutActivity.class);
			startActivity(about);
			break;

		case R.id.second_criteria:
			Intent secondCriteria = new Intent(this, CriteriaActivity.class);
			secondCriteria.putExtra("title", R.string.second_criteria_title);
			secondCriteria.putExtra("text", R.raw.second_criteria);
			secondCriteria.putExtra("color",
					getResources().getColor(R.color.second_title_color));
			startActivity(secondCriteria);
			break;

		case R.id.third_criteria:
			Intent thirdCriteria = new Intent(this, CriteriaActivity.class);
			thirdCriteria.putExtra("title", R.string.third_criteria_title);
			thirdCriteria.putExtra("text", R.raw.third_criteria);
			thirdCriteria.putExtra("color",
					getResources().getColor(R.color.third_title_color));
			startActivity(thirdCriteria);
			break;

		default:
			break;
		}

	}

	private class InitThread extends Thread {

		@Override
		public void run() {
			ThirdTestItemManager.getInstace().init(getApplicationContext());
			DBManager.getInstance().init(getApplicationContext());
		}
	}

}
