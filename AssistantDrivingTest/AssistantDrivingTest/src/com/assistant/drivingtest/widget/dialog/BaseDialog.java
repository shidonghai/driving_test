/**
 * 
 */

package com.assistant.drivingtest.widget.dialog;

import com.assistant.drivingtest.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * Top base class for dialog.
 * 
 */
public abstract class BaseDialog extends Dialog {
	private Context mContext;
	protected DialogListener mDialogListener;
	private CharSequence mTitle;
	private TextView mTitleView;

	public BaseDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		mContext = context;
	}

	public BaseDialog(Context context, int theme) {
		super(context, theme);
		mContext = context;
	}

	public BaseDialog(Context context) {
		super(context, android.R.style.Theme_Panel);
		mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.dimAmount = 0.65f;
		getWindow().setAttributes(lp);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		setContentView(R.layout.layout_base_dialog);
		mTitleView = (TextView) findViewById(R.id.dialog_title).findViewById(
				R.id.dialog_title_text);
		mTitleView.setText(mTitle);
		Button close = (Button) findViewById(R.id.dialog_bottom).findViewById(
				R.id.dialog_cancel);
		close.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		Button confirm = (Button) findViewById(R.id.dialog_bottom)
				.findViewById(R.id.dialog_confirm);
		confirm.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (null != mDialogListener) {
					mDialogListener.onConfirmed();
				}

				dismiss();
			}
		});
	}

	@Override
	public void setTitle(CharSequence title) {
		if (null != mTitleView) {
			mTitleView.setText(title);
		} else {
			mTitle = title;
		}
	}

	protected void setDialogContentView(int layoutResID) {
		ViewGroup contentRoot = (ViewGroup) findViewById(R.id.dialog_content);
		View content = LayoutInflater.from(mContext).inflate(layoutResID, null);
		contentRoot.addView(content);
	}

	public DialogListener getDialogListener() {
		return mDialogListener;
	}

	public void setDialogListener(DialogListener dialogListener) {
		this.mDialogListener = dialogListener;
	}

	public interface DialogListener {
		public void onConfirmed();
	}

	public void setConfirmEnable(boolean enable) {
		findViewById(R.id.dialog_bottom).findViewById(R.id.dialog_confirm)
				.setEnabled(enable);
	}

	public void setBtnText(Btn btn, int textId) {
		getBtn(btn).setText(textId);
	}

	public void setBtnVisiable(Btn btn, int visibility) {
		getBtn(btn).setVisibility(visibility);
	}

	public Button getBtn(Btn btn) {
		Button b = null;
		switch (btn) {
		case CONFIRM:
			b = (Button) findViewById(R.id.dialog_bottom).findViewById(
					R.id.dialog_confirm);
			break;
		case CANCEL:
			b = (Button) findViewById(R.id.dialog_bottom).findViewById(
					R.id.dialog_cancel);
			break;

		default:
			b = new Button(getContext());
			break;
		}
		return b;
	}

	public enum Btn {
		CONFIRM, CANCEL
	}
}
