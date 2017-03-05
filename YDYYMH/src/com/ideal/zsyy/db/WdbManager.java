package com.ideal.zsyy.db;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.ideal.zsyy.activity.ZsyyApplication;
import com.ideal.zsyy.entity.FaultItem;
import com.ideal.zsyy.entity.LocationInfo;
import com.ideal.zsyy.entity.WAnalysisItem;
import com.ideal.zsyy.entity.WBBItem;
import com.ideal.zsyy.entity.WBBList;
import com.ideal.zsyy.entity.WCBUserEntity;
import com.ideal.zsyy.entity.WCBWaterChargeItem;
import com.ideal.zsyy.entity.WCustomerAdviceInfo;
import com.ideal.zsyy.entity.WFaultReportInfo;
import com.ideal.zsyy.entity.WPicItem;
import com.ideal.zsyy.entity.WPointItem;
import com.ideal.zsyy.entity.WUploadItem;
import com.ideal.zsyy.entity.WUserItem;
import com.ideal.zsyy.entity.WaterPriceInfo;
import com.ideal.zsyy.service.PreferencesService;
import com.ideal.zsyy.utils.WaterUtils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class WdbManager {
	private Context _conteContext;
	SQLiteDatabase db = null;
	WdbHelper dbHelper = null;
	private static List<WaterPriceInfo> waterPriceInfos = new ArrayList<WaterPriceInfo>();
	private PreferencesService preferencesService;
	private String UserAccount="";
	public WdbManager(Context context) {
		this._conteContext = context;
		preferencesService=new PreferencesService(context);
		UserAccount=preferencesService.getLoginInfo().get("loginName").toString();
		dbHelper = new WdbHelper(_conteContext,UserAccount);
		db = dbHelper.getWritableDatabase();
	}

	// 获取表本信息
	public List<String> GetBiaoBenNos() {
		List<String> retList = new ArrayList<String>();
		String strSql = "select BId,NoteNo,CBMonth,CBYear,CBUser,CBUserID from TB_BiaoBenInfo";// "select name from sqlite_master where type='table'";//
		Cursor cr = db.rawQuery(strSql, null);
		while (cr.moveToNext()) {
			retList.add(cr.getString(1));
		}
		return retList;
	}

	// 抄表
	public void ChaoBiao(WUserItem userItem) {
		if (userItem == null || userItem.getYhbm() == "") {
			return;
		}
		ContentValues cValues = new ContentValues();
		cValues.put("UserNo", userItem.getYhbm());
		cValues.put("ChaoBiaoTag", 2);
		cValues.put("CurrentMonthValue", userItem.getByzd());
		cValues.put("ChaoBiaoDate", userItem.getCbrq());
		cValues.put("Latitude", userItem.getLatitude());
		cValues.put("Longitude", userItem.getLongitude());
		cValues.put("PreMoney", userItem.getYcje());
		cValues.put("OweMoney", userItem.getLjqf());
		cValues.put("CurrMonthWNum", userItem.getSl());
		cValues.put("CurrMonthFee", userItem.getCurrentMonthFee());
		cValues.put("Latitude", userItem.getLatitude());
		cValues.put("Longitude", userItem.getLongitude());
		cValues.put("alreadyUpload", 1);
		db.update("TB_UserInfo", cValues, "UserNo=?",
				new String[] { userItem.getYhbm() });
		List<WPicItem> picItems = userItem.getPicItems();
		if (picItems != null && picItems.size() > 0) {
			db.delete("TB_PicInfo", "UserNo=? and PicType=1",
					new String[] { userItem.getYhbm() });
			for (WPicItem itm : picItems) {
				cValues = new ContentValues();
				cValues.put("UserNo", itm.getUserNo());
				cValues.put("NoteNO", itm.getNoteNo());
				cValues.put("PicName", itm.getPicName());
				cValues.put("PicPath", itm.getPicPath());
				cValues.put("AddDate", itm.getAddDate());
				cValues.put("AddUserId", itm.getAddUserId());
				cValues.put("AddUser", itm.getAddUser());
				cValues.put("PicType", itm.getPicType());
				cValues.put("Latitude", itm.getLatitude());
				cValues.put("Longitude", itm.getLongitude());
				cValues.put("alreadyUpload", itm.getAlreadyUpload());
				db.insert("TB_PicInfo", "UserNo", cValues);
			}
		}
	}

	// 收费
	public void ShouFei(WUserItem userItem) {
		if (userItem == null || userItem.getYhbm() == "") {
			return;
		}
		ContentValues cValues = new ContentValues();
		cValues.put("UserNo", userItem.getYhbm());
		cValues.put("PreMoney", userItem.getYcje());
		cValues.put("OweMoney", userItem.getLjqf());
		cValues.put("ShouFei", userItem.getBysf());
		cValues.put("ShouFeiDate", userItem.getSfrq());
		cValues.put("Latitude", userItem.getLatitude());
		cValues.put("Longitude", userItem.getLongitude());
		cValues.put("alreadyUpload", 1);
		db.update("TB_UserInfo", cValues, "UserNo=?",
				new String[] { userItem.getYhbm() });

	}

	// 获取表本信息
	public List<WBBItem> GetBiaoBenInfo() {
		List<WBBItem> retList = new ArrayList<WBBItem>();
		String strSql = "select BId,NoteNo,CBMonth,CBYear,CBUser,CBUserID,CustomerCount,PianNo,AreaNo,DuanNo from TB_BiaoBenInfo";
		Cursor cr = db.rawQuery(strSql, null);
		while (cr.moveToNext()) {
			WBBItem item = new WBBItem();
			item.setBId(cr.getInt(cr.getColumnIndex("BId")));
			item.setCBMonth(cr.getInt(cr.getColumnIndex("CBMonth")));
			item.setCBUser(cr.getString(cr.getColumnIndex("CBUser")));
			item.setCBUserID(cr.getString(cr.getColumnIndex("CBUserID")));
			item.setNoteNo(cr.getString(cr.getColumnIndex("NoteNo")));
			item.setCBYear(cr.getInt(cr.getColumnIndex("CBYear")));
			item.setCustomerCount(cr.getInt(cr.getColumnIndex("CustomerCount")));
			item.setPianNo(cr.getString(cr.getColumnIndex("PianNo")));
			item.setAreaNo(cr.getString(cr.getColumnIndex("AreaNo")));
			item.setDuanNo(cr.getString(cr.getColumnIndex("DuanNo")));
			retList.add(item);
		}
		return retList;
	}

	// 添加表本信息
	public void AddBiaoBenInfo(List<WBBItem> bbItems, String currUserId) {
		if (bbItems != null && bbItems.size() > 0) {
			db.delete("TB_BiaoBenInfo", "CBUserID=?",
					new String[] { currUserId });
			for (WBBItem itm : bbItems) {
				ContentValues cValues = new ContentValues();
				cValues.put("NoteNo", itm.getNoteNo());
				cValues.put("CBMonth", itm.getCBMonth());
				cValues.put("CBUser", itm.getCBUser());
				cValues.put("CBUserID", itm.getCBUserID());
				cValues.put("CBYear", itm.getCBYear());
				cValues.put("CustomerCount", itm.getCustomerCount());
				cValues.put("PianNo", itm.getPianNo());
				cValues.put("AreaNo", itm.getAreaNo());
				cValues.put("DuanNo", itm.getDuanNo());
				db.insert("TB_BiaoBenInfo", "NoteNo", cValues);
			}
		}
	}

	//获取表本信息
	public WBBItem GetSingleBBItem()
	{
		WBBItem item =null;
		String strSql = "select BId,NoteNo,CBMonth,CBYear,CBUser,CBUserID,CustomerCount," +
				" PianNo,AreaNo,DuanNo from TB_BiaoBenInfo limit 1";
		Cursor cr = db.rawQuery(strSql, null);
		if (cr.moveToNext()) {
			item = new WBBItem();
			item.setBId(cr.getInt(cr.getColumnIndex("BId")));
			item.setCBMonth(cr.getInt(cr.getColumnIndex("CBMonth")));
			item.setCBUser(cr.getString(cr.getColumnIndex("CBUser")));
			item.setCBUserID(cr.getString(cr.getColumnIndex("CBUserID")));
			item.setNoteNo(cr.getString(cr.getColumnIndex("NoteNo")));
			item.setCBYear(cr.getInt(cr.getColumnIndex("CBYear")));
			item.setCustomerCount(cr.getInt(cr.getColumnIndex("CustomerCount")));
			item.setPianNo(cr.getString(cr.getColumnIndex("PianNo")));
			item.setAreaNo(cr.getString(cr.getColumnIndex("AreaNo")));
			item.setDuanNo(cr.getString(cr.getColumnIndex("DuanNo")));
		}
		return item;
	}
	
	// 获取用户信息
	public List<WUserItem> getCustomerInfo(int seType, String keyWord) {
		List<WUserItem> retList = new ArrayList<WUserItem>();
		String strSql = "select * from TB_UserInfo ";
		String strWhere = "";
		switch (seType) {
		case 1:// 户号
			strWhere = " where UserNo=?";
			break;
		case 2:
			strWhere = " where StealNo=?";
			break;
		case 3:
			strWhere = " where UserFName like ?";
			keyWord = "%" + keyWord + "%";
			break;
		case 4:
			strWhere = " where Address like ?";
			keyWord = "%" + keyWord + "%";
			break;
		default:
			break;
		}
		strSql = strSql + strWhere;

		Cursor cursor = db.rawQuery(strSql, new String[] { keyWord });
		while (cursor.moveToNext()) {
			WUserItem wItem = new WUserItem();
			wItem.setBbh(cursor.getString(cursor.getColumnIndex("NoteNo")));
			wItem.setBgh(cursor.getString(cursor.getColumnIndex("StealNo")));
			wItem.setByzd(cursor.getDouble(cursor
					.getColumnIndex("CurrentMonthValue")));
			wItem.setCbbz(cursor.getInt(cursor.getColumnIndex("ChaoBiaoTag")));
			wItem.setDh(cursor.getString(cursor.getColumnIndex("Phone")));
			wItem.setDz(cursor.getString(cursor.getColumnIndex("Address")));
			wItem.setJglx(cursor.getString(cursor.getColumnIndex("PriceType")));
			wItem.setJglxName(cursor.getString(cursor
					.getColumnIndex("PriceTypeName")));
			wItem.setLjqf(cursor.getDouble(cursor.getColumnIndex("OweMoney")));
			wItem.setSl(0);
			wItem.setSysf(cursor.getDouble(cursor
					.getColumnIndex("LastMonthFee")));
			wItem.setSysl(cursor.getDouble(cursor
					.getColumnIndex("LastMonthWater")));
			wItem.setSyzd(cursor.getDouble(cursor
					.getColumnIndex("LastMonthValue")));
			wItem.setXm(cursor.getString(cursor.getColumnIndex("UserFName")));
			wItem.setYcje(cursor.getDouble(cursor.getColumnIndex("PreMoney")));
			wItem.setYhbm(cursor.getString(cursor.getColumnIndex("UserNo")));
			retList.add(wItem);
		}
		return retList;
	}

	// 获取用户信息
	public WUserItem GetUserItemByNo(String userNo) {
		if (userNo == null || userNo == "") {
			return null;
		}
		WUserItem retItem = null;
		String strSql = "select * from TB_UserInfo where UserNo=? limit 1 ";
		Cursor cursor = db.rawQuery(strSql, new String[] { userNo });
		if (cursor.moveToNext()) {
			retItem = new WUserItem();
			retItem.setBbh(cursor.getString(cursor.getColumnIndex("NoteNo")));
			retItem.setBgh(cursor.getString(cursor.getColumnIndex("StealNo")));
			retItem.setByzd(cursor.getDouble(cursor.getColumnIndex("CurrentMonthValue")));
			retItem.setCbbz(cursor.getInt(cursor.getColumnIndex("ChaoBiaoTag")));
			retItem.setDh(cursor.getString(cursor.getColumnIndex("Phone")));
			retItem.setDz(cursor.getString(cursor.getColumnIndex("Address")));
			retItem.setJglx(cursor.getString(cursor.getColumnIndex("PriceType")));
			retItem.setJglxName(cursor.getString(cursor.getColumnIndex("PriceTypeName")));
			retItem.setLjqf(cursor.getDouble(cursor.getColumnIndex("OweMoney")));
			retItem.setSysf(cursor.getDouble(cursor.getColumnIndex("LastMonthFee")));
			retItem.setSysl(cursor.getDouble(cursor.getColumnIndex("LastMonthWater")));
			retItem.setSyzd(cursor.getDouble(cursor.getColumnIndex("LastMonthValue")));
			retItem.setXm(cursor.getString(cursor.getColumnIndex("UserFName")));
			retItem.setYcje(cursor.getDouble(cursor.getColumnIndex("PreMoney")));
			retItem.setYhbm(cursor.getString(cursor.getColumnIndex("UserNo")));
			retItem.setBysf(cursor.getDouble(cursor.getColumnIndex("ShouFei")));
			retItem.setBid(cursor.getInt(cursor.getColumnIndex("BId")));
		}
		return retItem;
	}

	// 获取用户信息
	public WCBUserEntity GetWCBUserItemByNo(String stealNo) {
		if (stealNo == null || stealNo == "") {
			return null;
		}
		WCBUserEntity retItem = null;
		String strSql = "select * from TB_UserInfo where StealNo=? limit 1 ";
		Cursor cursor = db.rawQuery(strSql, new String[] { stealNo });
		if (cursor.moveToNext()) {
			retItem = new WCBUserEntity();
			retItem.setUserID(cursor.getInt(cursor.getColumnIndex("UserID")));
			retItem.setNoteNo(cursor.getString(cursor.getColumnIndex("NoteNo")));
			retItem.setStealNo(cursor.getString(cursor.getColumnIndex("StealNo")));
			retItem.setCurrentMonthValue(cursor.getDouble(cursor.getColumnIndex("CurrentMonthValue")));
			retItem.setChaoBiaoTag(cursor.getInt(cursor.getColumnIndex("ChaoBiaoTag")));
			retItem.setPhone(cursor.getString(cursor.getColumnIndex("Phone")));
			retItem.setAddress(cursor.getString(cursor.getColumnIndex("Address")));
			retItem.setWaterMeterPositionName(cursor.getString(cursor.getColumnIndex("WaterMeterPositionName")));
			retItem.setPriceType(cursor.getString(cursor.getColumnIndex("PriceType")));
			retItem.setPriceTypeName(cursor.getString(cursor.getColumnIndex("PriceTypeName")));
			retItem.setOweMoney(cursor.getDouble(cursor.getColumnIndex("OweMoney")));
			retItem.setLastMonthFee(cursor.getDouble(cursor.getColumnIndex("LastMonthFee")));
			retItem.setLastMonthWater(cursor.getDouble(cursor.getColumnIndex("LastMonthWater")));
			retItem.setLastMonthValue(cursor.getDouble(cursor.getColumnIndex("LastMonthValue")));
			retItem.setUserFName(cursor.getString(cursor.getColumnIndex("UserFName")));
			retItem.setPreMoney(cursor.getDouble(cursor.getColumnIndex("PreMoney")));
			retItem.setUserNo(cursor.getString(cursor.getColumnIndex("UserNo")));
			retItem.setShouFei(cursor.getDouble(cursor.getColumnIndex("ShouFei")));
			retItem.setCurrMonthWNum(cursor.getDouble(cursor.getColumnIndex("CurrMonthWNum")));
			retItem.setCurrMonthFee(cursor.getDouble(cursor.getColumnIndex("CurrMonthFee")));
			retItem.setOrChaoBiaoTag(cursor.getInt(cursor.getColumnIndex("OrChaoBiaoTag")));
			retItem.setLatitude(cursor.getDouble(cursor.getColumnIndex("Latitude")));
			retItem.setLongitude(cursor.getDouble(cursor.getColumnIndex("Longitude")));
			retItem.setStepPrice(cursor.getString(cursor.getColumnIndex("StepPrice")));
			retItem.setExtraPrice(cursor.getString(cursor.getColumnIndex("ExtraPrice")));
			retItem.setIsSummaryMeter(cursor.getInt(cursor.getColumnIndex("IsSummaryMeter")));
			retItem.setOrderNumber(cursor.getInt(cursor.getColumnIndex("OrderNumber")));
			retItem.setWaterFixValue(cursor.getDouble(cursor.getColumnIndex("WaterFixValue")));
			retItem.setNFCTag(cursor.getString(cursor.getColumnIndex("NFCTag")));
			retItem.setLastChaoBiaoDate(cursor.getString(cursor.getColumnIndex("LastChaoBiaoDate")));
			retItem.setStealID(cursor.getString(cursor.getColumnIndex("StealID")));
			retItem.setBId(cursor.getInt(cursor.getColumnIndex("BId")));
			retItem.setAvePrice(cursor.getDouble(cursor.getColumnIndex("avePrice")));
			retItem.setExtraCharge1(cursor.getDouble(cursor.getColumnIndex("extraCharge1")));
			retItem.setExtraCharge2(cursor.getDouble(cursor.getColumnIndex("extraCharge2")));
			retItem.setExtraChargePrice1(cursor.getDouble(cursor.getColumnIndex("extraChargePrice1")));
			retItem.setExtraChargePrice2(cursor.getDouble(cursor.getColumnIndex("extraChargePrice2")));
			retItem.setExtraTotalCharge(cursor.getDouble(cursor.getColumnIndex("extraTotalCharge")));
			retItem.setTotalCharge(cursor.getDouble(cursor.getColumnIndex("TotalCharge")));
			retItem.setReadMeterRecordId(cursor.getString(cursor.getColumnIndex("readMeterRecordId")));
			retItem.setWaterMeterParentID(cursor.getString(cursor.getColumnIndex("WaterMeterParentID")));
			retItem.setWaterUserchargeType(cursor.getString(cursor.getColumnIndex("waterUserchargeType")));
			retItem.setWaterUserTypeId(cursor.getString(cursor.getColumnIndex("waterUserTypeId")));
			retItem.setMemo1(cursor.getString(cursor.getColumnIndex("Memo1")));
		}
		return retItem;
	}
	
	public WCBUserEntity GetWCBUserItemByRecordId(String recordId) {
		if (recordId == null || recordId == "") {
			return null;
		}
		WCBUserEntity retItem = null;
		String strSql = "select * from TB_UserInfo where readMeterRecordId=? limit 1 ";
		Cursor cursor = db.rawQuery(strSql, new String[] { recordId });
		if (cursor.moveToNext()) {
			retItem = new WCBUserEntity();
			retItem.setUserID(cursor.getInt(cursor.getColumnIndex("UserID")));
			retItem.setNoteNo(cursor.getString(cursor.getColumnIndex("NoteNo")));
			retItem.setStealNo(cursor.getString(cursor.getColumnIndex("StealNo")));
			retItem.setCurrentMonthValue(cursor.getDouble(cursor.getColumnIndex("CurrentMonthValue")));
			retItem.setChaoBiaoTag(cursor.getInt(cursor.getColumnIndex("ChaoBiaoTag")));
			retItem.setPhone(cursor.getString(cursor.getColumnIndex("Phone")));
			retItem.setAddress(cursor.getString(cursor.getColumnIndex("Address")));
			retItem.setPriceType(cursor.getString(cursor.getColumnIndex("PriceType")));
			retItem.setPriceTypeName(cursor.getString(cursor.getColumnIndex("PriceTypeName")));
			retItem.setOweMoney(cursor.getDouble(cursor.getColumnIndex("OweMoney")));
			retItem.setLastMonthFee(cursor.getDouble(cursor.getColumnIndex("LastMonthFee")));
			retItem.setLastMonthWater(cursor.getDouble(cursor.getColumnIndex("LastMonthWater")));
			retItem.setLastMonthValue(cursor.getDouble(cursor.getColumnIndex("LastMonthValue")));
			retItem.setUserFName(cursor.getString(cursor.getColumnIndex("UserFName")));
			retItem.setPreMoney(cursor.getDouble(cursor.getColumnIndex("PreMoney")));
			retItem.setUserNo(cursor.getString(cursor.getColumnIndex("UserNo")));
			retItem.setShouFei(cursor.getDouble(cursor.getColumnIndex("ShouFei")));
			retItem.setCurrMonthWNum(cursor.getDouble(cursor.getColumnIndex("CurrMonthWNum")));
			retItem.setCurrMonthFee(cursor.getDouble(cursor.getColumnIndex("CurrMonthFee")));
			retItem.setOrChaoBiaoTag(cursor.getInt(cursor.getColumnIndex("OrChaoBiaoTag")));
			retItem.setLatitude(cursor.getDouble(cursor.getColumnIndex("Latitude")));
			retItem.setLongitude(cursor.getDouble(cursor.getColumnIndex("Longitude")));
			retItem.setStepPrice(cursor.getString(cursor.getColumnIndex("StepPrice")));
			retItem.setExtraPrice(cursor.getString(cursor.getColumnIndex("ExtraPrice")));
			retItem.setIsSummaryMeter(cursor.getInt(cursor.getColumnIndex("IsSummaryMeter")));
			retItem.setOrderNumber(cursor.getInt(cursor.getColumnIndex("OrderNumber")));
			retItem.setWaterFixValue(cursor.getDouble(cursor.getColumnIndex("WaterFixValue")));
			retItem.setNFCTag(cursor.getString(cursor.getColumnIndex("NFCTag")));
			retItem.setLastChaoBiaoDate(cursor.getString(cursor.getColumnIndex("LastChaoBiaoDate")));
			retItem.setStealID(cursor.getString(cursor.getColumnIndex("StealID")));
			retItem.setBId(cursor.getInt(cursor.getColumnIndex("BId")));
			retItem.setAvePrice(cursor.getDouble(cursor.getColumnIndex("avePrice")));
			retItem.setExtraCharge1(cursor.getDouble(cursor.getColumnIndex("extraCharge1")));
			retItem.setExtraCharge2(cursor.getDouble(cursor.getColumnIndex("extraCharge2")));
			retItem.setExtraChargePrice1(cursor.getDouble(cursor.getColumnIndex("extraChargePrice1")));
			retItem.setExtraChargePrice2(cursor.getDouble(cursor.getColumnIndex("extraChargePrice2")));
			retItem.setExtraTotalCharge(cursor.getDouble(cursor.getColumnIndex("extraTotalCharge")));
			retItem.setTotalCharge(cursor.getDouble(cursor.getColumnIndex("TotalCharge")));
			retItem.setReadMeterRecordId(cursor.getString(cursor.getColumnIndex("readMeterRecordId")));
			retItem.setWaterMeterParentID(cursor.getString(cursor.getColumnIndex("WaterMeterParentID")));
			retItem.setWaterUserchargeType(cursor.getString(cursor.getColumnIndex("waterUserchargeType")));
			retItem.setWaterUserTypeId(cursor.getString(cursor.getColumnIndex("waterUserTypeId")));
			retItem.setMemo1(cursor.getString(cursor.getColumnIndex("Memo1")));
		}
		return retItem;
	}

	//根据户号获取用户信息
	public WCBUserEntity GetWCBUserItemByUserNo(String userNO) {
		if (userNO == null || userNO == "") {
			return null;
		}
		WCBUserEntity retItem = null;
		String strSql = "select * from TB_UserInfo where UserNo=? limit 1 ";
		Cursor cursor = db.rawQuery(strSql, new String[] { userNO });
		if (cursor.moveToNext()) {
			retItem = new WCBUserEntity();
			retItem.setUserID(cursor.getInt(cursor.getColumnIndex("UserID")));
			retItem.setNoteNo(cursor.getString(cursor.getColumnIndex("NoteNo")));
			retItem.setStealNo(cursor.getString(cursor.getColumnIndex("StealNo")));
			retItem.setCurrentMonthValue(cursor.getDouble(cursor.getColumnIndex("CurrentMonthValue")));
			retItem.setChaoBiaoTag(cursor.getInt(cursor.getColumnIndex("ChaoBiaoTag")));
			retItem.setPhone(cursor.getString(cursor.getColumnIndex("Phone")));
			retItem.setAddress(cursor.getString(cursor.getColumnIndex("Address")));
			retItem.setPriceType(cursor.getString(cursor.getColumnIndex("PriceType")));
			retItem.setPriceTypeName(cursor.getString(cursor.getColumnIndex("PriceTypeName")));
			retItem.setOweMoney(cursor.getDouble(cursor.getColumnIndex("OweMoney")));
			retItem.setLastMonthFee(cursor.getDouble(cursor.getColumnIndex("LastMonthFee")));
			retItem.setLastMonthWater(cursor.getDouble(cursor.getColumnIndex("LastMonthWater")));
			retItem.setLastMonthValue(cursor.getDouble(cursor.getColumnIndex("LastMonthValue")));
			retItem.setUserFName(cursor.getString(cursor.getColumnIndex("UserFName")));
			retItem.setPreMoney(cursor.getDouble(cursor.getColumnIndex("PreMoney")));
			retItem.setUserNo(cursor.getString(cursor.getColumnIndex("UserNo")));
			retItem.setShouFei(cursor.getDouble(cursor.getColumnIndex("ShouFei")));
			retItem.setCurrMonthWNum(cursor.getDouble(cursor.getColumnIndex("CurrMonthWNum")));
			retItem.setCurrMonthFee(cursor.getDouble(cursor.getColumnIndex("CurrMonthFee")));
			retItem.setOrChaoBiaoTag(cursor.getInt(cursor.getColumnIndex("OrChaoBiaoTag")));
			retItem.setLatitude(cursor.getDouble(cursor.getColumnIndex("Latitude")));
			retItem.setLongitude(cursor.getDouble(cursor.getColumnIndex("Longitude")));
			retItem.setStepPrice(cursor.getString(cursor.getColumnIndex("StepPrice")));
			retItem.setExtraPrice(cursor.getString(cursor.getColumnIndex("ExtraPrice")));
			retItem.setIsSummaryMeter(cursor.getInt(cursor.getColumnIndex("IsSummaryMeter")));
			retItem.setOrderNumber(cursor.getInt(cursor.getColumnIndex("OrderNumber")));
			retItem.setWaterFixValue(cursor.getDouble(cursor.getColumnIndex("WaterFixValue")));
			retItem.setNFCTag(cursor.getString(cursor.getColumnIndex("NFCTag")));
			retItem.setLastChaoBiaoDate(cursor.getString(cursor.getColumnIndex("LastChaoBiaoDate")));
			retItem.setStealID(cursor.getString(cursor.getColumnIndex("StealID")));
			retItem.setBId(cursor.getInt(cursor.getColumnIndex("BId")));
			retItem.setAvePrice(cursor.getDouble(cursor.getColumnIndex("avePrice")));
			retItem.setExtraCharge1(cursor.getDouble(cursor.getColumnIndex("extraCharge1")));
			retItem.setExtraCharge2(cursor.getDouble(cursor.getColumnIndex("extraCharge2")));
			retItem.setExtraChargePrice1(cursor.getDouble(cursor.getColumnIndex("extraChargePrice1")));
			retItem.setExtraChargePrice2(cursor.getDouble(cursor.getColumnIndex("extraChargePrice2")));
			retItem.setExtraTotalCharge(cursor.getDouble(cursor.getColumnIndex("extraTotalCharge")));
			retItem.setTotalCharge(cursor.getDouble(cursor.getColumnIndex("TotalCharge")));
			retItem.setWaterMeterParentID(cursor.getString(cursor.getColumnIndex("WaterMeterParentID")));
			retItem.setReadMeterRecordId(cursor.getString(cursor.getColumnIndex("readMeterRecordId")));
			retItem.setWaterUserchargeType(cursor.getString(cursor.getColumnIndex("waterUserchargeType")));
			retItem.setWaterUserTypeId(cursor.getString(cursor.getColumnIndex("waterUserTypeId")));
			retItem.setMemo1(cursor.getString(cursor.getColumnIndex("Memo1")));
		}
		return retItem;
	}
	
	// 根据顺序获取上一条下一条
	public WCBUserEntity GetWCBUserItemByOrder(String NoteNO, String StealNo,int orderNo,
			boolean isNext, int chaobiaotag) {
 		WCBUserEntity retItem = null;
		String strWhere = "";
		if (chaobiaotag != -1) {
			strWhere = " and ChaoBiaoTag=" + chaobiaotag;
		}
		
		String _UserID="";
		String _NotrNO="";
		String strMutl="select UserID,NoteNo from TB_UserInfo where StealNo=?" ;
		
		Cursor CUserID= db.rawQuery(strMutl,new String[] {StealNo });
		if(CUserID.moveToNext())
		{
			_UserID=CUserID.getString(0);
			_NotrNO=CUserID.getString(1);
		}

		String strSql = "select * from TB_UserInfo where NoteNo=? and ";
		if (!isNext) {
			strSql =strSql+ "UserID<"+_UserID + strWhere + " order by OrderNumber desc";
		}
		else
		{
			strSql =strSql+ "UserID>"+_UserID + strWhere+" order by OrderNumber asc";
		}
		strSql=strSql+ " limit 1 ";
		
		Cursor cursor = db.rawQuery(strSql,
				new String[] { _NotrNO });
		
		if (cursor.moveToNext()) {
			retItem = new WCBUserEntity();
			retItem.setUserID(cursor.getInt(cursor.getColumnIndex("UserID")));
			retItem.setNoteNo(cursor.getString(cursor.getColumnIndex("NoteNo")));
			retItem.setStealNo(cursor.getString(cursor.getColumnIndex("StealNo")));
			retItem.setCurrentMonthValue(cursor.getDouble(cursor.getColumnIndex("CurrentMonthValue")));
			retItem.setChaoBiaoTag(cursor.getInt(cursor.getColumnIndex("ChaoBiaoTag")));
			retItem.setPhone(cursor.getString(cursor.getColumnIndex("Phone")));
			retItem.setAddress(cursor.getString(cursor.getColumnIndex("Address")));
			retItem.setWaterMeterPositionName(cursor.getString(cursor.getColumnIndex("WaterMeterPositionName")));
			retItem.setPriceType(cursor.getString(cursor.getColumnIndex("PriceType")));
			retItem.setPriceTypeName(cursor.getString(cursor.getColumnIndex("PriceTypeName")));
			retItem.setOweMoney(cursor.getDouble(cursor.getColumnIndex("OweMoney")));
			retItem.setLastMonthFee(cursor.getDouble(cursor.getColumnIndex("LastMonthFee")));
			retItem.setLastMonthWater(cursor.getDouble(cursor.getColumnIndex("LastMonthWater")));
			retItem.setLastMonthValue(cursor.getDouble(cursor.getColumnIndex("LastMonthValue")));
			retItem.setUserFName(cursor.getString(cursor.getColumnIndex("UserFName")));
			retItem.setPreMoney(cursor.getDouble(cursor.getColumnIndex("PreMoney")));
			retItem.setUserNo(cursor.getString(cursor.getColumnIndex("UserNo")));
			retItem.setShouFei(cursor.getDouble(cursor.getColumnIndex("ShouFei")));
			retItem.setCurrMonthWNum(cursor.getDouble(cursor.getColumnIndex("CurrMonthWNum")));
			retItem.setCurrMonthFee(cursor.getDouble(cursor.getColumnIndex("CurrMonthFee")));
			retItem.setOrChaoBiaoTag(cursor.getInt(cursor.getColumnIndex("OrChaoBiaoTag")));
			retItem.setLatitude(cursor.getDouble(cursor.getColumnIndex("Latitude")));
			retItem.setLongitude(cursor.getDouble(cursor.getColumnIndex("Longitude")));
			retItem.setStepPrice(cursor.getString(cursor.getColumnIndex("StepPrice")));
			retItem.setExtraPrice(cursor.getString(cursor.getColumnIndex("ExtraPrice")));
			retItem.setIsSummaryMeter(cursor.getInt(cursor.getColumnIndex("IsSummaryMeter")));
			retItem.setOrderNumber(cursor.getInt(cursor.getColumnIndex("OrderNumber")));
			retItem.setWaterFixValue(cursor.getDouble(cursor.getColumnIndex("WaterFixValue")));
			retItem.setNFCTag(cursor.getString(cursor.getColumnIndex("NFCTag")));
			retItem.setLastChaoBiaoDate(cursor.getString(cursor.getColumnIndex("LastChaoBiaoDate")));
			retItem.setStealID(cursor.getString(cursor.getColumnIndex("StealID")));
			retItem.setBId(cursor.getInt(cursor.getColumnIndex("BId")));
			retItem.setAvePrice(cursor.getDouble(cursor.getColumnIndex("avePrice")));
			retItem.setExtraCharge1(cursor.getDouble(cursor.getColumnIndex("extraCharge1")));
			retItem.setExtraCharge2(cursor.getDouble(cursor.getColumnIndex("extraCharge2")));
			retItem.setExtraChargePrice1(cursor.getDouble(cursor.getColumnIndex("extraChargePrice1")));
			retItem.setExtraChargePrice2(cursor.getDouble(cursor.getColumnIndex("extraChargePrice2")));
			retItem.setExtraTotalCharge(cursor.getDouble(cursor.getColumnIndex("extraTotalCharge")));
			retItem.setTotalCharge(cursor.getDouble(cursor.getColumnIndex("TotalCharge")));
			retItem.setReadMeterRecordId(cursor.getString(cursor.getColumnIndex("readMeterRecordId")));
			retItem.setWaterMeterParentID(cursor.getString(cursor.getColumnIndex("WaterMeterParentID")));
			retItem.setWaterUserchargeType(cursor.getString(cursor.getColumnIndex("waterUserchargeType")));
			retItem.setWaterUserTypeId(cursor.getString(cursor.getColumnIndex("waterUserTypeId")));
			retItem.setMemo1(cursor.getString(cursor.getColumnIndex("Memo1")));
		}
		return retItem;
	}

	// 获取查询结果
	public List<WCBUserEntity> SearchResults(int operate, String keywords,
			String noteNo, int chaobiaotag) {
		List<WCBUserEntity> retList = new ArrayList<WCBUserEntity>();
		String strWhere = "";
		if (keywords != null && keywords.trim().length() > 0) {
			keywords = keywords.trim();
			switch (operate) {
			case 1:// 户号
				strWhere = " and UserNo like'%" + keywords + "%'";
				break;
			case 2:// 表钢号
				strWhere = " and StealNo='" + keywords + "'";
				break;
			case 3:// 用户名
				keywords = "%" + keywords + "%";
				strWhere = " and UserFName like '" + keywords + "'";
				break;
			case 4:// 地址
				keywords = "%" + keywords + "%";
				strWhere = " and Address like '" + keywords + "'";
				break;
			default:
				break;
			}
		}
		if (chaobiaotag != -1) {
			strWhere += " and chaobiaotag=" + chaobiaotag;
		}
		if(!noteNo.equalsIgnoreCase("0"))
		{
			strWhere += " and NoteNo='"+noteNo+"'";
		}
		WCBUserEntity retItem = null;
		String strSql = "select * from TB_UserInfo where 1=1 " + strWhere
				+ " order by OrderNumber";
		Cursor cursor = db.rawQuery(strSql, null);
		while (cursor.moveToNext()) {
			retItem = new WCBUserEntity();
			retItem.setUserID(cursor.getInt(cursor.getColumnIndex("UserID")));
			retItem.setNoteNo(cursor.getString(cursor.getColumnIndex("NoteNo")));
			retItem.setStealNo(cursor.getString(cursor.getColumnIndex("StealNo")));
			retItem.setCurrentMonthValue(cursor.getDouble(cursor.getColumnIndex("CurrentMonthValue")));
			retItem.setChaoBiaoTag(cursor.getInt(cursor.getColumnIndex("ChaoBiaoTag")));
			retItem.setPhone(cursor.getString(cursor.getColumnIndex("Phone")));
			retItem.setAddress(cursor.getString(cursor.getColumnIndex("Address")));
			retItem.setPriceType(cursor.getString(cursor.getColumnIndex("PriceType")));
			retItem.setPriceTypeName(cursor.getString(cursor.getColumnIndex("PriceTypeName")));
			retItem.setOweMoney(cursor.getDouble(cursor.getColumnIndex("OweMoney")));
			retItem.setLastMonthFee(cursor.getDouble(cursor.getColumnIndex("LastMonthFee")));
			retItem.setLastMonthWater(cursor.getDouble(cursor.getColumnIndex("LastMonthWater")));
			retItem.setLastMonthValue(cursor.getDouble(cursor.getColumnIndex("LastMonthValue")));
			retItem.setUserFName(cursor.getString(cursor.getColumnIndex("UserFName")));
			retItem.setPreMoney(cursor.getDouble(cursor.getColumnIndex("PreMoney")));
			retItem.setUserNo(cursor.getString(cursor.getColumnIndex("UserNo")));
			retItem.setShouFei(cursor.getDouble(cursor.getColumnIndex("ShouFei")));
			retItem.setReadMeterRecordId(cursor.getString(cursor.getColumnIndex("readMeterRecordId")));
			retList.add(retItem);
		}
		return retList;
	}

	// 获取用户信息
	public WCBUserEntity GetOrderUser(WBBItem bbitem, int seType, String keyWord) {
		if (bbitem == null) {
			return null;
		}
		String strWhere = "";
		if (keyWord != null && keyWord.trim() != "") {
			switch (seType) {
			case 1:// 户号
				strWhere = "  and UserNo='" + keyWord + "'";
				break;
			case 2:// 表钢号
				strWhere = " and StealNo='" + keyWord + "'";
				break;
			case 3:// 用户名
				keyWord = "%" + keyWord + "%";
				strWhere = " and UserFName like '" + keyWord + "'";
				break;
			case 4:// 地址
				keyWord = "%" + keyWord + "%";
				strWhere = " and Address like '" + keyWord + "'";
				break;
			default:
				break;
			}
		}
		WCBUserEntity retItem = null;
		String strSql = "select * from TB_UserInfo where NoteNo=? and ChaoBiaoTag=0 "
				+ strWhere + " order by OrderNumber limit 1";
		Cursor cursor = db
				.rawQuery(strSql, new String[] { bbitem.getNoteNo() });
		if (cursor.moveToNext()) {
			retItem = new WCBUserEntity();
			retItem.setUserID(cursor.getInt(cursor.getColumnIndex("UserID")));
			retItem.setNoteNo(cursor.getString(cursor.getColumnIndex("NoteNo")));
			retItem.setStealNo(cursor.getString(cursor.getColumnIndex("StealNo")));
			retItem.setCurrentMonthValue(cursor.getDouble(cursor.getColumnIndex("CurrentMonthValue")));
			retItem.setChaoBiaoTag(cursor.getInt(cursor.getColumnIndex("ChaoBiaoTag")));
			retItem.setPhone(cursor.getString(cursor.getColumnIndex("Phone")));
			retItem.setAddress(cursor.getString(cursor.getColumnIndex("Address")));
			retItem.setPriceType(cursor.getString(cursor.getColumnIndex("PriceType")));
			retItem.setPriceTypeName(cursor.getString(cursor.getColumnIndex("PriceTypeName")));
			retItem.setOweMoney(cursor.getDouble(cursor.getColumnIndex("OweMoney")));
			retItem.setLastMonthFee(cursor.getDouble(cursor.getColumnIndex("LastMonthFee")));
			retItem.setLastMonthWater(cursor.getDouble(cursor.getColumnIndex("LastMonthWater")));
			retItem.setLastMonthValue(cursor.getDouble(cursor.getColumnIndex("LastMonthValue")));
			retItem.setUserFName(cursor.getString(cursor.getColumnIndex("UserFName")));
			retItem.setPreMoney(cursor.getDouble(cursor.getColumnIndex("PreMoney")));
			retItem.setUserNo(cursor.getString(cursor.getColumnIndex("UserNo")));
			retItem.setShouFei(cursor.getDouble(cursor.getColumnIndex("ShouFei")));
			retItem.setLatitude(cursor.getDouble(cursor.getColumnIndex("Latitude")));
			retItem.setLongitude(cursor.getDouble(cursor.getColumnIndex("Longitude")));
			retItem.setBId(cursor.getInt(cursor.getColumnIndex("BId")));
		}
		return retItem;
	}

	// 是否有未抄的分表
	public boolean HasUnChildWaterMeter(String stealNo) {
		String strSql = "select count(1) as cnum from TB_UserInfo where chaobiaotag=0 and WaterMeterParentID=?";
		Cursor cursor = db.rawQuery(strSql, new String[] { stealNo });
		if (cursor.moveToNext()) {
			if (cursor.getInt(0) > 0) {
				return true;
			}
		}
		return false;
	}

	// 获取分表的水量
	public double GetDisWaterMeterValue(String stealNo) {
		String strSql = "select sum(CurrMonthWNum) as csum from TB_UserInfo where chaobiaotag>0 and WaterMeterParentID=?";
		Cursor cursor = db.rawQuery(strSql, new String[] { stealNo });
		if (cursor.moveToNext()) {
			return cursor.getDouble(0);
		}
		return 0d;
	}

	// 更新抄表信息
	public void UpdateUserItem(WCBUserEntity useritem) {
		if (useritem.getUserID() == 0) {
			return;
		}
		ContentValues cValues = new ContentValues();
		cValues.put("CurrentMonthValue", useritem.getCurrentMonthValue());
		cValues.put("CurrMonthWNum", useritem.getCurrMonthWNum());
		cValues.put("CurrMonthFee", useritem.getCurrMonthFee());
		cValues.put("ShouFei", useritem.getShouFei());
		cValues.put("ShouFeiDate", useritem.getShouFeiDate());
		cValues.put("ChaoBiaoTag", useritem.getChaoBiaoTag());
		cValues.put("ChaoBiaoDate", useritem.getChaoBiaoDate());
		cValues.put("PreMoney", useritem.getPreMoney());
		cValues.put("OweMoney", useritem.getOweMoney());
		cValues.put("Latitude", useritem.getLatitude());
		cValues.put("Longitude", useritem.getLongitude());
		cValues.put("alreadyUpload", useritem.getAlreadyUpload());
		cValues.put("avePrice", useritem.getAvePrice());
		cValues.put("extraChargePrice1", useritem.getExtraChargePrice1());
		cValues.put("extraCharge1", useritem.getExtraCharge1());
		cValues.put("extraChargePrice2", useritem.getExtraChargePrice2());
		cValues.put("extraCharge2", useritem.getExtraCharge2());
		cValues.put("extraTotalCharge", useritem.getExtraTotalCharge());
		cValues.put("TotalCharge", useritem.getTotalCharge());
		cValues.put("chargeID", useritem.getChargeID());
		cValues.put("Phone", useritem.getPhone());
		cValues.put("checkState", useritem.getCheckState());
		cValues.put("checkDateTime", useritem.getCheckDateTime());
		cValues.put("Memo1", useritem.getMemo1());
		db.update("TB_UserInfo", cValues, "UserId=?",
				new String[] { String.valueOf(useritem.getUserID()) });
	}

	// 当分表变化时更新总表数值
	public void UpdateSumWaterVal(String stealNo) {
		WCBUserEntity userItem = GetWCBUserItemByNo(stealNo);
		if (userItem == null || userItem.getChaoBiaoTag() < 1) {
			return;
		}
		double childValues = GetDisWaterMeterValue(stealNo);
		double totleValue = Math
				.abs((userItem.getCurrentMonthValue() - userItem
						.getLastMonthValue()));
		double currmonthNum = totleValue - childValues;
		double fee = WaterUtils.CalcWaterFee(userItem.getStepPrice(),
				userItem.getExtraPrice(), currmonthNum);
		double preMoney = userItem.getPreMoney();
		double oweMoney = userItem.getOweMoney();
		double levMoney = preMoney = preMoney - fee - oweMoney;
		if (levMoney < 0) {
			preMoney = 0;
			oweMoney = Math.abs(levMoney);
		} else {
			preMoney = levMoney;
			oweMoney = 0;
		}
		ContentValues cValues = new ContentValues();
		cValues.put("CurrMonthWNum", currmonthNum);
		cValues.put("CurrMonthFee", fee);
		cValues.put("PreMoney", preMoney);
		cValues.put("oweMoney", oweMoney);
		db.update("TB_UserInfo", cValues, "StealNo=?", new String[] { stealNo });
	}

	// 获取上传的客户信息，
	public List<WCBUserEntity> GetUserUpload(String NoteNo) {
		List<WCBUserEntity> retList = new ArrayList<WCBUserEntity>();
		String strSql = "select * from TB_UserInfo where alreadyUpload=1 ";
		if (!NoteNo.equals("0")) {
			 strSql += "and NoteNo='"+NoteNo+"'";
		}
		
		Cursor cursor = db.rawQuery(strSql, null);
		while (cursor.moveToNext()) {
			WCBUserEntity retItem = new WCBUserEntity();
			retItem.setUserID(cursor.getInt(cursor.getColumnIndex("UserID")));
			retItem.setReadMeterRecordId(cursor.getString(cursor.getColumnIndex("readMeterRecordId")));
			retItem.setCurrentMonthValue(cursor.getDouble(cursor.getColumnIndex("CurrentMonthValue")));
			retItem.setCurrMonthWNum(cursor.getDouble(cursor.getColumnIndex("CurrMonthWNum")));
			retItem.setAvePrice(cursor.getDouble(cursor.getColumnIndex("avePrice")));
			retItem.setTotalCharge(cursor.getDouble(cursor.getColumnIndex("TotalCharge")));
			retItem.setExtraChargePrice1(cursor.getDouble(cursor.getColumnIndex("extraChargePrice1")));
			retItem.setExtraCharge1(cursor.getDouble(cursor.getColumnIndex("extraCharge1")));
			retItem.setExtraChargePrice2(cursor.getDouble(cursor.getColumnIndex("extraChargePrice2")));
			retItem.setExtraCharge2(cursor.getDouble(cursor.getColumnIndex("extraCharge2")));
			retItem.setExtraTotalCharge(cursor.getDouble(cursor.getColumnIndex("extraTotalCharge")));
			retItem.setTotalCharge(cursor.getDouble(cursor.getColumnIndex("TotalCharge")));
			retItem.setOVERDUEMONEY(cursor.getDouble(cursor.getColumnIndex("OVERDUEMONEY")));
			retItem.setChaoBiaoDate(cursor.getString(cursor.getColumnIndex("ChaoBiaoDate")));
			retItem.setCheckState(cursor.getString(cursor.getColumnIndex("checkState")));
			retItem.setChecker(cursor.getString(cursor.getColumnIndex("checker")));
			retItem.setCheckDateTime(cursor.getString(cursor.getColumnIndex("checkDateTime")));
			retItem.setChaoBiaoTag(cursor.getInt(cursor.getColumnIndex("ChaoBiaoTag")));
			retItem.setLatitude(cursor.getDouble(cursor.getColumnIndex("Latitude")));
			retItem.setLongitude(cursor.getDouble(cursor.getColumnIndex("Longitude")));
			retItem.setChargeID(cursor.getString(cursor.getColumnIndex("chargeID")));
			retItem.setCurrMonthFee(cursor.getDouble(cursor.getColumnIndex("CurrMonthFee")));
			retItem.setPhone(cursor.getString(cursor.getColumnIndex("Phone")));
			retList.add(retItem);
		}
		return retList;
	}

	// 数据上传后更新标志位
	public void UpdateUserUploadTag(int userId) {
		ContentValues cValues = new ContentValues();
		cValues.put("alreadyUpload", "0");
		
		//更新抄表标志 RONG
		cValues.put("OrChaoBiaoTag", "2");
		//cValues.put("", 0);
		db.update("TB_UserInfo", cValues, "UserID=? and alreadyUpload=1",
				new String[] { String.valueOf(userId) });
	}

	// 获取图片信息
	public List<WPicItem> GetPicItem(String userNo, int picType) {
		List<WPicItem> retInfo = new ArrayList<WPicItem>();
		String strSql = "select * from TB_PicInfo where UserNo=? and PicType=?";
		Cursor cursor = db.rawQuery(strSql,
				new String[] { userNo, String.valueOf(picType) });
		while (cursor.moveToNext()) {
			WPicItem picItem = new WPicItem();
			picItem.setAddDate(cursor.getString(cursor
					.getColumnIndex("AddDate")));
			picItem.setAddUser(cursor.getString(cursor
					.getColumnIndex("AddUser")));
			// picItem.setAddUserId(cursor.getInt(cursor.getColumnIndex("AddUserID")));
			picItem.setAlreadyUpload(cursor.getInt(cursor
					.getColumnIndex("alreadyUpload")));
			picItem.setPicId(cursor.getInt(cursor.getColumnIndex("PicId")));
			picItem.setPicName(cursor.getString(cursor
					.getColumnIndex("PicName")));
			picItem.setPicPath(cursor.getString(cursor
					.getColumnIndex("PicPath")));
			picItem.setUserNo(cursor.getString(cursor.getColumnIndex("UserNo")));
			retInfo.add(picItem);
		}
		return retInfo;
	}

	// 根据表本号获取图片信息
	public List<WPicItem> GetPicItemByNoteNo() {
		List<WPicItem> retList = new ArrayList<WPicItem>();
		String strSql = "select * from TB_PicInfo where alreadyUpload=1";
		Cursor cursor = db.rawQuery(strSql,null);
		while (cursor.moveToNext()) {
			WPicItem picItem = new WPicItem();
			picItem.setAddDate(cursor.getString(cursor
					.getColumnIndex("AddDate")));
			picItem.setAddUser(cursor.getString(cursor
					.getColumnIndex("AddUser")));
			picItem.setAddUserId(cursor.getString(cursor
					.getColumnIndex("AddUser")));
			picItem.setPicId(cursor.getInt(cursor.getColumnIndex("PicId")));
			picItem.setPicName(cursor.getString(cursor
					.getColumnIndex("PicName")));
			picItem.setPicPath(cursor.getString(cursor
					.getColumnIndex("PicPath")));
			picItem.setUserNo(cursor.getString(cursor.getColumnIndex("UserNo")));
			picItem.setLatitude(cursor.getDouble(cursor
					.getColumnIndex("Latitude")));
			picItem.setLongitude(cursor.getDouble(cursor
					.getColumnIndex("Longitude")));
			picItem.setStealNo(cursor.getString(cursor
					.getColumnIndex("StealNo")));
			retList.add(picItem);
		}
		return retList;
	}

	//获取报修记录
	public List<FaultItem>GetUploadFaultItems()
	{
		List<FaultItem> retList = new ArrayList<FaultItem>();
		String strSql = "select f.UserNo,p.PicPath,f.FaultDescript,f.AddUserID,f.AddDate " +
				" from tb_faultreport f left join tb_picinfo p on f.userno=p.userno " +
				" where f.[alreadyUpload]=1 and p.PicType=2";
		Cursor cursor = db.rawQuery(strSql,null);
		while (cursor.moveToNext()) {
			FaultItem picItem = new FaultItem();
			picItem.setAddDate(cursor.getString(cursor.getColumnIndex("AddDate")));
			picItem.setUserNo(cursor.getString(cursor.getColumnIndex("UserNo")));
			picItem.setPicPath(cursor.getString(cursor.getColumnIndex("PicPath")));
			picItem.setContent(cursor.getString(cursor.getColumnIndex("FaultDescript")));
			picItem.setAddUserId(cursor.getString(cursor.getColumnIndex("AddUserID")));
			retList.add(picItem);
		}
		return retList;
	}
	
	// 更新图片标志
	public void UpdatePicTag(int picId) {
		if (picId <= 0) {
			return;
		}
		ContentValues cValues = new ContentValues();
		cValues.put("alreadyUpload", 0);
		db.update("TB_PicInfo", cValues, "PicId=?",
				new String[] { String.valueOf(picId) });
	}
	
	public void UpdateFaultReport(String userNo)
	{
		ContentValues cValues = new ContentValues();
		cValues.put("alreadyUpload", 0);
		db.update("TB_FaultReport", cValues, "UserNo=?",
				new String[] { userNo });
		cValues = new ContentValues();
		cValues.put("alreadyUpload", 0);
		db.update("TB_PicInfo", cValues, "UserNo=? and PicType=2",
				new String[] { userNo });
	}

	// 添加客户信息
	public void AddCustomerInfo() {
		LocationInfo locationInfo = ((ZsyyApplication) _conteContext
				.getApplicationContext()).currPoint;
		WBBList wlist = new WBBList();
		int[] arr = new int[] { 1, -1 };
		Random rad = new Random();
		int index = 0;
		List<WUserItem> userItems = wlist.getUserItems();
		String strSql = "insert into TB_UserInfo (UserNo,NoteNo,UserFName,Phone,Address,LastMonthValue,LastMonthWater,LastMonthFee,PriceType,StealNo,ChaoBiaoTag,PreMoney,OweMoney,CurrentMonthValue,ChaoBiaoDate,ShouFei,ShouFeiDate,Latitude,Longitude,alreadyUpload,OrOweFee,OrPreMoney)"
				+ "values(?,?,?,?,?,'1243',100,3000,'居民','100171',1,0,100,3000,'2015-12-17 00:08:00',234,'2015-12-17 00:09:00',?,?,1,100,0);";
		for (WUserItem itm : userItems) {
			double lau = locationInfo.getLatitude() + (rad.nextDouble() / 20)
					* arr[index % 2];
			double lag = locationInfo.getLontitude() + (rad.nextDouble() / 20)
					* arr[index % 2];
			db.execSQL(strSql,
					new Object[] { itm.getYhbm(), itm.getBbh(), itm.getXm(),
							itm.getDh(), itm.getDz(), lau, lag });
			index++;
		}
	}

	// 添加客户信息
	public void AddCustomerInfo(List<WCBUserEntity> userList) {
		if (userList == null || userList.size() == 0) {
			return;
		}
		db.beginTransaction();
		try {
			for (WCBUserEntity item : userList) {
				ContentValues cValues = new ContentValues();
				cValues.put("UserNo", item.getUserNo());
				cValues.put("NoteNo", item.getNoteNo());
				cValues.put("UserFName", item.getUserFName());
				cValues.put("Phone", item.getPhone());
				cValues.put("Address", item.getAddress());
				cValues.put("LastMonthValue", item.getLastMonthValue());
				cValues.put("LastMonthWater", item.getLastMonthWater());
				cValues.put("LastMonthFee", item.getLastMonthFee());
				cValues.put("PriceType", item.getPriceType());
				cValues.put("PriceTypeName", item.getPriceTypeName());
				cValues.put("StealNo", item.getStealNo());
				cValues.put("CurrentMonthValue", item.getCurrentMonthValue());
				cValues.put("CurrMonthWNum", item.getCurrMonthWNum());
				cValues.put("CurrMonthFee", item.getCurrMonthFee());
				cValues.put("ChaoBiaoTag", item.getChaoBiaoTag());
				cValues.put("ChaoBiaoDate", item.getChaoBiaoDate());
				cValues.put("ShouFei", item.getShouFei());
				cValues.put("ShouFeiDate", item.getShouFeiDate());
				cValues.put("Latitude", item.getLatitude());
				cValues.put("Longitude", item.getLongitude());
				cValues.put("OrChaoBiaoTag", item.getChaoBiaoTag());
				cValues.put("IsReverse", item.getIsReverse());
				cValues.put("alreadyUpload", 0);
				cValues.put("StepPrice", item.getStepPrice());
				cValues.put("ExtraPrice", item.getExtraPrice());
				cValues.put("IsSummaryMeter", item.getIsSummaryMeter());
				cValues.put("WaterMeterParentID", item.getWaterMeterParentID());
				cValues.put("OrderNumber", item.getOrderNumber());
				cValues.put("StealID", item.getStealID());
				cValues.put("WaterFixValue", item.getWaterFixValue());
				cValues.put("readMeterRecordId", item.getReadMeterRecordId());
				cValues.put("avePrice", item.getAvePrice());
				cValues.put("extraChargePrice1", item.getExtraChargePrice1());
				cValues.put("extraCharge1", item.getExtraCharge1());
				cValues.put("extraChargePrice2", item.getExtraChargePrice2());
				cValues.put("extraCharge2", item.getExtraCharge2());
				cValues.put("extraTotalCharge", item.getExtraTotalCharge());
				cValues.put("TotalCharge", item.getTotalCharge());
				cValues.put("OVERDUEMONEY", item.getOVERDUEMONEY());
				cValues.put("ReadMeterRecordYear",
						item.getReadMeterRecordYear());
				cValues.put("ReadMeterRecordMonth",
						item.getReadMeterRecordMonth());
				cValues.put("WaterMeterPositionName",
						item.getWaterMeterPositionName());
				cValues.put("LastChaoBiaoDate", item.getLastChaoBiaoDate());
				cValues.put("waterUserchargeType",item.getWaterUserchargeType());
				cValues.put("waterUserTypeId",item.getPriceType());
				cValues.put("Memo1",item.getMemo1());
				db.insert("TB_UserInfo", "UserNo", cValues);
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			db.endTransaction();
		}

	}
	
	//修改单个客户信息
	public void EditCustomerInfo(List<WCBUserEntity> userList) {
		if (userList == null || userList.size() == 0) {
			return;
		}
		db.beginTransaction();
		try {
			for (WCBUserEntity item : userList) {
				ContentValues cValues = new ContentValues();
				cValues.put("UserNo", item.getUserNo());
				cValues.put("NoteNo", item.getNoteNo());
				cValues.put("UserFName", item.getUserFName());
				cValues.put("Phone", item.getPhone());
				cValues.put("Address", item.getAddress());
				cValues.put("LastMonthValue", item.getLastMonthValue());
				cValues.put("LastMonthWater", item.getLastMonthWater());
				cValues.put("LastMonthFee", item.getLastMonthFee());
				cValues.put("PriceType", item.getPriceType());
				cValues.put("PriceTypeName", item.getPriceTypeName());
				cValues.put("StealNo", item.getStealNo());
				cValues.put("CurrentMonthValue", item.getCurrentMonthValue());
				cValues.put("CurrMonthWNum", item.getCurrMonthWNum());
				cValues.put("CurrMonthFee", item.getCurrMonthFee());
				cValues.put("ChaoBiaoTag", item.getChaoBiaoTag());
				cValues.put("ChaoBiaoDate", item.getChaoBiaoDate());
				cValues.put("ShouFei", item.getShouFei());
				cValues.put("ShouFeiDate", item.getShouFeiDate());
				cValues.put("Latitude", item.getLatitude());
				cValues.put("Longitude", item.getLongitude());
				cValues.put("OrChaoBiaoTag", item.getChaoBiaoTag());
				cValues.put("IsReverse", item.getIsReverse());
				cValues.put("alreadyUpload", 0);
				cValues.put("StepPrice", item.getStepPrice());
				cValues.put("ExtraPrice", item.getExtraPrice());
				cValues.put("IsSummaryMeter", item.getIsSummaryMeter());
				cValues.put("WaterMeterParentID", item.getWaterMeterParentID());
				cValues.put("OrderNumber", item.getOrderNumber());
				cValues.put("StealID", item.getStealID());
				cValues.put("WaterFixValue", item.getWaterFixValue());
				cValues.put("readMeterRecordId", item.getReadMeterRecordId());
				cValues.put("avePrice", item.getAvePrice());
				cValues.put("extraChargePrice1", item.getExtraChargePrice1());
				cValues.put("extraCharge1", item.getExtraCharge1());
				cValues.put("extraChargePrice2", item.getExtraChargePrice2());
				cValues.put("extraCharge2", item.getExtraCharge2());
				cValues.put("extraTotalCharge", item.getExtraTotalCharge());
				cValues.put("TotalCharge", item.getTotalCharge());
				cValues.put("OVERDUEMONEY", item.getOVERDUEMONEY());
				cValues.put("ReadMeterRecordYear",
						item.getReadMeterRecordYear());
				cValues.put("ReadMeterRecordMonth",
						item.getReadMeterRecordMonth());
				cValues.put("WaterMeterPositionName",
						item.getWaterMeterPositionName());
				cValues.put("LastChaoBiaoDate", item.getLastChaoBiaoDate());
				cValues.put("waterUserchargeType",item.getWaterUserchargeType());
				cValues.put("waterUserTypeId",item.getPriceType());
				cValues.put("Memo1",item.getMemo1());
				db.update("TB_UserInfo", cValues, " readMeterRecordId=?", 
						new String[] { String.valueOf(item.getReadMeterRecordId()) });
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			db.endTransaction();
		}

	}

	// 清空表数据
	public void ResetTable() {
		if (dbHelper != null) {
			dbHelper.ResetTable(db);
		}
	}

	// 添加故障报修
	public void AddFaultReport(WFaultReportInfo fReportInfo) {
		if (fReportInfo == null) {
			return;
		}
		if (fReportInfo.getFId() > 0) {
			ContentValues cValues = new ContentValues();
			cValues.put("UserNo", fReportInfo.getUserNo());
			cValues.put("NoteNo", fReportInfo.getNoteNo());
			cValues.put("FaultDescript", fReportInfo.getFaultDescript());
			cValues.put("AddDate", fReportInfo.getAddDate());
			cValues.put("AddUserID", fReportInfo.getAddUserID());
			cValues.put("BId", fReportInfo.getBId());
			cValues.put("alreadyUpload", fReportInfo.getAlreadyUpload());
			cValues.put("StealNo", fReportInfo.getStealNo());
			db.update("TB_FaultReport", cValues, "PicId=?",
					new String[] { String.valueOf(fReportInfo.getFId()) });
		} else {
			ContentValues cValues = new ContentValues();
			cValues.put("UserNo", fReportInfo.getUserNo());
			cValues.put("NoteNo", fReportInfo.getNoteNo());
			cValues.put("FaultDescript", fReportInfo.getFaultDescript());
			cValues.put("AddDate", fReportInfo.getAddDate());
			cValues.put("AddUserID", fReportInfo.getAddUserID());
			cValues.put("Bid", fReportInfo.getBId());
			cValues.put("alreadyUpload", fReportInfo.getAlreadyUpload());
			cValues.put("StealNo", fReportInfo.getStealNo());
			db.insert("TB_FaultReport", "UserNo", cValues);
		}

		db.delete("TB_PicInfo", "UserNo=? and PicType=2",
				new String[] { fReportInfo.getUserNo() });
		List<WPicItem> picItems = fReportInfo.getPicItems();
		if (picItems != null && picItems.size() > 0) {
			db.delete("TB_PicInfo", "UserNo=? and PicType=2",
					new String[] { fReportInfo.getUserNo() });
			for (WPicItem itm : picItems) {
				ContentValues cValues = new ContentValues();
				cValues.put("UserNo", itm.getUserNo());
				cValues.put("NoteNO", itm.getNoteNo());
				cValues.put("PicName", itm.getPicName());
				cValues.put("PicPath", itm.getPicPath());
				cValues.put("AddDate", itm.getAddDate());
				cValues.put("AddUserId", itm.getAddUserId());
				cValues.put("AddUser", itm.getAddUser());
				cValues.put("PicType", itm.getPicType());
				cValues.put("Latitude", itm.getLatitude());
				cValues.put("Longitude", itm.getLongitude());
				cValues.put("BId", itm.getBId());
				cValues.put("alreadyUpload", itm.getAlreadyUpload());
				db.insert("TB_PicInfo", "UserNo", cValues);
			}
		}
	}

	// 获取故障报修信息
	public WFaultReportInfo GetFaultReport(String userNo) {
		WFaultReportInfo retInfo = new WFaultReportInfo();
		String strSql = "select * from TB_FaultReport where UserNo=?";
		Cursor cursor = db.rawQuery(strSql, new String[] { userNo });
		if (cursor.moveToNext()) {
			retInfo = new WFaultReportInfo();
			retInfo.setAddDate(cursor.getString(cursor
					.getColumnIndex("AddDate")));
			retInfo.setAddUser(cursor.getString(cursor
					.getColumnIndex("AddUser")));
			retInfo.setAddUserID(cursor.getString(cursor
					.getColumnIndex("AddUserID")));
			retInfo.setAlreadyUpload(cursor.getInt(cursor
					.getColumnIndex("alreadyUpload")));
			retInfo.setFaultDescript(cursor.getString(cursor
					.getColumnIndex("FaultDescript")));
			retInfo.setFId(cursor.getInt(cursor.getColumnIndex("PicId")));
			retInfo.setUserNo(cursor.getString(cursor.getColumnIndex("UserNo")));
		}
		retInfo.setPicItems(new ArrayList<WPicItem>());
		strSql = "select * from TB_PicInfo where UserNo=? and PicType=2";
		cursor = db.rawQuery(strSql, new String[] { userNo });
		while (cursor.moveToNext()) {
			WPicItem picItem = new WPicItem();
			picItem.setAddDate(cursor.getString(cursor
					.getColumnIndex("AddDate")));
			picItem.setAddUser(cursor.getString(cursor
					.getColumnIndex("AddUser")));
			picItem.setAddUserId(cursor.getString(cursor
					.getColumnIndex("AddUserId")));
			picItem.setAlreadyUpload(cursor.getInt(cursor
					.getColumnIndex("alreadyUpload")));
			picItem.setPicId(cursor.getInt(cursor.getColumnIndex("PicId")));
			picItem.setPicName(cursor.getString(cursor
					.getColumnIndex("PicName")));
			picItem.setPicPath(cursor.getString(cursor
					.getColumnIndex("PicPath")));
			picItem.setUserNo(cursor.getString(cursor.getColumnIndex("UserNo")));
			retInfo.getPicItems().add(picItem);
		}
		return retInfo;
	}

	// 获取故障报修列表
	public List<WFaultReportInfo> GetUploadFault() {
		List<WFaultReportInfo> retList = new ArrayList<WFaultReportInfo>();
		String strSql = "select * from TB_FaultReport where alreadyUpload=1";
		Cursor cursor = db.rawQuery(strSql,null);
		while (cursor.moveToNext()) {
			WFaultReportInfo retInfo = new WFaultReportInfo();
			retInfo.setAddDate(cursor.getString(cursor
					.getColumnIndex("AddDate")));
			retInfo.setAddUser(cursor.getString(cursor
					.getColumnIndex("AddUser")));
			retInfo.setAddUserID(cursor.getString(cursor
					.getColumnIndex("AddUserID")));
			retInfo.setFaultDescript(cursor.getString(cursor
					.getColumnIndex("FaultDescript")));
			retInfo.setUserNo(cursor.getString(cursor.getColumnIndex("UserNo")));
			retInfo.setStealNo(cursor.getString(cursor
					.getColumnIndex("StealNo")));
			retList.add(retInfo);
		}
		return retList;
	}

	// 更新故障标志位
	public void UpdateFaultTag() {
		ContentValues cvValues = new ContentValues();
		cvValues.put("alreadyUpload", 0);
		db.update("TB_FaultReport", cvValues, " alreadyUpload=1 ",null);
	}

	// 添加客户建议
	public void AddCustomerAdvice(WCustomerAdviceInfo adviceInfo) {
		if (adviceInfo == null) {
			return;
		}
		if (adviceInfo.getAdviceID() > 0) {
			ContentValues cValues = new ContentValues();
			cValues.put("UserNo", adviceInfo.getUserNo());
			cValues.put("Advice", adviceInfo.getAdvice());
			cValues.put("AddDate", adviceInfo.getAddDate());
			cValues.put("AddUserID", adviceInfo.getAddUserId());
			cValues.put("AddUser", adviceInfo.getAddUser());
			cValues.put("BId", adviceInfo.getBId());
			cValues.put("alreadyUpload", "1");
			cValues.put("NoteNo", adviceInfo.getNoteNo());
			db.update("TB_CustomAdvice", cValues, "AdviceID=?",
					new String[] { String.valueOf(adviceInfo.getAdviceID()) });
		} else {
			ContentValues cValues = new ContentValues();
			cValues.put("UserNo", adviceInfo.getUserNo());
			cValues.put("Advice", adviceInfo.getAdvice());
			cValues.put("AddDate", adviceInfo.getAddDate());
			cValues.put("AddUserID", adviceInfo.getAddUserId());
			cValues.put("AddUser", adviceInfo.getAddUser());
			cValues.put("BId", adviceInfo.getBId());
			cValues.put("alreadyUpload", "1");
			cValues.put("NoteNo", adviceInfo.getNoteNo());
			db.insert("TB_CustomAdvice", "UserNo", cValues);
		}
	}

	// 获取客户建议
	public WCustomerAdviceInfo GetCustomerAdvice(String userNo) {
		if (userNo == null || userNo == "") {
			return null;
		}
		WCustomerAdviceInfo retInfo = new WCustomerAdviceInfo();
		String strSql = "select * from TB_CustomAdvice where UserNo=?";
		Cursor cursor = db.rawQuery(strSql, new String[] { userNo });
		if (cursor.moveToNext()) {
			retInfo = new WCustomerAdviceInfo();
			retInfo.setAddDate(cursor.getString(cursor
					.getColumnIndex("AddDate")));
			retInfo.setAddUser(cursor.getString(cursor
					.getColumnIndex("AddUser")));
			retInfo.setAddUserId(cursor.getString(cursor
					.getColumnIndex("AddUserId")));
			retInfo.setAlreadyUpload(cursor.getInt(cursor
					.getColumnIndex("alreadyUpload")));
			retInfo.setAdvice(cursor.getString(cursor.getColumnIndex("Advice")));
			retInfo.setAdviceID(cursor.getInt(cursor.getColumnIndex("AdviceID")));
			retInfo.setUserNo(cursor.getString(cursor.getColumnIndex("UserNo")));
		}
		return retInfo;
	}

	// 获取客户建议列表
	public List<WCustomerAdviceInfo> GetAdviceInfos() {
		List<WCustomerAdviceInfo> retList = new ArrayList<WCustomerAdviceInfo>();
		String strSql = "select * from TB_CustomAdvice where alreadyUpload=1 ";
		Cursor cursor = db.rawQuery(strSql,null);
		while (cursor.moveToNext()) {
			WCustomerAdviceInfo retInfo = new WCustomerAdviceInfo();
			retInfo.setAddDate(cursor.getString(cursor
					.getColumnIndex("AddDate")));
			retInfo.setAddUser(cursor.getString(cursor
					.getColumnIndex("AddUser")));
			retInfo.setAddUserId(cursor.getString(cursor
					.getColumnIndex("AddUserId")));
			retInfo.setAlreadyUpload(cursor.getInt(cursor
					.getColumnIndex("alreadyUpload")));
			retInfo.setAdvice(cursor.getString(cursor.getColumnIndex("Advice")));
			retInfo.setAdviceID(cursor.getInt(cursor.getColumnIndex("AdviceID")));
			retInfo.setUserNo(cursor.getString(cursor.getColumnIndex("UserNo")));
			retList.add(retInfo);
		}
		return retList;
	}

	// 更新客户建议上传Tag
	public void UpdateAdviceTag() {
		ContentValues cValues = new ContentValues();
		cValues.put("alreadyUpload", 0);
		db.update("TB_CustomAdvice", cValues, "alreadyUpload=1 ",null);
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
		db.insert("TB_LPoint", "UserId", cValues);
	}

	// 获取行走路径列表
	public List<WPointItem> GetWalkPoints() {
		List<WPointItem> retList = new ArrayList<WPointItem>();
		String strSql = "select * from TB_LPoint where alreadyUpload=1 ";
		Cursor cursor = db.rawQuery(strSql, null);
		while (cursor.moveToNext()) {
			WPointItem wItem = new WPointItem();
			wItem.setAddDate(cursor.getString(cursor.getColumnIndex("AddDate")));
			wItem.setLatitude(cursor.getDouble(cursor
					.getColumnIndex("Latitude")));
			wItem.setLongitude(cursor.getDouble(cursor
					.getColumnIndex("Longitude")));
			wItem.setUserId(cursor.getString(cursor.getColumnIndex("UserId")));
			wItem.setUserName(cursor.getString(cursor
					.getColumnIndex("UserName")));
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
			wItem.setLatitude(cursor.getDouble(cursor
					.getColumnIndex("Latitude")));
			wItem.setLongitude(cursor.getDouble(cursor
					.getColumnIndex("Longitude")));
			wItem.setUserId(cursor.getString(cursor.getColumnIndex("UserId")));
			wItem.setUserName(cursor.getString(cursor
					.getColumnIndex("UserName")));
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

	// 重置位置表
	public void ResetWpoint() {
		String strSql = "drop table IF EXISTS TB_LPoint";
		db.execSQL(strSql);
		strSql = "  CREATE TABLE IF NOT EXISTS TB_LPoint("
				+ " PId INTEGER Primary Key Autoincrement,Latitude real,"
				+ " Longitude real,UserId varchar(50),UserName varchar(50),"
				+ " AddDate varchar(50),alreadyUpload TINYINT);";
		db.execSQL(strSql);
	}

	// 附近用户坐标
	public List<WUserItem> GetNearUserLocation(int distance,
			LocationInfo currLocation) {
		List<WUserItem> retList = new ArrayList<WUserItem>();
		if (currLocation == null) {
			return retList;
		}
		if (currLocation.getLatitude() < 1 || currLocation.getLontitude() < 1) {
			return retList;
		}
		String strSql = "select UserNo,Address,Latitude,Longitude,ChaoBiaoTag from TB_UserInfo";
		Cursor cursor = db.rawQuery(strSql, null);
		double la = 0;
		double lon = 0;
		String address = "";
		String userNo = "";
		int cbTag = 0;
		LatLng pointEnd;
		LatLng pointBegin = new LatLng(currLocation.getLatitude(),
				currLocation.getLontitude());
		while (cursor.moveToNext()) {
			la = cursor.getDouble(cursor.getColumnIndex("Latitude"));
			lon = cursor.getDouble(cursor.getColumnIndex("Longitude"));
			address = cursor.getString(cursor.getColumnIndex("Address"));
			userNo = cursor.getString(cursor.getColumnIndex("UserNo"));
			cbTag = cursor.getInt(cursor.getColumnIndex("ChaoBiaoTag"));
			if (la == 0 || lon == 0) {
				continue;
			}
			pointEnd = new LatLng(la, lon);
			if (Math.abs(DistanceUtil.getDistance(pointBegin, pointEnd)) <= distance) {
				WUserItem pItem = new WUserItem();
				pItem.setLatitude(la);
				pItem.setLongitude(lon);
				pItem.setDz(address);
				pItem.setYhbm(userNo);
				pItem.setCbbz(cbTag);
				retList.add(pItem);
			}

		}
		return retList;
	}

	// 清空路径数据
	public void RestWalkPoint() {
		String strSql = " drop table IF EXISTS TB_LPoint; CREATE TABLE IF NOT EXISTS TB_LPoint("
				+ " PId INTEGER Primary Key Autoincrement,Latitude real,"
				+ " Longitude real,UserId varchar(50),UserName varchar(50),"
				+ " AddDate varchar(50),alreadyUpload TINYINT);";
		db.execSQL(strSql);
	}

	// 表本号获取上传信息
	public WUploadItem GetUploadInfo(String noteNo) {
		WUploadItem uploadItem = new WUploadItem();
		uploadItem.setCbMonth("");
		uploadItem.setCby("");
		
		String strSql = "select count(1) as cNum from TB_UserInfo where NoteNo=?";
		if (noteNo.isEmpty() || noteNo=="0") {
			strSql = "select count(1) as cNum from TB_UserInfo where NoteNo<>?";
		}
		Cursor cursor = db.rawQuery(strSql,
				new String[] { String.valueOf(noteNo) });
		if (cursor.moveToNext()) {
			uploadItem.setYhzs(cursor.getInt(0));
		}
		strSql = "select count(1) as cNum from TB_UserInfo where ('"+noteNo+"'='0' or NoteNo=?) and ChaoBiaoTag=1";
		cursor = db.rawQuery(strSql, new String[] { String.valueOf(noteNo) });
		if (cursor.moveToNext()) {
			uploadItem.setYcyhs(cursor.getInt(0));
		}
		strSql = "select count(1)as cNum from TB_UserInfo where ('"+noteNo+"'='0' or NoteNo=?) and ChaoBiaoTag=0";
		cursor = db.rawQuery(strSql, new String[] { String.valueOf(noteNo) });
		if (cursor.moveToNext()) {
			uploadItem.setWcyhs(cursor.getInt(0));
		}
		strSql = "select count(1)as cNum from TB_FaultReport where ('"+noteNo+"'='0' or NoteNo=?)  and alreadyUpload=1";
		cursor = db.rawQuery(strSql, new String[] { String.valueOf(noteNo) });
		if (cursor.moveToNext()) {
			uploadItem.setGzbx(cursor.getInt(0));
		}
		strSql = "select count(1)as cNum from TB_CustomAdvice where ('"+noteNo+"'='0' or NoteNo=?) and alreadyUpload=1";
		cursor = db.rawQuery(strSql, new String[] { String.valueOf(noteNo) });
		if (cursor.moveToNext()) {
			uploadItem.setKhjy(cursor.getInt(0));
		}
		strSql = "select count(1)as cNum from TB_PicInfo where ('"+noteNo+"'='0' or NoteNo=?) and alreadyUpload=1";
		cursor = db.rawQuery(strSql, new String[] { String.valueOf(noteNo) });
		if (cursor.moveToNext()) {
			uploadItem.setZpsl(cursor.getInt(0));
		}
		strSql = "select count(1)as cNum from TB_UserInfo where ('"+noteNo+"'='0' or NoteNo=?) and alreadyUpload=1";
		cursor = db.rawQuery(strSql, new String[] { String.valueOf(noteNo) });
		if (cursor.moveToNext()) {
			uploadItem.setWsc(cursor.getInt(0));
		}
		return uploadItem;
	}

	//表本号获取欠费信息
	
	// 阶梯价格列表
	public List<WaterPriceInfo> getWaterPriceInfo(String priceType) {
		String strSql = "";
		if (waterPriceInfos.size() == 0) {
			strSql = "select * from TB_UnitPrice where PriceType=? order by SIndex";
			Cursor cursor = db.rawQuery(strSql, new String[] { priceType });
			while (cursor.moveToNext()) {
				WaterPriceInfo priceInfo = new WaterPriceInfo();
				priceInfo.setSIndex(cursor.getInt(cursor
						.getColumnIndex("SIndex")));
				priceInfo.setUnid(cursor.getInt(cursor.getColumnIndex("Unid")));
				priceInfo.setWFrom(cursor.getDouble(cursor
						.getColumnIndex("WFrom")));
				priceInfo
						.setWTo(cursor.getDouble(cursor.getColumnIndex("WTo")));
				priceInfo.setWPrice(cursor.getDouble(cursor
						.getColumnIndex("WPrice")));
				waterPriceInfos.add(priceInfo);
			}
		}
		return waterPriceInfos;
	}

	// 计算水价,水量和价格类型
	public double CalcPrice(double waterValue, String priceType) {
		List<WaterPriceInfo> priceInfos = getWaterPriceInfo(priceType);
		if (priceInfos.size() == 0) {
			return 0;
		}
		double totleMoney = 0;
		for (WaterPriceInfo p : priceInfos) {
			if (waterValue > p.getWTo() && p.getWTo() > 0) {
				totleMoney += (p.getWTo() - p.getWFrom()) * p.getWPrice();
			} else {
				totleMoney += (waterValue - p.getWFrom()) * p.getWPrice();
				break;
			}
		}
		return totleMoney;
	}

	// 统计分析按月
	public WAnalysisItem GetAnalysisItemByMonth(String noteNo) {
		WAnalysisItem retItem = new WAnalysisItem();
		String currDay = new SimpleDateFormat("yyyy-MM").format(new Date());
		String strSql = "select count(1) as cNum from TB_UserInfo where ('"+noteNo+"'='0' or NoteNo=?) ";
		Cursor cursor = db.rawQuery(strSql,
				new String[] { String.valueOf(noteNo) });
		if (cursor.moveToNext()) {
			retItem.setTotle(cursor.getString(0));
		}
		strSql = "select count(1) as cNum from TB_UserInfo where ('"+noteNo+"'='0' or NoteNo=?) and ChaoBiaoTag in(1,3) and ChaoBiaoDate like ?";
		cursor = db.rawQuery(strSql, new String[] { noteNo ,
				currDay + "%"});
		if (cursor.moveToNext()) {
			retItem.setYcb(cursor.getString(0));
		}
		strSql = "select count(1)as cNum from TB_UserInfo where ('"+noteNo+"'='0' or NoteNo=?) and ChaoBiaoTag=0 and ChaoBiaoDate like ?";
		cursor = db.rawQuery(strSql, new String[] { noteNo ,
				currDay + "%"});
		if (cursor.moveToNext()) {
			retItem.setWcb(cursor.getString(0));
		}
		strSql = "select count(1)as cNum from TB_FaultReport where ('"+noteNo+"'='0' or NoteNo=?) and AddDate like ?";
		cursor = db.rawQuery(strSql, new String[] { noteNo ,
				currDay + "%"});
		if (cursor.moveToNext()) {
			retItem.setSbgz(cursor.getString(0));
		}
		strSql = "select sum(CurrMonthWNum) as wSum from TB_UserInfo where ('"+noteNo+"'='0' or NoteNo=?) and ChaoBiaoDate like ?";
		cursor = db.rawQuery(strSql, new String[] { noteNo ,
				currDay + "%"});
		if (cursor.moveToNext()) {
			retItem.setZsl(cursor.getString(0));
		}
		strSql = "select sum(totalCharge)as wSum from TB_UserInfo where ('"+noteNo+"'='0' or NoteNo=?) and ChaoBiaoDate like ?";
		cursor = db.rawQuery(strSql, new String[] { noteNo ,
				currDay + "%"});
		if (cursor.moveToNext()) {
			retItem.setZfy(cursor.getString(0));
		}
		strSql = "select sum(totalCharge)as wSum from TB_UserInfo where ('"+noteNo+"'='0' or NoteNo=?) and ChaoBiaoTag in(3) and ChaoBiaoDate like ?";
		cursor = db.rawQuery(strSql, new String[] { noteNo ,
				currDay + "%"});
		if (cursor.moveToNext()) {
			retItem.setYsje(cursor.getString(0));
		}
		strSql = "select sum(totalCharge)as wSum from TB_UserInfo where ('"+noteNo+"'='0' or NoteNo=?) and ChaoBiaoTag in(1) and ChaoBiaoDate like ?";
		cursor = db.rawQuery(strSql, new String[] { noteNo ,
				currDay + "%"});
		if (cursor.moveToNext()) {
			retItem.setWsje(cursor.getString(0));
		}
		return retItem;
	}

	// 统计分析按日
	@SuppressLint("SimpleDateFormat")
	public WAnalysisItem GetAnalysisItemByDay(String noteNo) {
		WAnalysisItem retItem = new WAnalysisItem();
		String currDay = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

		String strSql = "select count(1) as cNum from TB_UserInfo where ('"+noteNo+"'='0' or NoteNo=?)";
		Cursor cursor = db.rawQuery(strSql,
				new String[] { noteNo });
		if (cursor.moveToNext()) {
			retItem.setTotle(cursor.getString(0));
		}
		strSql = "select count(1) as cNum from TB_UserInfo where ('"+noteNo+"'='0' or NoteNo=?) and ChaoBiaoTag in(1,3) and ChaoBiaoDate like ?";
		cursor = db.rawQuery(strSql, new String[] { noteNo,
				currDay + "%" });
		if (cursor.moveToNext()) {
			retItem.setYcb(cursor.getString(0));
		}
		strSql = "select count(1)as cNum from TB_UserInfo where ('"+noteNo+"'='0' or NoteNo=?) and ChaoBiaoTag=0 and ChaoBiaoDate like ?";
		cursor = db.rawQuery(strSql, new String[] { noteNo,currDay + "%"  });
		if (cursor.moveToNext()) {
			retItem.setWcb(cursor.getString(0));
		}
		strSql = "select count(1)as cNum from TB_FaultReport where ('"+noteNo+"'='0' or NoteNo=?) and AddDate like ?";
		cursor = db.rawQuery(strSql, new String[] { noteNo,
				currDay + "%" });
		if (cursor.moveToNext()) {
			retItem.setSbgz(cursor.getString(0));
		}
		strSql = "select sum(CurrMonthWNum) as wSum from TB_UserInfo where ('"+noteNo+"'='0' or NoteNo=?) and ChaoBiaoDate like ?";
		cursor = db.rawQuery(strSql, new String[] { noteNo,
				currDay + "%" });
		if (cursor.moveToNext()) {
			retItem.setZsl(cursor.getString(0));
		}
		strSql = "select sum(totalCharge)as wSum from TB_UserInfo where ('"+noteNo+"'='0' or NoteNo=?) and ChaoBiaoDate like ?";
		cursor = db.rawQuery(strSql, new String[] { noteNo,
				currDay + "%" });
		if (cursor.moveToNext()) {
			retItem.setZfy(cursor.getString(0));
		}
		strSql = "select sum(totalCharge)as wSum from TB_UserInfo where ('"+noteNo+"'='0' or NoteNo=?) and ChaoBiaoTag=3 and ChaoBiaoDate like ?";
		cursor = db.rawQuery(strSql, new String[] { noteNo,
				currDay + "%" });
		if (cursor.moveToNext()) {
			retItem.setYsje(cursor.getString(0));
		}
		strSql = "select sum(totalCharge)as wSum from TB_UserInfo where ('"+noteNo+"'='0' or NoteNo=?) and ChaoBiaoTag=1 and ChaoBiaoDate like ?";
		cursor = db.rawQuery(strSql, new String[] { noteNo,
				currDay + "%" });
		if (cursor.moveToNext()) {
			retItem.setWsje(cursor.getString(0));
		}
		return retItem;
	}

	// 添加阶梯价格
	public void AddStatgPrice(List<WaterPriceInfo> prInfos) {
		if (prInfos == null || prInfos.size() == 0) {
			return;
		}
		// db.beginTransaction();
		db.delete("TB_UnitPrice", "", null);
		for (WaterPriceInfo pInfo : prInfos) {
			ContentValues cvalues = new ContentValues();
			cvalues.put("SIndex", pInfo.getSIndex());
			cvalues.put("WFrom", pInfo.getWFrom());
			cvalues.put("WTo", pInfo.getWTo());
			cvalues.put("WPrice", pInfo.getWPrice());
			cvalues.put("PriceType", pInfo.getPriceType());
			cvalues.put("PriceTypeName", pInfo.getPriceTypeName());
			db.insert("TB_UnitPrice", "Unid", cvalues);
		}
		// db.setTransactionSuccessful();
		// db.endTransaction();
	}

	// Upload Data Before DownLoad
	public boolean CheckUnUpload() {
		String strSql = "select count(1) as cNum from TB_UserInfo where alreadyUpload=1";
		Cursor cursor = db.rawQuery(strSql, null);
		int cnum = 0;
		if (cursor.moveToNext()) {
			cnum = cursor.getInt(0);
		}
		if (cnum > 0) {
			return true;
		}
		strSql = "select count(1)as cnum from TB_FaultReport where alreadyUpload=1";
		cursor = db.rawQuery(strSql, null);
		if (cursor.moveToNext()) {
			cnum = cursor.getInt(0);
		}
		if (cnum > 0) {
			return true;
		}
		strSql = "select count(1)as cnum from TB_CustomAdvice where alreadyUpload=1 ";
		cursor = db.rawQuery(strSql, null);
		if (cursor.moveToNext()) {
			cnum = cursor.getInt(0);
		}
		if (cnum > 0) {
			return true;
		}
//		strSql = "select count(1)as cnum from TB_PicInfo where alreadyUpload=1 ";
//		cursor = db.rawQuery(strSql, null);
//		if (cursor.moveToNext()) {
//			cnum = cursor.getInt(0);
//		}
//		if (cnum > 0) {
//			return true;
//		}
//		strSql="select count(1) as cnum from TB_WaterCharge where alreadyUpload=1";
//		cursor=db.rawQuery(strSql, null);
//		if(cursor.moveToNext())
//		{
//			cnum=cursor.getInt(0);
//		}
//		if(cnum>0)
//		{
//			return true;
//		}
		return false;
	}

	//上传收费信息
	public List<WCBWaterChargeItem>getUploadChargeItems()
	{
		List<WCBWaterChargeItem>retList=new ArrayList<WCBWaterChargeItem>();
		String strSql="select * from TB_WaterCharge where alreadyUpload=1";
		Cursor cursor=db.rawQuery(strSql, null);
		while(cursor.moveToNext())
		{
			WCBWaterChargeItem chargeItem=new WCBWaterChargeItem();
			chargeItem.setCHARGEBCSS(cursor.getDouble(cursor.getColumnIndex("CHARGEBCSS")));
			chargeItem.setCHARGEBCYS(cursor.getDouble(cursor.getColumnIndex("CHARGEBCYS")));
			chargeItem.setCHARGEClASS(cursor.getString(cursor.getColumnIndex("CHARGEClASS")));
			chargeItem.setCHARGEDATETIME(cursor.getString(cursor.getColumnIndex("CHARGEDATETIME")));
			chargeItem.setCHARGEID(cursor.getString(cursor.getColumnIndex("CHARGEID")));
			chargeItem.setCHARGETYPEID(cursor.getInt(cursor.getColumnIndex("CHARGETYPEID")));
			chargeItem.setCHARGEWORKERID(cursor.getString(cursor.getColumnIndex("CHARGEWORKERID")));
			chargeItem.setCHARGEWORKERNAME(cursor.getString(cursor.getColumnIndex("CHARGEWORKERNAME")));
			chargeItem.setCHARGEYSBCSZ(cursor.getDouble(cursor.getColumnIndex("CHARGEYSBCSZ")));
			chargeItem.setCHARGEYSJSYE(cursor.getDouble(cursor.getColumnIndex("CHARGEYSJSYE")));
			chargeItem.setCHARGEYSQQYE(cursor.getDouble(cursor.getColumnIndex("CHARGEYSQQYE")));
			chargeItem.setEXTRACHARGECHARGE1(cursor.getDouble(cursor.getColumnIndex("EXTRACHARGECHARGE1")));
			chargeItem.setEXTRACHARGECHARGE2(cursor.getDouble(cursor.getColumnIndex("EXTRACHARGECHARGE2")));
			chargeItem.setMEMO(cursor.getString(cursor.getColumnIndex("MEMO")));
			chargeItem.setOVERDUEMONEY(cursor.getDouble(cursor.getColumnIndex("OVERDUEMONEY")));
			chargeItem.setRECEIPTNO(cursor.getString(cursor.getColumnIndex("RECEIPTNO")));
			chargeItem.setRECEIPTPRINTCOUNT(cursor.getInt(cursor.getColumnIndex("RECEIPTPRINTCOUNT")));
			chargeItem.setTOTALCHARGE(cursor.getDouble(cursor.getColumnIndex("TOTALCHARGE")));
			chargeItem.setTOTALNUMBERCHARGE(cursor.getInt(cursor.getColumnIndex("TOTALNUMBERCHARGE")));
			chargeItem.setWATERTOTALCHARGE(cursor.getDouble(cursor.getColumnIndex("WATERTOTALCHARGE")));
			retList.add(chargeItem);
		}
		return retList;
	}
	
	//获取ChargeInfo
	public WCBWaterChargeItem getChargeItem(String chargeId)
	{
		WCBWaterChargeItem chargeItem=null;
		String strSql="select * from TB_WaterCharge where alreadyUpload=1 and CHARGEID=?";
		Cursor cursor=db.rawQuery(strSql, new String[]{chargeId});
		if(cursor.moveToNext())
		{
			chargeItem=new WCBWaterChargeItem();
			chargeItem.setCHARGEBCSS(cursor.getDouble(cursor.getColumnIndex("CHARGEBCSS")));
			chargeItem.setCHARGEBCYS(cursor.getDouble(cursor.getColumnIndex("CHARGEBCYS")));
			chargeItem.setCHARGEClASS(cursor.getString(cursor.getColumnIndex("CHARGEClASS")));
			chargeItem.setCHARGEDATETIME(cursor.getString(cursor.getColumnIndex("CHARGEDATETIME")));
			chargeItem.setCHARGEID(cursor.getString(cursor.getColumnIndex("CHARGEID")));
			chargeItem.setCHARGETYPEID(cursor.getInt(cursor.getColumnIndex("CHARGETYPEID")));
			chargeItem.setCHARGEWORKERID(cursor.getString(cursor.getColumnIndex("CHARGEWORKERID")));
			chargeItem.setCHARGEWORKERNAME(cursor.getString(cursor.getColumnIndex("CHARGEWORKERNAME")));
			chargeItem.setCHARGEYSBCSZ(cursor.getDouble(cursor.getColumnIndex("CHARGEYSBCSZ")));
			chargeItem.setCHARGEYSJSYE(cursor.getDouble(cursor.getColumnIndex("CHARGEYSJSYE")));
			chargeItem.setCHARGEYSQQYE(cursor.getDouble(cursor.getColumnIndex("CHARGEYSQQYE")));
			chargeItem.setEXTRACHARGECHARGE1(cursor.getDouble(cursor.getColumnIndex("EXTRACHARGECHARGE1")));
			chargeItem.setEXTRACHARGECHARGE2(cursor.getDouble(cursor.getColumnIndex("EXTRACHARGECHARGE2")));
			chargeItem.setMEMO(cursor.getString(cursor.getColumnIndex("MEMO")));
			chargeItem.setOVERDUEMONEY(cursor.getDouble(cursor.getColumnIndex("OVERDUEMONEY")));
			chargeItem.setRECEIPTNO(cursor.getString(cursor.getColumnIndex("RECEIPTNO")));
			chargeItem.setRECEIPTPRINTCOUNT(cursor.getInt(cursor.getColumnIndex("RECEIPTPRINTCOUNT")));
			chargeItem.setTOTALCHARGE(cursor.getDouble(cursor.getColumnIndex("TOTALCHARGE")));
			chargeItem.setTOTALNUMBERCHARGE(cursor.getInt(cursor.getColumnIndex("TOTALNUMBERCHARGE")));
			chargeItem.setWATERTOTALCHARGE(cursor.getDouble(cursor.getColumnIndex("WATERTOTALCHARGE")));
		}
		return chargeItem;
	}
	
	//更新收费信息状态
	public void UpdateChargeTag(String chargeId)
	{
		ContentValues cValues=new ContentValues();
		cValues.put("alreadyUpload",0);
		db.update("TB_WaterCharge",cValues,"CHARGEID=?", new String[]{chargeId});
	}
	
	// Check Data Before DownLoad By NoteNO
	public boolean CheckUserUnUpload(WBBItem bbItem) {
		String strSql = "select count(1) as cNum from TB_UserInfo where PianNo=? "
				+ " and AreaNo=? and DuanNo=? and  alreadyUpload=1";
		Cursor cursor = db.rawQuery(strSql, new String[] { bbItem.getPianNo(),
				bbItem.getAreaNo(), bbItem.getDuanNo() });
		int cnum = 0;
		if (cursor.moveToNext()) {
			cnum = cursor.getInt(0);
		}
		if (cnum > 0) {
			return true;
		}
		return false;
	}

	public void InsertCharegeRecord(WCBWaterChargeItem wItem) {
		if (wItem == null) {
			return;
		}
		ContentValues cValues=new ContentValues();
		cValues.put("CHARGEID",wItem.getCHARGEID());
		cValues.put("TOTALNUMBERCHARGE", wItem.getTOTALNUMBERCHARGE());
		cValues.put("EXTRACHARGECHARGE1",wItem.getEXTRACHARGECHARGE1());
		cValues.put("EXTRACHARGECHARGE2",wItem.getEXTRACHARGECHARGE2());
		cValues.put("WATERTOTALCHARGE",wItem.getWATERTOTALCHARGE());
		cValues.put("TOTALCHARGE",wItem.getTOTALCHARGE());
		cValues.put("OVERDUEMONEY",wItem.getOVERDUEMONEY());
		cValues.put("CHARGETYPEID",wItem.getCHARGETYPEID());
		cValues.put("CHARGEClASS",wItem.getCHARGEClASS());
		cValues.put("CHARGEBCYS",wItem.getCHARGEBCYS());
		cValues.put("CHARGEBCSS",wItem.getCHARGEBCSS());
		cValues.put("CHARGEYSQQYE",wItem.getCHARGEYSQQYE());
		cValues.put("CHARGEYSBCSZ",wItem.getCHARGEYSBCSZ());
		cValues.put("CHARGEYSJSYE",wItem.getCHARGEYSJSYE());
		cValues.put("CHARGEWORKERID",wItem.getCHARGEWORKERID());
		cValues.put("CHARGEWORKERNAME",wItem.getCHARGEWORKERNAME());
		cValues.put("CHARGEDATETIME",wItem.getCHARGEDATETIME());
		cValues.put("RECEIPTPRINTCOUNT",wItem.getRECEIPTPRINTCOUNT());
		cValues.put("RECEIPTNO", wItem.getRECEIPTNO());
		cValues.put("MEMO",wItem.getMEMO());
		cValues.put("alreadyUpload", 1);
		db.insert("TB_WaterCharge", "CHARGEID",cValues);
	}

	//获取用户建议信息
	public List<WCustomerAdviceInfo>GetAllAdviceInfos()
	{
		List<WCustomerAdviceInfo> retList=new ArrayList<WCustomerAdviceInfo>();
		String strSql="select u.[UserFName],c.[AddDate],c.[Advice] " +
				" from TB_UserInfo u join TB_CustomAdvice c on  u.UserNo=c.UserNo " +
				" order by c.AddDate Desc";
		Cursor cursor=db.rawQuery(strSql,null);
		while(cursor.moveToNext())
		{
			WCustomerAdviceInfo cuAdviceInfo=new WCustomerAdviceInfo();
			cuAdviceInfo.setAddDate(cursor.getString(cursor.getColumnIndex("AddDate")));
			cuAdviceInfo.setUserName(cursor.getString(cursor.getColumnIndex("UserFName")));
			cuAdviceInfo.setAdvice(cursor.getString(cursor.getColumnIndex("Advice")));
			retList.add(cuAdviceInfo);
		}
		return retList;
	}
	//获取欠费用用户信息
	
	
	
	
	public void closeDB() {
		// 释放数据库资源
		db.close();
	}
}
