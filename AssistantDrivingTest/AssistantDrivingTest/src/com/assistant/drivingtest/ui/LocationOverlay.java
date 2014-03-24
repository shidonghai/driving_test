package com.assistant.drivingtest.ui;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.assistant.drivingtest.R;
import com.assistant.drivingtest.db.DBManager;
import com.assistant.drivingtest.domain.ThirdTestItem;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.mapapi.map.RouteOverlay;
import com.baidu.mapapi.search.MKRoute;
import com.baidu.platform.comapi.basestruct.GeoPoint;

/**
 * 此demo用来展示如何结合定位SDK实现定位，并使用MyLocationOverlay绘制定位位置 同时展示如何使用自定义图标绘制并点击时弹出泡泡
 * 
 */
public class LocationOverlay extends Fragment {
	private enum E_BUTTON_TYPE {
		LOC, COMPASS, FOLLOW
	}

	private static final int LOAD_SUCCESS = 0;

	private E_BUTTON_TYPE mCurBtnType;

	// 定位相关
	LocationClient mLocClient;
	LocationData locData = null;
	public MyLocationListenner myListener = new MyLocationListenner();

	// 定位图层
	locationOverlay myLocationOverlay = null;
	// 弹出泡泡图层
	private PopupOverlay pop = null;// 弹出泡泡图层，浏览节点时使用
	private TextView popupText = null;// 泡泡view
	private View viewCache = null;

	// 地图相关，使用继承MapView的MyLocationMapView目的是重写touch事件实现泡泡处理
	// 如果不处理touch事件，则无需继承，直接使用MapView即可
	MyLocationMapView mMapView = null; // 地图View
	private MapController mMapController = null;

	// UI相关
	OnCheckedChangeListener radioButtonListener = null;
	boolean isRequest = false;// 是否手动触发请求定位
	boolean isFirstLoc = false;// 是否首次定位

	private TextView mLocation;

