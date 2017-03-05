package com.ideal.zsyy.activity;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.map.Text;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ideal.zsyy.Config;
import com.ideal.zsyy.db.RDBManager;
import com.ideal.zsyy.response.RBaseRes;
import com.ideal.zsyy.response.RworkItemRes;
import com.ideal.zsyy.service.PreferencesService;
import com.ideal.zsyy.service.UpdateService;
import com.ideal.zsyy.utils.DeviceHelper;
import com.ideal.zsyy.utils.DialogCirleProgress;
import com.ideal.zsyy.utils.FileUtils;
import com.ideal.zsyy.utils.GPSHelper;
import com.ideal.zsyy.utils.HttpUtil;
import com.ideal.zsyy.utils.UtilNetWork;
import com.ideal.zsyy.utils.UtilWifi;
import com.shenrenkeji.intelcheck.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

public class RSettingActivity extends Activity {

	private ToggleButton toggleWLAN;
	private ToggleButton toggleMobile;
	private ToggleButton toggleGPS;
	private ToggleButton toggleGJ;
	private ToggleButton toggleAuto;
	private TextView tv_version;
	private TextView tv_clear_data;
	private TextView tv_update_step;
	private TextView tv_update_task;
	private Button btn_back;
	private RDBManager rdbManager = null;
	PreferencesService preferencesService;
	public static int Req_Gps_Code = 1;
	UpdateService UpdateService = null;
	private DialogCirleProgress progress = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.r_syssettings);
		this.initView();
		this.initData();
	}

	private void initView() {
		toggleWLAN = (ToggleButton) findViewById(R.id.toggleWLAN);
		toggleMobile = (ToggleButton) findViewById(R.id.toggleMobile);
		toggleGPS = (ToggleButton) findViewById(R.id.toggleGPS);
		toggleGJ = (ToggleButton) findViewById(R.id.toggleGJ);
		toggleAuto = (ToggleButton) findViewById(R.id.toggleAuto);
		toggleWLAN.setOnCheckedChangeListener(checkListener);
		toggleMobile.setOnCheckedChangeListener(checkListener);
		toggleGPS.setOnCheckedChangeListener(checkListener);
		toggleGJ.setOnCheckedChangeListener(checkListener);
		toggleAuto.setOnCheckedChangeListener(checkListener);
		tv_version = (TextView) findViewById(R.id.softwareVersion);
		tv_clear_data = (TextView) findViewById(R.id.tv_clear_data);
		tv_update_step=(TextView)findViewById(R.id.tv_update_step);
		tv_update_task=(TextView)findViewById(R.id.tv_update_task);
		btn_back=(Button)findViewById(R.id.btn_back);
		btn_back.setOnClickListener(clickListener);
	}
	
	
	public void refreshData() {
		// dbManager.removeAlreadyData();
		if (!HttpUtil.checkNet(RSettingActivity.this)) {
			Toast.makeText(RSettingActivity.this, "请检查网络！", Toast.LENGTH_SHORT).show();
			return;
		}
		
		OkHttpUtils.get().url(Config.Apiurl).addParams("action", "querytask").addParams("pageIndex", "1")
				.addParams("pageSize", "1000").build().execute(new Callback<List<RworkItemRes>>() {

					@Override
					public void onBefore(Request request, int id) {
						// TODO Auto-generated method stub
						super.onBefore(request, id);
						Log.i("loginRequest", request.url().toString());
						progress.showProcessDialog();
					}

					@Override
					public void onError(Call arg0, Exception arg1, int arg2) {
						// TODO Auto-generated method stub
						progress.hideProcessDialog();
						Toast.makeText(RSettingActivity.this, "加载数据失败：" + arg1.getMessage(), Toast.LENGTH_SHORT)
								.show();
						return;
					}

					@Override
					public void onResponse(List<RworkItemRes> arg0, int arg1) {
						// TODO Auto-generated method stub
						progress.hideProcessDialog();
						rdbManager.removeAlreadyData();
						rdbManager.removeCurrmedia("");
						rdbManager.AddWorkItems(arg0);
						Toast.makeText(RSettingActivity.this, "数据更新成功！", Toast.LENGTH_SHORT)
						.show();
					}

					@Override
					public List<RworkItemRes> parseNetworkResponse(Response arg0, int arg1) throws Exception {
						// TODO Auto-generated method stub
						String bodyStr = arg0.body().string();
						Log.i("response", bodyStr);
						bodyStr = bodyStr.replace("\\", "/");
						Type tp = new TypeToken<RBaseRes<List<RworkItemRes>>>() {
						}.getType();
						RBaseRes<List<RworkItemRes>> baseRes = new Gson().fromJson(bodyStr, tp);
						String retCode = baseRes.getStatus();
						if (!"success".equalsIgnoreCase(retCode)) {
							Log.i("workData faile:", arg0.request().url().toString());
							throw new Exception("download work data error");
						}
						if (baseRes != null && baseRes.getData() != null) {
							return baseRes.getData();
						}
						return null;
					}

				});
	}

	private void initData() {
		preferencesService = new PreferencesService(RSettingActivity.this);
		progress = new DialogCirleProgress(RSettingActivity.this);
		try {
			if (UtilWifi.isWifiDataEnable(RSettingActivity.this)) {
				toggleWLAN.setChecked(true);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			if (UtilNetWork.isMobileDataEnable(RSettingActivity.this)) {
				toggleMobile.setChecked(true);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (GPSHelper.checkIsOpen(RSettingActivity.this)) {
			toggleGPS.setChecked(true);
		}

		if (preferencesService.getGpsTrackEnable()) {
			toggleGJ.setChecked(true);
		}
		if (preferencesService.getAutoLoginStatus()) {
			toggleAuto.setChecked(true);
		}
		tv_version.setText("版本升级 V" + DeviceHelper.getVersionName(RSettingActivity.this));
		tv_version.setOnClickListener(clickListener);
		tv_clear_data.setOnClickListener(clickListener);
	}

	private OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.softwareVersion:
				Intent intent_upservice = new Intent(RSettingActivity.this, UpdateService.class);
				startService(intent_upservice);
				break;
			case R.id.tv_clear_data:
				clearData();
				break;
			case R.id.tv_update_step:
				handler.sendEmptyMessageDelayed(2, 1000);
				break;
			case R.id.tv_update_task:
				refreshData();
				break;
			case R.id.btn_back:
				finish();
				break;
			default:
				break;
			}
		}
	};

	private OnCheckedChangeListener checkListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// TODO Auto-generated method stub
			switch (buttonView.getId()) {
			case R.id.toggleWLAN:
				if (isChecked) {
					new UtilWifi(RSettingActivity.this).openWifi();
				} else {
					new UtilWifi(RSettingActivity.this).closeWifi();
				}
				break;
			case R.id.toggleMobile:
				if (isChecked) {
					new UtilNetWork().openMobileData(RSettingActivity.this, true);
				} else {
					new UtilNetWork().openMobileData(RSettingActivity.this, false);
				}
				break;
			case R.id.toggleGPS:
				Intent intent_gps = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivityForResult(intent_gps, Req_Gps_Code);
				break;
			case R.id.toggleGJ:
				preferencesService.setGpsTrackEnable(isChecked);
			case R.id.toggleAuto:
				preferencesService.setAutoLogin(isChecked);
				break;
			default:
				break;
			}
		}
	};

	private void clearData() {
		AlertDialog.Builder builder = new AlertDialog.Builder(RSettingActivity.this);
		builder.setTitle("是否确定清空所有缓存数据？");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (progress != null) {
							progress.showProcessDialog();
						}
						if (rdbManager != null) {
							rdbManager.resetData();
						}
						FileUtils.deleteDir();
						try {
							FileUtils.createSDDir("");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (progress != null) {
							progress.hideProcessDialog();
						}
						handler.sendEmptyMessage(1);
					}
				}).start();
			}
		});
		
		
		
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

			}
		});
		builder.show();
		if (rdbManager == null) {
			rdbManager = new RDBManager(RSettingActivity.this);
		}
		rdbManager.resetData();
		FileUtils.deleteDir();
	}
	private Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what)
			{
			case 1:
				Toast.makeText(RSettingActivity.this,"清除成功",Toast.LENGTH_SHORT).show();
				break;
			case 2:
				Toast.makeText(RSettingActivity.this,"更新成功！",Toast.LENGTH_SHORT).show();
				break;
			}
		}
		
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 1:

			break;

		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (rdbManager != null) {
			rdbManager.closeDB();
		}
	}

}
