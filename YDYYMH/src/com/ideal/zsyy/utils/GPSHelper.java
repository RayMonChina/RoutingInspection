package com.ideal.zsyy.utils;

import android.content.Context;
import android.location.LocationManager;

public class GPSHelper {

	public static boolean checkIsOpen(Context context) {
		boolean result = false;
		LocationManager lManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		if (!lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
				|| !lManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			result = false;
		} else {
			result = true;
		}
		return result;
	}
	
}
