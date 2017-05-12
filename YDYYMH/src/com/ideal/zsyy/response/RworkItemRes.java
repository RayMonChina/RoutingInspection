package com.ideal.zsyy.response;

import java.util.Date;

public class RworkItemRes {

	private String id ;
	private String ticketnum;
	private String remark; 
	private String createtime;
	private String sendperson;
	private String province;
	private String city;
	private String county;
	private String unit;
	private String state;
	private String userid;
	private String taskname;
	private String worktype;
	private String volclass;
	private String scenefile;
	private String gpsaddress;
	private String pickind;
	private String gjdmc;
	private String gpstime;
	private int recordcount;
	private int alreadyUpload;
	private int isDownload;
	private String peiheren;
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
	public String getRemark() {
		if(remark==null)return "";
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getCreatetime() {
		if(createtime==null)return "";
		return createtime;
	}
	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}
	public String getSendperson() {
		return sendperson;
	}
	public void setSendperson(String sendperson) {
		this.sendperson = sendperson;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	
	public String getCounty() {
		return county;
	}
	public void setCounty(String county) {
		this.county = county;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getTaskname() {
		if(taskname==null)return "";
		return taskname;
	}
	public void setTaskname(String taskname) {
		this.taskname = taskname;
	}
	public String getWorktype() {
		if(worktype==null)return "";
		return worktype;
	}
	public void setWorktype(String worktype) {
		this.worktype = worktype;
	}
	public String getVolclass() {
		if(volclass==null)return "";
		return volclass;
	}
	public void setVolclass(String volclass) {
		this.volclass = volclass;
	}
	public int getAlreadyUpload() {
		return alreadyUpload;
	}
	public void setAlreadyUpload(int alreadyUpload) {
		this.alreadyUpload = alreadyUpload;
	}
	public int getIsDownload() {
		return isDownload;
	}
	public void setIsDownload(int isDownload) {
		this.isDownload = isDownload;
	}
	public String getScenefile() {
		return scenefile;
	}
	public void setScenefile(String scenefile) {
		this.scenefile = scenefile;
	}
	public String getGpsaddress() {
		if(gpsaddress==null)return "";
		return gpsaddress;
	}
	public void setGpsaddress(String gpsaddress) {
		this.gpsaddress = gpsaddress;
	}
	public String getPickind() {
		return pickind;
	}
	public void setPickind(String pickind) {
		this.pickind = pickind;
	}
	public String getGjdmc() {
		if(gjdmc==null)return "";
		return gjdmc;
	}
	public void setGjdmc(String gjdmc) {
		this.gjdmc = gjdmc;
	}
	public String getGpstime() {
		return gpstime;
	}
	public void setGpstime(String gpstime) {
		this.gpstime = gpstime;
	}
	public int getRecordcount() {
		return recordcount;
	}
	public void setRecordcount(int recordcount) {
		this.recordcount = recordcount;
	}
	public String getPeiheren() {
		return peiheren;
	}
	public void setPeiheren(String peiheren) {
		this.peiheren = peiheren;
	}
	
}
