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



package com.learnncode.mediachooser.fragment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AutoCompleteTextView.Validator;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.learnncode.mediachooser.MediaChooserConstants;
import com.learnncode.mediachooser.MediaModel;
import com.learnncode.mediachooser.R;
import com.learnncode.mediachooser.adapter.GridViewAdapter;
import com.learnncode.mediachooser.adapter.ListViewAudioAdapter;

public class VideoFragment extends Fragment implements OnScrollListener {

	private final static Uri MEDIA_EXTERNAL_CONTENT_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	private final static String MEDIA_DATA = MediaStore.Audio.Media.DATA;

	private ListViewAudioAdapter mAudioAdapter;
	private ListView mListView;
	private Cursor mCursor;
	private int mDataColumnIndex;
	private int mDisplayIndex;
	private ArrayList<String> mSelectedItems = new ArrayList<String>();
	private ArrayList<MediaModel> mGalleryModelList;
	private View mView;
	private SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	//private OnVideoSelectedListener mCallback;


	// Container Activity must implement this interface
	public interface OnVideoSelectedListener {
		public void onVideoSelected(int count);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
//		try {
//			mCallback = (OnVideoSelectedListener) activity;
//		} catch (ClassCastException e) {
//			throw new ClassCastException(activity.toString() + " must implement OnVideoSelectedListener");
//		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public VideoFragment(){
		setRetainInstance(true);
	}


	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if(mView == null){
			mView = inflater.inflate(R.layout.view_list_layout_audio_chooser, container, false);

			mListView = (ListView)mView.findViewById(R.id.lv_audio);

			initVideos();

		}else{
			((ViewGroup) mView.getParent()).removeView(mView);
			if(mAudioAdapter == null || mAudioAdapter.getCount() == 0){
				Toast.makeText(getActivity(), getActivity().getString(R.string.no_media_file_available), Toast.LENGTH_SHORT).show();
			}
		}

		return mView;
	};


	private void initVideos(String bucketName) {

		try {
			final String orderBy = MediaStore.Audio.Media.DATE_ADDED;
			String searchParams = null;
			searchParams = "bucket_display_name = \"" + bucketName + "\"";

			final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Video.Media._ID};
			mCursor = getActivity().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, columns, searchParams, null, orderBy + " DESC");
			setAdapter();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initVideos() {

		try {
			final String orderBy = MediaStore.Audio.Media.DATE_ADDED;
			//Here we set up a string array of the thumbnail ID column we want to get back

			String [] proj ={
//					VideoColumns.BUCKET_ID,
//					VideoColumns.BUCKET_DISPLAY_NAME,
//					VideoColumns.DATA,
					AudioColumns.ALBUM_ID,
					AudioColumns.DISPLAY_NAME,
					AudioColumns.DATA,
					AudioColumns.DATE_ADDED
				};

			mCursor =  getActivity().getContentResolver().query(MEDIA_EXTERNAL_CONTENT_URI, proj, null,null, orderBy + " DESC");
			setAdapter();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void setAdapter() {
		int count = mCursor.getCount();

		if(count > 0){
			mDataColumnIndex = mCursor.getColumnIndex(MEDIA_DATA);
			mDisplayIndex=mCursor.getColumnIndex(AudioColumns.DISPLAY_NAME);
			int addDateIndex=mCursor.getColumnIndex(AudioColumns.DATE_ADDED);
			//move position to first element
			mCursor.moveToFirst();
			long dateInfo=0;
			mGalleryModelList = new ArrayList<MediaModel>();
			for(int i= 0; i < count; i++) {
				mCursor.moveToPosition(i);
				String url = mCursor.getString(mDataColumnIndex);
				MediaModel mediaModel= new MediaModel(url, false);
				mediaModel.setDisplayName(mCursor.getString(mDisplayIndex));
				dateInfo=mCursor.getLong(addDateIndex)*1000;
				String strdate=simpleDateFormat.format(new Date(dateInfo));
				mediaModel.setDate_added(strdate);
				mGalleryModelList.add(mediaModel);
			}


			mAudioAdapter =  new ListViewAudioAdapter(getActivity(), 0, mGalleryModelList, true);
			mAudioAdapter.videoFragment = this;
			mListView.setAdapter(mAudioAdapter);
			mListView.setOnScrollListener(this);
		}else{
			Toast.makeText(getActivity(), getActivity().getString(R.string.no_media_file_available), Toast.LENGTH_SHORT).show();

		}


		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				GridViewAdapter adapter = (GridViewAdapter) parent.getAdapter();
				MediaModel galleryModel = (MediaModel) adapter.getItem(position);
				File file = new File(galleryModel.url);
				return false;
			}
		});

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// update the mStatus of each category in the adapter
				ListViewAudioAdapter adapter = (ListViewAudioAdapter) parent.getAdapter();
				MediaModel galleryModel = adapter.getItem(position);

				
				
				Intent intent = new Intent();
				intent.putExtra("fileUrl", galleryModel.url);
				intent.putExtra("fileType","audio");
				getActivity().setResult(Activity.RESULT_OK, intent);
				getActivity().finish();

//				if (mCallback != null) {
//					mCallback.onVideoSelected(mSelectedItems.size());
//					Intent intent = new Intent();
//					intent.putStringArrayListExtra("list", mSelectedItems);
//					getActivity().setResult(Activity.RESULT_OK, intent);
//				}

			}
		});

	}

	public void addItem(String item) {
		if(mAudioAdapter != null){
			MediaModel model = new MediaModel(item, false);
			mGalleryModelList.add(0, model);
			mAudioAdapter.notifyDataSetChanged();
		}else{
			initVideos();
		}
	}


	public ListViewAudioAdapter getAdapter() {
		if (mAudioAdapter != null) {
			return mAudioAdapter;
		}
		return null;
	}

	public ArrayList<String> getSelectedVideoList() {
		return mSelectedItems;
	}

	public void onScrollStateChanged(AbsListView view, int scrollState) {
		//		if (view.getId() == android.R.id.list) {
		if (view == mListView) {
			// Set scrolling to true only if the user has flinged the
			// ListView away, hence we skip downloading a series
			// of unnecessary bitmaps that the user probably
			// just want to skip anyways. If we scroll slowly it
			// will still download bitmaps - that means
			// that the application won't wait for the user
			// to lift its finger off the screen in order to
			// download.
			if (scrollState == SCROLL_STATE_FLING) {
				//chk
			} else {
				mAudioAdapter.notifyDataSetChanged();
			}
		}
	}

	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

	}


}

