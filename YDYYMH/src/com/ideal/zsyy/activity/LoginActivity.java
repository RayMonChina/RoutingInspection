package com.ideal.zsyy.activity;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ideal.zsyy.Config;
import com.ideal.zsyy.response.RBaseRes;
import com.ideal.zsyy.response.RUserInfoRes;
import com.shenrenkeji.intelcheck.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.ideal.zsyy.service.PreferencesService;
import com.ideal.zsyy.utils.DialogCirleProgress;
import com.ideal.zsyy.utils.HttpUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends Activity {

	private EditText login_user_name;
	private EditText login_pwd;
	private Intent intent;
	private PreferencesService preferencesService;
	private CheckBox ck_rember;
	private ZsyyApplication application;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		preferencesService = new PreferencesService(LoginActivity.this);
		application=(ZsyyApplication)getApplication();
		 initView();
	}

	private void initView() {
		login_user_name = (EditText) findViewById(R.id.login_tv_username);
		login_pwd = (EditText) findViewById(R.id.login_tv_password);
		Button pc_bt_submit = (Button) findViewById(R.id.login_bt_login);
		ck_rember=(CheckBox)findViewById(R.id.ck_rember_pwd);
		pc_bt_submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String uname = login_user_name.getText().toString();
				String pwd = login_pwd.getText().toString();
				if (uname != null && !uname.equals("") && !pwd.equals("") && pwd != null) {
					LoginApp(uname,pwd);
				} else if (uname == null || uname.equals("") || pwd.equals("") || pwd == null) {
					Toast.makeText(LoginActivity.this, "用户名或密码不能为空", 1).show();
					login_pwd.setText(null);
				}
			}
		});
		
		Map<String,Object> mapUserInfo= preferencesService.getLoginInfo();
		Boolean isRember=(Boolean)mapUserInfo.get("remberPwd");
		ck_rember.setChecked(isRember);
		login_user_name.setText(mapUserInfo.get("loginName").toString());
		if(isRember)
		{
			login_pwd.setText(mapUserInfo.get("pwd").toString());
		}

//		if (tv_version != null) {
//			String versionName = DeviceHelper.getVersionName(LoginActivity.this);
//			tv_version.setText(versionName);
//		}
	}
	
	public void LoginApp(String uName,final String pwd)
	{
		if(!HttpUtil.checkNet(LoginActivity.this))
		{
			Map<String, Object>userInfo=preferencesService.getLoginInfo();
			String userName=userInfo.get("loginName").toString();
			String upwd=userInfo.get("pwd").toString();
			if(userName.equals(uName)&&upwd.equals(pwd))
			{
				intent=new Intent(LoginActivity.this, RSlidMainActivity.class);
				startActivity(intent);
				finish();
				return;
			}
			else {
				Toast.makeText(LoginActivity.this,"用户名或密码错误，请联网后重试！",Toast.LENGTH_SHORT).show();
				return;
			}
		}
		final DialogCirleProgress dCirleProgress=new DialogCirleProgress(LoginActivity.this);
		OkHttpUtils.get().url(Config.Apiurl)
		.addParams("action", "login")
		.addParams("username",uName)
		.addParams("userpwd", pwd)
		.build().execute(new Callback<RUserInfoRes>() {

			@Override
			public void onBefore(Request request, int id) {
				// TODO Auto-generated method stub
				super.onBefore(request, id);
				Log.i("loginRequest", request.url().toString()) ;
				dCirleProgress.showProcessDialog();
			}

			@Override
			public void onError(Call arg0, Exception arg1, int arg2) {
				// TODO Auto-generated method stub
				dCirleProgress.hideProcessDialog();
				Toast.makeText(LoginActivity.this,"登录失败",Toast.LENGTH_SHORT).show();
				return;
			}

			@Override
			public void onResponse(RUserInfoRes arg0, int arg1) {
				// TODO Auto-generated method stub
				dCirleProgress.hideProcessDialog();
				if(arg0==null)
				{
					Toast.makeText(LoginActivity.this,"登录失败",Toast.LENGTH_SHORT).show();
					return;
				}
				preferencesService.saveLoginInfo(arg0.getUser_account(),pwd, 
						true, ck_rember.isChecked(), 
						arg0.getUser_id(), arg0.getUser_name(),arg0.getUser_code());
				if(application!=null)
				{
					application.UploadLocation(arg0.getUser_id());
				}
				intent=new Intent(LoginActivity.this, RSlidMainActivity.class);
				startActivity(intent);
				finish();
			}

			@Override
			public RUserInfoRes parseNetworkResponse(Response arg0, int arg1) throws Exception {
				// TODO Auto-generated method stub
				String bodyStr = arg0.body().string();
				Log.i("response",bodyStr);
				Type tp=new TypeToken<RBaseRes<List<RUserInfoRes>>>(){}.getType();
		        RBaseRes<List<RUserInfoRes>> baseRes = new Gson().fromJson(bodyStr,tp);
		        String retCode=baseRes.getStatus();
		        if(!"success".equalsIgnoreCase(retCode))
		        {
		        	Toast.makeText(LoginActivity.this,"登录失败",Toast.LENGTH_SHORT).show();
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

	public OnFocusChangeListener onFocusAutoClearHintListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			EditText textView = (EditText) v;
			String hint;
			if (hasFocus) {
				hint = textView.getHint().toString();
				textView.setTag(hint);
				textView.setHint("");
			} else {
				hint = textView.getTag().toString();
				textView.setHint(hint);
			}
		}
	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
	}
	
	
}
