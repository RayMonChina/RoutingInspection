package com.ideal.zsyy.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.ideal.zsyy.Config;
import com.ideal.zsyy.response.RUserInfoRes;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferencesService {

	private Context context;

	public final static String SPF = "DATA_CACHE_xunjian";

	public PreferencesService(Context context) {
		this.context = context;
	}

	public void clearLogin() {
		SharedPreferences preferences = context.getSharedPreferences(SPF,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();

		editor.putBoolean("isLogin", false);
		editor.commit();
	}
	
	public void saveLogin() {
		SharedPreferences preferences = context.getSharedPreferences(SPF,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();

		editor.putBoolean("isLogin", true);
		editor.commit();
	}

	public boolean getIsLogin() {
		
		SharedPreferences preferences = context.getSharedPreferences(SPF,
				Context.MODE_PRIVATE);
		return preferences.getBoolean("isLogin", false);
	}
	
	public void setGpsTrackEnable(boolean trackStatus)
	{
		SharedPreferences preferences = context.getSharedPreferences(SPF,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean("gps_track", trackStatus);
		editor.commit();
	}
	
	public boolean getGpsTrackEnable()
	{
		SharedPreferences preferences = context.getSharedPreferences(SPF,
				Context.MODE_PRIVATE);
		return preferences.getBoolean("gps_track", false);
	}
	
	public void setAutoLogin(boolean isAuto)
	{
		SharedPreferences preferences = context.getSharedPreferences(SPF,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean("auto_login", isAuto);
		editor.commit();
	}
	public boolean getAutoLoginStatus()
	{
		SharedPreferences preferences = context.getSharedPreferences(SPF,
				Context.MODE_PRIVATE);
		return preferences.getBoolean("auto_login", false);
	}

	public void saveLoginInfo(String loginName, String pwd, boolean isLogin,Boolean remberPwd,String use_id,String userName,String user_Code) {
		SharedPreferences preferences = context.getSharedPreferences(SPF,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();

		editor.putString("loginName", loginName);
		editor.putString("pwd", pwd);
		editor.putBoolean("isLogin", isLogin);
		editor.putBoolean("remberPwd", remberPwd);
		editor.putString("use_id", use_id);
		editor.putString("userName", userName);
		editor.putString("user_Code", user_Code);
		editor.commit();
	}
	

	public Map<String, Object> getLoginInfo() {
		Map<String, Object> params = new HashMap<String, Object>();
		SharedPreferences preferences = context.getSharedPreferences(SPF,
				Context.MODE_PRIVATE);
		params.put("loginName", preferences.getString("loginName", ""));
		params.put("pwd", preferences.getString("pwd", ""));
		params.put("isLogin", preferences.getBoolean("isLogin", true));
		params.put("use_id", preferences.getString("use_id", ""));
		params.put("userName", preferences.getString("userName",""));
		params.put("remberPwd", preferences.getBoolean("remberPwd", false));
		params.put("user_Code",preferences.getString("user_Code", ""));
		return params;
	}
	
	public String getUserCode()
	{
		Map<String, Object>mapUserInfo=getLoginInfo();
		return mapUserInfo.get("user_Code").toString();
	}
	
	public void saveAutoUpdate(boolean isFlag){
		SharedPreferences preferences = context.getSharedPreferences(SPF,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();

		editor.putBoolean("isAuto", isFlag);
		editor.commit();
	}
	
	public boolean getIsAutpUpdate(){
		SharedPreferences preferences = context.getSharedPreferences(SPF,
				Context.MODE_PRIVATE);
		boolean isAuto = preferences.getBoolean("isAutp", true);
		return isAuto;
	}
	
	public void saveSkin(String skinName) {
		SharedPreferences preferences = context.getSharedPreferences(SPF,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();

		editor.putString("skinName", skinName);
		editor.commit();
	}
	
	public Map<String, Object> getSkin() {
		
		Map<String, Object> params = new HashMap<String, Object>();
		SharedPreferences preferences = context.getSharedPreferences(SPF,
				Context.MODE_PRIVATE);
		
		params.put("skinName", preferences.getString("skinName", Config.SKIN_DEFAULT));
		return params;
	}
	
	public boolean getIslauncher(){
		SharedPreferences preferences = context.getSharedPreferences(SPF,
				Context.MODE_PRIVATE);
		boolean isAuto = preferences.getBoolean("Islauncher", false);
		return isAuto;
	}

	public void saveIslauncher(boolean Islauncher){
		SharedPreferences preferences = context.getSharedPreferences(SPF,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();

		editor.putBoolean("Islauncher", Islauncher);
		editor.commit();
	}
	
	public void saveCBDate()
	{
		SharedPreferences preferences=context.getSharedPreferences(SPF, Context.MODE_PRIVATE);
		Date dtNow=new Date();
		int year=dtNow.getYear();
		int month=dtNow.getMonth()+1;
		Editor editor=preferences.edit();
		editor.putInt("cYear",year);
		editor.putInt("cMonth", month);
		editor.commit();
	}
	
	public void saveCurrentCustomerInfo(String StealNo,int orderNo, String pianNo,String areaNo,String duanNO,String noteNo) {
		SharedPreferences preferences = context.getSharedPreferences(SPF,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();

		editor.putString("StealNo", StealNo);
		editor.putInt("OrderNo", orderNo);
		editor.putString("PianNo", pianNo);
		editor.putString("AreaNo", areaNo);
		editor.putString("DuanNo", duanNO);
		editor.putString("NoteNo", noteNo);
		editor.commit();
	}
	
	public Map<String,Integer>GetCBDateInfo()
	{
		Map<String, Integer>retVal=new HashMap<String,Integer>();
		SharedPreferences preferences=context.getSharedPreferences(SPF, Context.MODE_PRIVATE);
		retVal.put("cYear",preferences.getInt("cYear",0));
		retVal.put("cMonth",preferences.getInt("cMonth", 0));
		return retVal;
	}
	
	public Map<String, Object>GetCurrentCustomerInfo()
	{
		Map<String,Object>retVal=new HashMap<String, Object>();
		SharedPreferences preferences=context.getSharedPreferences(SPF, context.MODE_PRIVATE);
		retVal.put("StealNo",preferences.getString("StealNo",""));
		retVal.put("NoteNo", preferences.getString("NoteNo",""));
		retVal.put("OrderNo", preferences.getInt("OrderNo", 0));
		retVal.put("PianNo", preferences.getString("PianNo",""));
		retVal.put("AreaNo", preferences.getString("AreaNo",""));
		retVal.put("DuanNo", preferences.getString("DuanNo",""));
		return retVal;
	}
	
	public void SaveDeviceId(String deviceid)
	{
		SharedPreferences preferences=context.getSharedPreferences(SPF,context.MODE_PRIVATE);
		Editor editor= preferences.edit();
		editor.putString("deviceId",deviceid);
		editor.commit();
	}
	
	public String GetDeviceId()
	{
		SharedPreferences preferences=context.getSharedPreferences(SPF,context.MODE_PRIVATE);
		return preferences.getString("deviceId","");
	}
	
	public int GetChargeIndex()
	{
		SharedPreferences preferences=context.getSharedPreferences(SPF,context.MODE_PRIVATE);
		return preferences.getInt("chargeIndex",0);
	}
	
	public void SaveChargeIndex(int cindex)
	{
		SharedPreferences preferences=context.getSharedPreferences(SPF,context.MODE_PRIVATE);
		Editor editor=preferences.edit();
		editor.putInt("chargeIndex",cindex);
		editor.commit();
	}
	
	public void SaveBlueDevice(String deviceName,String address)
	{
		SharedPreferences preferences=context.getSharedPreferences(SPF, context.MODE_PRIVATE);
		Editor editor=preferences.edit();
		editor.putString("blueName", deviceName);
		editor.putString("blueAddress", address);
		editor.commit();
	}
	
	public Map<String,String>GetBlueDevice()
	{
		SharedPreferences preferences=context.getSharedPreferences(SPF, context.MODE_PRIVATE);
		Map<String,String>retDevice=new HashMap<String, String>();
		retDevice.put("blueName", preferences.getString("blueName",""));
		retDevice.put("blueAddress",preferences.getString("blueAddress",""));
		return retDevice;
	}
	//RONG 2016-5-4
	public void SaveZoomSize(float scale)
	{
		SharedPreferences preferences=context.getSharedPreferences(SPF,context.MODE_PRIVATE);
		Editor editor=preferences.edit();
		editor.putFloat("ZoomSize", scale);
		editor.commit();
	}
	//RONG 2016-5-4
	public float GetZoomSize()
	{
		SharedPreferences preferences=context.getSharedPreferences(SPF,context.MODE_PRIVATE);
		return preferences.getFloat("ZoomSize", 18f);
	}
	
}
