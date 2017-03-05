package com.ideal.zsyy.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.ideal.zsyy.db.WdbManager;
import com.ideal.zsyy.entity.ActionItem;
import com.ideal.zsyy.entity.LocationInfo;
import com.ideal.zsyy.entity.WCBUserEntity;
import com.ideal.zsyy.entity.WCBWaterChargeItem;
import com.ideal.zsyy.request.WUploadUserReq;
import com.ideal.zsyy.request.WaterChargeReq;
import com.ideal.zsyy.response.WUploadUserRes;
import com.ideal.zsyy.response.WaterChageRes;
import com.ideal.zsyy.service.BluetoothPrintService;
import com.ideal.zsyy.service.BluetoothService;
import com.ideal.zsyy.service.BluetoothService.bondFinishListener;
import com.ideal.zsyy.service.PreferencesService;
import com.ideal.zsyy.utils.PublicWay;
import com.ideal.zsyy.utils.Utility;
import com.ideal.zsyy.utils.WaterUtils;
import com.ideal.zsyy.view.TitlePopup;
import com.ideal.zsyy.view.TitlePopup.OnItemOnClickListener;
import com.ideal2.base.gson.GsonServlet;
import com.ideal2.base.gson.GsonServlet.OnResponseEndListening;
import com.shenrenkeji.intelcheck.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WCBRealNewActivity extends Activity {

	public static final int TAKE_PICTURE = 1;

	private Button btn_back = null;
	private TextView tv_w_userno, tv_w_bbh, tv_w_username, tv_w_dh, tv_w_dz, tv_w_sybs, tv_w_price_type, tv_w_steal_no,
			tv_w_cb_tag, tv_w_gps, tv_confirm, tv_cancel, tv_w_benqishuiliang, tv_w_benqishuifei, tv_w_lastchaobiadate,
			tv_update_gps, tv_w_avprice, tv_w_charge, tv_w_agv_wushuiprice, tv_w_agv_wushui_charge,
			tv_w_agv_fujia_price, tv_w_agv_fujia_charge, tv_title, tv_confirm_phone, tv_cancel_phone, tv_w_ponsition,
			tv_w_cb_Memo, tv_save_memo1, tv_cannle_memo1;
	private EditText edit_currvalue, edit_phone, edit_memo1;
	private ImageView img_editmonthvalue, img_editphone;
	private TextView tv_monthvalue, tv_btn_next, tv_btn_pre;
	private LinearLayout ll_top_menu, ly_currentvalue, ly_edit_phone, ly_memo_onoff, ly_Memo1,ly_currvalue,ly_currvalue_swith;
	private String stealNo = "", loginId = "", userName = "";
	private WdbManager dbmanager = null;
	private WCBUserEntity userItem = null;
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private boolean hasUnFenbiao;
	private PreferencesService preferencesService;
	private int op;// 1、顺序抄表
	private int chaobiaoTag = -1;
	private TitlePopup titlePopup;
	private BluetoothService bService;
	private BluetoothPrintService printService;
	private String[] printData;
	private float defaultsize;
	WdbManager dbHelper;
	// -----------------------------------------------
	private final int NONE = 0;// 空
	private final int DRAG = 1;// 按下第一个点
	private final int ZOOM = 2;// 按下第二个点
	private InputMethodManager mInputMethodManager;

	/** 屏幕上点的数量 */
	private int mode = NONE;

	/** 记录按下第二个点距第一个点的距离 */
	float oldDist;

	/** 最小字体 */
	public final float MIN_TEXT_SIZE = 16f;

	/** 最大子图 */
	public final float MAX_TEXT_SIZE = 28f;

	/** 缩放比例 */
	float scale = 4f;

	/** 设置字体大小 */
	float textSize = 16f;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.w_cb_real_new);
		preferencesService = new PreferencesService(WCBRealNewActivity.this);
		dbmanager = new WdbManager(getApplicationContext());
		stealNo = getIntent().getStringExtra("StealNo");
		loginId = preferencesService.getLoginInfo().get("use_id").toString();
		userName = preferencesService.getLoginInfo().get("userName").toString();
		op = getIntent().getIntExtra("op", 0);
		chaobiaoTag = getIntent().getIntExtra("ChaoBiaoTag", -1);
		bService = new BluetoothService(this);
		titlePopup = new TitlePopup(WCBRealNewActivity.this);
		titlePopup.addAction(new ActionItem(getResources().getDrawable(R.drawable.w_fault_report), "故障报修", 1));
		// titlePopup.addAction(new
		// ActionItem(getResources().getDrawable(R.drawable.wcb_print), "打印小票",
		// 2));
		titlePopup.addAction(new ActionItem(getResources().getDrawable(R.drawable.wc_fault), "用户建议", 3));
		titlePopup.addAction(new ActionItem(getResources().getDrawable(R.drawable.wcb_check_history), "历史查询", 4));
		titlePopup.addAction(new ActionItem(getResources().getDrawable(R.drawable.wcb_single_upload), "数据上传", 5));
		// titlePopup.addAction(new
		// ActionItem(getResources().getDrawable(R.drawable.wcb_print), "打印催费",
		// 6));
		initView();
		this.InitData();
		if (this.userItem != null && this.userItem.getWaterUserchargeType() != null
				&& this.userItem.getWaterUserchargeType().trim().equals("0")) {
			if (!this.userItem.getNoteNo().startsWith("a") && !this.userItem.getNoteNo().startsWith("A")) {
				titlePopup.addAction(new ActionItem(getResources().getDrawable(R.drawable.wcb_print), "打印小票", 2));
			}
		}

		defaultsize = preferencesService.GetZoomSize();
		SetDefaultFontSize();
		LinearLayout rl = (LinearLayout) findViewById(R.id.ly_maintop);
		rl.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				// case MotionEvent.ACTION_DOWN:
				// break;
				// case MotionEvent.ACTION_UP:
				// case MotionEvent.ACTION_POINTER_UP:
				// break;
				case MotionEvent.ACTION_POINTER_DOWN:
					zoomOut(2f);
					break;
				// case MotionEvent.ACTION_MOVE:
				// break;
				}
				return true;
			}
		});
	}
	/**
	 * 求出2个触点间的 距离
	 * 
	 * @param event
	 * @return
	 */
	// private float spacing(MotionEvent event)
	// {
	// float x = event.getX(0) - event.getX(1);
	// float y = event.getY(0) - event.getY(1);
	// return FloatMath.sqrt(x * x + y * y);
	// }

	/**
	 * 放大
	 */
	protected void zoomOut(float scales) {
		textSize += scale;
		if (textSize > MAX_TEXT_SIZE) {
			textSize = MIN_TEXT_SIZE;
		}
		LinearLayout loginLayout = (LinearLayout) findViewById(R.id.ly_maintop);
		SetFontseizeOut(loginLayout, textSize);
	}

	private void SetFontseizeOut(ViewGroup viewGroup, float textSize) {

		int count = viewGroup.getChildCount();
		for (int i = 0; i < count; i++) {
			View view = viewGroup.getChildAt(i);
			if (view instanceof TextView) { // 若是TextView记录下
				TextView newDtv = (TextView) view;
				newDtv.setTextSize(textSize);
			} else if (view instanceof ViewGroup) {
				// 若是布局控件（LinearLayout或RelativeLayout）,继续查询子View
				this.SetFontseizeOut((ViewGroup) view, textSize);
			}
		}
		preferencesService.SaveZoomSize(textSize);
	}

	private void initView() {
		btn_back = (Button) findViewById(R.id.btn_back);
		tv_w_bbh = (TextView) findViewById(R.id.tv_w_bbh);
		tv_w_cb_tag = (TextView) findViewById(R.id.tv_w_cb_tag);
		tv_w_dh = (TextView) findViewById(R.id.tv_w_dh);
		tv_w_dz = (TextView) findViewById(R.id.tv_w_dz);
		tv_w_price_type = (TextView) findViewById(R.id.tv_w_price_type);
		tv_w_steal_no = (TextView) findViewById(R.id.tv_w_steal_no);
		tv_w_sybs = (TextView) findViewById(R.id.tv_w_sybs);
		tv_w_username = (TextView) findViewById(R.id.tv_w_username);
		tv_w_userno = (TextView) findViewById(R.id.tv_w_userno);
		tv_w_ponsition = (TextView) findViewById(R.id.tv_w_ponsition);
		tv_w_gps = (TextView) findViewById(R.id.tv_w_gps);
		tv_confirm = (TextView) findViewById(R.id.tv_confirm);
		tv_cancel = (TextView) findViewById(R.id.tv_cancel);
		tv_w_benqishuiliang = (TextView) findViewById(R.id.tv_w_benqishuiliang);
		tv_w_benqishuifei = (TextView) findViewById(R.id.tv_w_benqishuifei);
		tv_w_lastchaobiadate = (TextView) findViewById(R.id.tv_w_lastchaobiadate);
		edit_currvalue = (EditText) findViewById(R.id.edit_currvalue);
		edit_phone = (EditText) findViewById(R.id.edit_phone);
		img_editmonthvalue = (ImageView) findViewById(R.id.img_editmonthvalue);
		img_editphone = (ImageView) findViewById(R.id.img_editphone);
		tv_monthvalue = (TextView) findViewById(R.id.tv_w_benqidushu);
		tv_btn_next = (TextView) findViewById(R.id.tv_btn_next);
		tv_btn_pre = (TextView) findViewById(R.id.tv_btn_pre);
		ll_top_menu = (LinearLayout) findViewById(R.id.ll_top_menu);
		tv_update_gps = (TextView) findViewById(R.id.tv_update_gps);
		tv_w_avprice = (TextView) findViewById(R.id.tv_w_avprice);
		tv_w_charge = (TextView) findViewById(R.id.tv_w_charge);
		tv_w_agv_wushuiprice = (TextView) findViewById(R.id.tv_w_agv_wushuiprice);
		tv_w_agv_wushui_charge = (TextView) findViewById(R.id.tv_w_agv_wushui_charge);
		tv_w_agv_fujia_price = (TextView) findViewById(R.id.tv_w_agv_fujia_price);
		tv_w_agv_fujia_charge = (TextView) findViewById(R.id.tv_w_agv_fujia_charge);
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_confirm_phone = (TextView) findViewById(R.id.tv_confirm_phone);
		tv_cancel_phone = (TextView) findViewById(R.id.tv_cancel_phone);
		ly_currentvalue = (LinearLayout) findViewById(R.id.ly_currentvalue);
		ly_edit_phone = (LinearLayout) findViewById(R.id.ly_edit_phone);
		tv_w_cb_Memo = (TextView) findViewById(R.id.tv_w_cb_Memo);
		ly_Memo1 = (LinearLayout) findViewById(R.id.ly_Memo1);
		ly_memo_onoff = (LinearLayout) findViewById(R.id.ly_memo_onoff);
		tv_save_memo1 = (TextView) findViewById(R.id.tv_save_memo1);
		tv_cannle_memo1 = (TextView) findViewById(R.id.tv_cannle_memo1);
		edit_memo1 = (EditText) findViewById(R.id.edit_memo1);
		ly_currvalue = (LinearLayout) findViewById(R.id.ly_currvalue);
		ly_currvalue_swith = (LinearLayout) findViewById(R.id.ly_currvalue_swith);
		
		//备注开关
		if (ly_memo_onoff != null) {
			ly_memo_onoff.setOnClickListener(clickListener);
		}

		if (btn_back != null) {
			btn_back.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					finish();
				}
			});
		}

