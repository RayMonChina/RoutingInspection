package com.ideal.zsyy.entity;

public class RWorkMediaInfo {

	private String id;
	private String ticketnum;
	private String pickind;
	private String gpsaddress;
	private String gpstime;
	private double lon;
	private double lat;
	private String picpath;
	private String userid;
	private String gjdmc;
	private String createtime;
	private String remark;
	private int alreadyUpload;
	private int hasdown;
	private int islast;
	private boolean isSelect;
	private String localFilePath;
	private String usermac;
	
	public String getUsermac() {
		return usermac;
	}
	public void setUsermac(String usermac) {
		this.usermac = usermac;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTicketnum() {
		return ticketnum;
	}
	public void setTicketnum(String ticketnum) {
		this.ticketnum = ticketnum;
	}
	public String getPickind() {
		return pickind;
	}
	public void setPickind(String pickind) {
		this.pickind = pickind;
	}
	public String getGpsaddress() {
		if(gpsaddress==null)
		{
			return "";
		}
		return gpsaddress;
	}
	public void setGpsaddress(String gpsaddress) {
		this.gpsaddress = gpsaddress;
	}
	public String getGpstime() {
		if(gpstime==null) return "";
		return gpstime;
	}
	public void setGpstime(String gpstime) {
		this.gpstime = gpstime;
	}
	public double getLon() {
		return lon;
	}
	public void setLon(double lon) {
		this.lon = lon;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public String getPicpath() {
		return picpath;
	}
	public void setPicpath(String picpath) {
		this.picpath = picpath;
	}
	public String getUserid() {
		if(userid==null)return "";
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getGjdmc() {
		if(gjdmc==null)return "";
		return gjdmc;
	}
	public void setGjdmc(String gjdmc) {
		this.gjdmc = gjdmc;
	}
	public String getCreatetime() {
		return createtime;
	}
	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}
	public int getHasdown() {
		return hasdown;
	}
	public void setHasdown(int hasdown) {
		this.hasdown = hasdown;
	}
	public int getIslast() {
		return islast;
	}
	public void setIslast(int islast) {
		this.islast = islast;
	}
	public String getRemark() {
		if(remark==null)return "";
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public int getAlreadyUpload() {
		return alreadyUpload;
	}
	public void setAlreadyUpload(int alreadyUpload) {
		this.alreadyUpload = alreadyUpload;
	}
	public boolean isSelect() {
		return isSelect;
	}
	public void setSelect(boolean isSelect) {
		this.isSelect = isSelect;
	}
	public String getLocalFilePath() {
		return localFilePath;
	}
	public void setLocalFilePath(String localFilePath) {
		this.localFilePath = localFilePath;
	}
	
}
