package com.ideal.zsyy.view;

import java.util.Map;

import com.baidu.platform.comapi.map.r;
import com.ideal.zsyy.activity.LoginActivity;
import com.ideal.zsyy.activity.RLocationStatusActivity;
import com.ideal.zsyy.activity.RSettingActivity;
import com.ideal.zsyy.service.PreferencesService;
import com.ideal.zsyy.utils.DeviceHelper;
import com.shenrenkeji.intelcheck.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LeftFragment extends Fragment {
	TextView tv_gps=null;
	TextView tv_setting=null;
	TextView tv_quit=null;
	TextView tv_appName=null;
	TextView tv_version=null;
	TextView tv_user_name=null;
	Context context=null;
	PreferencesService preferencesService=null;
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View frView= inflater.inflate(R.layout.r_left_fragment, null);
		tv_gps=(TextView)frView.findViewById(R.id.tv_Gps);
		tv_setting=(TextView)frView.findViewById(R.id.tv_setting);
		tv_quit=(TextView)frView.findViewById(R.id.tv_quit);
		tv_version=(TextView)frView.findViewById(R.id.tv_app_version);
		tv_appName=(TextView)frView.findViewById(R.id.tv_app_name);
		tv_user_name=(TextView)frView.findViewById(R.id.tv_user_name);
		return frView;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		context=getActivity();
		preferencesService=new PreferencesService(context);
		Map<String, Object>userInfos=preferencesService.getLoginInfo();
		if(tv_gps!=null)
		{
			tv_gps.setOnClickListener(clickListener);
		}
		String appName=context.getString(R.string.app_name);
		if(tv_appName!=null)
		{
			tv_appName.setText(appName);
		}
		if(tv_version!=null)
		{
			tv_version.setText(DeviceHelper.getVersionName(context));
		}
		if(tv_quit!=null)
		{
			tv_quit.setOnClickListener(clickListener);
		}
		if(tv_setting!=null)
		{
			tv_setting.setOnClickListener(clickListener);
		}
		tv_user_name.setText(userInfos.get("userName").toString());
	}
	
	private OnClickListener clickListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.tv_Gps:
				Intent intent_gps=new Intent(context,RLocationStatusActivity.class);
				context.startActivity(intent_gps);
				break;
			case R.id.tv_setting:
				Intent intent_setting=new Intent(context, RSettingActivity.class);
				context.startActivity(intent_setting);
				break;
			case R.id.tv_quit:
				preferencesService.clearLogin();
				Intent intent_login=new Intent(context,LoginActivity.class);
				startActivity(intent_login);
				getActivity().finish();
				break;
			default:
				break;
			}
		}
	};
	
	public class SampleAdapter extends ArrayAdapter<SampleItem> {
		public SampleAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.row, null);
			}
			ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon);
			icon.setImageResource(getItem(position).iconRes);
			TextView title = (TextView) convertView.findViewById(R.id.row_title);
			title.setText(getItem(position).tag);

			return convertView;
		}
	}
	
	private class SampleItem {
		public String tag;
		public int iconRes;
		public SampleItem(String tag, int iconRes) {
			this.tag = tag; 
			this.iconRes = iconRes;
		}
	}
}
