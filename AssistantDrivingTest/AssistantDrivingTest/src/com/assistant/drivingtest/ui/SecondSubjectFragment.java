package com.assistant.drivingtest.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.assistant.drivingtest.R;
import com.assistant.drivingtest.domain.SecondTestItem;

public class SecondSubjectFragment extends Fragment implements OnClickListener,
		OnItemClickListener {

	private static final String TAG = "zxh";

	private ListView mListView;

	private SecondSubjectAdapter mAdapter;

	private MediaPlayer mPlayer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.second_subject, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		initView(view);
		setupView();
	}

	private void setupView() {

		List<SecondTestItem> items = new ArrayList<SecondTestItem>();
		try {
			String[] subjects = getResources().getStringArray(
					R.array.second_subject);
			String[] accets = getActivity().getAssets().list("second");
			if (subjects.length != accets.length) {
				return;
			}

			SecondTestItem item;
			for (int i = 0; i < accets.length; i++) {
				item = new SecondTestItem();
				item.name = subjects[i];
				item.resId = accets[i];
				items.add(item);
			}

			mAdapter = new SecondSubjectAdapter(getActivity(), items);
			mListView.setAdapter(mAdapter);

			Log.d(TAG, Arrays.toString(accets));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initView(View view) {
		ImageView back = (ImageView) view.findViewById(R.id.back);
		back.setOnClickListener(this);

		mListView = (ListView) view.findViewById(R.id.second_list);
		mListView.setOnItemClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			getActivity().finish();
			break;

		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		playItem(arg2);
	}

	private void playItem(int position) {
		SecondTestItem item = mAdapter.getItem(position);
		if (null == item) {
			return;
		}

		mAdapter.setPlayPosition(position);

		if (null == mPlayer) {
			mPlayer = new MediaPlayer();
			mPlayer.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					mAdapter.setPlayPosition(-1);
				}
			});
			mPlayer.setOnErrorListener(new OnErrorListener() {

				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					mAdapter.setPlayPosition(-1);
					return false;
				}
			});
		} else {
			mPlayer.reset();
		}

		try {
			AssetFileDescriptor fileDescriptor = getActivity().getAssets()
					.openFd("second/" + item.resId);
			mPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
					fileDescriptor.getStartOffset(), fileDescriptor.getLength());
			mPlayer.prepare();
			mPlayer.start();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
