package com.ideal.zsyy.entity;

public class RDepartmentInfo {

	private String orgid;
	private String orgname;
	private String parentid;
	private String sortcode;
	public String getOrgid() {
		return orgid;
	}
	public void setOrgid(String orgid) {
		this.orgid = orgid;
	}
	public String getOrgname() {
		return orgname;
	}
	public void setOrgname(String orgname) {
		this.orgname = orgname;
	}
	public String getParentid() {
		return parentid;
	}
	public void setParentid(String parentid) {
		this.parentid = parentid;
	}
	public String getSortcode() {
		return sortcode;
	}
	public void setSortcode(String sortcode) {
		this.sortcode = sortcode;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return orgname;
	}
	
}
