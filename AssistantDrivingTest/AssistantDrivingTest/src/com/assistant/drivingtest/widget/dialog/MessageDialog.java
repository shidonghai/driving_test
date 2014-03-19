package com.assistant.drivingtest.widget.dialog;

import com.assistant.drivingtest.R;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Xiaohu on 13-11-10.
 */
public class MessageDialog extends BaseDialog implements
		android.view.View.OnClickListener {

	private String mMessage;

	private TextView mVoice;

	private TextView mStart;

	private TextView mEnd;

	private boolean mHasStart;

	public MessageDialog(Context context) {
		super(context);
	}

	public MessageDialog(Context context, boolean hasStart) {
		super(context);

		mHasStart = hasStart;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setDialogContentView(R.layout.message_dialog_layout);
		// TextView textview = (TextView) findViewById(R.id.content);
		// textview.setText(mMessage);

		mVoice = (TextView) findViewById(R.id.voice);
		mStart = (TextView) findViewById(R.id.start);
		mEnd = (TextView) findViewById(R.id.end);

		mVoice.setOnClickListener(this);
		mStart.setOnClickListener(this);
		mEnd.setOnClickListener(this);

		mStart.setVisibility(mHasStart ? View.VISIBLE : View.GONE);

		setBtnVisiable(Btn.CANCEL, View.GONE);
		setBtnVisiable(Btn.CONFIRM, View.GONE);
	}

	public void setMessage(String msg) {
		mMessage = msg;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.voice:
			mVoice.setEnabled(false);
			if (mHasStart) {
				mStart.setEnabled(true);
			} else {
				mEnd.setEnabled(true);
			}
			break;
		case R.id.start:
			mStart.setEnabled(false);
			mEnd.setEnabled(true);
			break;
		case R.id.end:
			dismiss();
			break;
		default:
			break;
		}
	}
}
