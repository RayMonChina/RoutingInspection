package com.ideal.zsyy.db;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WdbHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 16;
	private static String DATABASE_NAME = "r_db";
	public WdbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	public WdbHelper(Context context,String dbName)
	{
		super(context, DATABASE_NAME+"_"+dbName, null, DATABASE_VERSION);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		this.ResetTable(db);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		this.ResetTable(db);
	}
	
	public void ResetTable(SQLiteDatabase db)
	{
		String strSql="drop table IF EXISTS  TB_Works";
		db.execSQL(strSql);
		strSql="drop table IF EXISTS TB_LPoint";
		db.execSQL(strSql);
		strSql="drop table IF EXISTS TB_MediaInfo";
		db.execSQL(strSql);
		strSql="drop table IF EXISTS TB_WorkType";
		db.execSQL(strSql);
		strSql="drop table IF EXISTS TB_Volclass";
		db.execSQL(strSql);
		strSql="drop table IF EXISTS TB_WorkStep";
		db.execSQL(strSql);
		strSql="drop table IF EXISTS TB_LocalFileMap";
		db.execSQL(strSql);
		
		strSql="CREATE TABLE IF NOT EXISTS TB_Works("+
				 "id nvarchar(100),"+ 
				 "ticketnum nvarchar(50),"+
				 "remark varchar(300),"+
				 "createtime varchar(50),"+
				 "addDate varchar(50),  "+
				 "sendperson nvarchar(50),"+
				 "alreadyUpload TINYINT,"+
				 "province nvarchar(50),"+
				 "city nvarchar(50),"+
				 "county nvarchar(50),"+
				 "unit nvarchar(50),"+
				 "state nvarchar(10),"+
				 "userid nvarchar(50),"+
				 "taskname nvarchar(50),"+
				 "worktype nvarchar(50),"+
				 "volclass nvarchar(50),"
				 + " recordcount int,"
				 + "isDownLoad int,"
				 + "gjdmc nvarchar(100),"
				 + "peiheren nvarchar(50))";
		db.execSQL(strSql);              
		
		strSql="CREATE TABLE IF NOT EXISTS TB_MediaInfo"+
				"(id nvarchar(50),"+
				"ticketnum nvarchar(50),"+	
				"pickind nvarchar(20),"+		
				"gpsaddress	nvarchar(100),"+
				"gpstime varchar(50),"+	
				"lon nvarchar(50),"	+
				"lat nvarchar(50),"	+
				"picpath nvarchar(400),"+	
				"userid	nvarchar(50),"	+
				"gjdmc nvarchar(100),"	+
				"createtime	varchar(23),"+	
				"remark nvarchar(300),"
				+ "localpath nvarchar(300),"
				+ "hasdown int,"
				+ "islast int,"
				+ "alreadyUpload int)";
		db.execSQL(strSql); 
		
		strSql="  CREATE TABLE IF NOT EXISTS TB_LPoint(" +
				" Latitude real," +
				" Longitude real,UserId varchar(50),UserName varchar(50),Address nvarchar(300)," +
				" AddDate varchar(50),alreadyUpload TINYINT);";
		db.execSQL(strSql);
		
		strSql=" CREATE TABLE IF NOT EXISTS TB_WorkType("+
				"typeid	nvarchar(50),"+
				"typename	nvarchar(50),"+	
				"sortcode int)";
		db.execSQL(strSql);
		
		strSql="CREATE TABLE IF NOT EXISTS TB_Volclass("
				+ "typeid	nvarchar(50),"
				+ "typename	nvarchar(50),"
				+ "sortcode	int)";
		db.execSQL(strSql);
		
		strSql="CREATE TABLE IF NOT EXISTS TB_WorkStep("
				+ "id nvarchar(50),"
				+ "gjdmc nvarchar(100),	"
				+ "remark	nvarchar(200),"
				+ "sortcode int,"
				+ "worktype nvarchar(50),"
				+ "volclass nvarchar(50))";
		db.execSQL(strSql);
		
		strSql="CREATE TABLE IF NOT EXISTS TB_LocalFileMap("
				+ "id nvarchar(50) primary key,"
				+ "localFilePath nvarchar(300),"
				+ "adddate nvarchar(30))";
		db.execSQL(strSql);
	}

}
