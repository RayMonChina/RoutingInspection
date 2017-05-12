package com.ideal.zsyy.activity;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.baidu.location.BDLocation;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ideal.zsyy.Config;
import com.ideal.zsyy.db.RDBManager;
import com.ideal.zsyy.entity.RWorkMediaInfo;
import com.ideal.zsyy.entity.RWorkStepInfo;
import com.ideal.zsyy.entity.RWorkType;
import com.ideal.zsyy.response.RBaseRes;
import com.ideal.zsyy.response.RworkItemRes;
import com.ideal.zsyy.service.PreferencesService;
import com.ideal.zsyy.utils.DateUtils;
import com.ideal.zsyy.utils.DialogCirleProgress;
import com.ideal.zsyy.utils.FileUtils;
import com.ideal.zsyy.utils.HttpUtil;
import com.ideal.zsyy.utils.ImageUtils;
import com.ideal.zsyy.utils.StringHelper;
import com.ideal.zsyy.utils.UtilWifi;
import com.shenrenkeji.intelcheck.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.GetBuilder;
import com.zhy.http.okhttp.callback.Callback;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class RWorkOrderActivity extends Activity {

	String fileName = "";// 照片路径
	List<RWorkType> listWorkType;
	List<RWorkType> listDyLevel;
	List<String> picQulity = null;
	List<RWorkStepInfo> workSteps = null;
	ArrayAdapter<RWorkType> adpWorkTypes;
	ArrayAdapter<RWorkType> adpDyLevels;
	ArrayAdapter<RWorkStepInfo> adpWorkSteps;
	private Spinner spinnerWorkType = null;
	private Spinner spinnerDyLevel = null;
	// private EditText editNum = null;
	private EditText editPerson1 = null;
	private Spinner spiner_step = null;// 环节
	private Spinner spiPicChoice = null;// 图片清晰度
	private ImageView btnVideo = null;// 录像
	private ImageView btnCamera = null;// 拍照
	private EditText etWorkNum;// 编号
	private EditText etworkname;// 内容
	private EditText etworkRemark;// 备注
	private Button btn_back;
	// private ImageButton btn_vi
	private ImageView btnNextKey = null;
	private ImageButton btn_upload = null;
	PreferencesService preferencesService = null;
	public static int Video_Request_Code = 1;
	public static int Pic_Request_Code = 2;
	private ZsyyApplication application;
	private MediaRecorder mediarecordera;
	private boolean isRecording = false;
	private String AudioPath = "";
	private Chronometer chronometers;
	private TextView txtVedio;
	private RworkItemRes workItemInfo = null;
	private RDBManager dbManager = null;
	private ImageView btn_mediaManage = null;
	private SimpleDateFormat simFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private RWorkMediaInfo mediaInfo = null;
	private TextView tv_btnImgListLook = null;// 影音管理
	RworkItemRes localWorkItem = null;
	boolean hasWordExists = false;
	String taskNum = "";
	boolean isChecking=false;
	private String macAddress="";
	UtilWifi uwifi=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.r_work_order);
		preferencesService = new PreferencesService(RWorkOrderActivity.this);
		this.initData();
		this.initView();
		// this.initWokType();
		// getWorkStep("", "");
		this.initControlData();
	}///

	private void initData() {
		uwifi=new UtilWifi(RWorkOrderActivity.this);
		picQulity = new ArrayList<String>();
		picQulity.add("一般 640");
		picQulity.add("优 1280");
		picQulity.add("超清 2560");
		application = (ZsyyApplication) getApplicationContext();
		dbManager = new RDBManager(RWorkOrderActivity.this);
		taskNum = getIntent().getStringExtra("taskNum");
		if (taskNum != null && !"".equals(taskNum)) {
			workItemInfo = dbManager.getWorkItemBytaskNum(taskNum);
		}
		listWorkType = dbManager.GetWorkTypeList();
		listDyLevel = dbManager.GetVolClassList();
		workSteps = dbManager.GetSteps();

		adpWorkTypes = new ArrayAdapter<RWorkType>(RWorkOrderActivity.this, android.R.layout.simple_spinner_item,
				listWorkType);
		adpWorkTypes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		adpDyLevels = new ArrayAdapter<RWorkType>(RWorkOrderActivity.this, android.R.layout.simple_spinner_item,
				listDyLevel);
		adpDyLevels.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		adpWorkSteps = new ArrayAdapter<RWorkStepInfo>(RWorkOrderActivity.this, android.R.layout.simple_spinner_item,
				workSteps);
		adpWorkSteps.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	}

	private void initControlData() {
		if (workItemInfo != null) {
			etWorkNum.setText(workItemInfo.getTicketnum());
			etworkname.setText(workItemInfo.getTaskname());
			etworkRemark.setText(workItemInfo.getRemark());
			editPerson1.setText(workItemInfo.getPeiheren());
			if (tv_btnImgListLook != null) {
				tv_btnImgListLook.setText("影音管理(" + workItemInfo.getRecordcount() + ")");
			}
			handler.sendEmptyMessage(1);
			handler.sendEmptyMessage(2);
			handler.sendEmptyMessage(3);
		}

	}

	private void initView() {
		spinnerWorkType = (Spinner) findViewById(R.id.spiWorkType);
		spinnerWorkType.setAdapter(adpWorkTypes);
		if (spinnerWorkType != null)// 工作类型
		{
			spinnerWorkType.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					// TODO Auto-generated method stub
					RWorkType selItem = (RWorkType) arg0.getSelectedItem();
					RWorkType ovClassItem = (RWorkType) spinnerDyLevel.getSelectedItem();
					String workType = "";
					String dyLevel = "";
					if (selItem != null) {
						workType = selItem.getTypename();
					}
					if (ovClassItem != null) {
						dyLevel = ovClassItem.getTypename();
					}
					// getWorkStep(workType, dyLevel);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub

				}

			});
		} ///
		spinnerDyLevel = (Spinner) findViewById(R.id.spiEleLevel);// 工作电压
		spinnerDyLevel.setAdapter(adpDyLevels);
		if (spinnerDyLevel != null) {
			spinnerDyLevel.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					// TODO Auto-generated method stub
					RWorkType selItem = (RWorkType) arg0.getSelectedItem();
					RWorkType workTypeItem = (RWorkType) spinnerWorkType.getSelectedItem();
					String dyLevel = "";
					String workType = "";
					if (selItem != null) {
						dyLevel = selItem.getTypename();
					}
					if (workTypeItem != null) {
						workType = workTypeItem.getTypename();
					}

					// getWorkStep(workType, dyLevel);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub

				}

			});
		} ///
		Map<String, Object> mapUserInfo = preferencesService.getLoginInfo();
		etWorkNum = (EditText) findViewById(R.id.etWorkNum);
		String userCode = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + preferencesService.getUserCode();
		etWorkNum.setText(userCode);
		// Toast.makeText(RWorkOrderActivity.this,
		// etWorkNum.getText().toString(), Toast.LENGTH_SHORT).show();
		editPerson1 = (EditText) findViewById(R.id.etWorkPerson1);

		spiPicChoice = (Spinner) findViewById(R.id.spiPicChoice);// 分辨率
		ArrayAdapter<String> adpPicQuality = new ArrayAdapter<String>(RWorkOrderActivity.this,
				android.R.layout.simple_spinner_item, picQulity);
		adpPicQuality.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spiPicChoice.setAdapter(adpPicQuality);
		spiPicChoice.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}

		});///

		spiner_step = (Spinner) findViewById(R.id.spiner_step);// 操作步骤
		spiner_step.setAdapter(adpWorkSteps);
		spiner_step.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}

		});//

		btnNextKey = (ImageView) findViewById(R.id.btnNextKey);
		btnNextKey.setOnClickListener(clickListener);
		btnVideo = (ImageView) findViewById(R.id.btnVideo);
		btnVideo.setOnClickListener(clickListener);
		btnCamera = (ImageView) findViewById(R.id.btnCamera);
		btnCamera.setOnClickListener(clickListener);
		btn_upload = (ImageButton) findViewById(R.id.btn_upload);
		btn_upload.setOnClickListener(clickListener);
		etworkname = (EditText) findViewById(R.id.etworkname);// 内容
		etworkRemark = (EditText) findViewById(R.id.etworkRemark);// 备注
		chronometers = (Chronometer) findViewById(R.id.chronometers);// 计时器
		txtVedio = (TextView) findViewById(R.id.txtVedio);
		btn_mediaManage = (ImageView) findViewById(R.id.btn_manage);
		btn_mediaManage.setOnClickListener(clickListener);
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(clickListener);
		// if(taskNum!=null&&!"".equals(taskNum.trim())&&workItemInfo!=null&&workItemInfo.getAlreadyUpload()==1)
		// {
		// btn_upload.setVisibility(View.GONE);
		// }
		tv_btnImgListLook = (TextView) findViewById(R.id.btnImgListLook);
		
		etworkname.setOnFocusChangeListener(focusChangeListener);
	}
	
	private OnFocusChangeListener focusChangeListener=new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			// TODO Auto-generated method stub
			if(!hasFocus){
				//checkTaskName();
			}
		}
	};

	private OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
