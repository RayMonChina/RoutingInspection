package com.ideal.zsyy.adapter;

import java.util.ArrayList;
import java.util.List;

import com.ideal.zsyy.entity.RUserStatusInfo;
import com.ideal.zsyy.entity.WCustomerAdviceInfo;
import com.ideal.zsyy.response.RworkItemRes;
import com.shenrenkeji.intelcheck.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RUserStatusAdapter extends BaseAdapter{

	private List<RUserStatusInfo>rUserItems=null;
	private LayoutInflater inflater;
	private Context _context;
	public  RUserStatusAdapter(Context context,List<RUserStatusInfo>userItems) {
		// TODO Auto-generated constructor stub
		if(userItems==null)
		{
			userItems=new ArrayList<RUserStatusInfo>();
		}
		rUserItems=userItems;
		this._context=context;
		inflater=LayoutInflater.from(context);
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return rUserItems.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if(rUserItems!=null&&rUserItems.size()>0)
		{
			return rUserItems.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder = null;
		RUserStatusInfo userStatus = rUserItems.get(position);
		if (convertView != null) {
			if (convertView.getTag() != null) {
				viewHolder = (ViewHolder) convertView.getTag();
			}
		} else {
			LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.r_user_status_item, null);
			 TextView tv_department=(TextView)layout.findViewById(R.id.tv_department);// 部门
			 TextView tv_user_name=(TextView)layout.findViewById(R.id.tv_user_name);// 用户名
//			 TextView tv_work_status=(TextView)layout.findViewById(R.id.tv_work_status);// 工作状态
//			 TextView tv_online_time=(TextView)layout.findViewById(R.id.tv_online_time);//在线时长
//			 TextView tv_work_status_time=(TextView)layout.findViewById(R.id.tv_work_status_time);//工作时长
			 TextView tv_location_time=(TextView)layout.findViewById(R.id.tv_location_time);//定位时间
			viewHolder = new ViewHolder();
			viewHolder.setTv_department(tv_department);
			viewHolder.setTv_user_name(tv_user_name);
//			viewHolder.setTv_work_status(tv_work_status);
//			viewHolder.setTv_online_time(tv_online_time);
//			viewHolder.setTv_work_status_time(tv_work_status_time);
			viewHolder.setTv_location_time(tv_location_time);
			convertView = layout;
			convertView.setTag(viewHolder);
		}
		
		viewHolder.getTv_department().setText(userStatus.getOrgname());
		viewHolder.getTv_user_name().setText(userStatus.getUsername());
//		viewHolder.getTv_work_status().setText("");
//		viewHolder.getTv_online_time().setText("");
//		viewHolder.getTv_work_status_time().setText("");
		viewHolder.getTv_location_time().setText(userStatus.getCurdatetime());
		
		return convertView;
	}
	
	private class ViewHolder {
		private TextView tv_department;// 部门
		private TextView tv_user_name;// 用户名
		private TextView tv_work_status;//工作状态
		private TextView tv_online_time;//在线时间
		private TextView tv_work_status_time;//工作状态时间
		private TextView tv_location_time;//定位时间
		public TextView getTv_department() {
			return tv_department;
		}
		public void setTv_department(TextView tv_department) {
			this.tv_department = tv_department;
		}
		public TextView getTv_user_name() {
			return tv_user_name;
		}
		public void setTv_user_name(TextView tv_user_name) {
			this.tv_user_name = tv_user_name;
		}
		public TextView getTv_work_status() {
			return tv_work_status;
		}
		public void setTv_work_status(TextView tv_work_status) {
			this.tv_work_status = tv_work_status;
		}
		public TextView getTv_online_time() {
			return tv_online_time;
		}
		public void setTv_online_time(TextView tv_online_time) {
			this.tv_online_time = tv_online_time;
		}
		public TextView getTv_work_status_time() {
			return tv_work_status_time;
		}
		public void setTv_work_status_time(TextView tv_work_status_time) {
			this.tv_work_status_time = tv_work_status_time;
		}
		public TextView getTv_location_time() {
			return tv_location_time;
		}
		public void setTv_location_time(TextView tv_location_time) {
			this.tv_location_time = tv_location_time;
		}
		
		
	}

}
