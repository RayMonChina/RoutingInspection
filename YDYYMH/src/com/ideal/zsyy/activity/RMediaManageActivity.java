package com.ideal.zsyy.activity;

import java.io.File;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.baidu.location.BDLocation;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ideal.zsyy.Config;
import com.ideal.zsyy.adapter.RWorkInfoSelfAdapter;
import com.ideal.zsyy.db.RDBManager;
import com.ideal.zsyy.entity.RWorkMediaInfo;
import com.ideal.zsyy.entity.RWorkStepInfo;
import com.ideal.zsyy.response.RBaseRes;
import com.ideal.zsyy.response.RworkItemRes;
import com.ideal.zsyy.service.PreferencesService;
import com.ideal.zsyy.utils.DialogCirleProgress;
import com.ideal.zsyy.utils.HttpUtil;
import com.learnncode.mediachooser.activity.BucketHomeFragmentActivity;
import com.shenrenkeji.intelcheck.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.GetBuilder;
import com.zhy.http.okhttp.callback.Callback;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class RMediaManageActivity extends Activity {

	private Button btn_back;
	private TextView tv_addmore;
	private String taskNum;
	private RDBManager dbmanager;
	private List<RWorkMediaInfo>mediaInfos=new ArrayList<RWorkMediaInfo>();
	private RWorkInfoSelfAdapter workMediaAdapter;
	DialogCirleProgress dCirleProgress;
	private PreferencesService preference=null;
	private String userId="";
	private ListView lvImgList=null;
	private List<RWorkStepInfo>workSteps=new ArrayList<RWorkStepInfo>();
	private RWorkMediaInfo currMediaInfo=null;
	private LinearLayout ly_select_all=null;
	private LinearLayout ly_upload=null;
	private LinearLayout ly_delete=null;
	ArrayAdapter<RWorkStepInfo> adpWorkSteps;
	Spinner dialogSpiner;
	Dialog dialogStep=null;
	private SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	ZsyyApplication application;
	private boolean selectAll=false;
	private List<RWorkMediaInfo>selectMediaItems=new ArrayList<RWorkMediaInfo>();
	private TextView tv_select_all;
	private ImageButton img_select_all;
	private TextView tv_upload;
	private ImageButton img_upload;
	private TextView tv_delete;
	private ImageButton img_delete;
	private int uploadCount=0;
	private RworkItemRes workItem=null;
	private String remoatIds="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.r_media_manage);
		
		this.initData();
		this.initView();
		initMediaList();
		//this.getWorkStep();
	}

	private void initView() {
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(clickListener);
		tv_addmore=(TextView)findViewById(R.id.tv_addmore);
		tv_addmore.setOnClickListener(clickListener);
		lvImgList=(ListView)findViewById(R.id.lvImgList);
		lvImgList.setAdapter(workMediaAdapter);
		lvImgList.setOnItemClickListener(itemClickListener);
		dialogSpiner=(Spinner)dialogStep.findViewById(R.id.sp_step);
		if(dialogSpiner!=null)
		{
			dialogSpiner.setAdapter(adpWorkSteps);
			dialogSpiner.setOnItemSelectedListener(selectedListener);
		}
		ly_select_all=(LinearLayout)findViewById(R.id.ly_select_all);
		ly_upload=(LinearLayout)findViewById(R.id.ly_upload);
		ly_delete=(LinearLayout)findViewById(R.id.ly_delete);
		
		tv_select_all=(TextView)findViewById(R.id.tv_select_all);
		img_select_all=(ImageButton)findViewById(R.id.img_select_all);
		tv_upload=(TextView)findViewById(R.id.tv_upload);
		img_upload=(ImageButton)findViewById(R.id.img_upload);
		tv_delete=(TextView)findViewById(R.id.tv_delete);
		img_delete=(ImageButton)findViewById(R.id.img_delete);
		
		ly_select_all.setOnClickListener(clickListener);
		ly_upload.setOnClickListener(clickListener);
		ly_delete.setOnClickListener(clickListener);
		
		tv_select_all.setOnClickListener(clickListener);
		img_select_all.setOnClickListener(clickListener);
		tv_upload.setOnClickListener(clickListener);
		img_upload.setOnClickListener(clickListener);
		tv_delete.setOnClickListener(clickListener);
		img_delete.setOnClickListener(clickListener);
	}
	
	private OnItemSelectedListener selectedListener=new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			// TODO Auto-generated method stub
			
			if(arg2<workSteps.size())
			{
				RWorkStepInfo stepInfo=workSteps.get(arg2);
				currMediaInfo.setGjdmc(stepInfo.getGjdmc());
				dbmanager.updateMediaInfo(currMediaInfo);
				workMediaAdapter.notifyDataSetChanged();
			}
			if(dialogStep!=null&&dialogStep.isShowing())
			{
				dialogStep.dismiss();
			}
			
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	private void initData()
	{
		taskNum=getIntent().getStringExtra("taskNum");
		dbmanager=new RDBManager(RMediaManageActivity.this);
		workMediaAdapter=new RWorkInfoSelfAdapter(RMediaManageActivity.this,mediaInfos);
		dCirleProgress=new DialogCirleProgress(RMediaManageActivity.this);
		preference=new PreferencesService(RMediaManageActivity.this);
		userId=preference.getLoginInfo().get("use_id").toString();
		workSteps=dbmanager.GetSteps();
		adpWorkSteps=new ArrayAdapter<RWorkStepInfo>(RMediaManageActivity.this, android.R.layout.simple_spinner_item,workSteps);
		adpWorkSteps.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		dialogStep=new Dialog(RMediaManageActivity.this,R.style.dialog_media);
		dialogStep.setContentView(R.layout.r_dialog_step);
		application= (ZsyyApplication)getApplication();
		workItem=dbmanager.getWorkItemBytaskNum(taskNum);
		
	}
	
	//list view click
	private OnItemClickListener itemClickListener=new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			// TODO Auto-generated method stub
			if(mediaInfos!=null&&mediaInfos.size()>arg2)
			{
				currMediaInfo=mediaInfos.get(arg2);
				if(currMediaInfo.getAlreadyUpload()==1)
				{
					Toast.makeText(RMediaManageActivity.this,"已上传文件不能修改环节！", Toast.LENGTH_SHORT).show();
					return;
				}
				dialogStep.show();
			}
		}
		
	};


	private OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btn_back:
				Intent intent_data=new Intent();
				intent_data.putExtra("taskExists",workItem!=null);
				intent_data.putExtra("fileCount",mediaInfos.size());
				setResult(RESULT_OK, intent_data);
				finish();
				break;
			case R.id.tv_addmore:
				Intent intent_album=new Intent(RMediaManageActivity.this,BucketHomeFragmentActivity.class);
				startActivityForResult(intent_album, 2);
				break;
			case R.id.ly_select_all:
			case R.id.tv_select_all:
			case R.id.img_select_all:
				selectAll();
				break;
			case R.id.ly_upload:
			case R.id.tv_upload:
			case R.id.img_upload:
				uploadFiles();
				break;
			case R.id.ly_delete:
			case R.id.tv_delete:
			case R.id.img_delete:
				removeItem();
				break;
			default:
				break;
			}
		}
	};
	
	//全选
	private void selectAll()
	{
		selectAll=!selectAll;
		if(mediaInfos!=null)
		{
			for(RWorkMediaInfo info:mediaInfos)
			{
//				if(info.getAlreadyUpload()==1)
//				{
//					continue;
//				}
				info.setSelect(selectAll);
			}
			if(workMediaAdapter!=null)
			{
				workMediaAdapter.notifyDataSetChanged();
			}
		}
	}
	
	//删除
	private void removeItem()
	{
		selectMediaItems.clear();
		remoatIds="";
		for(int i=0;i<mediaInfos.size();i++)
		{
			RWorkMediaInfo mInfo=mediaInfos.get(i);
			if(mInfo.isSelect())
			{
				selectMediaItems.add(mInfo);
				if(mInfo.getAlreadyUpload()==1)
				{
					remoatIds=remoatIds+mInfo.getId()+",";
				}
			}
		}
		if(selectMediaItems.size()==0)
		{
			Toast.makeText(RMediaManageActivity.this,"请选择文件", Toast.LENGTH_SHORT).show();
			return;
		}
		dbmanager.removeMediaList(selectMediaItems);
		mediaInfos.removeAll(selectMediaItems);
		if(workItem!=null)
		{
			int totleRecordCount=workItem.getRecordcount()-selectMediaItems.size();;
			if(totleRecordCount<0)
			{
				totleRecordCount=0;
			}
			workItem.setRecordcount(totleRecordCount);
			dbmanager.updateWorkInfo(workItem);
		}
		if(workMediaAdapter!=null)
		{
			workMediaAdapter.notifyDataSetChanged();
		}
		
		//删除远程文件
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				remvoeRemoatFile();
			}
		}).start();
	}
	
	//文件上传
	private void uploadFiles()
	{
		selectMediaItems.clear();
		for(int i=0;i<mediaInfos.size();i++)
		{
			if(mediaInfos.get(i).isSelect()&&mediaInfos.get(i).getAlreadyUpload()==0)
			{
				selectMediaItems.add(mediaInfos.get(i));
			}
		}
		if(selectMediaItems.size()==0)
		{
			Toast.makeText(RMediaManageActivity.this,"请选择文件上传", Toast.LENGTH_SHORT).show();
			return;
		}
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				handler.sendEmptyMessage(4);
				uploadFile();
			}
		}).start();
		
		
	}
	
	//删除远程文件
	private void remvoeRemoatFile()
	{
		if(remoatIds==null||remoatIds.replace(",","").trim().equals(""))
		{
			return;
		}
		OkHttpUtils.post()
		.url(Config.Apiurl + "?action=mediadel")
		.addParams("ids", remoatIds)
		.addParams("userid", userId)
		.build().execute(new Callback<String>() {

			@Override
			public void onBefore(Request request, int id) {
				// TODO Auto-generated method stub
				super.onBefore(request, id);
				Log.i("loginRequest", request.url().toString());
			}

			@Override
			public void onError(Call arg0, Exception arg1, int arg2) {
				// TODO Auto-generated method stub
				handler.sendEmptyMessage(5);
			}

			@Override
			public void onResponse(String arg0, int arg1) {
				// TODO Auto-generated method stub
			}

			@Override
			public String parseNetworkResponse(Response arg0, int arg1) throws Exception {
				// TODO Auto-generated method stub
				String bodyStr = arg0.body().string();
				Log.i("response", bodyStr);
				Type tp = new TypeToken<RBaseRes<String>>() {
				}.getType();
				RBaseRes<String> baseRes = new Gson().fromJson(bodyStr, tp);
				if (baseRes != null && baseRes.getData() != null) {
					return baseRes.getData();
				}
				return "";
			}

		});
	}
	
	//
	private void uploadFile()
	{
		if(selectMediaItems==null&&selectMediaItems.size()>0&&selectMediaItems.size()<=uploadCount)
		{
			handler.sendEmptyMessage(5);
			return;
		}
		RWorkMediaInfo mInfo=selectMediaItems.get(uploadCount);
		File fileMedia=new File(mInfo.getPicpath());
		if(fileMedia.exists()&&fileMedia.isFile())
		{
			OkHttpUtils.post()
			.url(Config.Apiurl + "?action=addscene")
			.addParams("ticketnum", mInfo.getTicketnum())
			.addParams("gpstime",mInfo.getGpstime())
			.addParams("lon",String.valueOf(mInfo.getLon()))
			.addParams("lat",String.valueOf(mInfo.getLat()))
			.addParams("userid", mInfo.getUserid())
			.addParams("gjdmc",mInfo.getGjdmc())
			.addParams("gpsaddress",mInfo.getGpsaddress())
			.addParams("id", mInfo.getId())
			.addFile("mediafile", fileMedia.getName(),fileMedia).build().execute(new Callback<String>() {

				@Override
				public void onBefore(Request request, int id) {
					// TODO Auto-generated method stub
					super.onBefore(request, id);
					Log.i("loginRequest", request.url().toString());
				}

				@Override
				public void onError(Call arg0, Exception arg1, int arg2) {
					// TODO Auto-generated method stub
					handler.sendEmptyMessage(5);
					Toast.makeText(RMediaManageActivity.this,arg1.getMessage(),Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onResponse(String arg0, int arg1) {
					// TODO Auto-generated method stub
					RWorkMediaInfo rMediaInfo=selectMediaItems.get(uploadCount);
					rMediaInfo.setAlreadyUpload(1);
					dbmanager.updateMediaInfo(rMediaInfo);
					uploadCount++;
					if(uploadCount>=selectMediaItems.size())
					{
						if(workMediaAdapter!=null)
						{
							workMediaAdapter.notifyDataSetChanged();
						}
						handler.sendEmptyMessage(5);
						Toast.makeText(RMediaManageActivity.this,"上传成功!",Toast.LENGTH_SHORT).show();
					}
					else {
						uploadFile();
					}
					
				}

				@Override
				public String parseNetworkResponse(Response arg0, int arg1) throws Exception {
					// TODO Auto-generated method stub
					String bodyStr = arg0.body().string();
					Log.i("response", bodyStr);
					Type tp = new TypeToken<RBaseRes<String>>() {
					}.getType();
					RBaseRes<String> baseRes = new Gson().fromJson(bodyStr, tp);
					if (baseRes != null && baseRes.getData() != null) {
						return baseRes.getData();
					}
					return "";
				}

			});
		}
	}
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 1:
			break;
		case 2:
			if(data!=null)
			{
				String fileType=data.getStringExtra("fileType");
				String fileUrl=data.getStringExtra("fileUrl");
				fileSelected(fileType, fileUrl);
				handler.sendEmptyMessage(1);
			}
			
			break;
		default:
			break;
		}
	}
	
	private void fileSelected(String fileType,String fileUrl)
	{
		if(fileType==null||fileUrl==null)
		{
			return;
		}
		BDLocation bdLocation=application.currLocation;
		boolean locationSuccess=true;
		if(bdLocation==null)
		{
			Toast.makeText(RMediaManageActivity.this, "定位失败", Toast.LENGTH_SHORT).show();
			locationSuccess=false;
		}
		if(bdLocation.getLocType() == BDLocation.TypeServerError)
		{
			Toast.makeText(RMediaManageActivity.this, "服务端网络定位失败", Toast.LENGTH_SHORT).show();
			locationSuccess=false;
		}
		else if (bdLocation.getLocType() == BDLocation.TypeNetWorkException) {
			Toast.makeText(RMediaManageActivity.this, "网络不同导致定位失败，请检查网络是否通畅", Toast.LENGTH_SHORT).show();
			locationSuccess=false;
		} else if (bdLocation.getLocType() == BDLocation.TypeCriteriaException) {
			Toast.makeText(RMediaManageActivity.this, "无法获取有效定位依据导致定位失败", Toast.LENGTH_SHORT).show();
			locationSuccess=false;
		}
		RWorkMediaInfo mediaInfo=new RWorkMediaInfo();
		mediaInfo.setAlreadyUpload(0);
		mediaInfo.setCreatetime(simpleDateFormat.format(new Date()));
		if(workSteps!=null&&workSteps.size()>0)
		{
			mediaInfo.setGjdmc(workSteps.get(0).getGjdmc());
		}
		mediaInfo.setGpsaddress(bdLocation.getAddrStr());
		mediaInfo.setGpstime(bdLocation.getTime());
		mediaInfo.setId(UUID.randomUUID().toString());
		mediaInfo.setIslast(0);
		mediaInfo.setLat(bdLocation.getLatitude());
		mediaInfo.setLon(bdLocation.getLongitude());
		mediaInfo.setPicpath(fileUrl);
		mediaInfo.setRemark("");
		mediaInfo.setTicketnum(taskNum);
		mediaInfo.setUserid(userId);
		mediaInfo.setLocalFilePath(mediaInfo.getPicpath());
		if("img".equals(fileType))
		{
			mediaInfo.setPickind("图片");
			
		}
		else if("audio".equals(fileType)){
			mediaInfo.setPickind("录音");
		}
		
		mediaInfos.add(mediaInfo);
		dbmanager.addMediaInfo(mediaInfo);
		if(workItem!=null)
		{
			workItem.setRecordcount(workItem.getRecordcount()+1);
			dbmanager.updateWorkInfo(workItem);
		}
	}
	
	private Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				List<RWorkMediaInfo>mediaAll=dbmanager.GetMediaInfoByTaskNum(taskNum);
				if(mediaAll!=null)
				{
					mediaInfos.clear();
					mediaInfos.addAll(mediaAll);
				}
				workMediaAdapter.notifyDataSetChanged();
				break;
			case 3:
				if(adpWorkSteps!=null)
				{
					adpWorkSteps.notifyDataSetChanged();
				}
				break;
			case 4:
				dCirleProgress.showProcessDialog();
				break;
			case 5:
				dCirleProgress.hideProcessDialog();
				break; 
			default:
				break;
			}
		}
		
	};
	
	private void getWorkStep() {
		GetBuilder getBuilder = OkHttpUtils.get().url(Config.Apiurl).addParams("action", "querykeypoint");
		getBuilder.build().execute(new Callback<List<RWorkStepInfo>>() {

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
				Toast.makeText(RMediaManageActivity.this,arg1.getMessage(), Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onResponse(List<RWorkStepInfo> arg0, int arg1) {
				// TODO Auto-generated method stub
				dCirleProgress.hideProcessDialog();
				workSteps.clear();
				workSteps.addAll(arg0);
				handler.sendEmptyMessage(3);
			}

			@Override
			public List<RWorkStepInfo> parseNetworkResponse(Response arg0, int arg1) throws Exception {
				// TODO Auto-generated method stub
				String bodyStr = arg0.body().string();
				Log.i("response", bodyStr);
				Type tp = new TypeToken<RBaseRes<List<RWorkStepInfo>>>() {
				}.getType();
				RBaseRes<List<RWorkStepInfo>> baseRes = new Gson().fromJson(bodyStr, tp);
				if (baseRes != null && baseRes.getData() != null) {
					return baseRes.getData();
				}
				return null;
			}

		});
	}///
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			Intent intent_data=new Intent();
			intent_data.putExtra("taskExists",workItem!=null);
			intent_data.putExtra("fileCount",mediaInfos.size());
			setResult(RESULT_OK, intent_data);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void initMediaList()
	{
		if(!HttpUtil.checkNet(RMediaManageActivity.this))
		{
			//Toast.makeText(RMediaManageActivity.this,"请检查网络！",Toast.LENGTH_SHORT).show();
			handler.sendEmptyMessage(1);
			return;
		}
		OkHttpUtils.get().url(Config.Apiurl)
		.addParams("action", "mediamgr")
		.addParams("ticketnum", taskNum)
		.addParams("userid", userId)
		.build().execute(new Callback<List<RWorkMediaInfo>>() {

			@Override
			public void onBefore(Request request, int id) {
				// TODO Auto-generated method stub
				super.onBefore(request, id);
				Log.i("loginRequest", request.url().toString()) ;
				handler.sendEmptyMessage(4);
			}

			@Override
			public void onError(Call arg0, Exception arg1, int arg2) {
				// TODO Auto-generated method stub
				handler.sendEmptyMessage(5);
				return;
			}

			@Override
			public void onResponse(List<RWorkMediaInfo> arg0, int arg1) {
				// TODO Auto-generated method stub
				handler.sendEmptyMessage(5);
				if(arg0!=null)
				{
					dbmanager.removeMediaInfo();
					dbmanager.AddMediaInfos(arg0,taskNum);
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
		        	Toast.makeText(RMediaManageActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
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
