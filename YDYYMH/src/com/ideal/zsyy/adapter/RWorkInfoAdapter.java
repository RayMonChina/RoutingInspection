package com.ideal.zsyy.adapter;

import java.io.File;
import java.util.List;

import com.ideal.zsyy.Config;
import com.ideal.zsyy.entity.RImageTagInfo;
import com.ideal.zsyy.entity.RWorkMediaInfo;
import com.ideal.zsyy.utils.DialogCirleProgress;
import com.ideal.zsyy.utils.FileUtils;
import com.shenrenkeji.intelcheck.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import okhttp3.Call;

public class RWorkInfoAdapter extends BaseAdapter {

	private Context _context;
	private List<RWorkMediaInfo> mediaItems = null;
	private LayoutInflater inflater;
	private ImageView currImage=null;
	private RImageTagInfo currTagInfo=null;
	private File currFile=null;
	DialogCirleProgress cirleProgress=null;
	public RWorkInfoAdapter(Context context, List<RWorkMediaInfo> mediaList) {
		this._context = context;
		this.mediaItems = mediaList;
		cirleProgress=new DialogCirleProgress(_context);
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (mediaItems != null) {
			return mediaItems.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if (mediaItems != null && mediaItems.size() > position) {
			return mediaItems.get(position);
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
		final RWorkMediaInfo mediaInfo = mediaItems.get(position);
		if (convertView != null) {
			if (convertView.getTag() != null) {
				viewHolder = (ViewHolder) convertView.getTag();
			}
		} else {
			LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.r_work_info_item, null);
			TextView tv_work_info_date = (TextView) layout.findViewById(R.id.tv_work_info_date);// 户名
			TextView tv_work_info_step = (TextView) layout.findViewById(R.id.tv_work_info_step);// 时间
			ImageView img_work_info = (ImageView) layout.findViewById(R.id.img_work_info);// 用户编码
			TextView tv_location_info = (TextView) layout.findViewById(R.id.tv_location_info);// 单位

			viewHolder = new ViewHolder();
			viewHolder.setImg_work_info(img_work_info);
			viewHolder.setTv_location_info(tv_location_info);
			viewHolder.setTv_work_info_date(tv_work_info_date);
			viewHolder.setTv_work_info_step(tv_work_info_step);
			convertView = layout;
			convertView.setTag(viewHolder);
		}
		RImageTagInfo tagInfo=new RImageTagInfo();
		String fileUrl = mediaInfo.getPicpath();
		String fileName = "";
		if (fileUrl != null) {
			String[] arrDirs = fileUrl.split("/");
			fileName = arrDirs[arrDirs.length - 1];
		}
		if ("图片".equals(mediaInfo.getPickind())) {
			File file = new File(FileUtils.SDPATH + "image/" + fileName);
			tagInfo.setFilePath(FileUtils.SDPATH + "image/" + fileName);
			if (file.exists()) {
				viewHolder.getImg_work_info().setImageBitmap(BitmapFactory.decodeFile(tagInfo.getFilePath()));
			} else {
				viewHolder.getImg_work_info().setImageResource(R.drawable.defualtimg0);
			}
			viewHolder.getImg_work_info().setTag(tagInfo);
			tagInfo.setFileType("图片");
		} else if ("录音".equals(mediaInfo.getPickind())) {
			File file = new File(FileUtils.SDPATH + "audio/" + fileName);
			tagInfo.setFilePath(FileUtils.SDPATH + "audio/" + fileName);
			tagInfo.setFileType("录音");
			if(file.exists())
			{
				viewHolder.getImg_work_info().setImageResource(R.drawable.mp3_ok);
			}
			else {
				viewHolder.getImg_work_info().setImageResource(R.drawable.mp3);
			}
			viewHolder.getImg_work_info().setTag(tagInfo);
		}
		currTagInfo=tagInfo;
		viewHolder.getTv_location_info().setText(mediaInfo.getGpsaddress());
		viewHolder.getTv_work_info_date().setText(mediaInfo.getCreatetime());
		viewHolder.getTv_work_info_step().setText(mediaInfo.getGjdmc());
		
		viewHolder.getImg_work_info().setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				currImage=(ImageView)v;
				RImageTagInfo imageInfo=(RImageTagInfo)v.getTag();
				File fileImg=new File(imageInfo.getFilePath());
				if(fileImg.exists())
				{
					Intent intent = new Intent(Intent.ACTION_VIEW);
					if("图片".equals(imageInfo.getFileType()))
					{
						intent.setDataAndType(Uri.fromFile(fileImg), "image/*");
					}
					else if("录音".equals(imageInfo.getFileType())){
						intent.setDataAndType(Uri.fromFile(fileImg), "audio/*");
					}
					_context.startActivity(intent);
				}
				else {
					downLoadFile(mediaInfo.getPicpath(),imageInfo.getFileType(),(ImageView)v);
				}
			}
		});
		return convertView;
	}
	
	Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				if(currFile!=null&&currTagInfo!=null)
				{
					Intent intent = new Intent(Intent.ACTION_VIEW);
					if("图片".equals(currTagInfo.getFileType()))
					{
						currImage.setImageBitmap(BitmapFactory.decodeFile(currTagInfo.getFilePath()));
						intent.setDataAndType(Uri.fromFile(currFile), "image/*");
					}
					else if("录音".equals(currTagInfo.getFileType())){
						intent.setDataAndType(Uri.fromFile(currFile), "audio/mp3");
						currImage.setImageResource(R.drawable.mp3_ok);
					}
					_context.startActivity(intent);
				}
				break;

			default:
				break;
			}
		}
		
	};
	
	//下载文件
	private void downLoadFile(String fileUrl,final String fileType,final ImageView imageView)
	{
		String desPath="";
		String fileName="";
		if(fileUrl==null||"".equals(fileUrl)||fileType==null||"".equals(fileType))
		{
			return;
		}
		cirleProgress.showProcessDialog();
		String[] arrNames=fileUrl.split("/");
		fileName=arrNames[arrNames.length-1];
		desPath=FileUtils.SDPATH;
		if("图片".equals(fileType))
		{
			desPath=desPath+"image/";
		}
		else if("录音".equals(fileType)){
			desPath=desPath+"audio/";
		}
		final String fullFileName=desPath+fileName;
		OkHttpUtils.get().url(Config.apiHost+fileUrl.substring(1))//
        .build().execute(new FileCallBack(desPath,fileName) {
			
			@Override
			public void onResponse(File arg0, int arg1) {
				// TODO Auto-generated method stub
				cirleProgress.hideProcessDialog();
				currFile=arg0;
				new Handler(){

					@Override
					public void handleMessage(Message msg) {
						// TODO Auto-generated method stub
						super.handleMessage(msg);
						switch (msg.what) {
						case 1:
							Intent intent = new Intent(Intent.ACTION_VIEW);
							if("图片".equals(fileType))
							{
								imageView.setImageBitmap(BitmapFactory.decodeFile(fullFileName));
								intent.setDataAndType(Uri.fromFile(currFile), "image/*");
							}
							else if("录音".equals(fileType)){
								intent.setDataAndType(Uri.fromFile(currFile), "audio/mp3");
								imageView.setImageResource(R.drawable.mp3_ok);
							}
							_context.startActivity(intent);
							break;

						default:
							break;
						}
					}
					
				}.sendEmptyMessage(1);
			}
			
			@Override
			public void onError(Call arg0, Exception arg1, int arg2) {
				// TODO Auto-generated method stub
				Log.e("get file error",arg1.getMessage());
				cirleProgress.hideProcessDialog();
				Toast.makeText(_context, arg1.getMessage(),Toast.LENGTH_SHORT).show();
			}
			
			
		});
	}
	
	

	private class ViewHolder {
		private TextView tv_work_info_date;// 日期
		private TextView tv_work_info_step;// 步骤
		private ImageView img_work_info;// media
		private TextView tv_location_info;// 位置信息

		public TextView getTv_work_info_date() {
			return tv_work_info_date;
		}

		public void setTv_work_info_date(TextView tv_work_info_date) {
			this.tv_work_info_date = tv_work_info_date;
		}

		public TextView getTv_work_info_step() {
			return tv_work_info_step;
		}

		public void setTv_work_info_step(TextView tv_work_info_step) {
			this.tv_work_info_step = tv_work_info_step;
		}

		public ImageView getImg_work_info() {
			return img_work_info;
		}

		public void setImg_work_info(ImageView img_work_info) {
			this.img_work_info = img_work_info;
		}

		public TextView getTv_location_info() {
			return tv_location_info;
		}

		public void setTv_location_info(TextView tv_location_info) {
			this.tv_location_info = tv_location_info;
		}
	}

}