//		if (img_editmonthvalue != null) {
//			// img_editmonthvalue.setOnClickListener(clickListener);
//		}
//		if (tv_monthvalue != null) {
//			tv_monthvalue.setOnClickListener(clickListener);
//		}
//		if (tv_w_benqishuiliang != null) {
//			tv_w_benqishuiliang.setOnClickListener(clickListener);
//		}
//		if (tv_w_lastchaobiadate != null) {
//			tv_w_lastchaobiadate.setOnClickListener(clickListener);
//		}
		if (ly_currvalue_swith!=null) {
			ly_currvalue_swith.setOnClickListener(clickListener);
		}
//		if (ly_currentvalue != null) {
//			//ly_currentvalue.setOnClickListener(clickListener);
//		}
		if (edit_currvalue != null) {
			edit_currvalue.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					// TODO Auto-generated method stub
					if (!hasFocus) {
						//edit_currvalue.setVisibility(View.GONE);
						mInputMethodManager.hideSoftInputFromWindow(edit_currvalue.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
					}
					// else
					// {
					// mInputMethodManager.hideSoftInputFromWindow(edit_currvalue.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
					// }

				}
			});
		}
		if (tv_confirm != null) {
			tv_confirm.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					int waterValue = 0;
					try {
						String wValue = edit_currvalue.getText().toString();
						if (wValue == null || wValue.trim() == "") {
							Toast.makeText(WCBRealNewActivity.this, "抄表数值不能为空！", Toast.LENGTH_SHORT).show();
							return;
						}
						waterValue = Integer.parseInt(wValue);
						if (waterValue < 0) {
							Toast.makeText(WCBRealNewActivity.this, "抄表数值不能小于0！", Toast.LENGTH_SHORT).show();
							return;
						}
						// 判断等于0，清除抄表标志
						if (waterValue == 0) {
							ShowDiag(1, "是否删除本条抄表数据？");
							return;
						}

						if (waterValue < userItem.getLastMonthValue())// 判断是否反转
						{
							ShowDiag(1, "本月读数少于上月，是否确认？");
							return;
						}
						edit_currvalue.setEnabled(true);
						ChaoBiaoCinfirm(waterValue);

					} catch (Exception e) {
						// TODO: handle exception
						Toast.makeText(WCBRealNewActivity.this, "抄表数值应为整数！", Toast.LENGTH_SHORT).show();
						return;
					}
				}
			});

		}
		if (tv_cancel != null) {
			tv_cancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					edit_currvalue.clearFocus();
					tv_confirm.setVisibility(View.GONE);
					tv_cancel.setVisibility(View.GONE);
					//tv_monthvalue.setVisibility(View.VISIBLE);
					edit_currvalue.setVisibility(View.GONE);
					//ly_currentvalue.setVisibility(View.VISIBLE);
				}
			});
		}
		//备注保存
		if (tv_save_memo1!=null) {
			tv_save_memo1.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					String MemoText = edit_memo1.getText().toString();
					
					if (userItem.getMemo1() != MemoText) {
						userItem.setMemo1(MemoText);
						userItem.setAlreadyUpload(1);
						dbmanager.UpdateUserItem(userItem);
					}
					tv_w_cb_Memo.setText(MemoText);
					edit_memo1.setVisibility(View.GONE);
					tv_save_memo1.setVisibility(View.GONE);
					tv_cannle_memo1.setVisibility(View.GONE);
					//mInputMethodManager.hideSoftInputFromWindow(edit_memo1.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
				
					
				}
			});
		}
		//备注取消
		if (tv_cannle_memo1!=null) {
			tv_cannle_memo1.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					edit_memo1.setVisibility(View.GONE);
					tv_save_memo1.setVisibility(View.GONE);
					tv_cannle_memo1.setVisibility(View.GONE);
				}
			});
		}

		if (tv_btn_next != null) {
			tv_btn_next.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (img_editmonthvalue != null) {
						img_editmonthvalue.setVisibility(View.VISIBLE);
					}
					GetInfoByOrder(true);
				}
			});
		}
		if (tv_btn_pre != null) {
			tv_btn_pre.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (img_editmonthvalue != null) {
						img_editmonthvalue.setVisibility(View.VISIBLE);
					}
					GetInfoByOrder(false);
				}
			});
		}

		if (ll_top_menu != null) {
			ll_top_menu.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					titlePopup.show(v);
				}
			});
		}

		titlePopup.setItemOnClickListener(new OnItemOnClickListener() {

			@Override
			public void onItemClick(ActionItem item, int position) {
				// TODO Auto-generated method stub
				switch (item.operateId) {
				case 1:
					OPFaultReport();
					break;
				case 2:
					OPPrint();
					break;
				case 3:
					OpAdvice();
					break;
				case 4:
					OpHistory();
					break;
				case 5:
					if (userItem.getAlreadyUpload() == 0) {
						Toast.makeText(WCBRealNewActivity.this, "没有需要上传的数据", Toast.LENGTH_SHORT).show();
					} else {
						OpUploadSingle();
					}
					break;
				case 6:
					OpPrintBeforeFee();
					break;
				}

			}
		});

		if (tv_update_gps != null) {
			tv_update_gps.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					LocationInfo locationInfo = ((ZsyyApplication) getApplication()).currPoint;
					if (locationInfo != null) {
						userItem.setLatitude(locationInfo.getLatitude());
						userItem.setLongitude(locationInfo.getLontitude());
					}
					userItem.setAlreadyUpload(1);
					dbmanager.UpdateUserItem(userItem);
					String disGps = "纬度：" + userItem.getLatitude() + " \n\n经度：" + userItem.getLongitude();
					tv_w_gps.setText(disGps);
				}
			});
		}

		if (ly_edit_phone != null) {
			ly_edit_phone.setOnClickListener(clickListener);
		}

		if (img_editphone != null) {
			img_editphone.setOnClickListener(clickListener);
		}
		if (tv_w_dh != null) {
			// tv_w_dh.setOnClickListener(clickListener);
		}

		if (tv_confirm_phone != null) {
			tv_confirm_phone.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					PhoneEditConfirm();
				}
			});
		}
		if (tv_cancel_phone != null) {
			tv_cancel_phone.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					tv_w_dh.setVisibility(View.VISIBLE);
					ly_edit_phone.setVisibility(View.VISIBLE);
					tv_confirm_phone.setVisibility(View.GONE);
					tv_cancel_phone.setVisibility(View.GONE);
					edit_phone.setVisibility(View.GONE);
				}
			});
		}
	}

	private void SetDefaultFontSize() {
		LinearLayout loginLayout = (LinearLayout) findViewById(R.id.ly_maintop);
		SetFontseize(loginLayout);
	}

	private void SetFontseize(ViewGroup viewGroup) {

		int count = viewGroup.getChildCount();
		for (int i = 0; i < count; i++) {
			View view = viewGroup.getChildAt(i);
			if (view instanceof TextView) { // 若是Button记录下
				TextView newDtv = (TextView) view;
				newDtv.setTextSize(defaultsize);
			} else if (view instanceof ViewGroup) {
				// 若是布局控件（LinearLayout或RelativeLayout）,继续查询子View
				this.SetFontseize((ViewGroup) view);
			}
		}

		TextView TV_Pre = (TextView) this.findViewById(R.id.tv_btn_pre);
		TV_Pre.setTextSize(18);
		TextView TV_Nexp = (TextView) this.findViewById(R.id.tv_btn_next);
		TV_Nexp.setTextSize(18);

	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			for (int i = 0; i < PublicWay.activityList.size(); i++) {
				if (null != PublicWay.activityList.get(i)) {
					PublicWay.activityList.get(i).finish();
				}
			}

			finish();
		}
		return true;
	}

	private void InitData() {
		if (stealNo == null || stealNo == "") {
			return;
		}
		userItem = dbmanager.GetWCBUserItemByNo(stealNo);
		if (userItem == null) {
			Toast.makeText(WCBRealNewActivity.this, "没有查询到数据", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		// if (userItem != null && (op == 1 || op == 2)) {
		// preferencesService.saveCurrentCustomerInfo(userItem.getStealNo(),
		// userItem.getOrderNumber(), userItem.getPianNo(),
		// userItem.getAreaNo(), userItem.getDuanNo(),
		// userItem.getNoteNo());
		// }

		if (userItem != null) {
			preferencesService.saveCurrentCustomerInfo(userItem.getStealNo(), userItem.getOrderNumber(),
					userItem.getPianNo(), userItem.getAreaNo(), userItem.getDuanNo(), userItem.getNoteNo());
		}

		FillData(userItem);
	}

	private void FillData(WCBUserEntity userInfo) {
		if (userItem == null) {
			return;
		}
		tv_w_bbh.setText(userItem.getNoteNo());
		if (userItem.getChaoBiaoTag() == 0) {
			tv_w_cb_tag.setText("未抄表");
		}
		if (userItem.getChaoBiaoTag() == 1) {
			tv_w_cb_tag.setText("已抄表");
		}
		if (userItem.getChaoBiaoTag() == 3) {
			tv_w_cb_tag.setText("已收费");
		}
		if (userItem.getOrChaoBiaoTag() > 0) {
			img_editmonthvalue.setVisibility(View.GONE);
		}
		tv_w_username.setText(userItem.getUserFName());
		tv_w_userno.setText(userItem.getUserNo());
		tv_w_dh.setText(userItem.getPhone());
		tv_w_dz.setText(userItem.getAddress());
		tv_w_price_type.setText(userItem.getPriceTypeName());
		tv_w_steal_no.setText(userItem.getStealNo());
		tv_w_ponsition.setText(userItem.getWaterMeterPositionName());
		tv_w_sybs.setText(String.valueOf((int) userItem.getLastMonthValue()));
		tv_monthvalue.setText(String.valueOf((int) userItem.getCurrentMonthValue()));
		tv_w_benqishuiliang.setText(String.valueOf((int) userItem.getCurrMonthWNum()));
		tv_w_benqishuifei.setText(String.valueOf(userItem.getTotalCharge()));
		tv_w_lastchaobiadate.setText(userItem.getLastChaoBiaoDate());
		tv_w_avprice.setText(String.valueOf(userItem.getAvePrice()));
		tv_w_charge.setText(String.valueOf(userItem.getCurrMonthFee()));
		tv_w_agv_wushuiprice.setText(String.valueOf(userItem.getExtraChargePrice1()));
		tv_w_agv_wushui_charge.setText(String.valueOf(userItem.getExtraCharge1()));
		tv_w_agv_fujia_price.setText(String.valueOf(userItem.getExtraChargePrice2()));
		tv_w_agv_fujia_charge.setText(String.valueOf(userItem.getExtraCharge2()));
		edit_currvalue.setText(String.valueOf((int) userItem.getCurrentMonthValue()));
		tv_w_cb_Memo.setText(String.valueOf(userItem.getMemo1()));
		String disGps = "纬度：" + userItem.getLatitude() + " \n\n经度：" + userItem.getLongitude();
		tv_w_gps.setText(disGps);
		if (userItem.getUserFName().length() > 10) {
			tv_title.setText(userItem.getUserFName().subSequence(0, 10));
		} else {
			tv_title.setText(userItem.getUserFName());
		}
		edit_phone.setText(userItem.getPhone());

		tv_w_cb_Memo.setText(userItem.getMemo1());

		if (userItem.getIsSummaryMeter() == 2) {
			tv_title.setText(userItem.getUserFName() + "-总表");
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (dbmanager != null) {
			dbmanager.closeDB();
		}
		if(bService!=null)
		{
			bService.unRegisterBlue();
		}
		if(printService!=null)
		{
			printService.disconnect();
		}
	}

	private void ChaoBiaoCinfirm(double waterValue) {
		if (waterValue == 0) {
			// 清除数据：修改抄表标志，清除已抄数据，

			// 恢复初始值：monthNum，waterValue，waterCharge，avgPrice，dbWPrice，

			// 更新抄表信息
			// public void UpdateUserItem(WCBUserEntity useritem)
			userItem.setCurrMonthWNum(0);
			userItem.setCurrentMonthValue(0);
			userItem.setCurrMonthFee(0);
			userItem.setAvePrice(0);
			userItem.setTotalCharge(0);
			userItem.setExtraTotalCharge(0);
			userItem.setExtraChargePrice1(0);
			userItem.setExtraChargePrice2(0);
			userItem.setExtraCharge1(0);
			userItem.setExtraCharge2(0);
			userItem.setPreMoney(0);
			userItem.setOweMoney(0);
			userItem.setChaoBiaoTag(0);
			userItem.setAlreadyUpload(0);
		} else {
			double monthNum = Math.abs(waterValue - userItem.getLastMonthValue());
			if (userItem.getIsSummaryMeter() == 2) {
				monthNum = monthNum - dbmanager.GetDisWaterMeterValue(userItem.getStealNo());
			}
			double avgPrice = WaterUtils.GetAvgPrice(userItem.getStepPrice());
			Map<String, Double> dicExtPrice = WaterUtils.GetExtPrice(userItem.getExtraPrice());
			double waterCharge = WaterUtils.round(monthNum * avgPrice, 2);
			double extCharge1 = WaterUtils.round(dicExtPrice.get("F1") * monthNum, 2);
			double extCharge2 = WaterUtils.round(dicExtPrice.get("F2") * monthNum, 2);
			double dbWPrice = WaterUtils.round(waterCharge + extCharge1 + extCharge2, 2);
			userItem.setCurrMonthWNum((int) monthNum);
			userItem.setCurrentMonthValue(waterValue);
			// double
			// dbWPrice=WaterUtils.CalcWaterFee(userItem.getStepPrice(),userItem.getExtraPrice(),userItem.getCurrMonthWNum());

			userItem.setCurrMonthFee(waterCharge);
			userItem.setAvePrice(avgPrice);
			userItem.setTotalCharge(dbWPrice);
			userItem.setExtraTotalCharge(WaterUtils.round(extCharge1 + extCharge2, 3));
			userItem.setExtraChargePrice1(dicExtPrice.get("F1"));
			userItem.setExtraChargePrice2(dicExtPrice.get("F2"));
			userItem.setExtraCharge1(extCharge1);
			userItem.setExtraCharge2(extCharge2);
			double preMoney = userItem.getOrPreMoney();
			double oweMoney = userItem.getOrOweFee();
			double levMoney = userItem.getShouFei() + preMoney - dbWPrice - oweMoney;
			if (levMoney < 0) {
				preMoney = 0;
				oweMoney = Math.abs(levMoney);
			} else {
				preMoney = levMoney;
				oweMoney = 0;
			}
			userItem.setPreMoney(WaterUtils.round(preMoney, 3));
			userItem.setOweMoney(WaterUtils.round(oweMoney, 3));
			userItem.setChaoBiaoTag(1);
			userItem.setCheckState("1");
			userItem.setCheckDateTime(simpleDateFormat.format(new Date()));
			userItem.setChaoBiaoDate(simpleDateFormat.format(new Date()));
			userItem.setAlreadyUpload(1);
			// if(userItem.getNoteNo().startsWith("a")||userItem.getNoteNo().startsWith("A"))
			// {
			// 公厕特殊情况取消 2016-5-4
			// if(!userItem.getWaterUserTypeId().equalsIgnoreCase("0006"))
			// {
			// userItem.setChaoBiaoTag(3);
			// userItem.setCheckState("1");
			// userItem.setCheckDateTime(simpleDateFormat.format(new Date()));
			// WCBWaterChargeItem chargeItem = GetChageItem();
			// if (chargeItem != null) {
			// userItem.setChargeID(chargeItem.getCHARGEID());
			// dbmanager.InsertCharegeRecord(chargeItem);
			// }
			// }
			// else {
			// userItem.setCheckState("0");
			// userItem.setCheckDateTime(simpleDateFormat.format(new Date()));
			// }
			// }
		}
		ly_currentvalue.setVisibility(View.VISIBLE);
		edit_currvalue.setVisibility(View.GONE);
		tv_confirm.setVisibility(View.GONE);
		tv_cancel.setVisibility(View.GONE);
		//tv_monthvalue.setVisibility(View.VISIBLE);

		dbmanager.UpdateUserItem(userItem);
		FillData(userItem);
	}

	private WCBWaterChargeItem GetChageItem() {
		WCBWaterChargeItem cbChargeItem = new WCBWaterChargeItem();
		String chargeId = new SimpleDateFormat("yyyyMMdd").format(new Date()) + loginId + "SF";
		// 可能重复==========================================================================
		int chargeIndex = preferencesService.GetChargeIndex();
		chargeIndex++;
		preferencesService.SaveChargeIndex(chargeIndex);
		chargeId = chargeId + Utility.PaddingLeft(String.valueOf(chargeIndex), '0', 6);
		if (userItem == null) {
			return null;
		}
		cbChargeItem.setAlreadyUpload(1);
		cbChargeItem.setCHARGEID(chargeId);
		cbChargeItem.setCHARGEBCSS(userItem.getTotalCharge());
		cbChargeItem.setCHARGEBCYS(userItem.getTotalCharge());
		cbChargeItem.setCHARGEClASS("1");
		cbChargeItem.setCHARGEDATETIME(userItem.getChaoBiaoDate());
		cbChargeItem.setCHARGETYPEID(1);
		cbChargeItem.setCHARGEWORKERID(loginId);
		cbChargeItem.setCHARGEWORKERNAME(userName);
		cbChargeItem.setCHARGEYSBCSZ(0);
		cbChargeItem.setCHARGEYSJSYE(0);
		cbChargeItem.setCHARGEYSQQYE(0);
		cbChargeItem.setEXTRACHARGECHARGE1(userItem.getExtraCharge1());
		cbChargeItem.setEXTRACHARGECHARGE2(userItem.getExtraCharge2());
		cbChargeItem.setMEMO("");
		cbChargeItem.setOVERDUEMONEY(0);
		cbChargeItem.setRECEIPTNO("");
		cbChargeItem.setRECEIPTPRINTCOUNT(1);
		cbChargeItem.setTOTALCHARGE(userItem.getTotalCharge());
		cbChargeItem.setTOTALNUMBERCHARGE((int) userItem.getCurrMonthWNum());
		cbChargeItem.setWATERTOTALCHARGE(userItem.getCurrMonthFee());
		return cbChargeItem;
	}

	private void GetInfoByOrder(boolean isnext) {
		Map<String, Object> currCustomer = preferencesService.GetCurrentCustomerInfo();
		String NoteNo = currCustomer.get("NoteNo").toString();
		int orderNo = Integer.parseInt(currCustomer.get("OrderNo").toString());
		String StealNo = currCustomer.get("StealNo").toString();
		userItem = dbmanager.GetWCBUserItemByOrder(NoteNo, StealNo, orderNo, isnext, chaobiaoTag);
		if (userItem == null) {
			if (isnext) {
				Toast.makeText(WCBRealNewActivity.this, "没有下一条数据", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(WCBRealNewActivity.this, "没有上一条数据", Toast.LENGTH_SHORT).show();
			}
			return;
		}
		preferencesService.saveCurrentCustomerInfo(userItem.getStealNo(), userItem.getOrderNumber(),
				userItem.getPianNo(), userItem.getAreaNo(), userItem.getDuanNo(), userItem.getNoteNo());
		stealNo = userItem.getStealNo();

		tv_confirm.setVisibility(View.GONE);
		tv_cancel.setVisibility(View.GONE);
		//tv_monthvalue.setVisibility(View.VISIBLE);
		edit_currvalue.setVisibility(View.GONE);
		ly_currentvalue.setVisibility(View.VISIBLE);

		FillData(userItem);
	}

	private void ShowDiag(final int operate, String title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(WCBRealNewActivity.this);
		builder.setTitle(title);
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (operate == 1) {
					String wValue = edit_currvalue.getText().toString();
					double waterValue = Double.parseDouble(wValue);
					if (userItem != null) {
						userItem.setIsReverse(1);
						ChaoBiaoCinfirm(waterValue);
					}

				}
			}
		});

		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}

		});
		builder.create().show();
	}

	//====================================================================
	private void UpdatePrintState() {
		userItem.setChaoBiaoTag(3);
		userItem.setAlreadyUpload(1);
		userItem.setCheckState("1");
		userItem.setCheckDateTime(simpleDateFormat.format(new Date()));
//		WCBWaterChargeItem chargeItem = GetChageItem();
//		if (chargeItem != null) {
//			userItem.setChargeID(chargeItem.getCHARGEID());
//			dbmanager.UpdateUserItem(userItem);
//			dbmanager.InsertCharegeRecord(chargeItem);
//		}
		// userItem.getReadMeterRecordId();
		//============================================
		dbmanager.UpdateUserItem(userItem);
		
		OpUploadSingle();
	}

	// 判断打印机状态，上传修改收费信息
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				UpdatePrintState();
				break;
			}
		}

	};

	private OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
