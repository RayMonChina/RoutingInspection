package com.ideal.zsyy.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.shenrenkeji.intelcheck.R;
import com.ideal.zsyy.db.WdbManager;
import com.ideal.zsyy.entity.FaultItem;
import com.ideal.zsyy.entity.WBBItem;
import com.ideal.zsyy.entity.WCBUserEntity;
import com.ideal.zsyy.entity.WCBWaterChargeItem;
import com.ideal.zsyy.entity.WUploadItem;
import com.ideal.zsyy.request.WAdviceReq;
import com.ideal.zsyy.request.WFaultReq;
import com.ideal.zsyy.request.WUploadUserReq;
import com.ideal.zsyy.request.WaterChargeReq;
import com.ideal.zsyy.response.WAdviceRes;
import com.ideal.zsyy.response.WFaultRes;
import com.ideal.zsyy.response.WUploadUserRes;
import com.ideal.zsyy.response.WaterChageRes;
import com.ideal.zsyy.service.PreferencesService;
import com.ideal.zsyy.utils.ImageItem;
import com.ideal2.base.gson.GsonServlet;
import com.ideal2.base.gson.GsonServlet.OnResponseEndListening;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class WUploadDataActivity extends Activity {

	private Button btn_back=null,btn_upload=null;
	private Spinner spinnerBBH = null;
	private TextView tv_cby = null, tv_cbyf = null, tv_yhzs = null,
			tv_ycyhs = null, tv_wcyhs = null,tv_gzbx = null,tv_wsc=null,
			tv_khjy = null,tv_zpsl = null;
	private WdbManager dbManage=null;
	private Map<String, Object>userInfo;
	private Map<String, Integer>userDateInfo;
	//private String passNoteNO="";
	private int picNum=0;
	private List<WCBUserEntity>userList=new ArrayList<WCBUserEntity>();
	private List<WCBWaterChargeItem>chargeItems=new ArrayList<WCBWaterChargeItem>();
	private List<FaultItem>faultItems=new ArrayList<FaultItem>();
	private int uploadUserIndex=0;
	private int chargeIndex=0;
	private int picIndex=0;
	private String NoteNo="0";
	private PreferencesService preferencesService;
	private float defaultsize=18f;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.w_upload_data);
		dbManage=new WdbManager(WUploadDataActivity.this);
		PreferencesService pService=new PreferencesService(WUploadDataActivity.this);
		userInfo=pService.getLoginInfo();
		userDateInfo=pService.GetCBDateInfo();
		initView();
		SetDefaultFontSize();
		setEventListener();
	}
	private void initView()
	{
		btn_back=(Button)findViewById(R.id.btn_back);
		btn_upload=(Button)findViewById(R.id.btn_upload);
		spinnerBBH = (Spinner) findViewById(R.id.sp_bbh);
		tv_cby = (TextView) findViewById(R.id.tv_cby);
		tv_cbyf = (TextView) findViewById(R.id.tv_cbyf);
		tv_yhzs = (TextView) findViewById(R.id.tv_yhzs);
		tv_ycyhs = (TextView) findViewById(R.id.tv_ycyhs);
		tv_wcyhs = (TextView) findViewById(R.id.tv_wcyhs);
		tv_gzbx = (TextView) findViewById(R.id.tv_gzbx);
		tv_khjy = (TextView) findViewById(R.id.tv_khjy);
		tv_zpsl = (TextView) findViewById(R.id.tv_zpsl);
		tv_wsc= (TextView) findViewById(R.id.tv_wsc);
		
		
	}
	private void SetDefaultFontSize() {
		LinearLayout loginLayout = (LinearLayout) findViewById(R.id.ly_showpannl);
		SetFontseize(loginLayout);
		//defaultsize = preferencesService.GetZoomSize();
	}
	private void SetFontseize(ViewGroup viewGroup) {

		int count = viewGroup.getChildCount();
		for (int i = 0; i < count; i++) {
			View view = viewGroup.getChildAt(i);
			if (view instanceof TextView) {
				TextView newDtv = (TextView) view;
				newDtv.setTextSize(defaultsize);
			} else if (view instanceof ViewGroup) {
				this.SetFontseize((ViewGroup) view);
			}
		}
	}
	
	private void setEventListener()
	{
		if(btn_back!=null)
		{
			btn_back.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					finish();
				}
			});
		}
		if(btn_upload!=null)
		{
			btn_upload.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					//根据表本上传数据
					
					userList=dbManage.GetUserUpload(NoteNo);
					
					//chargeItems=dbManage.getUploadChargeItems();
					faultItems=dbManage.GetUploadFaultItems();
					UploadUserInfo();
					//UploadChargeData();
					UploadPicData();
					//UploadFault();
					UploadAdvice();
				}
			});
		}
		
		if (spinnerBBH != null) {
			List<WBBItem>bbList=dbManage.GetBiaoBenInfo();
			WBBItem bbItemAll=new WBBItem();
			bbItemAll.setBId(0);
			bbItemAll.setNoteNo("全部");
			if(bbList!=null)
			{
				bbList.add(0, bbItemAll);
			}
			ArrayAdapter<WBBItem> adapter = new ArrayAdapter<WBBItem>(this,
					android.R.layout.simple_spinner_item, bbList);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinnerBBH.setAdapter(adapter);
			spinnerBBH.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int position, long id) {
					// TODO Auto-generated method stub
					WBBItem bbItem = (WBBItem)arg0.getItemAtPosition(position);
					NoteNo=bbItem.getBId()==0?"0":bbItem.getNoteNo();
					WUploadItem wDataItem = dbManage.GetUploadInfo(NoteNo);
					if (wDataItem != null) {
						picNum=wDataItem.getZpsl();
						if(userInfo!=null)
						{
							if(userInfo.containsKey("userName"))
							{
								wDataItem.setCby(userInfo.get("userName").toString());
							}
						}
						if(userDateInfo!=null)
						{
							if(userDateInfo.containsKey("cMonth"))
							{
								wDataItem.setCbMonth(String.valueOf(userDateInfo.get("cMonth")));
							}
						}
						if (tv_cby != null) {
							tv_cby.setText(wDataItem.getCby());
						}
						if (tv_cbyf != null) {
							tv_cbyf.setText(bbItem.getCBMonth()+"月");
						}
						if (tv_yhzs != null) {
							tv_yhzs.setText(String.valueOf(wDataItem.getYhzs()));
						}
						if (tv_ycyhs != null) {
							tv_ycyhs.setText(String.valueOf(wDataItem.getYcyhs()));
						}
						if (tv_wcyhs != null) {
							tv_wcyhs.setText(String.valueOf(wDataItem.getWcyhs()));
						}
						if (tv_gzbx != null) {
							tv_gzbx.setText(String.valueOf(wDataItem.getGzbx()));
						}
						if (tv_khjy != null) {
							tv_khjy.setText(String.valueOf(wDataItem.getKhjy()));
						}
						if (tv_zpsl != null) {
							tv_zpsl.setText(String.valueOf(wDataItem.getZpsl()));
						}
						if (tv_wsc!=null) {
							tv_wsc.setText(String.valueOf(wDataItem.getWsc()));	
						}
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub

				}
			});
		}
		
		
	}
	
	//上传用户信息
	private void UploadUserInfo() {
		if(userList==null&&userList.size()==0)
		{
			return;
		}
		if(userList.size()<=uploadUserIndex)
		{
			Toast.makeText(WUploadDataActivity.this,"抄表数据上传成功",Toast.LENGTH_SHORT).show();
			tv_wsc.setText("0");
			return;
		}
		WUploadUserReq req = new WUploadUserReq();
		req.setOperType("5");
		WCBUserEntity userEntity=userList.get(uploadUserIndex);
		req.setAvePrice(userEntity.getAvePrice());
		req.setChargeID(userEntity.getChargeID());
		req.setChargeState(String.valueOf(userEntity.getChaoBiaoTag()));
		req.setCheckDateTime(userEntity.getCheckDateTime());
		req.setChecker(userEntity.getChecker());
		req.setCheckState(userEntity.getCheckState());
		req.setExtraCharge1(userEntity.getExtraCharge1());
		req.setExtraCharge2(userEntity.getExtraCharge2());
		req.setExtraChargePrice1(userEntity.getExtraChargePrice1());
		req.setExtraChargePrice2(userEntity.getExtraChargePrice2());
		req.setExtraTotalCharge(userEntity.getExtraTotalCharge());
		req.setOVERDUEMONEY(userEntity.getOVERDUEMONEY());
		req.setReadMeterRecordDate(userEntity.getChaoBiaoDate());
		req.setReadMeterRecordId(userEntity.getReadMeterRecordId());
		req.setTotalCharge(userEntity.getTotalCharge());
		req.setTotalNumber((int)userEntity.getCurrMonthWNum());
		req.setWaterMeterEndNumber((int)userEntity.getCurrentMonthValue());
		req.setWaterTotalCharge(userEntity.getCurrMonthFee());
		req.setLatitude(String.valueOf(userEntity.getLatitude()));
		req.setLongitude(String.valueOf(userEntity.getLongitude()));
		req.setPhone(userEntity.getPhone());
		req.setMemo1(userEntity.getMemo1());
		GsonServlet<WUploadUserReq, WUploadUserRes> gServlet = new GsonServlet<WUploadUserReq, WUploadUserRes>(
				this);
		gServlet.request(req, WUploadUserRes.class);
		gServlet.setOnResponseEndListening(new OnResponseEndListening<WUploadUserReq, WUploadUserRes>() {

			@Override
			public void onResponseEnd(WUploadUserReq commonReq, WUploadUserRes commonRes,
					boolean result, String errmsg, int responseCode) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onResponseEndSuccess(WUploadUserReq commonReq,
					WUploadUserRes commonRes, String errmsg, int responseCode) {
				// TODO Auto-generated method stub
				if(commonRes!=null)
				{
					dbManage.UpdateUserUploadTag(userList.get(uploadUserIndex).getUserID());
					uploadUserIndex++;
					UploadUserInfo();
				}

			}

			@Override
			public void onResponseEndErr(WUploadUserReq commonReq,
					WUploadUserRes commonRes, String errmsg, int responseCode) {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), errmsg,
						Toast.LENGTH_SHORT).show();
			}

		});
	}
	
	private void UploadAdvice()
	{
		WAdviceReq req = new WAdviceReq();
		req.setOperType("7");
		req.setAdviceItems(dbManage.GetAdviceInfos());
		if(req.getAdviceItems()==null||req.getAdviceItems().size()==0)
		{
			return;
		}
		GsonServlet<WAdviceReq, WAdviceRes> gServlet = new GsonServlet<WAdviceReq, WAdviceRes>(
				this);
		gServlet.request(req, WAdviceRes.class);
		gServlet.setOnResponseEndListening(new OnResponseEndListening<WAdviceReq, WAdviceRes>() {

			@Override
			public void onResponseEnd(WAdviceReq commonReq, WAdviceRes commonRes,
					boolean result, String errmsg, int responseCode) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onResponseEndSuccess(WAdviceReq commonReq,
					WAdviceRes commonRes, String errmsg, int responseCode) {
				// TODO Auto-generated method stub
				if(commonRes!=null)
				{
					dbManage.UpdateAdviceTag();
					handler.sendEmptyMessage(2);
					Toast.makeText(WUploadDataActivity.this,"用户建议上传成功!",Toast.LENGTH_SHORT).show();
				}

			}

			@Override
			public void onResponseEndErr(WAdviceReq commonReq,
					WAdviceRes commonRes, String errmsg, int responseCode) {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), errmsg,
						Toast.LENGTH_SHORT).show();
			}

		});
	}
	
	//上传报修故障
	private void UploadPicData() {
	
		if(faultItems==null||faultItems.size()==0)
		{
			return;
		}
		if(picIndex>=faultItems.size())
		{
			Toast.makeText(WUploadDataActivity.this,"故障报修数据上传成功!",Toast.LENGTH_SHORT).show();
			return;
		}
		FaultItem item=faultItems.get(picIndex);
		ImageItem imageItem=new ImageItem();
		imageItem.setImagePath(item.getPicPath());
		WFaultReq req = new WFaultReq();
		req.setOperType("6");
		req.setCreateDateTime(item.getAddDate());
		req.setDescribe(item.getContent());
		req.setLoginId(item.getAddUserId());
		req.setWaterUserId(item.getUserNo());
		req.setImgData(imageItem.imgToBase64());
		GsonServlet<WFaultReq, WFaultRes> gServlet = new GsonServlet<WFaultReq, WFaultRes>(
				this);
		gServlet.request(req, WFaultRes.class);
		gServlet.setOnResponseEndListening(new OnResponseEndListening<WFaultReq, WFaultRes>() {

			@Override
			public void onResponseEnd(WFaultReq commonReq, WFaultRes commonRes,
					boolean result, String errmsg, int responseCode) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onResponseEndSuccess(WFaultReq commonReq,
					WFaultRes commonRes, String errmsg, int responseCode) {
				// TODO Auto-generated method stub
				if (commonRes != null) {
					dbManage.UpdateFaultReport(commonReq.getWaterUserId());
					picIndex++;
					handler.sendEmptyMessage(3);
					UploadPicData();
					
				}

			}

			@Override
			public void onResponseEndErr(WFaultReq commonReq, WFaultRes commonRes,
					String errmsg, int responseCode) {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), errmsg,
						Toast.LENGTH_SHORT).show();
			}

		});
	}
	
	//上传收费信息
	private void UploadChargeData()
	{
		if(chargeItems==null||chargeItems.size()==0)
		{
			return;
		}
		if(chargeIndex>=chargeItems.size())
		{
			Toast.makeText(WUploadDataActivity.this,"收费数据上传成功！",Toast.LENGTH_SHORT).show();
			return;
		}
		WaterChargeReq req=new WaterChargeReq();
		req.setOperType("11");
		req.setChageItem(chargeItems.get(chargeIndex));
		GsonServlet<WaterChargeReq, WaterChageRes> gServlet = new GsonServlet<WaterChargeReq, WaterChageRes>(
				this);
		gServlet.request(req, WaterChageRes.class);
		gServlet.setOnResponseEndListening(new OnResponseEndListening<WaterChargeReq, WaterChageRes>() {

			@Override
			public void onResponseEnd(WaterChargeReq commonReq, WaterChageRes commonRes,
					boolean result, String errmsg, int responseCode) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onResponseEndSuccess(WaterChargeReq commonReq,
					WaterChageRes commonRes, String errmsg, int responseCode) {
				// TODO Auto-generated method stub
				if (commonRes != null) {
					WCBWaterChargeItem cItem=chargeItems.get(chargeIndex);
					dbManage.UpdateChargeTag(cItem.getCHARGEID());
					chargeIndex++;
					UploadChargeData();
				}

			}

			@Override
			public void onResponseEndErr(WaterChargeReq commonReq, WaterChageRes commonRes,
					String errmsg, int responseCode) {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), errmsg,
						Toast.LENGTH_SHORT).show();
			}

		});
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		//return super.onKeyDown(keyCode, event);
		if(keyCode==KeyEvent.KEYCODE_BACK)
		{
			finish();
		}
		return false;
	}
	
	private Handler handler=new Handler()
	{

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what)
			{
				case 1:
					if(tv_gzbx!=null)
					{
						tv_gzbx.setText("0");
					}
					break;
				case 2:
					if(tv_khjy!=null)
					{
						tv_khjy.setText("0");
					}
					break;
				case 3:
					picNum--;
					if(picNum<0)
					{
						picNum=0;
					}
					tv_zpsl.setText(String.valueOf(picNum));
					break;
				default:
					break;
			}
		}
		
	};
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(dbManage!=null)
		{
			dbManage.closeDB();
		}
	}

	
}
