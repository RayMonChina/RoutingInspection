package com.ideal.zsyy.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.baidu.location.BDLocation;
import com.ideal.zsyy.adapter.RLocationItemAdapter;
import com.ideal.zsyy.entity.LocationInfo;
import com.shenrenkeji.intelcheck.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class RLocationStatusActivity extends Activity {

	private TextView tv_status=null;
	private TextView tv_date=null;
	private TextView tv_location=null;
	private TextView tv_lat=null;
	private TextView tv_lon=null;
	private Button btn_back=null;
	ZsyyApplication application=null;
	private Timer timer=new Timer();
	private ListView lv_location;
	private List<LocationInfo>locationInfos=new ArrayList<LocationInfo>();
	private SimpleDateFormat simFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private RLocationItemAdapter adapterLocation=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.r_location_status);
		initView();
		initData();
	}
	
	private void initView()
	{
		tv_status=(TextView)findViewById(R.id.tv_status);
		tv_date=(TextView)findViewById(R.id.tv_location_time);
		tv_location=(TextView)findViewById(R.id.tv_location);
		btn_back=(Button)findViewById(R.id.btn_back);
		btn_back.setOnClickListener(clickListener);
		tv_lat=(TextView)findViewById(R.id.tv_lat);
		tv_lon=(TextView)findViewById(R.id.tv_lon);
		lv_location=(ListView)findViewById(R.id.lv_location);
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
		application=(ZsyyApplication)getApplication();
		BDLocation locationInfo=application.currLocation;
		if(locationInfo==null)
		{
			tv_status.setText("定位失败");
			return;
		}
		if(locationInfo.getLocType() == BDLocation.TypeServerError)
		{
			tv_status.setText("服务端网络定位失败");
			return;
		}
		else if (locationInfo.getLocType() == BDLocation.TypeNetWorkException) {
			tv_status.setText(("网络不同导致定位失败，请检查网络是否通畅"));
			return;
		} else if (locationInfo.getLocType() == BDLocation.TypeCriteriaException) {
			tv_status.setText("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
			return;
		}
		tv_status.setText("定位成功");
		tv_location.setText(locationInfo.getAddrStr());
		tv_lat.setText("纬度:"+locationInfo.getLatitude());
		tv_lon.setText("经度:"+locationInfo.getLongitude());
		tv_date.setText(locationInfo.getTime());
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				locationInfos.add(application.tempPoint);
				handler.sendEmptyMessage(1);
			}
		},0, 1000*60*5);
		adapterLocation=new RLocationItemAdapter(RLocationStatusActivity.this,locationInfos);
		lv_location.setAdapter(adapterLocation);
		
	}
	
	Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				adapterLocation.notifyDataSetChanged();
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
		if(timer!=null)
		{
			timer.cancel();
		}
	}
	
	
}
