package com.ideal.zsyy.activity;

import java.util.List;

import com.shenrenkeji.intelcheck.R;
import com.ideal.zsyy.db.WdbManager;
import com.ideal.zsyy.entity.WAnalysisItem;
import com.ideal.zsyy.entity.WBBItem;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class WAnalysisActivity extends Activity {

	private Spinner spinnerBBH = null;
	private TextView tv_yhzs = null, tv_ycyhs = null, tv_wcyhs = null,
			tv_sbgz = null, tv_zsl = null, tv_zfy = null,tv_ysje=null,tv_wsje=null;
	private RadioGroup rgDays=null;
	private RadioButton rb_month;
	private Button btn_back=null;
	private WdbManager wManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.w_static_analysis);
		wManager=new WdbManager(WAnalysisActivity.this);
		this.initView();
		this.setEventListener();
	}

	private void initView() {
		spinnerBBH = (Spinner) findViewById(R.id.sp_bbh);
		tv_yhzs = (TextView) findViewById(R.id.tv_yhzs);
		tv_ycyhs = (TextView) findViewById(R.id.tv_ycyhs);
		tv_wcyhs = (TextView) findViewById(R.id.tv_wcyhs);
		tv_sbgz = (TextView) findViewById(R.id.tv_sbgz);
		tv_zsl = (TextView) findViewById(R.id.tv_zsl);
		tv_zfy = (TextView) findViewById(R.id.tv_zfy);
		tv_ysje=(TextView)findViewById(R.id.tv_ysje);
		tv_wsje=(TextView)findViewById(R.id.tv_wsje);
		rgDays=(RadioGroup)findViewById(R.id.rg_search_type);
		btn_back=(Button)findViewById(R.id.btn_back);
		rb_month=(RadioButton)findViewById(R.id.rb_month);
	}

	//初始化事件监听
	private void setEventListener() {

		if (spinnerBBH != null) {
			List<WBBItem>bbList=wManager.GetBiaoBenInfo();
			if(bbList!=null&&bbList.size()>0)
			{
				WBBItem bbItem=new WBBItem();
				bbItem.setNoteNo("全部");
				bbItem.setBId(0);
				bbList.add(0,bbItem);
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
					GetAnaResult();
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub

				}
			});

			if(rgDays!=null)
			{
				rgDays.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub
						GetAnaResult();
					}
				});
			}
		}
		
		if(rgDays!=null)
		{
			rgDays.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					// TODO Auto-generated method stub
					
				}
			});
		}
		
		if(btn_back!=null)
		{
			btn_back.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					finish();
				}
			});
		}
	}


	private void GetAnaResult()
	{
		String noteNO="";
		boolean isMonthCheck=false;
		WAnalysisItem wItem=new WAnalysisItem();
		if(this.spinnerBBH!=null)
		{
			WBBItem bbitem=(WBBItem)this.spinnerBBH.getSelectedItem();
			noteNO=bbitem.getBId()==0?"0":bbitem.getNoteNo();
		}
		if(rb_month!=null)
		{
			isMonthCheck=rb_month.isChecked();
		}
		if(isMonthCheck)
		{
			wItem= wManager.GetAnalysisItemByMonth(noteNO);
		}
		else {
			wItem=wManager.GetAnalysisItemByDay(noteNO);
		}
		if (wItem != null) {
			if (tv_yhzs != null) {
				tv_yhzs.setText(wItem.getTotle());
			}
			if (tv_ycyhs != null) {
				tv_ycyhs.setText(wItem.getYcb());
			}
			if (tv_wcyhs != null) {
				tv_wcyhs.setText(wItem.getWcb());
			}
			if (tv_sbgz != null) {
				tv_sbgz.setText(wItem.getSbgz());
			}
			if (tv_zsl != null) {
				tv_zsl.setText(wItem.getZsl());
			}
			if (tv_zfy != null) {
				tv_zfy.setText(wItem.getZfy());
			}
			if(tv_ysje!=null)
			{
				tv_ysje.setText(wItem.getYsje());
			}
			if(tv_wsje!=null)
			{
				tv_wsje.setText(wItem.getWsje());
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(wManager!=null)
		{
			wManager.closeDB();
		}
	}

	
	
}
