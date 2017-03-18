package com.ideal.zsyy.activity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ideal.zsyy.Config;
import com.ideal.zsyy.adapter.RWorkInfoAdapter;
import com.ideal.zsyy.db.RDBManager;
import com.ideal.zsyy.entity.RWorkMediaInfo;
import com.ideal.zsyy.response.RBaseRes;
import com.ideal.zsyy.response.RworkItemRes;
import com.ideal.zsyy.utils.DialogCirleProgress;
import com.ideal.zsyy.utils.HttpUtil;
import com.shenrenkeji.intelcheck.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class RWorkInfoDetailActivity extends Activity {

	private TextView tv_company;
	private Button btn_back;
	private TextView tv_unit;
	private TextView tv_username;
	private TextView tv_content;
	private TextView tv_record_date;
	private ListView lv_location;
	private String taskName;
	private RDBManager dbmanager;
	private RworkItemRes workItem;
	private List<RWorkMediaInfo>mediaInfos=new ArrayList<RWorkMediaInfo>();
	private RWorkInfoAdapter workMediaAdapter;
	DialogCirleProgress dCirleProgress;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.r_work_info_detail);
		this.initData();
		this.initView();
		this.initMediaList();
	}

	private void initView()
	{
		tv_company=(TextView)findViewById(R.id.tv_company);
		btn_back=(Button)findViewById(R.id.btn_back);
		tv_unit=(TextView)findViewById(R.id.tv_unit);
		tv_username=(TextView)findViewById(R.id.tv_username);
		tv_content=(TextView)findViewById(R.id.tv_content);
		tv_record_date=(TextView)findViewById(R.id.tv_record_date);
		lv_location=(ListView)findViewById(R.id.lv_location);
		btn_back.setOnClickListener(clickListener);
		lv_location.setAdapter(workMediaAdapter);
		if(workItem!=null)
		{
			tv_company.setText(workItem.getCounty());
			tv_unit.setText(workItem.getUnit());
			tv_username.setText(workItem.getSendperson());
			tv_content.setText(workItem.getTaskname());
			tv_record_date.setText(workItem.getCreatetime());
		}
	}
	
	private OnClickListener clickListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btn_back:
				finish();
				break;

			default:
				break;
			}
		}
	};
	
	
	private void initData()
	{
		taskName=getIntent().getStringExtra("taskNum");
		dbmanager=new RDBManager(RWorkInfoDetailActivity.this);
		workMediaAdapter=new RWorkInfoAdapter(RWorkInfoDetailActivity.this, mediaInfos);
		workItem=dbmanager.getWorkItemBytaskNum(taskName);
		dCirleProgress=new DialogCirleProgress(RWorkInfoDetailActivity.this);
	}
	
	private Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				if(workMediaAdapter!=null)
				{
					workMediaAdapter.notifyDataSetChanged();
				}
				break;

			default:
				break;
			}
		}
		
	};
	
	private void initMediaList()
	{
		if(!HttpUtil.checkNet(RWorkInfoDetailActivity.this))
		{
			Toast.makeText(RWorkInfoDetailActivity.this,"请检查网络！",Toast.LENGTH_SHORT).show();
			return;
		}
		if(workItem==null)
		{
			Toast.makeText(RWorkInfoDetailActivity.this,"数据加载失败",Toast.LENGTH_SHORT).show();
			return;
		}
		OkHttpUtils.get().url(Config.Apiurl)
		.addParams("action", "mediamgr")
		.addParams("ticketnum", taskName)
		.addParams("userid", workItem.getUserid())
		.build().execute(new Callback<List<RWorkMediaInfo>>() {

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
				return;
			}

			@Override
			public void onResponse(List<RWorkMediaInfo> arg0, int arg1) {
				// TODO Auto-generated method stub
				dCirleProgress.hideProcessDialog();
				if(arg0!=null)
				{
					mediaInfos.clear();
					mediaInfos.addAll(arg0);
					handler.sendEmptyMessage(1);
				}
			}

			@Override
			public List<RWorkMediaInfo> parseNetworkResponse(Response arg0, int arg1) throws Exception {
				// TODO Auto-generated method stub
				String bodyStr = arg0.body().string();
				Log.i("response",bodyStr);
				Type tp=new TypeToken<RBaseRes<List<RWorkMediaInfo>>>(){}.getType();
				List<RWorkMediaInfo> retList=null;
				if(bodyStr==null)
				{
					return retList;
				}
				bodyStr=bodyStr.replace("\\", "/");
		        RBaseRes<List<RWorkMediaInfo>> baseRes = new Gson().fromJson(bodyStr,tp);
		        String retCode=baseRes.getStatus();
		        if(!"success".equalsIgnoreCase(retCode))
		        {
		        	Toast.makeText(RWorkInfoDetailActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
		        	return retList;
		        }
		        if(baseRes!=null&&baseRes.getData()!=null)
		        {
		        	retList= baseRes.getData();
		        }
		        return retList;
			}
			
		});
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(dbmanager!=null)
		{
			dbmanager.closeDB();
		}
	}
	
	
}
