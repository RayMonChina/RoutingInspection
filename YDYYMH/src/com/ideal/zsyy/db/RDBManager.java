package com.ideal.zsyy.db;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.ideal.zsyy.entity.RLocalFileMapping;
import com.ideal.zsyy.entity.RWorkMediaInfo;
import com.ideal.zsyy.entity.RWorkStepInfo;
import com.ideal.zsyy.entity.RWorkType;
import com.ideal.zsyy.entity.WPointItem;
import com.ideal.zsyy.entity.WaterPriceInfo;
import com.ideal.zsyy.response.RworkItemRes;
import com.ideal.zsyy.service.PreferencesService;
import com.ideal.zsyy.utils.DateUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class RDBManager {
	private Context _conteContext;
	SQLiteDatabase db = null;
	WdbHelper dbHelper = null;
	private static List<WaterPriceInfo> waterPriceInfos = new ArrayList<WaterPriceInfo>();
	private PreferencesService preferencesService;
	private String UserAccount = "";
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public RDBManager(Context context) {
		this._conteContext = context;
		preferencesService = new PreferencesService(context);
		UserAccount = preferencesService.getLoginInfo().get("loginName").toString();
		dbHelper = new WdbHelper(_conteContext, UserAccount);
		db = dbHelper.getWritableDatabase();
	}

	// 添加行走路径
	public void AddWalkPoint(WPointItem point) {
		if (point == null) {
			return;
		}
		ContentValues cValues = new ContentValues();
		cValues.put("Latitude", point.getLatitude());
		cValues.put("Longitude", point.getLongitude());
		cValues.put("UserId", point.getUserId());
		cValues.put("UserName", point.getUserName());
		cValues.put("AddDate", point.getAddDate());
		cValues.put("alreadyUpload", 1);
		cValues.put("Address", point.getAddress());
		db.insert("TB_LPoint", "UserId", cValues);
	}

	// 添加任务
	public void AddWorkItems(List<RworkItemRes> workItems) {
		if (workItems != null) {
			for (RworkItemRes itm : workItems) {
				itm.setAlreadyUpload(1);
				itm.setIsDownload(1);
				AddWorkItem(itm);
			}
		}
	}
	// 添加任务
		public void AddWorkItemsAndRemoveOld(List<RworkItemRes> workItems) {
			if (workItems != null) {
				for (RworkItemRes itm : workItems) {
					itm.setAlreadyUpload(1);
					itm.setIsDownload(1);
					removeWorkByNum(itm.getTicketnum());
					AddWorkItem(itm);
				}
			}
		}

	// 添加任务
	public void AddWorkItem(RworkItemRes workItem) {
		if (workItem == null) {
			return;
		}
		ContentValues cValues = new ContentValues();
		cValues.put("id", workItem.getId());
		cValues.put("ticketnum", workItem.getTicketnum());
		cValues.put("remark", workItem.getRemark());
		cValues.put("createtime", DateUtils.changeDateFormat(workItem.getCreatetime()));
		cValues.put("addDate", workItem.getCreatetime());
		cValues.put("sendperson", workItem.getSendperson());
		cValues.put("alreadyUpload", workItem.getAlreadyUpload());
		cValues.put("city", workItem.getCity());
		cValues.put("county", workItem.getCounty());
		cValues.put("unit", workItem.getUnit());
		cValues.put("state", workItem.getState());
		cValues.put("userid", workItem.getUserid());
		cValues.put("taskname", workItem.getTaskname());
		cValues.put("worktype", workItem.getWorktype());
		cValues.put("volclass", workItem.getVolclass());
		cValues.put("isDownLoad", workItem.getIsDownload());
		cValues.put("gjdmc", workItem.getGjdmc());
		cValues.put("recordcount", workItem.getRecordcount());
		cValues.put("gjdmc", workItem.getGjdmc());
		cValues.put("peiheren", workItem.getPeiheren());
		db.insert("TB_Works", "id", cValues);
	}

	// 添加工作
	public List<RworkItemRes> GetWorkItems(int start, int limit) {
		List<RworkItemRes> retList = new ArrayList<RworkItemRes>();
		String strSql = "select * from TB_Works order by createtime desc limit " + limit + " offset " + start;
		Cursor cursor = db.rawQuery(strSql, null);
		while (cursor.moveToNext()) {
			RworkItemRes rWorkItem = new RworkItemRes();
			rWorkItem.setId(cursor.getString(cursor.getColumnIndex("id")));
			rWorkItem.setTicketnum(cursor.getString(cursor.getColumnIndex("ticketnum")));
			rWorkItem.setRemark(cursor.getString(cursor.getColumnIndex("remark")));
			rWorkItem.setCreatetime(cursor.getString(cursor.getColumnIndex("createtime")));
			rWorkItem.setCreatetime(cursor.getString(cursor.getColumnIndex("addDate")));
			rWorkItem.setSendperson(cursor.getString(cursor.getColumnIndex("sendperson")));
			rWorkItem.setCity(cursor.getString(cursor.getColumnIndex("city")));
			rWorkItem.setCounty(cursor.getString(cursor.getColumnIndex("county")));
			rWorkItem.setUnit(cursor.getString(cursor.getColumnIndex("unit")));
			rWorkItem.setState(cursor.getString(cursor.getColumnIndex("state")));
			rWorkItem.setUserid(cursor.getString(cursor.getColumnIndex("userid")));
			rWorkItem.setTaskname(cursor.getString(cursor.getColumnIndex("taskname")));
			rWorkItem.setAlreadyUpload(cursor.getInt(cursor.getColumnIndex("alreadyUpload")));
			rWorkItem.setWorktype(cursor.getString(cursor.getColumnIndex("worktype")));
			rWorkItem.setVolclass(cursor.getString(cursor.getColumnIndex("volclass")));
			rWorkItem.setGjdmc(cursor.getString(cursor.getColumnIndex("gjdmc")));
			rWorkItem.setRecordcount(cursor.getInt(cursor.getColumnIndex("recordcount")));
			rWorkItem.setPeiheren(cursor.getString(cursor.getColumnIndex("peiheren")));
			retList.add(rWorkItem);
		}
		return retList;
	}

	public int unLoadWorkCount(String userId) {
		if (userId == null || "".equals(userId)) {
			return 0;
		}
		String strSql = "select count(1) as cnum from TB_Works where alreadyUpload=0 and userid=?";
		Cursor cursor = db.rawQuery(strSql, new String[] { userId });
		if (cursor.moveToNext()) {
			return cursor.getInt(0);
		}
		return 0;
	}

	// 未上传任务
	public List<RworkItemRes> GetUnLoadWorks(String userId) {
		List<RworkItemRes> retList = new ArrayList<RworkItemRes>();
		if (userId == null || "".equals(userId)) {
			return retList;
		}
		String strSql = "select * from TB_Works where alreadyUpload=0 and userid=?";
		Cursor cursor = db.rawQuery(strSql, new String[] { userId });
		while (cursor.moveToNext()) {
			RworkItemRes rWorkItem = new RworkItemRes();
			rWorkItem.setId(cursor.getString(cursor.getColumnIndex("id")));
			rWorkItem.setTicketnum(cursor.getString(cursor.getColumnIndex("ticketnum")));
			rWorkItem.setRemark(cursor.getString(cursor.getColumnIndex("remark")));
			rWorkItem.setCreatetime(cursor.getString(cursor.getColumnIndex("createtime")));
			rWorkItem.setCreatetime(cursor.getString(cursor.getColumnIndex("addDate")));
			rWorkItem.setSendperson(cursor.getString(cursor.getColumnIndex("sendperson")));
			rWorkItem.setCity(cursor.getString(cursor.getColumnIndex("city")));
			rWorkItem.setCounty(cursor.getString(cursor.getColumnIndex("county")));
			rWorkItem.setUnit(cursor.getString(cursor.getColumnIndex("unit")));
			rWorkItem.setState(cursor.getString(cursor.getColumnIndex("state")));
			rWorkItem.setUserid(cursor.getString(cursor.getColumnIndex("userid")));
			rWorkItem.setTaskname(cursor.getString(cursor.getColumnIndex("taskname")));
			rWorkItem.setAlreadyUpload(cursor.getInt(cursor.getColumnIndex("alreadyUpload")));
			rWorkItem.setWorktype(cursor.getString(cursor.getColumnIndex("worktype")));
			rWorkItem.setVolclass(cursor.getString(cursor.getColumnIndex("volclass")));
			rWorkItem.setGjdmc(cursor.getString(cursor.getColumnIndex("gjdmc")));
			rWorkItem.setRecordcount(cursor.getInt(cursor.getColumnIndex("recordcount")));
			rWorkItem.setPeiheren(cursor.getString(cursor.getColumnIndex("peiheren")));
			retList.add(rWorkItem);
		}
		return retList;
	}

	public int unLoadMediaCount(String userId) {
		if (userId == null || "".equals(userId)) {
			return 0;
		}
		String strSql = "select count(1) as cnum from TB_MediaInfo where alreadyUpload=0 and userid=?";
		Cursor cursor = db.rawQuery(strSql, new String[] { userId });
		if (cursor.moveToNext()) {
			return cursor.getInt(0);
		}
		return 0;
	}

	/// 媒体文件
	public List<RWorkMediaInfo> unLoadMediaList(String userId) {
		List<RWorkMediaInfo> retList = new ArrayList<RWorkMediaInfo>();
		if (userId == null || "".equals(userId)) {
			return retList;
		}
		String strSql = "select * from TB_MediaInfo where alreadyUpload=0 and userid=?";
		Cursor cursor = db.rawQuery(strSql, new String[] { userId });
		while (cursor.moveToNext()) {
			RWorkMediaInfo mediaItem = new RWorkMediaInfo();
			mediaItem.setId(cursor.getString(cursor.getColumnIndex("id")));
			mediaItem.setTicketnum(cursor.getString(cursor.getColumnIndex("ticketnum")));
			mediaItem.setPickind(cursor.getString(cursor.getColumnIndex("pickind")));
			mediaItem.setGpsaddress(cursor.getString(cursor.getColumnIndex("gpsaddress")));
			mediaItem.setGpstime(cursor.getString(cursor.getColumnIndex("gpstime")));
			mediaItem.setLon(cursor.getDouble(cursor.getColumnIndex("lon")));
			mediaItem.setLat(cursor.getDouble(cursor.getColumnIndex("lat")));
			mediaItem.setPicpath(cursor.getString(cursor.getColumnIndex("picpath")));
			mediaItem.setUserid(cursor.getString(cursor.getColumnIndex("userid")));
			mediaItem.setGjdmc(cursor.getString(cursor.getColumnIndex("gjdmc")));
			mediaItem.setCreatetime(cursor.getString(cursor.getColumnIndex("createtime")));
			mediaItem.setRemark(cursor.getString(cursor.getColumnIndex("remark")));
			mediaItem.setAlreadyUpload(cursor.getInt(cursor.getColumnIndex("alreadyUpload")));
			retList.add(mediaItem);
		}
		return retList;
	}

	public void removeWorkByNum(String taskNum) {
		if (taskNum == null || "".equals(taskNum)) {
			return;
		}
		db.delete("TB_Works", "ticketnum=?", new String[] { taskNum });
	}

	public void removeMediaByNum(String taskNum) {
		if (taskNum == null || "".equals(taskNum)) {
			return;
		}
		db.delete("TB_MediaInfo", "ticketnum=?", new String[] { taskNum });
	}

	// 根据任务编号获取任务信息
	public RworkItemRes getWorkItemBytaskNum(String taskNum) {
		if (taskNum == null || "".equals(taskNum.trim())) {
			return null;
		}
		String strSql = "select * from TB_Works where ticketnum=? ";
		Cursor cursor = db.rawQuery(strSql, new String[] { taskNum });
		RworkItemRes rWorkItem = null;
		if (cursor.moveToNext()) {
			rWorkItem = new RworkItemRes();
			rWorkItem.setId(cursor.getString(cursor.getColumnIndex("id")));
			rWorkItem.setTicketnum(cursor.getString(cursor.getColumnIndex("ticketnum")));
			rWorkItem.setRemark(cursor.getString(cursor.getColumnIndex("remark")));
			rWorkItem.setCreatetime(cursor.getString(cursor.getColumnIndex("createtime")));
			rWorkItem.setCreatetime(cursor.getString(cursor.getColumnIndex("addDate")));
			rWorkItem.setSendperson(cursor.getString(cursor.getColumnIndex("sendperson")));
			rWorkItem.setCity(cursor.getString(cursor.getColumnIndex("city")));
			rWorkItem.setCounty(cursor.getString(cursor.getColumnIndex("county")));
			rWorkItem.setUnit(cursor.getString(cursor.getColumnIndex("unit")));
			rWorkItem.setState(cursor.getString(cursor.getColumnIndex("state")));
			rWorkItem.setUserid(cursor.getString(cursor.getColumnIndex("userid")));
			rWorkItem.setTaskname(cursor.getString(cursor.getColumnIndex("taskname")));
			rWorkItem.setAlreadyUpload(cursor.getInt(cursor.getColumnIndex("alreadyUpload")));
			rWorkItem.setWorktype(cursor.getString(cursor.getColumnIndex("worktype")));
			rWorkItem.setVolclass(cursor.getString(cursor.getColumnIndex("volclass")));
			rWorkItem.setGjdmc(cursor.getString(cursor.getColumnIndex("gjdmc")));
			rWorkItem.setPeiheren(cursor.getString(cursor.getColumnIndex("peiheren")));
			rWorkItem.setRecordcount(cursor.getInt(cursor.getColumnIndex("recordcount")));
		}

		return rWorkItem;
	}

	public void updateWorkInfo(RworkItemRes workItem) {
		if (workItem == null) {
			return;
		}
		ContentValues cValues = new ContentValues();
		cValues.put("alreadyUpload", workItem.getAlreadyUpload());
		cValues.put("recordcount", workItem.getRecordcount());
		cValues.put("gjdmc", workItem.getGjdmc());
		cValues.put("taskname", workItem.getTaskname());
		cValues.put("worktype", workItem.getWorktype());
		cValues.put("volclass", workItem.getVolclass());
		cValues.put("ticketnum", workItem.getTicketnum());
		cValues.put("peiheren", workItem.getPeiheren());
		cValues.put("remark", workItem.getRemark());
		db.update("TB_Works", cValues, "id=?", new String[] { workItem.getId() });
	}

	// 删除已上传数据
	public void removeAlreadyData() {
		db.delete("TB_Works", "alreadyUpload=1", null);
		db.delete("TB_MediaInfo", "alreadyUpload=1", null);
	}

	// 删除最后一次上传的影音文件
	public void removeCurrmedia(String taskNum) {
		if (taskNum == null || "".equals(taskNum)) {
			db.delete("TB_MediaInfo", "alreadyUpload=1 and islast=1 ", null);
		} else {
			db.delete("TB_MediaInfo", "alreadyUpload=1 and islast=1 and ticketnum=?", new String[] { taskNum });
		}
	}

	// 删除已上传的文件
	public void removeMediaInfo() {
		db.delete("TB_MediaInfo", "alreadyUpload=1", null);
	}

	// 刪除文件
	public void removeMediaList(List<RWorkMediaInfo> mediaList) {
		if (mediaList == null) {
			return;
		}
		for (RWorkMediaInfo info : mediaList) {
			removeMediaById(info.getId());
		}
	}

	public void removeMediaById(String fileId) {
		if (fileId == null || "".equals(fileId)) {
			return;
		}
		db.delete("TB_MediaInfo", "id=?", new String[] { fileId });
	}

	// 添加文件
	public void AddMediaInfos(List<RWorkMediaInfo> mediaInfos, String ticketNum) {
		if (mediaInfos != null && mediaInfos.size() > 0) {
			for (RWorkMediaInfo info : mediaInfos) {
				if (info.getTicketnum() == null || "".equals(info.getTicketnum()))
					info.setTicketnum(ticketNum);
				info.setAlreadyUpload(1);
				if (info.getId() == null || "".equals(info.getId()))
					info.setId(UUID.randomUUID().toString());
				addMediaInfo(info);
			}
		}
	}

	// 添加媒体信息
	public void addMediaInfo(RWorkMediaInfo mediaInfo) {
		if (mediaInfo == null) {
			return;
		}
		ContentValues cValues = new ContentValues();
		cValues.put("id", mediaInfo.getId());
		cValues.put("ticketnum", mediaInfo.getTicketnum());
		cValues.put("pickind", mediaInfo.getPickind());
		cValues.put("gpsaddress", mediaInfo.getGpsaddress());
		cValues.put("gpstime", DateUtils.changeDateFormat(mediaInfo.getGpstime()));
		cValues.put("lon", mediaInfo.getLon());
		cValues.put("lat", mediaInfo.getLat());
		cValues.put("picpath", mediaInfo.getPicpath());
		cValues.put("userid", mediaInfo.getUserid());
		cValues.put("gjdmc", mediaInfo.getGjdmc());
		cValues.put("createtime", DateUtils.changeDateFormat(mediaInfo.getCreatetime()));
		cValues.put("remark", mediaInfo.getRemark());
		cValues.put("alreadyUpload", mediaInfo.getAlreadyUpload());
		this.db.insert("TB_MediaInfo", "id", cValues);
		RemoveMediaMapData(mediaInfo.getId());
		
		RLocalFileMapping fileMap=new RLocalFileMapping();
		fileMap.setAdddate(simpleDateFormat.format(new Date()));
		fileMap.setId(mediaInfo.getId());
		fileMap.setLocalFilePath(mediaInfo.getLocalFilePath());
		AddFileMapping(fileMap);
	}

	//
	public void updateMediaInfoAll(RWorkMediaInfo mediaInfo) {
		if (mediaInfo == null) {
			return;
		}
		ContentValues cValues = new ContentValues();
		cValues.put("ticketnum", mediaInfo.getTicketnum());
		cValues.put("pickind", mediaInfo.getPickind());
		cValues.put("gpsaddress", mediaInfo.getGpsaddress());
		cValues.put("gpstime", mediaInfo.getGpstime());
		cValues.put("lon", mediaInfo.getLon());
		cValues.put("lat", mediaInfo.getLat());
		cValues.put("picpath", mediaInfo.getPicpath());
		cValues.put("userid", mediaInfo.getUserid());
		cValues.put("gjdmc", mediaInfo.getGjdmc());
		cValues.put("createtime", DateUtils.changeDateFormat(mediaInfo.getCreatetime()));
		cValues.put("remark", mediaInfo.getRemark());
		cValues.put("alreadyUpload", mediaInfo.getAlreadyUpload());
		this.db.update("TB_MediaInfo", cValues, "id=?", new String[] { mediaInfo.getId() });
	}

	public List<RWorkMediaInfo> GetMediaInfoByTaskNum(String taskNum) {

		List<RWorkMediaInfo> retList = new ArrayList<RWorkMediaInfo>();
		if (taskNum == null || "".equals(taskNum)) {
			return retList;
		}
		String strSql = "select media.*,mp.localFilePath from tb_mediainfo media left join tb_localfilemap mp on media.id=mp.id where media.ticketnum=?";
		Cursor cursor = db.rawQuery(strSql, new String[] { taskNum });
		while (cursor.moveToNext()) {
			RWorkMediaInfo mediaItem = new RWorkMediaInfo();
			mediaItem.setId(cursor.getString(cursor.getColumnIndex("id")));
			mediaItem.setTicketnum(cursor.getString(cursor.getColumnIndex("ticketnum")));
			mediaItem.setPickind(cursor.getString(cursor.getColumnIndex("pickind")));
			mediaItem.setGpsaddress(cursor.getString(cursor.getColumnIndex("gpsaddress")));
			mediaItem.setGpstime(cursor.getString(cursor.getColumnIndex("gpstime")));
			mediaItem.setLon(cursor.getDouble(cursor.getColumnIndex("lon")));
			mediaItem.setLat(cursor.getDouble(cursor.getColumnIndex("lat")));
			mediaItem.setPicpath(cursor.getString(cursor.getColumnIndex("picpath")));
			mediaItem.setUserid(cursor.getString(cursor.getColumnIndex("userid")));
			mediaItem.setGjdmc(cursor.getString(cursor.getColumnIndex("gjdmc")));
			mediaItem.setCreatetime(cursor.getString(cursor.getColumnIndex("createtime")));
			mediaItem.setRemark(cursor.getString(cursor.getColumnIndex("remark")));
			mediaItem.setAlreadyUpload(cursor.getInt(cursor.getColumnIndex("alreadyUpload")));
			mediaItem.setLocalFilePath(cursor.getString(cursor.getColumnIndex("localFilePath")));
			retList.add(mediaItem);
		}
		return retList;
	}

	// 删除media信息
	public void removeMediaInfo(String mediaId) {
		if (mediaId == null || "".equals(mediaId.trim())) {
			return;
		}

		this.db.delete("TB_MediaInfo", "id=?", new String[] { mediaId });
	}

	// 更新媒体信息
	public void updateMediaInfo(RWorkMediaInfo mediaInfo) {
		if (mediaInfo == null) {
			return;
		}
		ContentValues cValues = new ContentValues();
		cValues.put("gjdmc", mediaInfo.getGjdmc());
		cValues.put("remark", mediaInfo.getRemark());
		cValues.put("alreadyUpload", mediaInfo.getAlreadyUpload());
		this.db.update("TB_MediaInfo", cValues, "id=?", new String[] { mediaInfo.getId() });
	}
	//

	// 获取行走路径列表
	public List<WPointItem> GetWalkPoints() {
		List<WPointItem> retList = new ArrayList<WPointItem>();
		String strSql = "select * from TB_LPoint where alreadyUpload=1 ";
		Cursor cursor = db.rawQuery(strSql, null);
		while (cursor.moveToNext()) {
			WPointItem wItem = new WPointItem();
			wItem.setAddDate(cursor.getString(cursor.getColumnIndex("AddDate")));
			wItem.setLatitude(cursor.getDouble(cursor.getColumnIndex("Latitude")));
			wItem.setLongitude(cursor.getDouble(cursor.getColumnIndex("Longitude")));
			wItem.setUserId(cursor.getString(cursor.getColumnIndex("UserId")));
			wItem.setUserName(cursor.getString(cursor.getColumnIndex("UserName")));
			retList.add(wItem);
		}
		return retList;
	}

	public List<WPointItem> GetDisWalkPoints() {
		List<WPointItem> retList = new ArrayList<WPointItem>();
		String strSql = "select * from TB_LPoint order by PId desc limit 1000";
		Cursor cursor = db.rawQuery(strSql, null);
		while (cursor.moveToNext()) {
			WPointItem wItem = new WPointItem();
			wItem.setAddDate(cursor.getString(cursor.getColumnIndex("AddDate")));
			wItem.setLatitude(cursor.getDouble(cursor.getColumnIndex("Latitude")));
			wItem.setLongitude(cursor.getDouble(cursor.getColumnIndex("Longitude")));
			wItem.setUserId(cursor.getString(cursor.getColumnIndex("UserId")));
			wItem.setUserName(cursor.getString(cursor.getColumnIndex("UserName")));
			retList.add(wItem);
		}
		return retList;
	}

	// 更新行走路径标志位
	public void UpdateWPointTag() {
		ContentValues cValues = new ContentValues();
		cValues.put("alreadyUpload", 0);
		db.update("TB_LPoint", cValues, "alreadyUpload=1", null);
	}

	// 清空路径数据
	public void RestWalkPoint() {
		String strSql = " drop table IF EXISTS TB_LPoint; CREATE TABLE IF NOT EXISTS TB_LPoint(" + " Latitude real,"
				+ " Longitude real,UserId varchar(50),UserName varchar(50),Address nvarchar(300),"
				+ " AddDate varchar(50),alreadyUpload TINYINT);";
		db.execSQL(strSql);
	}

	// 删除前一天数据
	public void RemoveOldData(String spDate) {
		String strSql = "delete from TB_LPoint where AddDate<'" + spDate + "'";
		db.execSQL(strSql);
	}

	// 工作类型
	public void AddWorkType(List<RWorkType> typeList) {
		if (typeList == null || typeList.size() == 0) {
			return;
		}
		for (RWorkType tp : typeList) {
			ContentValues cValues = new ContentValues();
			cValues.put("typeid", tp.getTypeid());
			cValues.put("typename", tp.getTypename());
			cValues.put("sortcode", tp.getSortcode());
			db.insert("TB_WorkType", "typeid", cValues);
		}
	}

	// 获取工作类型
	public List<RWorkType> GetWorkTypeList() {
		String strSql = "select * from TB_WorkType order by sortcode";
		Cursor cursor = db.rawQuery(strSql, null);
		List<RWorkType> workTypes = new ArrayList<RWorkType>();
		while (cursor.moveToNext()) {
			RWorkType workType = new RWorkType();
			workType.setSortcode(cursor.getString(cursor.getColumnIndex("sortcode")));
			workType.setTypeid(cursor.getString(cursor.getColumnIndex("typeid")));
			workType.setTypename(cursor.getString(cursor.getColumnIndex("typename")));
			workTypes.add(workType);
		}
		return workTypes;
	}

	// 电压等级
	public void AddVolClass(List<RWorkType> volList) {
		if (volList == null || volList.size() == 0) {
			return;
		}
		for (RWorkType tp : volList) {
			ContentValues cValues = new ContentValues();
			cValues.put("typeid", tp.getTypeid());
			cValues.put("typename", tp.getTypename());
			cValues.put("sortcode", tp.getSortcode());
			db.insert("TB_Volclass", "typeid", cValues);
		}
	}

	// 获取电压登记
	public List<RWorkType> GetVolClassList() {
		String strSql = "select * from TB_Volclass order by sortcode";
		Cursor cursor = db.rawQuery(strSql, null);
		List<RWorkType> workTypes = new ArrayList<RWorkType>();
		while (cursor.moveToNext()) {
			RWorkType workType = new RWorkType();
			workType.setSortcode(cursor.getString(cursor.getColumnIndex("sortcode")));
			workType.setTypeid(cursor.getString(cursor.getColumnIndex("typeid")));
			workType.setTypename(cursor.getString(cursor.getColumnIndex("typename")));
			workTypes.add(workType);
		}
		return workTypes;
	}
	//环节
	public void AddWorkSteps(List<RWorkStepInfo>steps)
	{
		if(steps==null||steps.size()==0)
		{
			return;
		}
		for(RWorkStepInfo step:steps)
		{
			ContentValues cValues = new ContentValues();
			cValues.put("id",step.getId());
			cValues.put("gjdmc",step.getGjdmc());
			cValues.put("remark",step.getRemark());
			cValues.put("sortcode",step.getSortcode());
			cValues.put("worktype",step.getWorktype());
			cValues.put("volclass",step.getVolclass());
			db.insert("TB_WorkStep", "id", cValues);
		}
	}
	
	//获取环节信息
	public List<RWorkStepInfo>GetSteps()
	{
		String strSql = "select * from TB_WorkStep order by sortcode";
		Cursor cursor = db.rawQuery(strSql, null);
		List<RWorkStepInfo> workSteps = new ArrayList<RWorkStepInfo>();
		while (cursor.moveToNext()) {
			RWorkStepInfo stepInfo = new RWorkStepInfo();
			stepInfo.setSortcode(cursor.getString(cursor.getColumnIndex("sortcode")));
			stepInfo.setGjdmc(cursor.getString(cursor.getColumnIndex("gjdmc")));
			stepInfo.setId(cursor.getString(cursor.getColumnIndex("id")));
			stepInfo.setRemark(cursor.getString(cursor.getColumnIndex("remark")));
			stepInfo.setWorktype(cursor.getString(cursor.getColumnIndex("worktype")));
			stepInfo.setVolclass(cursor.getString(cursor.getColumnIndex("volclass")));
			workSteps.add(stepInfo);
		}
		return workSteps;
	}
	
	public void RemoveBasicData()
	{
		String strSql="delete from TB_WorkType";
		db.execSQL(strSql);
		strSql="delete from TB_Volclass";
		db.execSQL(strSql);
		strSql="delete from TB_WorkStep";
		db.execSQL(strSql);
	}
	
	//添加本地文件路径
	public void AddFileMapping(RLocalFileMapping fileMap)
	{
		if(fileMap==null)
		{
			return;
		}
		ContentValues cValues=new ContentValues();
		cValues.put("id", fileMap.getId());
		cValues.put("localFilePath",fileMap.getLocalFilePath());
		cValues.put("adddate", fileMap.getAdddate());
		db.insert("TB_LocalFileMap","id", cValues);
	}
	//移除本地映射
	public void RemoveMediaMapData(String mid)
	{
		if(mid==null||"".equals(mid))
		{
			return;
		}
		db.delete("TB_LocalFileMap","id=?", new String[]{mid});
	}

	public void resetData() {
		if (dbHelper != null) {
			dbHelper.ResetTable(db);
		}

	}

	// 获取欠费用用户信息

	public void closeDB() {
		// 释放数据库资源
		db.close();
	}
}
