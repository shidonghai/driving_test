package com.assistant.drivingtest.ui;

import java.util.List;

import com.assistant.drivingtest.R;
import com.assistant.drivingtest.domain.SecondTestItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SecondSubjectAdapter extends BaseAdapter {

	private LayoutInflater mInflater;

	private List<SecondTestItem> mItems;

	private int mPlayPosition = -1;

	public SecondSubjectAdapter(Context context, List<SecondTestItem> items) {
		mInflater = LayoutInflater.from(context);
		mItems = items;
	}

	@Override
	public int getCount() {
		return mItems == null ? 0 : mItems.size();
	}

	@Override
	public SecondTestItem getItem(int arg0) {
		return mItems.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		ViewHolder holder = null;
		if (null == arg1) {
			arg1 = mInflater.inflate(R.layout.second_subject_item, arg2, false);

			holder = new ViewHolder();
			holder.playIcon = (ImageView) arg1.findViewById(R.id.control);
			holder.title = (TextView) arg1.findViewById(R.id.item);
			arg1.setTag(holder);
		} else {
			holder = (ViewHolder) arg1.getTag();
		}

		SecondTestItem item = mItems.get(arg0);
		if (null != item) {
			holder.title.setText(item.name);
			holder.playIcon
					.setImageResource(mPlayPosition == arg0 ? R.drawable.pause
							: R.drawable.play);
		}

		return arg1;
	}

	public void setPlayPosition(int position) {
		mPlayPosition = position;
		notifyDataSetChanged();
	}

	private class ViewHolder {

		ImageView playIcon;

		TextView title;

	}

}
