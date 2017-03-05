package com.ideal.zsyy.response;

import java.io.Serializable;

public class RBaseRes<T> implements Serializable {
	private String status;
	private T data;
	private static final long serialVersionUID = -3440061414071692254L;
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

}
