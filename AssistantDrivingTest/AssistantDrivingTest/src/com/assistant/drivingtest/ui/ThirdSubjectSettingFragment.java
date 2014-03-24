package com.assistant.drivingtest.ui;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.assistant.drivingtest.R;
import com.assistant.drivingtest.db.DBManager;
import com.assistant.drivingtest.domain.ThirdSubject;

public class ThirdSubjectSettingFragment extends Fragment implements
		OnClickListener {

	private static final String TAG = "ThirdSubjectSettingFragment";

	private static final int LOAD_SUCCESS = 0;

	private ThirdSubjectAdapter mAdapter;

	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {

			case LOAD_SUCCESS:
				List<ThirdSubject> list = (List<ThirdSubject>) msg.obj;
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
		return inflater.inflate(R.layout.third_subject_setting, container,
				false);
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

		RelativeLayout newLine = (RelativeLayout) view
				.findViewById(R.id.new_line);
		newLine.setOnClickListener(this);

		ListView listView = (ListView) view.findViewById(R.id.third_list);
		mAdapter = new ThirdSubjectAdapter();
		listView.setAdapter(mAdapter);
		listView.setDividerHeight(0);
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
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.back:
			getActivity().finish();
			getActivity().setResult(Activity.RESULT_OK);
			break;

		case R.id.new_line:
			Intent newLine = new Intent(getActivity(),
					ThirdSubjectNewLineActivity.class);
			startActivityForResult(newLine, 0);

		default:
			break;
		}
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
						R.layout.third_subject_setting_layout, arg2, false);
			}

			final ThirdSubject subject = mList.get(arg0);

			TextView name = (TextView) arg1.findViewById(R.id.name);
			name.setText(subject.name);

			Button delete = (Button) arg1.findViewById(R.id.delete);
			delete.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					DBManager.getInstance().deleteLine(subject.id);
					loadLines();
				}
			});

			return arg1;
		}

		public void setList(List<ThirdSubject> list) {
			mList = list;
			notifyDataSetChanged();
		}

	}

}
