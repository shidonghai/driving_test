package com.assistant.drivingtest.ui;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.assistant.drivingtest.R;
import com.assistant.drivingtest.db.DBManager;
import com.assistant.drivingtest.domain.ThirdSubject;

public class ThirdSubjectFragment extends Fragment implements OnClickListener,
		OnItemClickListener {

	private static final String TAG = "zxh";

	private static final int LOAD_SUCCESS = 0;

	private ThirdSubjectAdapter mAdapter;

	private TextView mEmptyView;

	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {

			case LOAD_SUCCESS:
				List<ThirdSubject> list = (List<ThirdSubject>) msg.obj;
				if (null == list || list.size() == 0) {
					mEmptyView.setText(R.string.third_subject_empty);
				} else {
					mEmptyView.setVisibility(View.GONE);
				}
				mAdapter.setList(list);
				break;

			default:
				break;
			}
		};
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.third_subject, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		initView(view);
		loadLines();
	}

	private void initView(View view) {
		ImageView back = (ImageView) view.findViewById(R.id.back);
		back.setOnClickListener(this);

		ImageView setting = (ImageView) view.findViewById(R.id.setting);
		setting.setOnClickListener(this);

		View nightDriving = view.findViewById(R.id.night_driving);
		nightDriving.setOnClickListener(this);

		mEmptyView = (TextView) view.findViewById(R.id.empty_view);
		ListView listView = (ListView) view.findViewById(R.id.third_list);
		mAdapter = new ThirdSubjectAdapter();
		listView.setAdapter(mAdapter);
		listView.setDividerHeight(0);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.back:
			getActivity().finish();
			break;

		case R.id.setting:
			Intent third = new Intent(getActivity(),
					ThirdSubjectSettingActivity.class);
			startActivityForResult(third, 0);
			break;

		case R.id.night_driving:
			Intent nightDriving = new Intent(getActivity(),
					NightDrivingActivity.class);
			startActivity(nightDriving);
			break;

		default:
			break;
		}
	}

	private void loadLines() {
		new Thread() {
			public void run() {
				DBManager dbManager = DBManager.getInstance();
				List<ThirdSubject> list = dbManager.getThirdSubjects();
				mHandler.sendMessage(mHandler.obtainMessage(LOAD_SUCCESS, list));
			};
		}.start();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		loadLines();
	}

	private class ThirdSubjectAdapter extends BaseAdapter {

		private List<ThirdSubject> mList;

		@Override
		public int getCount() {
			return mList == null ? 0 : mList.size();
		}

		@Override
		public ThirdSubject getItem(int arg0) {
			return mList == null ? null : mList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			if (null == arg1) {
				arg1 = LayoutInflater.from(getActivity()).inflate(
						R.layout.third_subject_layout, arg2, false);
			}

			TextView name = (TextView) arg1.findViewById(R.id.name);
			name.setText(mList.get(arg0).name);

			return arg1;
		}

		public void setList(List<ThirdSubject> list) {
			mList = list;
			notifyDataSetChanged();
		}

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		ThirdSubject subject = mAdapter.getItem(arg2);

		Intent intent = new Intent(getActivity(),
				ThirdSubjectItemActivity.class);
		intent.putExtra("id", subject.id);
		intent.putExtra("name", subject.name);
		startActivity(intent);

	}

}