//			case R.id.tv_w_benqidushu:
//				// case R.id.img_editmonthvalue:
//			case R.id.tv_w_lastchaobiadate:
//			case R.id.tv_w_benqishuiliang:
//			case R.id.ly_currentvalue:
			case R.id.ly_currvalue_swith:
				//EditMonthClickMethod();
				ChangeMonthMethod();
				break;
			case R.id.img_editphone:
			case R.id.tv_w_dh:
			case R.id.ly_edit_phone:
				EditPhoneClickMethod();
				break;
			case R.id.ly_memo_onoff:
				EditMemoSwitch();
				break;
			}
		}

		private void ChangeMonthMethod() {
			if (!(edit_currvalue.getVisibility()==View.VISIBLE)) {
				EditMonthClickMethod();
			}
			
		}
	};

	private void EditMemoSwitch() {
		//ly_Memo1.setVisibility(View.VISIBLE);
		edit_memo1.setVisibility(View.VISIBLE);
		tv_save_memo1.setVisibility(View.VISIBLE);
		tv_cannle_memo1.setVisibility(View.VISIBLE);
		
		edit_memo1.setText(tv_w_cb_Memo.getText());
	}

	private void EditPhoneClickMethod() {
		tv_w_dh.setVisibility(View.GONE);
		ly_edit_phone.setVisibility(View.GONE);
		tv_confirm_phone.setVisibility(View.VISIBLE);
		tv_cancel_phone.setVisibility(View.VISIBLE);
		edit_phone.setVisibility(View.VISIBLE);
	}

	private void PhoneEditConfirm() {
		String phoneNumber = edit_phone.getText().toString();
		if (phoneNumber.length() < 7) {
			Toast.makeText(WCBRealNewActivity.this, "电话输入错误", Toast.LENGTH_SHORT).show();
			return;
		}
		if (userItem.getPhone() != phoneNumber) {
			userItem.setPhone(phoneNumber);
			userItem.setAlreadyUpload(1);
		}
		tv_w_dh.setText(phoneNumber);
		dbmanager.UpdateUserItem(userItem);

		tv_w_dh.setVisibility(View.VISIBLE);
		ly_edit_phone.setVisibility(View.VISIBLE);
		tv_confirm_phone.setVisibility(View.GONE);
		tv_cancel_phone.setVisibility(View.GONE);
		edit_phone.setVisibility(View.GONE);
	}

	private void EditMonthClickMethod() {
		if (userItem.getOrChaoBiaoTag() != 0) {
			Toast.makeText(WCBRealNewActivity.this, "该表已经抄过", Toast.LENGTH_SHORT).show();
			return;
		}
		if (userItem.getIsSummaryMeter() == 2) {
			hasUnFenbiao = dbmanager.HasUnChildWaterMeter(userItem.getStealNo());
			if (hasUnFenbiao) {
				ShowDiag(2, "该表为总表，请先抄分表");
				return;
			}
		}
		//if (userItem.getChaoBiaoTag() ==1 && userItem.getAlreadyUpload()==0) {
		// if (userItem.getOrChaoBiaoTag() !=0 ) {
		// Toast.makeText(WCBRealNewActivity.this, "该表已经抄过",
		// Toast.LENGTH_SHORT).show();
		// return;
		// }

		mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

		edit_currvalue.setVisibility(View.VISIBLE);
		edit_currvalue.setEnabled(true);
		edit_currvalue.setFocusable(true);
		edit_currvalue.setFocusableInTouchMode(true);
		edit_currvalue.requestFocus();
		mInputMethodManager.showSoftInput(edit_currvalue, 0);
		tv_cancel.setVisibility(View.VISIBLE);
		tv_confirm.setVisibility(View.VISIBLE);
		//tv_monthvalue.setVisibility(View.GONE);
		//ly_currentvalue.setVisibility(View.GONE);
		if (userItem != null) {
			if (userItem.getOrChaoBiaoTag() > 0 || userItem.getChaoBiaoTag() == 3) {
				//ly_currentvalue.setVisibility(View.GONE);
				edit_currvalue.setVisibility(View.GONE);
				tv_cancel.setVisibility(View.GONE);
				tv_confirm.setVisibility(View.GONE);
				//tv_monthvalue.setVisibility(View.VISIBLE);
			}
			if (userItem.getCurrentMonthValue() == 0) {
				edit_currvalue.setText("");
			}
			if (userItem.getWaterFixValue() > 0) {
				edit_currvalue.setText(String.valueOf((int) userItem.getWaterFixValue()));
				edit_currvalue.setEnabled(false);
				edit_currvalue.clearFocus();
				mInputMethodManager.hideSoftInputFromWindow(edit_currvalue.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
	}

	private void OPFaultReport() {
		Intent intent = new Intent(WCBRealNewActivity.this, WFaultReportActivity.class);
		intent.putExtra("UserNo", userItem.getUserNo());
		startActivity(intent);
	}

	private void OPPrint() {
		if (userItem != null && userItem.getChaoBiaoTag() == 1)//
		{
			if (userItem.getAlreadyUpload() == 1) {
				Toast.makeText(WCBRealNewActivity.this, "请先进行数据上传！", Toast.LENGTH_SHORT).show();
				return;
			}
			printData = GetPrintData();
			if (!bService.isOpen()) {
				bService.open();
			}

			// GetSingleMeterData();
			if(printService==null)
			{
				printService = new BluetoothPrintService(WCBRealNewActivity.this);
			}
			
			if (printService.connect()) {
				if (printService.send(printData) == 0) {
					// UpdatePrintState();
					handler.sendEmptyMessage(1);
				}
				//printService.disconnect();
				
			} else {
				bService.searchDevice();
			}
			bService.setBonListener(new bondFinishListener() {

				@Override
				public void doOperate() {
					// TODO Auto-generated method stub
					if(printService==null)
					{
						printService = new BluetoothPrintService(WCBRealNewActivity.this);
					}
					
					if (printService.connect()) {
						if (printService.send(printData) == 0) {
							// UpdatePrintState();
							handler.sendEmptyMessage(1);
						}
						//printService.disconnect();
						
					}
				}
			});
		} else {
			Toast.makeText(WCBRealNewActivity.this, "现在不能打印小票", Toast.LENGTH_SHORT).show();
		}
	}

	private void OpAdvice() {
		Intent intent = new Intent(WCBRealNewActivity.this, WCustomAdviceActivity.class);
		intent.putExtra("UserNo", userItem.getUserNo());
		startActivity(intent);
	}

	private void OpHistory() {
		Intent intent = new Intent(WCBRealNewActivity.this, WCBHistoryActivity.class);
		intent.putExtra("stealNo", userItem.getStealNo());
		startActivity(intent);
	}
	// =======================================================================================
	// private void GetSingleMeterData()
	// {
	// WSingleUserItemReq req = new WSingleUserItemReq();
	// req.setOperType("14");
	// req.setReadMeterRecordId(userItem.getReadMeterRecordId());
	//
	// GsonServlet<WSingleUserItemReq, WDownUserRes> gServlet = new
	// GsonServlet<WSingleUserItemReq, WDownUserRes>(
	// this);
	// gServlet.request(req, WDownUserRes.class);
	// gServlet.setOnResponseEndListening(new
	// OnResponseEndListening<WSingleUserItemReq, WDownUserRes>(){
	// @Override
	// public void onResponseEnd(WSingleUserItemReq commonReq, WDownUserRes
	// commonRes,
	// boolean result, String errmsg, int responseCode) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void onResponseEndSuccess(WSingleUserItemReq commonReq,
	// WDownUserRes commonRes, String errmsg, int responseCode) {
	// // TODO Auto-generated method stub
	// if(commonRes!=null&&commonRes.getUserItems()!=null&&commonRes.getUserItems().size()>0)
	// {
	// dbHelper.EditCustomerInfo(commonRes.getUserItems());
	// //Toast.makeText(WDownloadDataActivity.this,"下载成功！",Toast.LENGTH_SHORT).show();
	// FillData(userItem);
	// return;
	// }
	//
	// }
	//
	// @Override
	// public void onResponseEndErr(WSingleUserItemReq commonReq,
	// WDownUserRes commonRes, String errmsg, int responseCode) {
	// // TODO Auto-generated method stub
	// Toast.makeText(getApplicationContext(), errmsg,
	// Toast.LENGTH_SHORT).show();
	// }
	//
	//
	//
	// });
	// }

	private void OpUploadSingle() {
		if (userItem == null) {
			return;
		}
		WUploadUserReq req = new WUploadUserReq();
		req.setOperType("5");
		WCBUserEntity userEntity = userItem;
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
		req.setTotalNumber((int) userEntity.getCurrMonthWNum());
		req.setWaterMeterEndNumber((int) userEntity.getCurrentMonthValue());
		req.setWaterTotalCharge(userEntity.getCurrMonthFee());
		req.setLatitude(String.valueOf(userEntity.getLatitude()));
		req.setLongitude(String.valueOf(userEntity.getLongitude()));
		req.setPhone(userEntity.getPhone());
		req.setMemo1(userEntity.getMemo1());
		GsonServlet<WUploadUserReq, WUploadUserRes> gServlet = new GsonServlet<WUploadUserReq, WUploadUserRes>(this);
		gServlet.request(req, WUploadUserRes.class);
		gServlet.setOnResponseEndListening(new OnResponseEndListening<WUploadUserReq, WUploadUserRes>() {

			@Override
			public void onResponseEnd(WUploadUserReq commonReq, WUploadUserRes commonRes, boolean result, String errmsg,
					int responseCode) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onResponseEndSuccess(WUploadUserReq commonReq, WUploadUserRes commonRes, String errmsg,
					int responseCode) {
				// TODO Auto-generated method stub
				if (commonRes != null) {
					userItem.setAlreadyUpload(0);
					dbmanager.UpdateUserUploadTag(userItem.getUserID());
					if (userItem.getChargeID() == null || userItem.getChargeID() == "") {
						Toast.makeText(WCBRealNewActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
						// =======================================================================================
						// GetSingleMeterData()
						// GetSingleMeterData();
						FillData(userItem);
						return;
					}
					// WCBWaterChargeItem
					// uploadChargeItem=dbmanager.getChargeItem(userItem.getChargeID());
					// if(uploadChargeItem==null)
					// {
					// Toast.makeText(WCBRealNewActivity.this,"上传成功",Toast.LENGTH_SHORT).show();
					// return;
					// }
					// UploadChargeData(uploadChargeItem);
				}

			}

			@Override
			public void onResponseEndErr(WUploadUserReq commonReq, WUploadUserRes commonRes, String errmsg,
					int responseCode) {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), errmsg, Toast.LENGTH_SHORT).show();
			}

		});
	}

	// 打印催收小票
	private void OpPrintBeforeFee() {
		if (userItem != null && userItem.getChaoBiaoTag() != 3)//
		{
			if (userItem.getAlreadyUpload() == 1) {
				Toast.makeText(WCBRealNewActivity.this, "请先进行数据上传！", Toast.LENGTH_SHORT).show();
				return;
			}
			printData = GetPrintData();
			if (!bService.isOpen()) {
				bService.open();
			}
			if(printService==null)
			{
				printService = new BluetoothPrintService(WCBRealNewActivity.this);
			}
			if (printService.connect()) {
				if (printService.send(printData) == 0) {
					
					// UpdatePrintState();
				}
				//printService.disconnect();
			} else {
				bService.searchDevice();
			}
			bService.setBonListener(new bondFinishListener() {

				@Override
				public void doOperate() {
					// TODO Auto-generated method stub
					if(printService==null)
					{
						printService = new BluetoothPrintService(WCBRealNewActivity.this);
					}
					if (printService.connect()) {
						if (printService.send(printData) == 0) {
							
							// UpdatePrintState();
						}
						//printService.disconnect();
					}
				}
			});
		} else {
			Toast.makeText(WCBRealNewActivity.this, "现在不能打印催收小票", Toast.LENGTH_SHORT).show();
		}
	}

	private void UploadChargeData(WCBWaterChargeItem chargeItem) {
		if (chargeItem == null) {
			return;
		}
		WaterChargeReq req = new WaterChargeReq();
		req.setOperType("11");
		req.setChageItem(chargeItem);
		GsonServlet<WaterChargeReq, WaterChageRes> gServlet = new GsonServlet<WaterChargeReq, WaterChageRes>(this);
		gServlet.request(req, WaterChageRes.class);
		gServlet.setOnResponseEndListening(new OnResponseEndListening<WaterChargeReq, WaterChageRes>() {

			@Override
			public void onResponseEnd(WaterChargeReq commonReq, WaterChageRes commonRes, boolean result, String errmsg,
					int responseCode) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onResponseEndSuccess(WaterChargeReq commonReq, WaterChageRes commonRes, String errmsg,
					int responseCode) {
				// TODO Auto-generated method stub
				if (commonRes != null) {
					Toast.makeText(WCBRealNewActivity.this, "上传成功！", Toast.LENGTH_SHORT).show();
					dbmanager.UpdateChargeTag(commonReq.getChageItem().getCHARGEID());
				}

			}

			@Override
			public void onResponseEndErr(WaterChargeReq commonReq, WaterChageRes commonRes, String errmsg,
					int responseCode) {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), errmsg, Toast.LENGTH_SHORT).show();
			}

		});
	}

	private String[] GetPrintData() {
		// =======================================================================================
		// GetSingleMeterData()

		Date currDate = new Date();
		List<String> arrList = new ArrayList<String>();
		String strDate = new SimpleDateFormat("yyyy年MM月dd日").format(currDate);
		if (userItem == null) {
			return null;
		}
		arrList.add("\n喀左县自来水公司");
		arrList.add("水费收费凭条\n");
		arrList.add("\n\r");
		arrList.add("\n\r");
		arrList.add("用户号： " + userItem.getUserNo() + "\n");
		arrList.add("户  名： " + userItem.getUserFName() + "\n");
		arrList.add("电  话：" + (userItem.getPhone() == null ? "" : userItem.getPhone()) + "\n");
		arrList.add("地  址：" + userItem.getAddress() + "\n");
		arrList.add("-----------------------\n");
		arrList.add("上期读数：" + userItem.getLastMonthValue() + "\n");
		arrList.add("本期读数：" + userItem.getCurrentMonthValue() + "\n");
		arrList.add("本期水量：" + userItem.getCurrMonthWNum() + "\n");
		arrList.add("水费单价：" + userItem.getAvePrice() + "元/吨\n");
		arrList.add("污水处理费单价：" + userItem.getExtraChargePrice1() + "元/吨\n");
		arrList.add("水费：" + userItem.getCurrMonthFee() + "元\n");
		arrList.add("附加费：" + userItem.getExtraCharge2() + "元\n");
		arrList.add("污水处理费：" + userItem.getExtraCharge1() + "元\n");
		arrList.add("金额合计：" + userItem.getTotalCharge() + "元\n");
		arrList.add("-----------------------\n");
		arrList.add("收费员：" + preferencesService.getLoginInfo().get("userName").toString() + "\n");
		arrList.add("打票时间：" + strDate + "\n");
		arrList.add("维修电话：4822974\n");
		arrList.add("备注：非报销凭证，请持此收费凭条30日内到自来水公司换取正式发票\n");
		arrList.add("\n----------------------\n");
		arrList.add("\n\r");

		return (String[]) arrList.toArray(new String[arrList.size()]);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
	}
	
	
	
}