	private List<ThirdTestItem> mItems;

	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {

			case LOAD_SUCCESS:
				mItems = (List<ThirdTestItem>) msg.obj;
				setOverlay();
				break;

			default:
				break;
			}
		};
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_locationoverlay, container,
				false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		initView(view);

		Bundle bundle = getArguments();
		long id = bundle.getLong("id");
		loadLines(id);
	}

	private void loadLines(final long id) {
		new Thread() {
			public void run() {
				DBManager dbManager = DBManager.getInstance();
				List<ThirdTestItem> list = dbManager.getThirdTestItems(id);
				mHandler.sendMessage(mHandler.obtainMessage(LOAD_SUCCESS, list));
			};
		}.start();
	}

	private void initView(View view) {
		CharSequence titleLable = "定位功能";
		mCurBtnType = E_BUTTON_TYPE.LOC;

		mLocation = (TextView) view.findViewById(R.id.location);

		// 地图初始化
		mMapView = (MyLocationMapView) view.findViewById(R.id.bmapView);
		mMapController = mMapView.getController();
		mMapView.getController().setZoom(14);
		mMapView.getController().enableClick(true);
		mMapView.setBuiltInZoomControls(true);

		// 定位初始化
		mLocClient = new LocationClient(getActivity());
		locData = new LocationData();
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		// option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000);
		mLocClient.setLocOption(option);
		mLocClient.start();

		// 定位图层初始化
		myLocationOverlay = new locationOverlay(mMapView);
		// 设置定位数据
		myLocationOverlay.setData(locData);
		// 添加定位图层
		mMapView.getOverlays().add(myLocationOverlay);
		myLocationOverlay.enableCompass();
		// 修改定位数据后刷新图层生效
		mMapView.refresh();
	}

	private void setOverlay() {
		// ItemizedOverlay<OverlayItem> overlay = new
		// ItemizedOverlay<OverlayItem>(
		// getResources().getDrawable(R.drawable.nav_turn_via_1), mMapView);
		//
		// GeoPoint p = new GeoPoint((int) (mItems.get(0).latitude * 1E6),
		// (int) (mItems.get(0).longitude * 1E6));
		// OverlayItem item = new OverlayItem(p, "起点", "");
		// item.setMarker(getResources().getDrawable(R.drawable.nav_turn_via_1));
		// overlay.addItem(item);
		// mMapView.getOverlays().add(overlay);
		// mMapView.refresh();

		if (null == mItems || mItems.size() == 0) {
			return;
		}

		GeoPoint start = new GeoPoint((int) (mItems.get(0).voiceLatitude * 1E6),
				(int) (mItems.get(0).voiceLongitude * 1E6));

		GeoPoint stop = new GeoPoint(
				(int) (mItems.get(mItems.size() - 1).voiceLatitude * 1E6),
				(int) (mItems.get(mItems.size() - 1).voiceLongitude * 1E6));
		// GeoPoint[] step = new GeoPoint[mItems.size()];
		// for (int i = 0; i < mItems.size(); i++) {
		// step[i] = new GeoPoint((int) (mItems.get(i).latitude * 1E6),
		// (int) (mItems.get(i).longitude * 1E6));
		// }

		GeoPoint[][] routeData = new GeoPoint[mItems.size()][];
		for (int i = 0; i < mItems.size(); i++) {

			routeData[i] = new GeoPoint[] { new GeoPoint(
					(int) (mItems.get(i).voiceLatitude * 1E6),
					(int) (mItems.get(i).voiceLongitude * 1E6)) };
		}

		// 用站点数据构建一个MKRoute
		MKRoute route = new MKRoute();
		route.customizeRoute(start, stop, routeData);
		// 将包含站点信息的MKRoute添加到RouteOverlay中
		RouteOverlay routeOverlay = new RouteOverlay(getActivity(), mMapView);
		routeOverlay.setData(route);
		// 向地图添加构造好的RouteOverlay
		mMapView.getOverlays().add(routeOverlay);
		// 执行刷新使生效
		mMapView.refresh();

	}

	/**
	 * 手动触发一次定位请求
	 */
	public void requestLocClick() {
		isRequest = true;
		mLocClient.requestLocation();
		Toast.makeText(getActivity(), "正在定位……", Toast.LENGTH_SHORT).show();
	}

	/**
	 * 修改位置图标
	 * 
	 * @param marker
	 */
	public void modifyLocationOverlayIcon(Drawable marker) {
		// 当传入marker为null时，使用默认图标绘制
		myLocationOverlay.setMarker(marker);
		// 修改图层，需要刷新MapView生效
		mMapView.refresh();
	}

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null || myLocationOverlay == null)
				return;

			locData.latitude = location.getLatitude();
			locData.longitude = location.getLongitude();
			// 如果不显示定位精度圈，将accuracy赋值为0即可
			locData.accuracy = location.getRadius();
			// 此处可以设置 locData的方向信息, 如果定位 SDK 未返回方向信息，用户可以自己实现罗盘功能添加方向信息。
			locData.direction = location.getDerect();
			// 更新定位数据
			myLocationOverlay.setData(locData);

			mLocation.setText(locData.latitude + " -- " + locData.longitude);
			// 更新图层数据执行刷新后生效
			mMapView.refresh();
			// 是手动触发请求或首次定位时，移动到定位点
			// if (isRequest || isFirstLoc) {
			// 移动地图到定位点
			Log.d("LocationOverlay", "receive location, animate to it");
			if (isFirstLoc) {
				mMapController.animateTo(new GeoPoint(
						(int) (locData.latitude * 1e6),
						(int) (locData.longitude * 1e6)));
				isRequest = false;
				// myLocationOverlay.setLocationMode(LocationMode.FOLLOWING);
				mCurBtnType = E_BUTTON_TYPE.FOLLOW;
				// 首次定位完成
				isFirstLoc = false;
			}

		}

		public void onReceivePoi(BDLocation poiLocation) {
			if (poiLocation == null) {
				return;
			}
		}
	}

	// 继承MyLocationOverlay重写dispatchTap实现点击处理
	public class locationOverlay extends MyLocationOverlay {

		public locationOverlay(MapView mapView) {
			super(mapView);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected boolean dispatchTap() {
			// TODO Auto-generated method stub
			// 处理点击事件,弹出泡泡
			// popupText.setBackgroundResource(R.drawable.popup);
			// popupText.setText("我的位置");
			// pop.showPopup(BMapUtil.getBitmapFromView(popupText),
			// new GeoPoint((int)(locData.latitude*1e6),
			// (int)(locData.longitude*1e6)),
			// 8);
			return true;
		}

	}

	@Override
	public void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	public void onResume() {
		mMapView.onResume();
		super.onResume();
	}

	@Override
	public void onDestroy() {
		// 退出时销毁定位
		if (mLocClient != null)
			mLocClient.stop();
		mMapView.destroy();
		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);

	}

}

/**
 * 继承MapView重写onTouchEvent实现泡泡处理操作
 * 
 * @author hejin
 * 
 */
class MyLocationMapView extends MapView {
	static PopupOverlay pop = null;// 弹出泡泡图层，点击图标使用

	public MyLocationMapView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public MyLocationMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyLocationMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!super.onTouchEvent(event)) {
			// 消隐泡泡
			if (pop != null && event.getAction() == MotionEvent.ACTION_UP)
				pop.hidePop();
		}
		return true;
	}
}
