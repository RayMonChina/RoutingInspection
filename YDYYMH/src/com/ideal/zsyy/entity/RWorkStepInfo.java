package com.ideal.zsyy.entity;

public class RWorkStepInfo {

	private String id;
	private String gjdmc;
	private String remark;
	private String sortcode;
	private String worktype;
	private String volclass;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getGjdmc() {
		return gjdmc;
	}
	public void setGjdmc(String gjdmc) {
		this.gjdmc = gjdmc;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getSortcode() {
		return sortcode;
	}
	public void setSortcode(String sortcode) {
		this.sortcode = sortcode;
	}
	public String getWorktype() {
		return worktype;
	}
	public void setWorktype(String worktype) {
		this.worktype = worktype;
	}
	public String getVolclass() {
		return volclass;
	}
	public void setVolclass(String volclass) {
		this.volclass = volclass;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.gjdmc;
	}
	
}