//			v.setFocusableInTouchMode(true);
//			v.setFocusable(true);
//			v.requestFocus();
//			if(isChecking)
//			{
//				Toast.makeText(RWorkOrderActivity.this, "正在验证数据，请稍候重试",Toast.LENGTH_SHORT).show();
//				return;
//			}
			//String tickName = etworkname.getText().toString();
			switch (v.getId()) {
			case R.id.btnNextKey:
				nextStep();
				break;
			case R.id.btnVideo:
				// Intent intentVideo = new Intent(RWorkOrderActivity.this,
				// VideoDemoActivity.class);
				// startActivityForResult(intentVideo, Video_Request_Code);
				//AudioRecord();
				checkTaskName(R.id.btnVideo);
				break;
			case R.id.btnCamera:
				//photo();
				checkTaskName(R.id.btnCamera);
				break;
			case R.id.btn_upload:
				//uploadWork();
				checkTaskName(R.id.btn_upload);
				break;
			case R.id.btn_manage:
//				// initWorkData();
//				taskNum = etWorkNum.getText().toString();
//				Intent intent_media = new Intent(RWorkOrderActivity.this, RMediaManageActivity.class);
//				intent_media.putExtra("taskNum", taskNum);
//				startActivityForResult(intent_media, 3);
				checkTaskName(R.id.btn_manage);
				break;
			case R.id.btn_back:
				finish();
				break;
			default:
				break;
			}
		}
	};
	
	private Handler handlerClick=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case R.id.btnVideo:
				AudioRecord();//录音
				break;
			case R.id.btnCamera:
				photo();//拍照
				break;
			case R.id.btn_upload://上传
				uploadWork();
				break;
			case R.id.btn_manage://选择文件
				taskNum = etWorkNum.getText().toString();
				Intent intent_media = new Intent(RWorkOrderActivity.this, RMediaManageActivity.class);
				intent_media.putExtra("taskNum", taskNum);
				startActivityForResult(intent_media, 3);
				break;
			default:
				break;
			}
		};
	};

	private void nextStep() {
		if (workSteps == null || workSteps.size() == 0) {
			Toast.makeText(RWorkOrderActivity.this, "到达末环节", Toast.LENGTH_SHORT).show();
			return;
		}
		int currPosition = spiner_step.getSelectedItemPosition();
		if (currPosition + 1 >= workSteps.size()) {
			Toast.makeText(RWorkOrderActivity.this, "到达末环节", Toast.LENGTH_SHORT).show();
			return;
		}
		spiner_step.setSelection(currPosition + 1);
	}

	public void initWokType() {
		if (!HttpUtil.checkNet(RWorkOrderActivity.this)) {
			Toast.makeText(RWorkOrderActivity.this, "请检查网络！", Toast.LENGTH_SHORT).show();
			return;
		}
		final DialogCirleProgress dCirleProgress = new DialogCirleProgress(RWorkOrderActivity.this);
		OkHttpUtils.get().url(Config.Apiurl).addParams("action", "getworktype").build()
				.execute(new Callback<List<RWorkType>>() {

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
						return;
					}

					@Override
					public void onResponse(List<RWorkType> arg0, int arg1) {
						// TODO Auto-generated method stub
						dCirleProgress.hideProcessDialog();
						listWorkType.clear();
						listWorkType.addAll(arg0);
						handler.sendEmptyMessage(1);
						// handler.sendEmptyMessage(7);
						if (listDyLevel == null || listDyLevel.size() == 0) {
							handler.sendEmptyMessage(4);
						}
					}

					@Override
					public List<RWorkType> parseNetworkResponse(Response arg0, int arg1) throws Exception {
						// TODO Auto-generated method stub
						String bodyStr = arg0.body().string();
						Log.i("response", bodyStr);
						Type tp = new TypeToken<RBaseRes<List<RWorkType>>>() {
						}.getType();
						RBaseRes<List<RWorkType>> baseRes = new Gson().fromJson(bodyStr, tp);
						if (baseRes != null && baseRes.getData() != null) {
							return baseRes.getData();
						}
						return null;
					}

				});

	}/// 工作类型

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				adpWorkTypes.notifyDataSetChanged();
				if (listWorkType != null && workItemInfo != null) {
					for (int i = 0; i < listWorkType.size(); i++) {
						if (listWorkType.get(i).getTypename().equals(workItemInfo.getWorktype())) {
							spinnerWorkType.setSelection(i);
							break;
						}
					}
				}
				break;
			case 2:
				adpDyLevels.notifyDataSetChanged();
				if (listDyLevel != null && workItemInfo != null) {
					for (int i = 0; i < listDyLevel.size(); i++) {
						if (listDyLevel.get(i).getTypename().equals(workItemInfo.getVolclass())) {
							spinnerDyLevel.setSelection(i);
							break;
						}
					}
				}
				break;
			case 3:
				adpWorkSteps.notifyDataSetChanged();
				if (workSteps != null && workItemInfo != null) {
					for (int i = 0; i < workSteps.size(); i++) {
						if (workSteps.get(i).getGjdmc().equals(workItemInfo.getGjdmc())) {
							spiner_step.setSelection(i);
							break;
						}
					}
				}
				break;
			case 4:
				initDyLevel();
				break;
			case 5:
				getWorkStep("", "");
			case 6:
				if (workItemInfo != null) {
					tv_btnImgListLook.setText("影音管理(" + workItemInfo.getRecordcount() + ")");
				}
				break;
			case 7:
				Toast.makeText(getApplicationContext(), "上传成功！", Toast.LENGTH_SHORT).show();
				if(StringHelper.isEmpty(workItemInfo.getUnit()))
				{
					updateTaskInfo();
				}
				// Intent intent_upSuccess=new Intent();
				// setResult(RESULT_OK, intent_upSuccess);
				// finish();
				break;
				
			default:
				break;
			}
		}

	};

	public void initDyLevel() {
		final DialogCirleProgress dCirleProgress = new DialogCirleProgress(RWorkOrderActivity.this);
		OkHttpUtils.get().url(Config.Apiurl).addParams("action", "getvolclass").build()
				.execute(new Callback<List<RWorkType>>() {

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
					}

					@Override
					public void onResponse(List<RWorkType> arg0, int arg1) {
						// TODO Auto-generated method stub
						dCirleProgress.hideProcessDialog();
						listDyLevel.clear();
						listDyLevel.addAll(arg0);
						handler.sendEmptyMessage(2);
					}

					@Override
					public List<RWorkType> parseNetworkResponse(Response arg0, int arg1) throws Exception {
						// TODO Auto-generated method stub
						String bodyStr = arg0.body().string();
						Log.i("response", bodyStr);
						Type tp = new TypeToken<RBaseRes<List<RWorkType>>>() {
						}.getType();
						RBaseRes<List<RWorkType>> baseRes = new Gson().fromJson(bodyStr, tp);
						if (baseRes != null && baseRes.getData() != null) {
							return baseRes.getData();
						}
						return null;
					}

				});

	}/// 电压等级

	private void initWorkData() {
		String ticketnum = etWorkNum.getText().toString();
		String tickName = etworkname.getText().toString();
		String tickMark = etworkRemark.getText().toString();
		String workType = "";
		String ovClass = "";
		String step = "";
		String userId = preferencesService.getLoginInfo().get("use_id").toString();
		String peiheren = editPerson1.getText().toString();
		if (spinnerWorkType.getSelectedItem() != null) {
			RWorkType wRWorkTypeItem = (RWorkType) spinnerWorkType.getSelectedItem();
			workType = wRWorkTypeItem.getTypename();
		}
		if (spinnerDyLevel.getSelectedItem() != null) {
			RWorkType dyLevelItem = (RWorkType) spinnerDyLevel.getSelectedItem();
			ovClass = dyLevelItem.getTypename();
		}
		if (spiner_step.getSelectedItem() != null) {
			RWorkStepInfo workStep = (RWorkStepInfo) spiner_step.getSelectedItem();
			step = workStep.getGjdmc();
		}
		if (workItemInfo == null) {
			workItemInfo = new RworkItemRes();
			workItemInfo.setAlreadyUpload(0);
			workItemInfo.setCity("");
			workItemInfo.setCounty("");
			workItemInfo.setCreatetime(simFormat.format(new Date()));
			workItemInfo.setId(UUID.randomUUID().toString());
			workItemInfo.setIsDownload(0);
			workItemInfo.setProvince("");
			workItemInfo.setRemark(tickMark);
			workItemInfo.setState("");
			workItemInfo.setTaskname(tickName);
			workItemInfo.setTicketnum(ticketnum);
			workItemInfo.setUnit("");
			workItemInfo.setUserid(userId);
			workItemInfo.setVolclass(ovClass);
			workItemInfo.setWorktype(workType);
			workItemInfo.setPeiheren(peiheren);
			workItemInfo.setGjdmc(step);
			workItemInfo.setCreatetime(simFormat.format(new Date()));
			// dbManager.AddWorkItem(workItemInfo);
			taskNum = ticketnum;
		} else {
			workItemInfo.setRemark(tickMark);
			workItemInfo.setTaskname(tickName);
			workItemInfo.setTicketnum(ticketnum);
			workItemInfo.setUserid(userId);
			workItemInfo.setVolclass(ovClass);
			workItemInfo.setWorktype(workType);
			workItemInfo.setGjdmc(step);
			workItemInfo.setPeiheren(peiheren);
			// dbManager.updateWorkInfo(workItemInfo);
		}
	}
	
	private String getMacAddress(){
		if(StringHelper.isEmpty(macAddress)){
			macAddress=uwifi.getMacAddress();
		}
		return macAddress;
	}

	public void uploadWork()// 上传
	{
		initWorkData();
		if (workItemInfo != null) {
			workItemInfo.setUsermac(getMacAddress());
			if (workItemInfo.getAlreadyUpload() == 0
					&& dbManager.getWorkItemBytaskNum(workItemInfo.getTicketnum()) == null)
				dbManager.AddWorkItem(workItemInfo);
			else
				dbManager.updateWorkInfo(workItemInfo);
		}
		else {
			Toast.makeText(RWorkOrderActivity.this, "上传异常", Toast.LENGTH_SHORT).show();
			return;
		}
		if (workItemInfo.getTaskname() == null || workItemInfo.getTaskname().trim().equals("")) {
			Toast.makeText(RWorkOrderActivity.this, "请输入工程名称", Toast.LENGTH_SHORT).show();
			return;
		}
		if (!HttpUtil.checkNet(RWorkOrderActivity.this)) {
			Toast.makeText(RWorkOrderActivity.this, "网络异常，请检查网络", Toast.LENGTH_SHORT).show();
			return;
		}
		final DialogCirleProgress dCirleProgress = new DialogCirleProgress(RWorkOrderActivity.this);
		OkHttpUtils.post().url(Config.Apiurl + "?action=addtask")
		.addParams("ticketnum", workItemInfo.getTicketnum())
		.addParams("taskname", workItemInfo.getTaskname())
		.addParams("userid", workItemInfo.getUserid())
		.addParams("worktype", workItemInfo.getWorktype())
		.addParams("volclass", workItemInfo.getVolclass())
		.addParams("remark", workItemInfo.getRemark())
		.addParams("peiheren", workItemInfo.getPeiheren())
		.addParams("usermac", getMacAddress())
		.build()
		.execute(new Callback<String>() {

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
						Toast.makeText(RWorkOrderActivity.this, arg1.getMessage(), Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(String arg0, int arg1) {
						// TODO Auto-generated method stub
						dCirleProgress.hideProcessDialog();
						workItemInfo.setAlreadyUpload(1);
						dbManager.updateWorkInfo(workItemInfo);
						handler.sendEmptyMessage(7);
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
	private void getWorkStep(String workType, String dyLevel) {
		final DialogCirleProgress dCirleProgress = new DialogCirleProgress(RWorkOrderActivity.this);
		GetBuilder getBuilder = OkHttpUtils.get().url(Config.Apiurl).addParams("action", "querykeypoint");
		if (workType != null && "".equals(workType.trim())) {
			getBuilder.addParams("worktype", workType);
		}
		if (dyLevel != null && "".equals(dyLevel.trim())) {
			getBuilder.addParams("volclass", dyLevel);
		}
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

	public void photo() {
		Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		String dirPath = "";
		try {
			dirPath = FileUtils.createSDDir("temp").getAbsolutePath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(RWorkOrderActivity.this, "拍摄失败", Toast.LENGTH_SHORT).show();
			return;
		}
		fileName = dirPath + "/" + String.valueOf(System.currentTimeMillis()) + ".jpg";
		openCameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(fileName)));
		startActivityForResult(openCameraIntent, Pic_Request_Code);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 2:
			saveImage();
			break;
		case 3:
			int fileCount = data.getIntExtra("fileCount", 0);
			boolean taskExists = data.getBooleanExtra("taskExists", false);
			if (fileCount > 0 && !taskExists) {
				initWorkData();
				if (workItemInfo != null) {
					workItemInfo.setRecordcount(fileCount);
				}
				dbManager.AddWorkItem(workItemInfo);
				handler.sendEmptyMessage(6);
			}
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (dbManager != null) {
			dbManager.closeDB();
		}
	}

	// 保存图片
	private void saveImage() {
		if (fileName == null || "".equals(fileName.trim())) {
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String desPath = "";
				try {
					desPath = FileUtils.createSDDir("image/").getAbsolutePath() + "/"
							+ String.valueOf(System.currentTimeMillis()) + "_" + preferencesService.getUserCode()
							+ ".jpg";
					ImageUtils.compressPicture(fileName, desPath);
					uploadFile(desPath, "图片");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void showSelfDialog(final int viewId) {

		AlertDialog.Builder builder = new AlertDialog.Builder(RWorkOrderActivity.this);
		builder.setTitle("检测到该任务名称已存在，继续使用该名称将修改原数据信息？");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//workItemInfo = localWorkItem;
				taskNum=localWorkItem.getTicketnum();
				etWorkNum.setText(taskNum);
				initWorkData();
				initControlData();
				hasWordExists = false;
				handlerClick.sendEmptyMessage(viewId);
			}
		});

		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

			}
		});
		builder.show();
	}

	// 查看taskName是否已存在
	public void checkTaskName(final int viewId) {
		// dbManager.removeAlreadyData();
		String userId = preferencesService.getLoginInfo().get("use_id").toString();
		String taskName = etworkname.getText().toString();
		taskNum = etWorkNum.getText().toString();
		if (StringHelper.isEmpty(taskName)) {
			Toast.makeText(RWorkOrderActivity.this, "请输入工程名称", Toast.LENGTH_SHORT).show();
			return;
		}
		localWorkItem = dbManager.getWorkItemByTaskName(userId, taskName, taskNum);
		if (localWorkItem != null) {
			if(!StringHelper.isEmpty(localWorkItem.getTaskname())&&localWorkItem.getTicketnum().equalsIgnoreCase(taskNum)){	
				handlerClick.sendEmptyMessage(viewId);
				return;
			}
			else{
				hasWordExists=true;
				showSelfDialog(viewId);
				return;
			}
			
		}
		if (!HttpUtil.checkNet(RWorkOrderActivity.this)) {
			handlerClick.sendEmptyMessage(viewId);
			return;
		}
		isChecking=true;
		final DialogCirleProgress dCirleProgress = new DialogCirleProgress(RWorkOrderActivity.this);
		OkHttpUtils.get().url(Config.Apiurl)
		.addParams("action", "queryusertask")
		.addParams("taskname", taskName)
		.addParams("userid", userId).build().execute(new Callback<List<RworkItemRes>>() {

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
						isChecking=false;
						return;
					}

					@Override
					public void onResponse(List<RworkItemRes> arg0, int arg1) {
						// TODO Auto-generated method stub
						dCirleProgress.hideProcessDialog();
						isChecking=false;
						if (arg0 == null || arg0.size() == 0||arg0.get(0).getTicketnum().equalsIgnoreCase(taskNum)) {
							hasWordExists = false;
							handlerClick.sendEmptyMessage(viewId);
							if(arg0.size()>0)
								localWorkItem = arg0.get(0);
							return;
						}
						hasWordExists=true;
						dbManager.AddWorkItems(arg0);
						localWorkItem = arg0.get(0);
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
	
		public void updateTaskInfo() {
			// dbManager.removeAlreadyData();
			String userId = preferencesService.getLoginInfo().get("use_id").toString();
			String taskName = etworkname.getText().toString();
			if (StringHelper.isEmpty(taskName)) {
				return;
			}
			OkHttpUtils.get().url(Config.Apiurl)
			.addParams("action", "queryusertask")
			.addParams("taskname", taskName)
			.addParams("userid", userId).build().execute(new Callback<List<RworkItemRes>>() {

						@Override
						public void onBefore(Request request, int id) {
							// TODO Auto-generated method stub
							super.onBefore(request, id);
							Log.i("loginRequest", request.url().toString());
							
						}

						@Override
						public void onError(Call arg0, Exception arg1, int arg2) {
							// TODO Auto-generated method stub
							return;
						}

						@Override
						public void onResponse(List<RworkItemRes> arg0, int arg1) {
							// TODO Auto-generated method stub
							if(arg0!=null&&arg0.size()>0)
							   dbManager.updateWorkInfoByTaskNum(arg0.get(0));
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

	private void uploadFile(String fileName, String fileType) {
		if (workItemInfo == null) {
			initWorkData();
			dbManager.AddWorkItem(workItemInfo);
		}
		File file = new File(fileName);
		if (!file.exists()) {
			Toast.makeText(RWorkOrderActivity.this, "文件不存在，上传失败！", Toast.LENGTH_SHORT).show();
			return;
		}
		BDLocation bdLocation = application.currLocation;
		if (bdLocation == null) {
			Toast.makeText(RWorkOrderActivity.this, "定位失败", Toast.LENGTH_SHORT).show();
			return;
		}
		if (bdLocation.getLocType() == BDLocation.TypeServerError) {
			Toast.makeText(RWorkOrderActivity.this, "服务端网络定位失败", Toast.LENGTH_SHORT).show();
			return;
		} else if (bdLocation.getLocType() == BDLocation.TypeNetWorkException) {
			Toast.makeText(RWorkOrderActivity.this, "网络不同导致定位失败，请检查网络是否通畅", Toast.LENGTH_SHORT).show();
			return;
		} else if (bdLocation.getLocType() == BDLocation.TypeCriteriaException) {
			Toast.makeText(RWorkOrderActivity.this, "无法获取有效定位依据导致定位失败", Toast.LENGTH_SHORT).show();
			return;
		}
		String str_step = "";
		if (spiner_step != null) {
			RWorkStepInfo stepInfo = (RWorkStepInfo) spiner_step.getSelectedItem();
			if (stepInfo != null) {
				str_step = stepInfo.getGjdmc();
			}
		}
		mediaInfo = new RWorkMediaInfo();
		mediaInfo.setAlreadyUpload(0);
		mediaInfo.setCreatetime(simFormat.format(new Date()));
		mediaInfo.setGjdmc(str_step);
		mediaInfo.setGpsaddress(bdLocation.getAddrStr());
		mediaInfo.setGpstime(bdLocation.getTime());
		mediaInfo.setId(UUID.randomUUID().toString());
		mediaInfo.setLat(bdLocation.getLatitude());
		mediaInfo.setLon(bdLocation.getLongitude());
		mediaInfo.setUserid(preferencesService.getLoginInfo().get("use_id").toString());
		mediaInfo.setTicketnum(etWorkNum.getText().toString());
		mediaInfo.setPicpath(fileName);
		mediaInfo.setPickind(fileType);
		mediaInfo.setLocalFilePath(fileName);
		mediaInfo.setUsermac(getMacAddress());
		dbManager.addMediaInfo(mediaInfo);
		OkHttpUtils.post().url(Config.Apiurl + "?action=addscene")
				.addParams("ticketnum", etWorkNum.getText().toString())
				.addParams("gpstime", bdLocation.getTime())
				.addParams("lon", String.valueOf(bdLocation.getLongitude()))
				.addParams("lat", String.valueOf(bdLocation.getLatitude()))
				.addParams("userid", mediaInfo.getUserid())
				.addParams("gjdmc", str_step)
				.addParams("gpsaddress", bdLocation.getAddrStr())
				.addParams("id", mediaInfo.getId())
				.addParams("usermac", getMacAddress())
				.addFile("mediafile", file.getName(), file).build()
				.execute(new Callback<String>() {

					@Override
					public void onBefore(Request request, int id) {
						// TODO Auto-generated method stub
						super.onBefore(request, id);
						Log.i("loginRequest", request.url().toString());
						// dCirleProgress.showProcessDialog();
					}

					@Override
					public void onError(Call arg0, Exception arg1, int arg2) {
						// TODO Auto-generated method stub
						// dCirleProgress.hideProcessDialog();
						// Toast.makeText(RWorkOrderActivity.this, "上传失败," +
						// arg1.getMessage(), Toast.LENGTH_SHORT).show();
						workItemInfo.setRecordcount(workItemInfo.getRecordcount() + 1);
						dbManager.updateWorkInfo(workItemInfo);
						handler.sendEmptyMessage(6);
					}

					@Override
					public void onResponse(String arg0, int arg1) {
						// TODO Auto-generated method stub
						// dCirleProgress.hideProcessDialog();
						mediaInfo.setAlreadyUpload(1);
						workItemInfo.setRecordcount(workItemInfo.getRecordcount() + 1);
						try{
							dbManager.updateMediaInfo(mediaInfo);
							dbManager.updateWorkInfo(workItemInfo);
							handler.sendEmptyMessage(6);
						}
						catch (Exception e) {
							// TODO: handle exception
							Log.e("更新數據錯誤", e.getMessage());
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

	private void AudioRecord() {
		if (!isRecording) {
			if (this.txtVedio.getText().toString().trim().equals("开始录音")) {
				this.btnVideo.setImageResource(R.drawable.popumenu_audio_1);
				this.chronometers.setText("00:00");
				this.txtVedio.setText("结束录音");
				this.chronometers.setBase(SystemClock.elapsedRealtime());
				this.chronometers.start();
				audioRecord();
			}
		} else {
			this.btnVideo.setImageResource(R.anim.popumenu_audio);
			this.txtVedio.setText("开始录音");
			stopRecord();
			this.chronometers.stop();
			Toast.makeText(this, "录音停止", Toast.LENGTH_SHORT).show();
		}

	}

	private void uploadAudio(final String filePath) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				uploadFile(filePath, "录音");
			}
		}).start();
		;
	}

	// 录音
	public void audioRecord() {
		String fileName = UUID.randomUUID().toString() + ".mp3";
		try {
			AudioPath = FileUtils.createSDDir("audio/").getAbsolutePath() + "/" + fileName;
		} catch (IOException e) {
			// TODO: handle exception
			Toast.makeText(RWorkOrderActivity.this, "录音失败:文件创建失败", Toast.LENGTH_SHORT).show();
			return;
		}

		this.mediarecordera = new MediaRecorder();
		this.mediarecordera.setAudioSource(1);
		this.mediarecordera.setOutputFormat(2);
		this.mediarecordera.setAudioEncoder(0);
		this.mediarecordera.setMaxDuration(600000);
		this.mediarecordera.setOutputFile(AudioPath);
		try {
			this.mediarecordera.prepare();
			this.mediarecordera.start();
			this.isRecording = true;
			return;
		} catch (IllegalStateException localIllegalStateException) {
			Log.e("181", localIllegalStateException.getMessage() + "  录音错误...");
			return;
		} catch (IOException localIOException) {
			Log.e("180", localIOException.getMessage() + "  录音错误...");
		}
	}

	public void stopRecord() {
		try {
			isRecording = false;
			if (this.mediarecordera != null) {
				this.mediarecordera.stop();
			}
			uploadAudio(AudioPath);
			return;
		} catch (Exception localException) {
			Log.e("180", "录音失败" + localException.getMessage());
			return;
		} finally {
			if (this.mediarecordera != null) {
				this.mediarecordera.release();
				this.mediarecordera = null;
			}
		}
	}

	public class OnChronometerTickListenerImpl implements Chronometer.OnChronometerTickListener {
		public OnChronometerTickListenerImpl() {
		}

		public void onChronometerTick(Chronometer paramChronometer) {
			if (chronometers.getText().toString().equals("10:00")) {
				chronometers.stop();
				txtVedio.setText("开始录音");
				btnVideo.setImageResource(R.anim.popumenu_audio);
				stopRecord();
			}
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (taskNum != null && !"".equals(taskNum)) {
			workItemInfo = dbManager.getWorkItemBytaskNum(taskNum);
			if (workItemInfo != null)
				tv_btnImgListLook.setText("影音管理(" + workItemInfo.getRecordcount() + ")");
		}
	}

}
