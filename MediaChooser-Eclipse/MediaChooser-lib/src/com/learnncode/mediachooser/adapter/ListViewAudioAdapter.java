/*
 * Copyright 2013 - learnNcode (learnncode@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


package com.learnncode.mediachooser.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.learnncode.mediachooser.MediaModel;
import com.learnncode.mediachooser.R;
import com.learnncode.mediachooser.async.ImageLoadAsync;
import com.learnncode.mediachooser.async.MediaAsync;
import com.learnncode.mediachooser.async.VideoLoadAsync;
import com.learnncode.mediachooser.fragment.VideoFragment;

public class ListViewAudioAdapter extends ArrayAdapter<MediaModel> {
	public VideoFragment videoFragment;  

	private Context mContext;
	private List<MediaModel> mGalleryModelList;
	private int mWidth;
	private boolean mIsFromVideo;
	LayoutInflater viewInflater;
	

	public ListViewAudioAdapter(Context context, int resource, List<MediaModel> categories, boolean isFromVideo) {
		super(context, resource, categories);
		mGalleryModelList = categories;
		mContext          = context;
		mIsFromVideo      = isFromVideo;
		viewInflater = LayoutInflater.from(mContext);
	}

	public int getCount() {
		return mGalleryModelList.size();
	}

	@Override
	public MediaModel getItem(int position) {
		return mGalleryModelList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;

		if (convertView == null) {

			
			convertView = viewInflater.inflate(R.layout.view_listview_item_audio_chooser, parent, false);
			holder = new ViewHolder();
			holder.setTv_file_Name((TextView) convertView.findViewById(R.id.tv_audio_name));
			holder.setTv_date((TextView)convertView.findViewById(R.id.tv_date));
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if(mGalleryModelList.size()>0)
		{
			MediaModel mediaModel=mGalleryModelList.get(position);
			holder.tv_file_Name.setText(mediaModel.displayName);
			holder.tv_date.setText(mediaModel.getDate_added());
		}
		

		return convertView;
	}

	class ViewHolder {
		private TextView tv_file_Name;

		private TextView tv_date;
		public TextView getTv_file_Name() {
			return tv_file_Name;
		}

		public void setTv_file_Name(TextView tv_file_Name) {
			this.tv_file_Name = tv_file_Name;
		}

		public TextView getTv_date() {
			return tv_date;
		}

		public void setTv_date(TextView tv_date) {
			this.tv_date = tv_date;
		}
		
	}

}
