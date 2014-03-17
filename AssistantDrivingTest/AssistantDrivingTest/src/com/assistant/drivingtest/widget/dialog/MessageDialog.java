
package com.assistant.drivingtest.widget.dialog;

import com.assistant.drivingtest.R;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


/**
 * Created by Xiaohu on 13-11-10.
 */
public class MessageDialog extends BaseDialog {
    private String mMessage;

    public MessageDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setDialogContentView(R.layout.message_dialog_layout);
      //  TextView textview = (TextView) findViewById(R.id.content);
       // textview.setText(mMessage);
        
        setBtnVisiable(Btn.CANCEL, View.GONE);
        setBtnVisiable(Btn.CONFIRM, View.GONE);
    }

    public void setMessage(String msg) {
        mMessage = msg;
    }
}
