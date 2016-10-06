package com.csc.hybridbase;

import android.util.Log;

/**
 * Release 모드 일 경우에만 로그를 표시 한다(error, warning로그는 제외)
 *
 */
public class HLog {
	public static void e(String tag, String msg) {
		Log.e(tag, msg);
	}

	public static void d(String tag, String msg) {
		if (Constants.APP_MODE == Constants.DEV_MODE) {
			Log.d(tag, msg);
		}
	}

	public static void i(String tag, String msg) {
		if (Constants.APP_MODE == Constants.DEV_MODE) {
			Log.i(tag, msg);
		}
	}

	public static void v(String tag, String msg) {
		if (Constants.APP_MODE == Constants.DEV_MODE) {
			Log.v(tag, msg);
		}
	}

	public static void w(String tag, String msg) {
		Log.w(tag, msg);
	}
}
