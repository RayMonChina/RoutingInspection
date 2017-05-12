package com.ideal.zsyy.adapter;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.map.Text;
import com.ideal.zsyy.entity.WCustomerAdviceInfo;
import com.ideal.zsyy.response.RworkItemRes;
import com.ideal.zsyy.utils.StringHelper;
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

public class RWorkItemAdapter extends BaseAdapter{

	private List<RworkItemRes>rworkItems=null;
	private LayoutInflater inflater;
	private Context _context;
	private String _userId="";
	private DelClickListener delClickListener;
	public  RWorkItemAdapter(Context context,List<RworkItemRes>workItems,String userId) {
		// TODO Auto-generated constructor stub
		if(workItems==null)
		{
			workItems=new ArrayList<RworkItemRes>();
		}
		rworkItems=workItems;
		this._context=context;
		inflater=LayoutInflater.from(context);
		this._userId=userId;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return rworkItems.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if(rworkItems!=null&&rworkItems.size()>0)
		{
			return rworkItems.get(position);
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
		RworkItemRes workItem = rworkItems.get(position);
		if (convertView != null) {
			if (convertView.getTag() != null) {
				viewHolder = (ViewHolder) convertView.getTag();
			}
		} else {
			LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.r_work_item, null);
			 TextView tv_username=(TextView)layout.findViewById(R.id.tv_username);// 户名
			 TextView tv_date=(TextView)layout.findViewById(R.id.tv_add_time);// 时间
			 TextView tv_company=(TextView)layout.findViewById(R.id.tv_company);// 用户编码
			 TextView tv_unit=(TextView)layout.findViewById(R.id.tv_unit);//单位
			 TextView btn_delete=(TextView)layout.findViewById(R.id.btn_delete);//删除
			 TextView tv_content=(TextView)layout.findViewById(R.id.tv_content);//内容
			 TextView tv_record_count=(TextView)layout.findViewById(R.id.tv_record_count);
			 TextView tv_equipment=(TextView)layout.findViewById(R.id.tv_equipment);
			viewHolder = new ViewHolder();
			viewHolder.setTv_username(tv_username);
			viewHolder.setTv_addDate(tv_date);
			viewHolder.setTv_company(tv_company);
			viewHolder.setTv_unit(tv_unit);
			viewHolder.setBtn_delete(btn_delete);
			viewHolder.setTv_record_count(tv_record_count);
			viewHolder.setTv_content(tv_content);
			viewHolder.setTv_equipment(tv_equipment);
			convertView = layout;
			convertView.setTag(viewHolder);
		}
		
		viewHolder.getTv_username().setText(workItem.getSendperson());
		viewHolder.getTv_addDate().setText(workItem.getCreatetime());
		viewHolder.getTv_company().setText(workItem.getCounty());
		viewHolder.getTv_unit().setText(workItem.getUnit());
		viewHolder.getTv_record_count().setText("共"+workItem.getRecordcount()+"条记录");
		viewHolder.getTv_content().setText(StringHelper.subString(workItem.getTaskname(), 15));
		viewHolder.getTv_equipment().setText(workItem.getUsermac());
		if(_userId!=null&&_userId.equals(workItem.getUserid())&&workItem.getAlreadyUpload()==0)
		{
			viewHolder.getBtn_delete().setVisibility(View.VISIBLE);
			viewHolder.getBtn_delete().setTag(workItem.getTicketnum());
			viewHolder.getBtn_delete().setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String taskNmu=(String)v.getTag();
					if(delClickListener!=null)
					{
						delClickListener.onDelete(taskNmu);
					}
				}
			});
		}
		else {
			viewHolder.getBtn_delete().setVisibility(View.GONE);
		}
		
		return convertView;
	}
	
	public void SetOnClickListener(DelClickListener delClickListener)
	{
		this.delClickListener=delClickListener;
	}



	public interface DelClickListener{
		void onDelete(String taskNum);
	}
	
	private class ViewHolder {
		private TextView tv_company;// 公司
		private TextView tv_unit;// 单位
		private TextView tv_username;// 用户名
		private TextView tv_addDate;//添加时间
		private TextView btn_delete;
		private TextView tv_record_count;
		private TextView tv_content;
		private TextView tv_equipment;
		
		public TextView getTv_equipment() {
			return tv_equipment;
		}
		public void setTv_equipment(TextView tv_equipment) {
			this.tv_equipment = tv_equipment;
		}
		public TextView getTv_username() {
			return tv_username;
		}
		public void setTv_username(TextView tv_username) {
			this.tv_username = tv_username;
		}
		public TextView getTv_company() {
			return tv_company;
		}
		public void setTv_company(TextView tv_company) {
			this.tv_company = tv_company;
		}
		public TextView getTv_unit() {
			return tv_unit;
		}
		public void setTv_unit(TextView tv_unit) {
			this.tv_unit = tv_unit;
		}
		public TextView getTv_addDate() {
			return tv_addDate;
		}
		public void setTv_addDate(TextView tv_addDate) {
			this.tv_addDate = tv_addDate;
		}
		public TextView getBtn_delete() {
			return btn_delete;
		}
		public void setBtn_delete(TextView btn_delete) {
			this.btn_delete = btn_delete;
		}
		public TextView getTv_record_count() {
			return tv_record_count;
		}
		public void setTv_record_count(TextView tv_record_count) {
			this.tv_record_count = tv_record_count;
		}
		public TextView getTv_content() {
			return tv_content;
		}
		public void setTv_content(TextView tv_content) {
			this.tv_content = tv_content;
		}
		
		
	}

}
