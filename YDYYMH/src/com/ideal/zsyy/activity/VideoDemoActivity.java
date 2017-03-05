package com.ideal.zsyy.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;

import com.shenrenkeji.intelcheck.R;

import android.app.Activity;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class VideoDemoActivity extends Activity implements OnClickListener, SurfaceHolder.Callback {
	private int mCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;// 默认后置摄像头
	// 程序中的两个按钮
	ImageButton recode;
	ImageButton stop;
	// 系统的视频文件
	File videoFile;
	MediaRecorder mRecorder;
	// 显示视频预览的SurfaceView
	Camera camera;
	private int recLen = 0;
	// 记录是否正在进行录制
	private boolean isRecoding = false;
	private Camera.Size mSize = null;// 相机的尺寸
	private SurfaceHolder mSurfaceHolder;

	private SurfaceView mSurfaceView;
	private TextView tv_time;
	private static final SparseIntArray orientations = new SparseIntArray();// 手机旋转对应的调整角度

	private Chronometer chm_video;
	
	Random random=new Random();

	static {
		orientations.append(Surface.ROTATION_0, 90);
		orientations.append(Surface.ROTATION_90, 0);
		orientations.append(Surface.ROTATION_180, 270);
		orientations.append(Surface.ROTATION_270, 180);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.r_video_demo);
		this.initViews();
	}

	TimerTask task = new TimerTask() {
		@Override
		public void run() {

			runOnUiThread(new Runnable() { // UI thread
				@Override
				public void run() {
					tv_time.setText("" + recLen);
					if (recLen >= 30) {
						stopRecord();
					}
					recLen++;
				}
			});
		}
	};

	private void initViews() {
		// 获取程序界面中的两个按钮
		recode = (ImageButton) findViewById(R.id.recode);
		stop = (ImageButton) findViewById(R.id.stop);
		tv_time = (TextView) findViewById(R.id.tv_time);
		// 让stop按钮不可用
		stop.setEnabled(false);
		// 为两个按钮的单击事件绑定监听器
		recode.setOnClickListener(this);
		stop.setOnClickListener(this);
		// 获取程序界面中的SurfaceView
		mSurfaceView = (SurfaceView) this.findViewById(R.id.sView);
		// 下面设置Surface不需要自己维护缓冲区
		mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		// 设置该组件让屏幕不会自动关闭
		mSurfaceView.getHolder().setKeepScreenOn(true);
		mSurfaceView.getHolder().addCallback(this);
		chm_video = (Chronometer) findViewById(R.id.chm_video);
		chm_video.setOnChronometerTickListener(chmTickListener);
		
	}

	private OnChronometerTickListener chmTickListener = new OnChronometerTickListener() {

		@Override
		public void onChronometerTick(Chronometer chronometer) {
			// TODO Auto-generated method stub
			if(chm_video!=null)
			{
				if(chm_video.getText().toString().equals("00:30"))
				{
					stopRecord();
					Toast.makeText(VideoDemoActivity.this, "视频最大长度为30s", Toast.LENGTH_SHORT).show();
				}
				
			}
		}
	};

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub
		// 将holder，这个holder为开始在onCreate里面取得的holder，将它赋给mSurfaceHolder
		mSurfaceHolder = holder;
		if (camera == null) {
			return;
		}
		try {
			// 设置显示
			camera.setPreviewDisplay(holder);
			camera.startPreview();
		} catch (Exception e) {
			e.printStackTrace();
			releaseCamera();
			finish();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		mSurfaceHolder = holder;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		if (isRecoding && camera != null) {
			camera.lock();
		}
		mSurfaceView = null;
		mSurfaceHolder = null;
		releaseMediaRecorder();
		releaseCamera();
	}

	private void releaseMediaRecorder() {
		if (mRecorder != null) {
			mRecorder.release();
			mRecorder = null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		initCamer();
	}

	@Override
	public void onPause() {
		releaseCamera();
		super.onPause();
	}

	private void initCamer() {
		if (Camera.getNumberOfCameras() == 2) {
			camera = Camera.open(mCameraFacing);
		} else {
			camera = Camera.open();
		}

		Camera.Parameters parameters = camera.getParameters();

		if (mSize == null) {
			List<Camera.Size> vSizeList = parameters.getSupportedPreviewSizes();

			for (int num = 0; num < vSizeList.size(); num++) {
				Camera.Size size = vSizeList.get(num);

				if (size.width >= 800 && size.height >= 480) {
					this.mSize = size;
					break;
				}
			}
			mSize = vSizeList.get(0);

			List<String> focusModesList = parameters.getSupportedFocusModes();

			// 增加对聚焦模式的判断
			if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
				parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
			} else if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
				parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			}
			camera.setParameters(parameters);
		}
		int rotation = getWindowManager().getDefaultDisplay().getRotation();
		int orientation = orientations.get(rotation);
		camera.setDisplayOrientation(orientation);

	}

	private void releaseCamera() {
		// TODO Auto-generated method stub
		try {
			if (camera != null) {
				camera.stopPreview();
				camera.setPreviewCallback(null);
				camera.unlock();
				camera.release();
			}
		} catch (RuntimeException e) {
		} finally {
			camera = null;
		}
	}

	@Override
	public void onClick(View source) {
		// TODO Auto-generated method stub
		switch (source.getId()) {
		// 单击录制按钮
		case R.id.recode:
			if(!isRecoding)
			{
				if (!Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
					Toast.makeText(VideoDemoActivity.this, "SD卡不存在，请插入SD卡！", 5000).show();
					return;
				}
				try {
					String filePath=Environment.getExternalStorageDirectory()+"/rcheck/video/";
					File dir=new File(filePath);
					if(!dir.exists())
					{
						dir.mkdirs();
					}
					Log.i("VideoPath","file:"+dir.exists()+","+dir.getAbsolutePath());
					String fileName="v"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+random.nextInt(1000)+".mp4";
					// 创建保存录制视频的视频文件
					videoFile = new File(filePath+ fileName);
					// 创建MediaRecorder对象
					if (mRecorder == null) {
						mRecorder = new MediaRecorder();
					}

					if (camera != null) {
						camera.stopPreview();
						camera.unlock();
						mRecorder.setCamera(camera);
					}

					// 设置音频采集方式
					mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
					// 设置视频的采集方式
					mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
					// 设置文件的输出格式
					mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);// aac_adif，
																					// aac_adts，
																					// output_format_rtp_avp，
																					// output_format_mpeg2ts
																					// ，webm
					// 设置audio的编码格式
					mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
					// 设置video的编码格式
					mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
					// 设置录制的视频编码比特率
					mRecorder.setVideoEncodingBitRate(1024 * 1024);
					// 设置录制的视频帧率,注意文档的说明:
					mRecorder.setVideoFrameRate(30);
					// 设置要捕获的视频的宽度和高度
					mSurfaceHolder.setFixedSize(320, 240);// 最高只能设置640x480
					mRecorder.setVideoSize(320, 240);// 最高只能设置640x480
					// 设置记录会话的最大持续时间（毫秒）
					mRecorder.setMaxDuration(70 * 1000);
					mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
					// 每秒4帧
					// mRecorder.setVideoFrameRate(4);
					mRecorder.setOutputFile(videoFile.getAbsolutePath());
					// 指定使用SurfaceView来预览视频
					mRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
					mRecorder.prepare();
					// 开始录制
					mRecorder.start();
					System.out.println("----recoding----");
					// 让recorde按钮不可用
					//recode.setEnabled(false);
					// 让stop按钮可用
					//stop.setEnabled(true);
					isRecoding = true;
					this.chm_video.start();
					this.chm_video.setText("00:00");
					this.chm_video.setBase(SystemClock.elapsedRealtime());
					
					recode.setImageResource(R.drawable.stop);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				stopRecord();
			}
			
			break;
		// 单击停止按钮
		case R.id.stop:
			// 如果正在进行录制
			stopRecord();
			break;
		}
	}

	private void stopRecord() {
		if (isRecoding) {
			try {
				// 停止录制
				mRecorder.stop();
				// 重置
				mRecorder.reset();
			} catch (Exception e) {
				// TODO: handle exception
			}
			if (camera != null) {
				camera.stopPreview();
			}

			isRecoding = false;
			// 让record按钮可用
			recode.setEnabled(true);
			// 让stop按钮不可用
			stop.setEnabled(false);
			chm_video.stop();
			recLen = 0;
			recode.setImageResource(R.drawable.start);
		}
	}

	private class CameraSizeComparator implements Comparator<Camera.Size> {
		public int compare(Camera.Size lhs, Camera.Size rhs) {
			if (lhs.width == rhs.width) {
				return 0;
			} else if (lhs.width > rhs.width) {
				return 1;
			} else {
				return -1;
			}
		}
	}

}
