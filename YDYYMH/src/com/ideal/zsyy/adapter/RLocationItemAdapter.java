package com.ideal.zsyy.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.ideal.zsyy.entity.LocationInfo;
import com.ideal.zsyy.response.RworkItemRes;
import com.shenrenkeji.intelcheck.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RLocationItemAdapter extends BaseAdapter{

	private List<LocationInfo>locationInfos=null;
	private LayoutInflater inflater;
	private Context _context;
	private String _userId="";
	private SimpleDateFormat simpleFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public  RLocationItemAdapter(Context context,List<LocationInfo>locations) {
		// TODO Auto-generated constructor stub
		if(locations==null)
		{
			locations=new ArrayList<LocationInfo>();
		}
		locationInfos=locations;
		this._context=context;
		inflater=LayoutInflater.from(context);
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(locationInfos!=null)
		{
			return locationInfos.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if(locationInfos!=null&&locationInfos.size()>0)
		{
			return locationInfos.get(position);
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
		LocationInfo workItem = locationInfos.get(position);
		if (convertView != null) {
			if (convertView.getTag() != null) {
				viewHolder = (ViewHolder) convertView.getTag();
			}
		} else {
			LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.r_location_item, null);
			 TextView tv_location_item=(TextView)layout.findViewById(R.id.tv_location_item);// 
			 TextView tv_location_time=(TextView)layout.findViewById(R.id.tv_location_time);// 时间
			 
			viewHolder = new ViewHolder();
			viewHolder.setTv_location_item(tv_location_item);
			viewHolder.setTv_location_time(tv_location_time);
			convertView = layout;
			convertView.setTag(viewHolder);
		}
		String strStatus="";
		if(workItem.getErrMsg()!=null&&!workItem.getErrMsg().equals(""))
		{
			strStatus="上报失败";
			
		}
		else {
			strStatus="上报成功！";
		}
		viewHolder.getTv_location_item().setText(strStatus);
		viewHolder.getTv_location_time().setText(simpleFormat.format(workItem.getGetDate()));
		return convertView;
	}
	
	private class ViewHolder {
		private TextView tv_location_item;// 公司
		private TextView tv_location_time;// 单位
		public TextView getTv_location_item() {
			return tv_location_item;
		}
		public void setTv_location_item(TextView tv_location_item) {
			this.tv_location_item = tv_location_item;
		}
		public TextView getTv_location_time() {
			return tv_location_time;
		}
		public void setTv_location_time(TextView tv_location_time) {
			this.tv_location_time = tv_location_time;
		}
		

	}

}
