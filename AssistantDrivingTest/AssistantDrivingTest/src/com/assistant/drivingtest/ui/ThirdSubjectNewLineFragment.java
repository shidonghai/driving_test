package com.assistant.drivingtest.ui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.assistant.drivingtest.R;
import com.assistant.drivingtest.db.DBManager;
import com.assistant.drivingtest.domain.ThirdSubject;
import com.assistant.drivingtest.domain.ThirdTestItem;
import com.assistant.drivingtest.location.LocationTask;
import com.assistant.drivingtest.location.LocationTask.LocationListener;
import com.assistant.drivingtest.logic.ThirdTestItemManager.TestItem;
import com.assistant.drivingtest.logic.ThirdTestItemManager.Type;
import com.assistant.drivingtest.utils.LogUtil;
import com.assistant.drivingtest.utils.Util;
import com.assistant.drivingtest.widget.dialog.BaseDialog.DialogListener;
import com.assistant.drivingtest.widget.dialog.MessageDialog;
import com.assistant.drivingtest.widget.dialog.MessageDialog.MessageDialogListener;
import com.baidu.mapapi.map.LocationData;

public class ThirdSubjectNewLineFragment extends Fragment implements
		OnClickListener, LocationListener, OnItemClickListener {

	private static final String TAG = "zxh";

	private LocationTask mLocationTask;

	private TextView mGPS;

	private EditText mSpeed;

	private ThirdItemsAdapter mAdapter;

	private ThirdSubject mThirdSubject;

	private List<ThirdTestItem> mTestItems;

	private LocationData mLocationData;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.third_subject_new_line, container,
				false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mLocationTask = new LocationTask(getActivity());
		mLocationTask.setLocationListener(this);

		mThirdSubject = new ThirdSubject();
		mTestItems = new ArrayList<ThirdTestItem>();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		initView(view);
	}

	@Override
	public void onResume() {
		super.onResume();

		mLocationTask.start();
	}

	@Override
	public void onPause() {
		super.onPause();

		mLocationTask.stop();
	}

	private void initView(View view) {
		ImageView back = (ImageView) view.findViewById(R.id.back);
		back.setOnClickListener(this);

		TextView save = (TextView) view.findViewById(R.id.save);
		save.setOnClickListener(this);

		mGPS = (TextView) view.findViewById(R.id.gps);
		mSpeed = (EditText) view.findViewById(R.id.speed_limit);

		mAdapter = new ThirdItemsAdapter(getActivity());
		GridView gridView = (GridView) view.findViewById(R.id.item_grid);
		gridView.setAdapter(mAdapter);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gridView.setOnItemClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.back:
			getActivity().finish();
			break;

		case R.id.save:
			save();
			break;
		default:
			break;
		}
	}

	private void save() {
		if (mTestItems.size() == 0) {
			Toast.makeText(getActivity(), R.string.test_item_empty,
					Toast.LENGTH_SHORT).show();
			return;
		}

		DBManager dbManager = DBManager.getInstance();

		int size = dbManager.getSubjectSize();
		String name;
		// if (size == 0) {
		// name = getString(R.string.new_subject_name,
		// Util.convertToRightFormat("1"));
		// } else {
		long id = dbManager.getThirdSubjectTableMaxId();
		LogUtil.d(TAG, "id:" + id);
		name = getString(R.string.new_subject_name,
				Util.convertToRightFormat(String.valueOf(id + 1)));
		// }
		LogUtil.d(TAG, "name:" + name);

		mThirdSubject.name = name;
		mThirdSubject.items = mTestItems;
		boolean result = dbManager.saveNewLine(mThirdSubject);
		Toast.makeText(getActivity(),
				result ? R.string.save_success : R.string.save_fail,
				Toast.LENGTH_SHORT).show();

		getActivity().setResult(Activity.RESULT_OK);
		getActivity().finish();

	}

	@Override
	public void onSuccess(LocationData location) {

		mLocationData = location;
		if (null == mLocationData) {
			return;
		}

		DecimalFormat decimalFormat = new DecimalFormat("#.00");
		// mGPS.setText(decimalFormat.format(location.longitude) + ","
		// + decimalFormat.format(location.latitude));
		mGPS.setText(getString(R.string.gps_res,
				decimalFormat.format(location.longitude),
				decimalFormat.format(location.latitude)));

		Log.d(TAG, "speed:" + mLocationData.speed);

	}

	@Override
	public void onFail() {
		Log.d(TAG, "location onFail");
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		TestItem item = mAdapter.getItem(arg2);

		// if (Type.START == item.type) {
		// MessageDialog dialog = new MessageDialog(getActivity());
		// dialog.setTitle(item.name);
		// dialog.setDialogListener(new StartItemDialogListener());
		// dialog.setMessage(getString(R.string.start));
		// dialog.show();
		// }

		switch (item.type) {

		case Type.START:
			MessageDialog dialog = new MessageDialog(getActivity());
			dialog.setTitle(item.name);
			dialog.setDialogListener(new StartItemDialogListener());
			dialog.setMessage(getString(R.string.start));
			dialog.show();
			break;

		case Type.END:
			break;

		default:
			MessageDialog itmeDialog = new MessageDialog(getActivity(),
					item.hasStart);
			itmeDialog.setTitle(item.name);
			itmeDialog.setDialogListener(new TestItemDialogListener(item));
			itmeDialog.setMessage(getString(R.string.add_item));
			itmeDialog.show();
			break;
		}
	}

	private class StartItemDialogListener implements DialogListener {

		@Override
		public void onConfirmed() {
			mAdapter.onStartItemClick();
		}

	}

	private class TestItemDialogListener implements MessageDialogListener {

		private TestItem testItem;
		ThirdTestItem thirdItem;

		public TestItemDialogListener(TestItem item) {
			testItem = item;
			thirdItem = new ThirdTestItem();
		}

		@Override
		public void onVoiceClick() {
			if (null != mLocationData) {
				thirdItem.voiceLatitude = mLocationData.latitude;
				thirdItem.voiceLongitude = mLocationData.longitude;
			} else {
				Toast.makeText(getActivity(), R.string.gps_error,
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onStartClick() {
			if (null != mLocationData) {
				thirdItem.startLatitude = mLocationData.latitude;
				thirdItem.startLongitude = mLocationData.longitude;
			} else {
				Toast.makeText(getActivity(), R.string.gps_error,
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onEndClick() {
			if (null != mLocationData) {
				thirdItem.endLatitude = mLocationData.latitude;
				thirdItem.endLongitude = mLocationData.longitude;
				thirdItem.name = testItem.name;
				thirdItem.type = testItem.type;
				thirdItem.voice = testItem.voice;
				thirdItem.speed = Double.valueOf(mSpeed.getText().toString()
						.trim());

				mTestItems.add(thirdItem);

			} else {
				Toast.makeText(getActivity(), R.string.gps_error,
						Toast.LENGTH_SHORT).show();
			}
		}

	}
}
