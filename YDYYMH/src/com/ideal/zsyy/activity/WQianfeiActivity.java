package com.ideal.zsyy.activity;

import java.util.List;
import java.util.Map;

import com.ideal.zsyy.db.WdbManager;
import com.ideal.zsyy.entity.WBBItem;
import com.ideal.zsyy.entity.WUploadItem;
import com.ideal.zsyy.service.PreferencesService;
import com.shenrenkeji.intelcheck.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class WQianfeiActivity extends Activity {

	private Spinner spinnerBBH = null;
	private TextView tv_yhzs = null, tv_ycyhs = null, tv_wcyhs = null, tv_qfje = null, tv_updateqianfei = null;
	private Button btn_back = null, btn_upload = null;
	private WdbManager dbManage = null;
	private Map<String, Object> userInfo;
	private Map<String, Integer> userDateInfo;
	private String NoteNo = "0";
	private PreferencesService preferencesService;
	private float defaultsize = 18f;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.w_cb_qianfei);
		dbManage = new WdbManager(WQianfeiActivity.this);
		PreferencesService pService = new PreferencesService(WQianfeiActivity.this);
		userInfo = pService.getLoginInfo();
		userDateInfo = pService.GetCBDateInfo();
		this.initView();
		this.setEventListener();
		SetDefaultFontSize();

	}

	private void setEventListener() {
		if (btn_back != null) {
			btn_back.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					finish();
				}
			});
		}
		if (tv_updateqianfei != null) {
			tv_updateqianfei.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

				}
			});
		}
		if (btn_upload != null) {
			btn_upload.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

				}
			});
		}

		if (spinnerBBH != null) {
			List<WBBItem> bbList = dbManage.GetBiaoBenInfo();
			WBBItem bbItemAll = new WBBItem();
			bbItemAll.setBId(0);
			bbItemAll.setNoteNo("全部");
			if (bbList != null) {
				bbList.add(0, bbItemAll);
			}
			ArrayAdapter<WBBItem> adapter = new ArrayAdapter<WBBItem>(this, android.R.layout.simple_spinner_item,
					bbList);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinnerBBH.setAdapter(adapter);
			spinnerBBH.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
					WBBItem bbItem = (WBBItem) arg0.getItemAtPosition(position);
					NoteNo = bbItem.getBId() == 0 ? "0" : bbItem.getNoteNo();
					WUploadItem wDataItem = dbManage.GetUploadInfo(NoteNo);
					if (wDataItem != null) {
						if (userInfo != null) {
							if (userInfo.containsKey("userName")) {
								wDataItem.setCby(userInfo.get("userName").toString());
							}
						}
						if (userDateInfo != null) {
							if (userDateInfo.containsKey("cMonth")) {
								wDataItem.setCbMonth(String.valueOf(userDateInfo.get("cMonth")));
							}
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
						if (tv_qfje != null) {
							tv_qfje.setText(String.valueOf(wDataItem.getGzbx()));
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

	private void initView() {
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_upload = (Button) findViewById(R.id.btn_upload);
		spinnerBBH = (Spinner) findViewById(R.id.sp_bbh);
		tv_yhzs = (TextView) findViewById(R.id.tv_yhzs);
		tv_ycyhs = (TextView) findViewById(R.id.tv_ycyhs);
		tv_wcyhs = (TextView) findViewById(R.id.tv_wcyhs);
		tv_qfje = (TextView) findViewById(R.id.tv_qfje);
		tv_updateqianfei = (TextView) findViewById(R.id.tv_updateqianfei);
	}

	private void SetDefaultFontSize() {
		LinearLayout loginLayout = (LinearLayout) findViewById(R.id.ly_showpannl);
		SetFontseize(loginLayout);
		// defaultsize = preferencesService.GetZoomSize();
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dbManage != null) {
			dbManage.closeDB();
		}
	}
}
