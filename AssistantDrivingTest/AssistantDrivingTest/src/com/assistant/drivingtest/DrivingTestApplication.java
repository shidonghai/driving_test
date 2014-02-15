package com.assistant.drivingtest;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.assistant.drivingtest.db.DBManager;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.MKEvent;

public class DrivingTestApplication extends Application {

	private static DrivingTestApplication mInstance = null;
	public boolean m_bKeyRight = true;
	BMapManager mBMapManager = null;

	public static final String strKey = "Fw6krawbfjaAYCbqSQqGhCnY";

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
		initEngineManager(this);
	}

	public void initEngineManager(Context context) {
		if (mBMapManager == null) {
			mBMapManager = new BMapManager(context);
		}

		if (!mBMapManager.init(strKey, new MyGeneralListener())) {
			Toast.makeText(
					DrivingTestApplication.getInstance()
							.getApplicationContext(), "BMapManager  初始化错误!",
					Toast.LENGTH_SHORT).show();
		}
	}

	public static DrivingTestApplication getInstance() {
		return mInstance;
	}

	// 常用事件监听，用来处理通常的网络错误，授权验证错误等
	static class MyGeneralListener implements MKGeneralListener {

		@Override
		public void onGetNetworkState(int iError) {
			if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
				Toast.makeText(
						DrivingTestApplication.getInstance()
								.getApplicationContext(), "您的网络出错啦！",
						Toast.LENGTH_SHORT).show();
			} else if (iError == MKEvent.ERROR_NETWORK_DATA) {
				Toast.makeText(
						DrivingTestApplication.getInstance()
								.getApplicationContext(), "输入正确的检索条件！",
						Toast.LENGTH_SHORT).show();
			}
			// ...
		}

		@Override
		public void onGetPermissionState(int iError) {
			// 非零值表示key验证未通过
			if (iError != 0) {
				// 授权Key错误：
				Toast.makeText(
						DrivingTestApplication.getInstance()
								.getApplicationContext(),
						"请输入正确的授权Key,并检查您的网络连接是否正常！error: "
								+ iError, Toast.LENGTH_SHORT).show();
				DrivingTestApplication.getInstance().m_bKeyRight = false;
			} else {
				DrivingTestApplication.getInstance().m_bKeyRight = true;
//				Toast.makeText(
//						DrivingTestApplication.getInstance()
//								.getApplicationContext(), "key认证成功",
//						Toast.LENGTH_SHORT).show();
			}
		}
	}
}