package com.ideal.zsyy.entity;

public class WPointItem {

	private double Latitude;
	private double Longitude;
	private String UserId ;
	private String UserName;
	private String AddDate;
	private int alreadyUpload;
	private String Address;
	public String getAddress() {
		if(Address!=null&&Address.length()>300)
		{
			Address=Address.substring(0, 290);
		}
		return Address;
	}
	public void setAddress(String address) {
		Address = address;
	}
	public double getLatitude() {
		return Latitude;
	}
	public void setLatitude(double latitude) {
		Latitude = latitude;
	}
	public double getLongitude() {
		return Longitude;
	}
	public void setLongitude(double longitude) {
		Longitude = longitude;
	}
	public String getUserId() {
		return UserId;
	}
	public void setUserId(String userId) {
		UserId = userId;
	}
	public String getUserName() {
		return UserName;
	}
	public void setUserName(String userName) {
		UserName = userName;
	}
	public String getAddDate() {
		return AddDate;
	}
	public void setAddDate(String addDate) {
		AddDate = addDate;
	}
	public int getAlreadyUpload() {
		return alreadyUpload;
	}
	public void setAlreadyUpload(int alreadyUpload) {
		this.alreadyUpload = alreadyUpload;
	}
	
}
