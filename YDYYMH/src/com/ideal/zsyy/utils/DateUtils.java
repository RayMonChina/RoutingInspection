package com.ideal.zsyy.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public class DateUtils {

	static SimpleDateFormat simpleFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static SimpleDateFormat simpFrom=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	public static String changeDateFormat(String strDate)
	{
		try{
			Date date=simpFrom.parse(strDate);
			return simpleFormat.format(date);
		}
		catch (Exception e) {
			// TODO: handle exception
			Log.e("date format error", e.getMessage()+"\r\n"+e.getStackTrace());
			return strDate;
		}
	}
}
