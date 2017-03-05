package com.ideal.zsyy.activity;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ideal.zsyy.Config;
import com.ideal.zsyy.entity.LocationInfo;
import com.ideal.zsyy.entity.RLocationInfo;
import com.ideal.zsyy.entity.WPointItem;
import com.ideal.zsyy.map.DrivingRouteOverlay;
import com.ideal.zsyy.map.OverlayManager;
import com.ideal.zsyy.response.RBaseRes;
import com.ideal.zsyy.utils.DialogCirleProgress;
import com.ideal.zsyy.utils.HttpUtil;
import com.shenrenkeji.intelcheck.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class RTrackActivity extends Activity implements BaiduMap.OnMapClickListener, OnGetRoutePlanResultListener {

	private ImageView img_location;
	private ImageView img_nav;
	private ImageView img_track;
	private Button btn_back;
	private String userId;
	private RLocationInfo currLocation;
	private MapView bmapsView;
	BaiduMap mBaiduMap = null;
	RouteLine route = null;
	private ZsyyApplication application;
	PlanNode stNode;
	PlanNode enNode;
	private LatLng currUserLocation;
	Polyline mPolyline;
	private List<RLocationInfo> locationPoints = new ArrayList<RLocationInfo>();
	// 搜索相关
	RoutePlanSearch mSearch = null; // 搜索模块，也可去掉地图模块独立使用
	OverlayManager routeOverlay = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.r_map_baidu);
		this.initView();
		this.initData();
		// this.setLocation();
	}

	private void initData() {
		application = (ZsyyApplication) getApplication();
		currUserLocation = new LatLng(application.currLocation.getLatitude(), application.currLocation.getLongitude());
		stNode = PlanNode.withLocation(currUserLocation);
		userId = getIntent().getStringExtra("user_id");
		if (userId == null || "".equals(userId)) {
			Toast.makeText(RTrackActivity.this, "用户ID不正确！", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		getCurrLocation();
		
	}

	private void initView() {
		img_location = (ImageView) findViewById(R.id.ac_main_bmap_ibtn_myloc);
		img_nav = (ImageView) findViewById(R.id.ac_main_bmap_ibtn_nav);
		img_track = (ImageView) findViewById(R.id.ac_main_bmap_ibtn_tra);
		img_location.setOnClickListener(clickListener);
		img_nav.setOnClickListener(clickListener);
		img_track.setOnClickListener(clickListener);
		btn_back=(Button)findViewById(R.id.btn_back);
		btn_back.setOnClickListener(clickListener);
		bmapsView = (MapView) findViewById(R.id.bmapsView);
		if (bmapsView != null) {
			mBaiduMap = bmapsView.getMap();
			mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(LocationMode.NORMAL, true, null));
			// 开启定位图层
			mBaiduMap.setMyLocationEnabled(true);
			// 地图点击事件处理
			mBaiduMap.setOnMapClickListener(this);
			// 初始化搜索模块，注册事件监听
			mSearch = RoutePlanSearch.newInstance();
			mSearch.setOnGetRoutePlanResultListener(this);
		}

	}

	private OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.ac_main_bmap_ibtn_myloc:
				setLocation();
				break;
			case R.id.ac_main_bmap_ibtn_nav:
				routLink();
				break;
			case R.id.ac_main_bmap_ibtn_tra:
				if(locationPoints==null||locationPoints.size()==0)
				{
					getTrackPoints();
				}
				else {
					drawTrack();
				}
				
				break;
			case R.id.btn_back:
				finish();
			default:
				break;
			}
		}
	};

	private void setLocation() {
		mBaiduMap.clear();
		if (currLocation != null) {
			MyLocationData locData = new MyLocationData.Builder().accuracy(0)
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(currLocation.getLat()).longitude(currLocation.getLon()).build();
			mBaiduMap.setMyLocationData(locData);
			LatLng ll = new LatLng(currLocation.getLat(), currLocation.getLon());
			MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
			mBaiduMap.animateMapStatus(u);

		}
	}

	private void routLink() {
		mBaiduMap.clear();
		// 设置起终点信息，对于tranist search 来说，城市名无意义
		if (stNode != null && enNode != null) {
			mSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode).to(enNode));
		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mSearch != null)
			mSearch.destroy();
		if (bmapsView != null)
			bmapsView.onDestroy();
	}

	/// 获取用户当前位置
	private void getCurrLocation() {
		if (!HttpUtil.checkNet(RTrackActivity.this)) {
			Toast.makeText(RTrackActivity.this, "请检查网络！", Toast.LENGTH_SHORT).show();
			return;
		}
		final DialogCirleProgress dCirleProgress = new DialogCirleProgress(RTrackActivity.this);
		OkHttpUtils.get().url(Config.Apiurl).addParams("action", "getlocation").addParams("userid", userId).build()
				.execute(new Callback<RLocationInfo>() {

					@Override
					public void onBefore(Request request, int id) {
						// TODO Auto-generated method stub
						super.onBefore(request, id);
						Log.i("loginRequest", request.url().toString());
						dCirleProgress.showProcessDialog();
					}

					@Override
					public void onError(Call arg0, Exception arg1, int arg2) {
						// TODO Auto-generated method stub
						dCirleProgress.hideProcessDialog();
						Toast.makeText(RTrackActivity.this, "获取用户位置失败！：" + arg1.getMessage(), Toast.LENGTH_SHORT)
								.show();
						return;
					}

					@Override
					public void onResponse(RLocationInfo arg0, int arg1) {
						// TODO Auto-generated method stub
						dCirleProgress.hideProcessDialog();
						if (arg0 == null) {
							Toast.makeText(RTrackActivity.this, "获取用户位置失败", Toast.LENGTH_SHORT).show();
							return;
						}
						currLocation = arg0;
						setLocation();
						enNode = PlanNode.withLocation(new LatLng(currLocation.getLat(), currLocation.getLon()));
					}

					@Override
					public RLocationInfo parseNetworkResponse(Response arg0, int arg1) throws Exception {
						// TODO Auto-generated method stub
						RLocationInfo retData = null;
						String bodyStr = arg0.body().string();
						Log.i("response", bodyStr);
						Type tp = new TypeToken<RBaseRes<List<RLocationInfo>>>() {
						}.getType();
						RBaseRes<List<RLocationInfo>> baseRes = new Gson().fromJson(bodyStr, tp);
						String retCode = baseRes.getStatus();
						if (!"success".equalsIgnoreCase(retCode)) {
							Toast.makeText(RTrackActivity.this, "获取用户位置失败", Toast.LENGTH_SHORT).show();
							return retData;
						}
						if (baseRes != null && baseRes.getData() != null && baseRes.getData().size() > 0) {
							Log.i("location response:", "" + baseRes.getData().size());
							retData = baseRes.getData().get(0);
						}
						return retData;

					}

				});
	}

	@Override
	public void onGetDrivingRouteResult(DrivingRouteResult result) {
		// TODO Auto-generated method stub
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(RTrackActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			// result.getSuggestAddrInfo()
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			route = result.getRouteLines().get(0);
			DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaiduMap);
			routeOverlay = overlay;
			mBaiduMap.setOnMarkerClickListener(overlay);
			overlay.setData(result.getRouteLines().get(0));
			overlay.addToMap();
			overlay.zoomToSpan();
		}
	}

	@Override
	public void onGetTransitRouteResult(TransitRouteResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGetWalkingRouteResult(WalkingRouteResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMapClick(LatLng arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onMapPoiClick(MapPoi arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	// 定制RouteOverly
	private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

		public MyDrivingRouteOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public BitmapDescriptor getStartMarker() {
			return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {
			return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
		}
	}

	//绘制行走轨迹
	private void drawTrack()
	{
		if(mBaiduMap!=null)
		{
			mBaiduMap.clear();
		}
		if(locationPoints!=null&&locationPoints.size()>0)
		{
			if(currLocation!=null)
			{
				
				LatLng latLng = new LatLng(currLocation.getLat(),currLocation.getLon());
				MapStatus mMapStatus = new MapStatus.Builder().target(latLng).zoom(15).build();
				MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
				mBaiduMap.animateMapStatus(mMapStatusUpdate);
			}
			
			List<LatLng> list = new ArrayList<LatLng>();
			LatLng tempPoint=null;
			for (int i = 0; i < locationPoints.size(); i++) {
				double lat = locationPoints.get(i).getLat();
				double lon = locationPoints.get(i).getLon();
				LatLng p1 = new LatLng(lat, lon);
				if(tempPoint==null)
				{
					tempPoint=p1;
					list.add(p1);
				}
				else {
					if(DistanceUtil.getDistance(p1, tempPoint)>10)
					{
						list.add(p1);
						tempPoint=p1;
					}
				}
				
			}
			OverlayOptions ooPolyline = new PolylineOptions().width(5)
					.color(0xAAFF0000).points(list);
			mPolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline);
			
		}
		
	}
	
	// 获取gps信息
	private void getTrackPoints() {
		if (!HttpUtil.checkNet(RTrackActivity.this)) {
			Toast.makeText(RTrackActivity.this, "请检查网络！", Toast.LENGTH_SHORT).show();
			return;
		}
		final DialogCirleProgress dCirleProgress = new DialogCirleProgress(RTrackActivity.this);
		OkHttpUtils.get().url(Config.Apiurl)
		.addParams("action", "gettrajectory")
		.addParams("userid", userId)
				.addParams("date", new SimpleDateFormat("yyyy-MM-dd").format(new Date())).build()
				.execute(new Callback<List<RLocationInfo>>() {

					@Override
					public void onBefore(Request request, int id) {
						// TODO Auto-generated method stub
						super.onBefore(request, id);
						Log.i("loginRequest", request.url().toString());
						dCirleProgress.showProcessDialog();
					}

					@Override
					public void onError(Call arg0, Exception arg1, int arg2) {
						// TODO Auto-generated method stub
						dCirleProgress.hideProcessDialog();
						Toast.makeText(RTrackActivity.this, "获取用户位置失败！：" + arg1.getMessage(), Toast.LENGTH_SHORT)
								.show();
						return;
					}

					@Override
					public void onResponse(List<RLocationInfo> arg0, int arg1) {
						// TODO Auto-generated method stub
						dCirleProgress.hideProcessDialog();
						if (arg0 == null) {
							Toast.makeText(RTrackActivity.this, "获取用户位置失败", Toast.LENGTH_SHORT).show();
							return;
						}
						locationPoints=arg0;
						drawTrack();
					}

					@Override
					public List<RLocationInfo> parseNetworkResponse(Response arg0, int arg1) throws Exception {
						// TODO Auto-generated method stub
						List<RLocationInfo> retData = null;
						String bodyStr = arg0.body().string();
						Log.i("response", bodyStr);
						Type tp = new TypeToken<RBaseRes<List<RLocationInfo>>>() {
						}.getType();
						RBaseRes<List<RLocationInfo>> baseRes = new Gson().fromJson(bodyStr, tp);
						String retCode = baseRes.getStatus();
						if (!"success".equalsIgnoreCase(retCode)) {
							Toast.makeText(RTrackActivity.this, "获取用户位置失败", Toast.LENGTH_SHORT).show();
							return retData;
						}
						if (baseRes != null && baseRes.getData() != null) {
							Log.i("location response:", "" + baseRes.getData().size());
							retData = baseRes.getData();
						}
						return retData;

					}

				});
	}
}
