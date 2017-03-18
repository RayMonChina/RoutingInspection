package com.ideal.zsyy.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ideal.zsyy.Config;
import com.ideal.zsyy.adapter.RWorkItemAdapter;
import com.ideal.zsyy.adapter.RWorkItemAdapter.DelClickListener;
import com.ideal.zsyy.db.RDBManager;
import com.ideal.zsyy.entity.RWorkStepInfo;
import com.ideal.zsyy.entity.RWorkType;
import com.ideal.zsyy.response.RBaseRes;
import com.ideal.zsyy.response.RworkItemRes;
import com.ideal.zsyy.service.PreferencesService;
import com.ideal.zsyy.utils.DialogCirleProgress;
import com.ideal.zsyy.utils.FileUtils;
import com.ideal.zsyy.utils.HttpUtil;
import com.ideal.zsyy.utils.UtilNetWork;
import com.ideal.zsyy.view.LeftFragment;
import com.ideal.zsyy.view.PullToRefreshLayout;
import com.ideal.zsyy.view.PullToRefreshLayout.OnRefreshListener;
import com.ideal.zsyy.view.PullableListView;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.shenrenkeji.intelcheck.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.GetBuilder;
import com.zhy.http.okhttp.callback.Callback;

/**
 * 
 * @author yechao
 * @说明 通过改变不同的值来改变滑动菜单不同的效果
 */
public class RSlidMainActivity extends SlidingFragmentActivity implements UncaughtExceptionHandler {

	Button btn_report = null;
	Button btn_people = null;
	PullableListView pListView;
	private PullToRefreshLayout pullRefresh;
	private RDBManager dbManager = null;
	private int pageIndex = 1;
	private int pageSize = 7;
	private List<RworkItemRes> workItems = new ArrayList<RworkItemRes>();
	private RWorkItemAdapter workAdapter;
	private PreferencesService preference;
	private String userId;
	private ZsyyApplication application = null;
	private String taskNum = "";
	AlertDialog.Builder builder;

	final DialogCirleProgress dCirleProgress = new DialogCirleProgress(RSlidMainActivity.this);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(this);

		// 设置标题栏的标题
		setTitle("SlidingMenu Properties");

		// 设置是否能够使用ActionBar来滑动
		setSlidingActionBarEnabled(true);

		// 设置主界面视图
		setContentView(R.layout.r_main_slidingmenu);

		dbManager = new RDBManager(RSlidMainActivity.this);
		preference = new PreferencesService(RSlidMainActivity.this);
		userId = preference.getLoginInfo().get("use_id").toString();
		application = (ZsyyApplication) getApplication();

		// 初始化滑动菜单
		initSlidingMenu(savedInstanceState);

		// 初始化组件
		initView();

