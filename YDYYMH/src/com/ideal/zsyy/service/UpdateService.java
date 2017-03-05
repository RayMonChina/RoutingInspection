package com.ideal.zsyy.service;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ideal.zsyy.Config;
import com.ideal.zsyy.activity.LoginActivity;
import com.ideal.zsyy.activity.RSlidMainActivity;
import com.ideal.zsyy.entity.AppVersionInfo;
import com.ideal.zsyy.response.RBaseRes;
import com.ideal.zsyy.response.RUserInfoRes;
import com.ideal.zsyy.utils.DeviceHelper;
import com.ideal.zsyy.utils.DialogCirleProgress;
import com.ideal.zsyy.utils.HttpUtil;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import okhttp3.Call;
import okhttp3.Response;

public class UpdateService extends Service {
	/** 安卓系统下载类 **/
	DownloadManager manager;

	/** 接收下载完的广播 **/
	DownloadCompleteReceiver receiver;

	/** 初始化下载器 **/
	private void initDownManager(String downloadFile) {

		manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

		receiver = new DownloadCompleteReceiver();

		// 设置下载地址
		DownloadManager.Request down = new DownloadManager.Request(
				Uri.parse(downloadFile));
		down.setTitle("智能巡检下载");

		// 设置允许使用的网络类型，这里是移动网络和wifi都可以
		down.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);

		// 下载时，通知栏显示途中
		down.setNotificationVisibility(Request.VISIBILITY_VISIBLE);

		// 显示下载界面
		down.setVisibleInDownloadsUi(true);

		// 设置下载后文件存放的位置
		down.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS,"RoutingCheck"+new SimpleDateFormat("MMddHHmmss").format(new Date())+".apk");

		// 将下载请求放入队列
		manager.enqueue(down);
		// 注册下载广播
		registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// 调用下载
		//initDownManager();
		reqUpload();
		return super.onStartCommand(intent, flags, startId);
	}
	
	private void reqUpload()
	{
		if(!HttpUtil.checkNet(UpdateService.this))
		{
			//Toast.makeText(UpdateService.this,"请检查网络！",Toast.LENGTH_SHORT).show();
			Log.i("update service", "更新失败，请检查网络");
			return;
		}
		OkHttpUtils.get().url(Config.Apiurl)
		.addParams("action", "getversion")
		.build().execute(new Callback<AppVersionInfo>() {

			@Override
			public void onBefore(okhttp3.Request request, int id) {
				// TODO Auto-generated method stub
				super.onBefore(request, id);
				Log.i("loginRequest", request.url().toString()) ;
			}

			@Override
			public void onError(Call arg0, Exception arg1, int arg2) {
				// TODO Auto-generated method stub
				Log.i("update service error","登录失败："+arg1.getMessage());
				return;
			}

			@Override
			public void onResponse(AppVersionInfo arg0, int arg1) {
				// TODO Auto-generated method stub
				if(arg0==null)
				{
					Log.i("update serivce res", "更新失败,返回值为空");
					return;
				}
				String appVersion=arg0.getApp_version();
				String currVersion=DeviceHelper.getVersionName(UpdateService.this);
				if(currVersion.compareToIgnoreCase(appVersion)<0)
				{
					initDownManager(arg0.getApp_path());
				}
				
			}

			@Override
			public AppVersionInfo parseNetworkResponse(Response arg0, int arg1) throws Exception {
				// TODO Auto-generated method stub
				String bodyStr = arg0.body().string();
				Log.i("response",bodyStr);
				Type tp=new TypeToken<RBaseRes<List<AppVersionInfo>>>(){}.getType();
		        RBaseRes<List<AppVersionInfo>> baseRes = new Gson().fromJson(bodyStr,tp);
		        String retCode=baseRes.getStatus();
		        if(!"success".equalsIgnoreCase(retCode))
		        {
		        	Log.i("update serivce req", "更新失败");
		        	return null;
		        }
		        if(baseRes!=null&&baseRes.getData()!=null&&baseRes.getData().size()>0)
		        {
		        	return baseRes.getData().get(0);
		        }
		        return null;
			}
			
		});
	}

	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}

	@Override
	public void onDestroy() {

		// 注销下载广播
		if (receiver != null)
			unregisterReceiver(receiver);

		super.onDestroy();
	}

	// 接受下载完成后的intent
	class DownloadCompleteReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			// 判断是否下载完成的广播
			if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {

				// 获取下载的文件id
				long downId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

				// 自动安装apk
				installAPK(manager.getUriForDownloadedFile(downId));

				// 停止服务并关闭广播
				UpdateService.this.stopSelf();

			}
		}

		/**
		 * 安装apk文件
		 */
		private void installAPK(Uri apk) {

			// 通过Intent安装APK文件
			Intent intents = new Intent();

			intents.setAction("android.intent.action.VIEW");
			intents.addCategory("android.intent.category.DEFAULT");
			intents.setType("application/vnd.android.package-archive");
			intents.setData(apk);
			intents.setDataAndType(apk, "application/vnd.android.package-archive");
			intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			android.os.Process.killProcess(android.os.Process.myPid());
			// 如果不加上这句的话在apk安装完成之后点击单开会崩溃

			startActivity(intents);

		}

	}
}
