package com.ideal.zsyy.utils;

import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class UtilWifi {
	private Context mContext;
	private List mWifiConfigurations;
	private WifiInfo mWifiInfo;
	private List mWifiList;
	WifiManager.WifiLock mWifiLock;
	private WifiManager mWifiManager;

	public UtilWifi(Context paramContext) {
		this.mContext = paramContext;
		this.mWifiManager = ((WifiManager) paramContext.getSystemService("wifi"));
		this.mWifiInfo = this.mWifiManager.getConnectionInfo();
	}

	public static boolean isWifiDataEnable(Context paramContext) throws Exception {
		NetworkInfo networkInfo= ((ConnectivityManager) paramContext.getSystemService("connectivity")).getNetworkInfo(1);
		return (paramContext != null) && (networkInfo.isConnectedOrConnecting());
	}

	public void acquireWifiLock() {
		this.mWifiLock.acquire();
	}

	public void addNetWork(WifiConfiguration paramWifiConfiguration) {
		int i = this.mWifiManager.addNetwork(paramWifiConfiguration);
		this.mWifiManager.enableNetwork(i, true);
	}

	public int checkState() {
		return this.mWifiManager.getWifiState();
	}

	public void closeWifi() {
		if (isWifiDataEnable()) {
			this.mWifiManager.setWifiEnabled(false);
		}
	}

	public void createWifiLock() {
		this.mWifiLock = this.mWifiManager.createWifiLock("test");
	}

	public void disConnectionWifi(int paramInt) {
		this.mWifiManager.disableNetwork(paramInt);
		this.mWifiManager.disconnect();
	}

	public String getBSSID() {
		if (this.mWifiInfo == null) {
			return "NULL";
		}
		return this.mWifiInfo.getBSSID();
	}

	public List getConfiguration() {
		return this.mWifiConfigurations;
	}

	public int getIpAddress() {
		if (this.mWifiInfo == null) {
			return 0;
		}
		return this.mWifiInfo.getIpAddress();
	}

	public String getMacAddress() {
		if (this.mWifiInfo == null) {
			return "NULL";
		}
		return this.mWifiInfo.getMacAddress();
	}

	public int getNetWordId() {
		if (this.mWifiInfo == null) {
			return 0;
		}
		return this.mWifiInfo.getNetworkId();
	}

	public String getWifiInfo() {
		if (this.mWifiInfo == null) {
			return "NULL";
		}
		return this.mWifiInfo.toString();
	}

	public List getWifiList() {
		return this.mWifiList;
	}

	public boolean isWifiDataEnable() {
		NetworkInfo localNetworkInfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity"))
				.getNetworkInfo(1);
		return (localNetworkInfo != null) && (localNetworkInfo.isConnectedOrConnecting());
	}

	public StringBuffer lookUpScan() {
		StringBuffer localStringBuffer = new StringBuffer();
		int i = 0;
		for (;;) {
			if (i >= this.mWifiList.size()) {
				return localStringBuffer;
			}
			localStringBuffer.append("Index_" + new Integer(i + 1).toString() + ":");
			localStringBuffer.append(this.mWifiList.get(i).toString()).append("n");
			i += 1;
		}
	}

	public void openWifi() {
		if (!isWifiDataEnable()) {
			this.mWifiManager.setWifiEnabled(true);
		}
	}

	public void releaseWifiLock() {
		if (this.mWifiLock.isHeld()) {
			this.mWifiLock.acquire();
		}
	}

	public void startScan() {
		this.mWifiManager.startScan();
		this.mWifiList = this.mWifiManager.getScanResults();
		this.mWifiConfigurations = this.mWifiManager.getConfiguredNetworks();
	}
}