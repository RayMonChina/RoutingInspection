package com.ideal.zsyy.activity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ideal.zsyy.Config;
import com.ideal.zsyy.adapter.RUserStatusAdapter;
import com.ideal.zsyy.entity.RDepartmentInfo;
import com.ideal.zsyy.entity.RUserStatusInfo;
import com.ideal.zsyy.response.RBaseRes;
import com.ideal.zsyy.service.PreferencesService;
import com.ideal.zsyy.utils.DialogCirleProgress;
import com.ideal.zsyy.utils.HttpUtil;
import com.shenrenkeji.intelcheck.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.GetBuilder;
import com.zhy.http.okhttp.callback.Callback;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class RPersonStatusActivity extends Activity {

	private Button btn_back;
	private ListView lv_status_user;
	private List<RUserStatusInfo>userList=null;
	private RUserStatusAdapter adapetrUserStatus=null;
	private PreferencesService preference=null;
	private String currentUserId="";
	private ToggleButton toggleOnline;
	private Spinner sp_department;
	private List<RDepartmentInfo>departmentList=new ArrayList<RDepartmentInfo>();
	private ArrayAdapter<RDepartmentInfo>adapterDepart=null;
	private ImageButton btn_refresh=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.r_user_line_status);
		this.initView();
		this.initData();
		getUserData();
		getDepartInfo();
	}

	private void initView()
	{
		btn_back=(Button)findViewById(R.id.btn_back);
		btn_back.setOnClickListener(clickListener);
		lv_status_user=(ListView)findViewById(R.id.lv_status_user_list);
		toggleOnline=(ToggleButton)findViewById(R.id.toggleOnline);
		sp_department=(Spinner)findViewById(R.id.sp_department);
		btn_refresh=(ImageButton)findViewById(R.id.btn_refresh);
		btn_refresh.setOnClickListener(clickListener);
	}
	private void initData()
	{
		preference=new PreferencesService(RPersonStatusActivity.this);
		currentUserId=preference.getLoginInfo().get("use_id").toString();
		userList=new ArrayList<RUserStatusInfo>();
		adapetrUserStatus=new RUserStatusAdapter(RPersonStatusActivity.this,userList);
		lv_status_user.setAdapter(adapetrUserStatus);
		toggleOnline.setOnCheckedChangeListener(checkedChangeListener);
		lv_status_user.setOnItemClickListener(itemClickListener);
		
		adapterDepart = new ArrayAdapter<RDepartmentInfo>(RPersonStatusActivity.this,
				android.R.layout.simple_spinner_item, departmentList);
		adapterDepart.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp_department.setAdapter(adapterDepart);
		sp_department.setOnItemSelectedListener(departClickListener);
	}
	
	private Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				if(adapetrUserStatus!=null)
				{
					adapetrUserStatus.notifyDataSetChanged();
				}
				toggleOnline.setEnabled(true);
				sp_department.setEnabled(true);
				break;

			default:
				break;
			}
		}
		
	};
	
	private OnItemSelectedListener departClickListener=new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			// TODO Auto-generated method stub
			getUserData();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}


		
	};
	
	private OnItemClickListener itemClickListener=new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			// TODO Auto-generated method stub
			if(userList==null||userList.size()<=position)
			{
				return;
			}
			RUserStatusInfo itemUserStatus=(RUserStatusInfo)userList.get(position);
			Intent intent_item=new Intent(RPersonStatusActivity.this,RTrackActivity.class);
			intent_item.putExtra("user_id", itemUserStatus.getUserid());
			startActivity(intent_item);
		}
		
	};
	
	private OnCheckedChangeListener checkedChangeListener=new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// TODO Auto-generated method stub
			switch (buttonView.getId()) {
			case R.id.toggleOnline:
				getUserData();
				break;
			default:
				break;
			}
		}
	};
	final DialogCirleProgress dCirleProgress=new DialogCirleProgress(RPersonStatusActivity.this);
	private void getUserData()
	{
		boolean isOnline=toggleOnline.isChecked();
		RDepartmentInfo departmentInfo=(RDepartmentInfo)sp_department.getSelectedItem();
		String lineStatus=isOnline?"online":"offline";
		if(!HttpUtil.checkNet(RPersonStatusActivity.this))
		{
			Toast.makeText(RPersonStatusActivity.this,"请检查网络！",Toast.LENGTH_SHORT).show();
			return;
		}
		GetBuilder gBuilder=OkHttpUtils.get().url(Config.Apiurl)
				.addParams("action", "userstatus")
				.addParams("status",lineStatus)
				.addParams("userid", currentUserId);
		if(departmentInfo!=null&&departmentInfo.getOrgid()!=null)
		{
			gBuilder.addParams("orgid",departmentInfo.getOrgid());
		}
		userList.clear();
		toggleOnline.setEnabled(false);
		sp_department.setEnabled(false);
		gBuilder.build().execute(new Callback<List<RUserStatusInfo>>() {

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
				handler.sendEmptyMessage(1);
				Toast.makeText(RPersonStatusActivity.this,"获取数据失败："+arg1.getMessage(),Toast.LENGTH_SHORT).show();
				return;
			}

			@Override
			public void onResponse(List<RUserStatusInfo> arg0, int arg1) {
				// TODO Auto-generated method stub
				dCirleProgress.hideProcessDialog();
				if(userList==null)
				{
					userList=new ArrayList<RUserStatusInfo>();
				}
				if(arg0==null)
				{
					return;
				}
				userList.addAll(arg0);
				handler.sendEmptyMessage(1);
			}

			@Override
			public List<RUserStatusInfo> parseNetworkResponse(Response arg0, int arg1) throws Exception {
				// TODO Auto-generated method stub
				String bodyStr = arg0.body().string();
				Log.i("response",bodyStr);
				Type tp=new TypeToken<RBaseRes<List<RUserStatusInfo>>>(){}.getType();
		        RBaseRes<List<RUserStatusInfo>> baseRes = new Gson().fromJson(bodyStr,tp);
		        if(baseRes!=null&&baseRes.getData()!=null)
		        {
		        	return baseRes.getData();
		        }
		        return null;
			}
			
		});
	}
	
	private void getDepartInfo()
	{
		OkHttpUtils.get().url(Config.Apiurl)
		.addParams("action", "getdepartment")
		.addParams("userid", currentUserId)
		.build().execute(new Callback<List<RDepartmentInfo>>() {

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
				Toast.makeText(RPersonStatusActivity.this,"获取数据失败："+arg1.getMessage(),Toast.LENGTH_SHORT).show();
				return;
			}

			@Override
			public void onResponse(List<RDepartmentInfo> arg0, int arg1) {
				// TODO Auto-generated method stub
				dCirleProgress.hideProcessDialog();
				departmentList.addAll(arg0);
				adapterDepart.notifyDataSetChanged();
			}

			@Override
			public List<RDepartmentInfo> parseNetworkResponse(Response arg0, int arg1) throws Exception {
				// TODO Auto-generated method stub
				String bodyStr = arg0.body().string();
				Log.i("response",bodyStr);
				Type tp=new TypeToken<RBaseRes<List<RDepartmentInfo>>>(){}.getType();
		        RBaseRes<List<RDepartmentInfo>> baseRes = new Gson().fromJson(bodyStr,tp);
		        if(baseRes!=null&&baseRes.getData()!=null)
		        {
		        	return baseRes.getData();
		        }
		        return null;
			}
			
		});
	}
	
	private OnClickListener clickListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btn_back:
				finish();
				break;
			case R.id.btn_refresh:
				getUserData();
				break;
			default:
				break;
			}
		}
	};
	
}