		initData();
	}

	private long exitTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			} else {
				preference.clearLogin();
				if (application != null) {
					stopService(application.iLocalService);
				}
				finish();
				System.exit(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 初始化滑动菜单
	 */
	private void initSlidingMenu(Bundle savedInstanceState) {
		// 设置滑动菜单的视图
		setBehindContentView(R.layout.menu_frame);
		getSupportFragmentManager().beginTransaction().replace(R.id.menu_frame, new LeftFragment()).commit();

		// 实例化滑动菜单对象
		SlidingMenu sm = getSlidingMenu();
		// 设置滑动阴影的宽度
		sm.setShadowWidthRes(R.dimen.shadow_width);
		// 设置滑动阴影的图像资源
		sm.setShadowDrawable(R.drawable.shadow);
		// 设置滑动菜单视图的宽度
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		// 设置渐入渐出效果的值
		sm.setFadeDegree(0.35f);
		// 设置触摸屏幕的模式
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
	}

	/**
	 * 初始化组件
	 */
	private void initView() {
		btn_report = (Button) findViewById(R.id.btn_r_report);
		btn_report.setOnClickListener(clickListener);
		btn_people = (Button) findViewById(R.id.btn_people);
		btn_people.setOnClickListener(clickListener);
		pullRefresh = (PullToRefreshLayout) findViewById(R.id.refresh_view);
		pullRefresh.setOnRefreshListener(refreshListener);
		pListView = (PullableListView) findViewById(R.id.content_view);
		pListView.setOnItemClickListener(itemClickListener);
		workAdapter = new RWorkItemAdapter(RSlidMainActivity.this, workItems, userId);
		pListView.setAdapter(workAdapter);

		workAdapter.SetOnClickListener(new DelClickListener() {

			@Override
			public void onDelete(String tkNum) {

				taskNum = tkNum;
				builder.show();
			}
		});

		builder = new AlertDialog.Builder(RSlidMainActivity.this);
		builder.setTitle("删除该条数据？");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (taskNum != null && !"".equals(taskNum)) {
					dbManager.removeWorkByNum(taskNum);
					dbManager.removeMediaByNum(taskNum);
					reloadData();
				} else {
					Toast.makeText(RSlidMainActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
				}
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

			}
		});

	}

	private void initData() {
		this.refreshData();
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// TODO Auto-generated method stub
			if (position < workItems.size()) {
				RworkItemRes workItemInfo = workItems.get(position);
				Intent intent_workDetail = null;
				if (userId.equals(workItemInfo.getUserid())) {
					intent_workDetail = new Intent(RSlidMainActivity.this, RWorkOrderActivity.class);
					intent_workDetail.putExtra("taskNum", workItemInfo.getTicketnum());

				} else {
					intent_workDetail = new Intent(RSlidMainActivity.this, RWorkInfoDetailActivity.class);
					intent_workDetail.putExtra("taskNum", workItemInfo.getTicketnum());
				}
				startActivity(intent_workDetail);
			}
		}

	};

	private OnRefreshListener refreshListener = new OnRefreshListener() {

		@Override
		public void onRefresh(final PullToRefreshLayout pullToRefreshLayout) {
			// TODO Auto-generated method stub
			refreshData();
			// 下拉刷新操作
			Log.i("finish", "refresh");
			new Handler() {
				@Override
				public void handleMessage(Message msg) {
					// 千万别忘了告诉控件刷新完毕了哦！
					// refreshData();
					pullRefresh.refreshFinish(PullToRefreshLayout.SUCCEED);
					Log.i("finish", "finish");
				}
			}.sendEmptyMessageDelayed(0, 2000);
		}

		@Override
		public void onLoadMore(final PullToRefreshLayout pullToRefreshLayout) {
			// TODO Auto-generated method stub

			Log.i("finish", "onLoadMore");
			// 加载操作
			new Handler() {
				@Override
				public void handleMessage(Message msg) {
					loadMore();
					// 千万别忘了告诉控件加载完毕了哦！
					pullRefresh.loadmoreFinish(PullToRefreshLayout.SUCCEED);
				}
			}.sendEmptyMessageDelayed(0, 2000);
		}
	};

	private OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btn_r_report:
				Intent intentWork = new Intent(RSlidMainActivity.this, RWorkOrderActivity.class);
				startActivityForResult(intentWork, 2);
				break;
			case R.id.btn_people:
				Intent intent_people = new Intent(RSlidMainActivity.this, RPersonStatusActivity.class);
				startActivity(intent_people);
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 菜单按钮点击事件，通过点击ActionBar的Home图标按钮来打开滑动菜单
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		// TODO Auto-generated method stub
		Log.i("AAA", "uncaughtException   " + ex);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (dbManager != null) {
			dbManager.closeDB();
		}
	}

	/// 加载更多
	public void loadMore() {
		List<RworkItemRes>nextItems=dbManager.GetWorkItems((pageIndex - 1) * pageSize, pageSize);
		if(nextItems==null||nextItems.size()==0)
		{
			refreshNextage();
			return;
		}
		workItems.addAll(nextItems);
		workAdapter.notifyDataSetChanged();
		pageIndex++;
	}

	// 重新加载数据
	public void reloadData() {
		workItems.clear();
		workItems.addAll(dbManager.GetWorkItems(0, pageIndex * pageSize));
		workAdapter.notifyDataSetChanged();
	}
	
	public void refreshData() {
		// dbManager.removeAlreadyData();
		if (!HttpUtil.checkNet(RSlidMainActivity.this)) {
			Toast.makeText(RSlidMainActivity.this, "请检查网络！", Toast.LENGTH_SHORT).show();
			return;
		}

		OkHttpUtils.get().url(Config.Apiurl)
		.addParams("action", "querytask")
		.addParams("pageIndex", "1")
		.addParams("pageSize", "70")
		.addParams("userid", userId).build().execute(new Callback<List<RworkItemRes>>() {

					@Override
					public void onBefore(Request request, int id) {
						// TODO Auto-generated method stub
						super.onBefore(request, id);
						Log.i("loginRequest", request.url().toString());
						Toast.makeText(RSlidMainActivity.this, "正在加载数据！", Toast.LENGTH_SHORT).show();
						dCirleProgress.showProcessDialog();
					}

					@Override
					public void onError(Call arg0, Exception arg1, int arg2) {
						// TODO Auto-generated method stub
						dCirleProgress.hideProcessDialog();
						Toast.makeText(RSlidMainActivity.this, "加载数据失败：" + arg1.getMessage(), Toast.LENGTH_SHORT)
								.show();
						return;
					}

					@Override
					public void onResponse(List<RworkItemRes> arg0, int arg1) {
						// TODO Auto-generated method stub
						dCirleProgress.hideProcessDialog();
						dbManager.removeAlreadyData();
						dbManager.removeCurrmedia("");
						dbManager.AddWorkItems(arg0);
						workItems.clear();
						List<RworkItemRes> allWorkItems = dbManager.GetWorkItems(0, pageIndex * pageSize);
						if (allWorkItems == null) {
							allWorkItems = new ArrayList<RworkItemRes>();
						}
						workItems.addAll(allWorkItems);
						workAdapter.notifyDataSetChanged();
						pageIndex++;
						//Toast.makeText(RSlidMainActivity.this, "数据加载成功！", Toast.LENGTH_SHORT).show();
						dbManager.RemoveBasicData();
						handlerMessage.sendEmptyMessage(2);
					}

					@Override
					public List<RworkItemRes> parseNetworkResponse(Response arg0, int arg1) throws Exception {
						// TODO Auto-generated method stub
						String bodyStr = arg0.body().string();
						Log.i("response", bodyStr);
						bodyStr = bodyStr.replace("\\", "/");
						Type tp = new TypeToken<RBaseRes<List<RworkItemRes>>>() {
						}.getType();
						RBaseRes<List<RworkItemRes>> baseRes = new Gson().fromJson(bodyStr, tp);
						String retCode = baseRes.getStatus();
						if (!"success".equalsIgnoreCase(retCode)) {
							Log.i("workData faile:", arg0.request().url().toString());
							throw new Exception("download work data error");
						}
						if (baseRes != null && baseRes.getData() != null) {
							return baseRes.getData();
						}
						return null;
					}

				});
	}

	//下拉加载
	public void refreshNextage() {
		// dbManager.removeAlreadyData();
		if (!HttpUtil.checkNet(RSlidMainActivity.this)) {
			Toast.makeText(RSlidMainActivity.this, "请检查网络！", Toast.LENGTH_SHORT).show();
			return;
		}

		OkHttpUtils.get().url(Config.Apiurl)
		.addParams("action", "querytask")
		.addParams("pageIndex",pageIndex+"")
		.addParams("pageSize",pageSize+"")
		.addParams("userid", userId).build().execute(new Callback<List<RworkItemRes>>() {

					@Override
					public void onBefore(Request request, int id) {
						// TODO Auto-generated method stub
						super.onBefore(request, id);
						Log.i("loginRequest", request.url().toString());
						//Toast.makeText(RSlidMainActivity.this, "正在加载数据！", Toast.LENGTH_SHORT).show();
						dCirleProgress.showProcessDialog();
					}

					@Override
					public void onError(Call arg0, Exception arg1, int arg2) {
						// TODO Auto-generated method stub
						dCirleProgress.hideProcessDialog();
						Toast.makeText(RSlidMainActivity.this, "加载数据失败：" + arg1.getMessage(), Toast.LENGTH_SHORT)
								.show();
						return;
					}

					@Override
					public void onResponse(List<RworkItemRes> arg0, int arg1) {
						// TODO Auto-generated method stub
						dCirleProgress.hideProcessDialog();
						if(arg0==null||arg0.size()==0)
						{
							Toast.makeText(RSlidMainActivity.this, "未查到数据", Toast.LENGTH_SHORT).show();
							return;
						}
						dbManager.AddWorkItemsAndRemoveOld(arg0);
						List<RworkItemRes> allWorkItems = dbManager.GetWorkItems((pageIndex-1)*pageSize, pageSize);
						if (allWorkItems == null) {
							allWorkItems = new ArrayList<RworkItemRes>();
						}
						workItems.addAll(allWorkItems);
						workAdapter.notifyDataSetChanged();
						pageIndex++;
					}

					@Override
					public List<RworkItemRes> parseNetworkResponse(Response arg0, int arg1) throws Exception {
						// TODO Auto-generated method stub
						String bodyStr = arg0.body().string();
						Log.i("response", bodyStr);
						bodyStr = bodyStr.replace("\\", "/");
						Type tp = new TypeToken<RBaseRes<List<RworkItemRes>>>() {
						}.getType();
						RBaseRes<List<RworkItemRes>> baseRes = new Gson().fromJson(bodyStr, tp);
						String retCode = baseRes.getStatus();
						if (!"success".equalsIgnoreCase(retCode)) {
							Log.i("workData faile:", arg0.request().url().toString());
							throw new Exception("download work data error");
						}
						if (baseRes != null && baseRes.getData() != null) {
							return baseRes.getData();
						}
						return null;
					}

				});
	}

	public Handler handlerMessage = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 1:
				workAdapter.notifyDataSetChanged();
				break;
			case 2:
				initWokType();
				break;
			case 3:
				initDyLevel();
				break;
			case 4:
				getWorkStep("", "");
				break;
			default:
				break;
			}
		}

	};

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		reloadData();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 2:
			// if (resultCode == RESULT_OK)
			// refreshData();
			break;

		default:
			break;
		}
	}

	// ================*
	public void initWokType() {
		if (!HttpUtil.checkNet(RSlidMainActivity.this)) {
			Toast.makeText(RSlidMainActivity.this, "请检查网络！", Toast.LENGTH_SHORT).show();
			return;
		}
		OkHttpUtils.get().url(Config.Apiurl).addParams("action", "getworktype").build()
				.execute(new Callback<List<RWorkType>>() {

					@Override
					public void onBefore(Request request, int id) {
						// TODO Auto-generated method stub
						super.onBefore(request, id);
						Log.i("loginRequest", request.url().toString());
						dCirleProgress.showProcessDialog();
					}

					@Override
					public void onError(Call arg0, Exception arg1, int arg2) {
						// TODO Auto-generated method stub
						dCirleProgress.hideProcessDialog();
						Toast.makeText(RSlidMainActivity.this,"WorkType 加载失败！"+arg1.getMessage(),Toast.LENGTH_SHORT).show();
						return;
					}

					@Override
					public void onResponse(List<RWorkType> arg0, int arg1) {
						// TODO Auto-generated method stub
						dCirleProgress.hideProcessDialog();
						dbManager.AddWorkType(arg0);
						handlerMessage.sendEmptyMessage(3);
					}

					@Override
					public List<RWorkType> parseNetworkResponse(Response arg0, int arg1) throws Exception {
						// TODO Auto-generated method stub
						String bodyStr = arg0.body().string();
						Log.i("response", bodyStr);
						Type tp = new TypeToken<RBaseRes<List<RWorkType>>>() {
						}.getType();
						RBaseRes<List<RWorkType>> baseRes = new Gson().fromJson(bodyStr, tp);
						if (baseRes != null && baseRes.getData() != null) {
							return baseRes.getData();
						}
						return null;
					}

				});

	}/// 工作类型

	public void initDyLevel() {
		OkHttpUtils.get().url(Config.Apiurl).addParams("action", "getvolclass").build()
				.execute(new Callback<List<RWorkType>>() {

					@Override
					public void onBefore(Request request, int id) {
						// TODO Auto-generated method stub
						super.onBefore(request, id);
						Log.i("loginRequest", request.url().toString());
						dCirleProgress.showProcessDialog();
					}

					@Override
					public void onError(Call arg0, Exception arg1, int arg2) {
						// TODO Auto-generated method stub
						dCirleProgress.hideProcessDialog();
						Toast.makeText(RSlidMainActivity.this, arg1.getMessage(), Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(List<RWorkType> arg0, int arg1) {
						// TODO Auto-generated method stub
						dCirleProgress.hideProcessDialog();
						dbManager.AddVolClass(arg0);
						handlerMessage.sendEmptyMessage(4);
					}

					@Override
					public List<RWorkType> parseNetworkResponse(Response arg0, int arg1) throws Exception {
						// TODO Auto-generated method stub
						String bodyStr = arg0.body().string();
						Log.i("response", bodyStr);
						Type tp = new TypeToken<RBaseRes<List<RWorkType>>>() {
						}.getType();
						RBaseRes<List<RWorkType>> baseRes = new Gson().fromJson(bodyStr, tp);
						if (baseRes != null && baseRes.getData() != null) {
							return baseRes.getData();
						}
						return null;
					}

				});

	}/// 电压等级

	public void getWorkStep(String workType, String dyLevel) {
		GetBuilder getBuilder = OkHttpUtils.get().url(Config.Apiurl).addParams("action", "querykeypoint");
		if (workType != null && !"".equals(workType.trim())) {
			getBuilder.addParams("worktype", workType);
		}
		if (dyLevel != null && !"".equals(dyLevel.trim())) {
			getBuilder.addParams("volclass", dyLevel);
		}
		getBuilder.build().execute(new Callback<List<RWorkStepInfo>>() {

			@Override
			public void onBefore(Request request, int id) {
				// TODO Auto-generated method stub
				super.onBefore(request, id);
				Log.i("loginRequest", request.url().toString());
				dCirleProgress.showProcessDialog();
			}

			@Override
			public void onError(Call arg0, Exception arg1, int arg2) {
				// TODO Auto-generated method stub
				dCirleProgress.hideProcessDialog();
			}

			@Override
			public void onResponse(List<RWorkStepInfo> arg0, int arg1) {
				// TODO Auto-generated method stub
				dCirleProgress.hideProcessDialog();
				dbManager.AddWorkSteps(arg0);
				Toast.makeText(RSlidMainActivity.this,"数据加载成功！",Toast.LENGTH_SHORT).show();
			}

			@Override
			public List<RWorkStepInfo> parseNetworkResponse(Response arg0, int arg1) throws Exception {
				// TODO Auto-generated method stub
				String bodyStr = arg0.body().string();
				Log.i("response", bodyStr);
				Type tp = new TypeToken<RBaseRes<List<RWorkStepInfo>>>() {
				}.getType();
				RBaseRes<List<RWorkStepInfo>> baseRes = new Gson().fromJson(bodyStr, tp);
				if (baseRes != null && baseRes.getData() != null) {
					return baseRes.getData();
				}
				return null;
			}
		});
	}///
	// ================
	
	private void checkUnUPloadWord()
	{
		int unWorkCount=dbManager.unLoadWorkCount(userId);
		int unMediaCount=dbManager.unLoadMediaCount(userId);
		if(unMediaCount<=0&&unWorkCount<=0)
		{
			return;
		}
		if(!HttpUtil.checkNet(RSlidMainActivity.this))
		{
			return;
		}
		String tipMsg="";
		if(unWorkCount>0)
		{
			tipMsg="未上传工作"+unWorkCount+"项\n";
		}
		if(unMediaCount>0)
		{
			tipMsg+="未上传文件"+unMediaCount+"个";
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(
				RSlidMainActivity.this);
		builder.setTitle(tipMsg);
		builder.setPositiveButton("上传",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						
					}
				});
		builder.setNegativeButton("取消",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						// TODO Auto-generated method stub

					}
				});
		builder.show();
	}
}
