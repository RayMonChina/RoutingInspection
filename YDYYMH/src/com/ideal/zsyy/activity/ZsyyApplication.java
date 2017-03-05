package com.ideal.zsyy.activity;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ideal.zsyy.Config;
import com.ideal.zsyy.db.RDBManager;
import com.ideal.zsyy.entity.LocationInfo;
import com.ideal.zsyy.entity.WPointItem;
import com.ideal.zsyy.response.RBaseRes;
import com.ideal.zsyy.service.LocationService;
import com.ideal.zsyy.service.PreferencesService;
import com.ideal.zsyy.utils.FileUtils;
import com.ideal.zsyy.utils.HttpUtil;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class ZsyyApplication extends Application {

	public LocationClient mLocationClient;
	public MyLocationListener mMyLocationListener;

	public Vibrator mVibrator;

	public LocationInfo currPoint=null;
	public LocationInfo tempPoint=null;
	LatLng pointEnd;
	LatLng pointBegin;
	public BDLocation currLocation=null;
	PreferencesService pService=null;
	Intent iLocalService=null;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		pService=new PreferencesService(getApplicationContext());
		SDKInitializer.initialize(this);
//		CrashHandler crashHandler = CrashHandler.getInstance();  
//        crashHandler.init(getApplicationContext());  
        
		mLocationClient = new LocationClient(this.getApplicationContext());
		mMyLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(mMyLocationListener);
		mVibrator = (Vibrator) getApplicationContext().getSystemService(
				Service.VIBRATOR_SERVICE);
		
		iLocalService=new Intent(getApplicationContext(),LocationService.class);
		startService(iLocalService);
		
//		Intent iserviceTick=new Intent(getApplicationContext(),TickService.class);
//		startService(iserviceTick);
		try {
			FileUtils.createSDDir("");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
		new PreferencesService(getApplicationContext()).clearLogin();
	}


	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			currLocation=location;
			// Receive Location
			Boolean hasValue=false;
			StringBuffer sb = new StringBuffer(256);
			sb.append("time : ");
			sb.append(location.getTime());
			sb.append("\nerror code : ");
			sb.append(location.getLocType());
			sb.append("\nlatitude : ");
			sb.append(location.getLatitude());
			sb.append("\nlontitude : ");
			sb.append(location.getLongitude());
			sb.append("\nradius : ");
			sb.append(location.getRadius());
			if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
				sb.append("\nspeed : ");
				sb.append(location.getSpeed());// 单位：公里每小时
				sb.append("\nsatellite : ");
				sb.append(location.getSatelliteNumber());
				sb.append("\nheight : ");
				sb.append(location.getAltitude());// 单位：米
				sb.append("\ndirection : ");
				sb.append(location.getDirection());
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				sb.append("\ndescribe : ");
				sb.append("gps定位成功");
				//
				LocationInfo lInfo=new LocationInfo();
				lInfo.setGetDate(new Date());
				lInfo.setLatitude(location.getLatitude());
				lInfo.setLontitude(location.getLongitude());
				lInfo.setRadius(location.getRadius());
				if(location.hasAddr())
				{
					lInfo.setAddress(location.getAddrStr());
				}
				currPoint=lInfo;
				hasValue=true;

			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				// 运营商信息
				sb.append("\noperationers : ");
				sb.append(location.getOperators());
				sb.append("\ndescribe : ");
				sb.append("网络定位成功");
				//
				LocationInfo lInfo=new LocationInfo();
				lInfo.setGetDate(new Date());
				lInfo.setLatitude(location.getLatitude());
				lInfo.setLontitude(location.getLongitude());
				lInfo.setRadius(location.getRadius());
				if(location.hasAddr())
				{
					lInfo.setAddress(location.getAddrStr());
				}
				currPoint=lInfo;
				hasValue=true;
			} else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
				sb.append("\ndescribe : ");
				sb.append("离线定位成功，离线定位结果也是有效的");
				//
				LocationInfo lInfo=new LocationInfo();
				lInfo.setGetDate(new Date());
				lInfo.setLatitude(location.getLatitude());
				lInfo.setLontitude(location.getLongitude());
				lInfo.setRadius(location.getRadius());
				if(location.hasAddr())
				{
					lInfo.setAddress(location.getAddrStr());
				}
				currPoint=lInfo;
				hasValue=true;
			} else if (location.getLocType() == BDLocation.TypeServerError) {
				sb.append("\ndescribe : ");
				sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
			} else if (location.getLocType() == BDLocation.TypeNetWorkException) {
				sb.append("\ndescribe : ");
				sb.append("网络不同导致定位失败，请检查网络是否通畅");
			} else if (location.getLocType() == BDLocation.TypeCriteriaException) {
				sb.append("\ndescribe : ");
				sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
			}
			
			logMsg(sb.toString());
			Log.i("BaiduLocationApiDem", sb.toString());
			if(hasValue)
			{
				if(tempPoint==null)
				{
					tempPoint=currPoint;
				}
				Date currDate=new Date();
				long diffSecond=(currDate.getTime()/1000/60-tempPoint.getGetDate().getTime()/1000/60);
				if(diffSecond>=5||(tempPoint!=null&& tempPoint.equals(currPoint)))
				{
					tempPoint=currPoint;
					
					Map<String, Object>userInfo=pService.getLoginInfo();
					if(userInfo!=null&&pService.getIsLogin())
					{
						RDBManager wManager=new RDBManager(getApplicationContext());
						WPointItem pointItem=new WPointItem();
						pointItem.setAddDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
						pointItem.setAlreadyUpload(1);
						pointItem.setLatitude(currPoint.getLatitude());
						pointItem.setLongitude(currPoint.getLontitude());
						pointItem.setUserId(userInfo.get("use_id").toString());
						pointItem.setUserName(userInfo.get("userName").toString());
						pointItem.setAddress(currPoint.getAddress());
						wManager.AddWalkPoint(pointItem);
						if(HttpUtil.checkNet(getApplicationContext()))
						{
							UploadLocation(pointItem.getUserId());
						}
						else {
							//Toast.makeText(getApplicationContext(),"请检查网络是否开启",Toast.LENGTH_SHORT).show();
						}
						
						wManager.closeDB();
					}
				}
			}
		}

	}
	
	public void UploadLocation(String userId)
	{
		if(this.currPoint==null||userId==null||"".equals(userId))
		{
			return;
		}
		OkHttpUtils.post().url(Config.Apiurl)
		.addParams("action", "location")
		.addParams("userid", userId)
		.addParams("lat",Double.toString(this.currPoint.getLatitude()))
		.addParams("lon", Double.toString(this.currPoint.getLontitude()))
		.addParams("curdatetime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
		.addParams("gpsaddress", this.currPoint.getAddress())
		.build().execute(new Callback<RBaseRes<String>>() {

			@Override
			public void onBefore(Request request, int id) {
				// TODO Auto-generated method stub
				super.onBefore(request, id);
				Log.i("upload location", request.url().toString()) ;
			}

			@Override
			public void onError(Call arg0, Exception arg1, int arg2) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onResponse(RBaseRes arg0, int arg1) {
				// TODO Auto-generated method stub
			}

			@Override
			public RBaseRes<String> parseNetworkResponse(Response arg0, int arg1) throws Exception {
				// TODO Auto-generated method stub
				String bodyStr = arg0.body().string();
				Log.i("response",bodyStr);
				Type tp=new TypeToken<RBaseRes<String>>(){}.getType();
		        RBaseRes<String> baseRes = new Gson().fromJson(bodyStr,tp);
		        String retCode=baseRes.getStatus();
		        if(!"success".equalsIgnoreCase(retCode))
		        {
		        	//Toast.makeText(ZsyyApplication.this,"登录失败",Toast.LENGTH_SHORT).show();
		        	return null;
		        }
		        if(baseRes!=null)
		        {
		        	return baseRes;
		        }
		        return null;
			}
			
		});
		
	}

	public void logMsg(String str) {
		try {
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
