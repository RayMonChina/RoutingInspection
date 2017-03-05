package com.ideal.zsyy.entity;

public class RWorkType {
	private String typeid;
	private String typename;
	private String sortcode;
	public String getTypeid() {
		return typeid;
	}
	public void setTypeid(String typeid) {
		this.typeid = typeid;
	}
	public String getTypename() {
		return typename;
	}
	public void setTypename(String typename) {
		this.typename = typename;
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
		return typename;
	}
	
	
	
}
