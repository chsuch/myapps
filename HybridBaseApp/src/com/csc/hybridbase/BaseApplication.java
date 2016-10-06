package com.csc.hybridbase;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import android.app.Application;
import android.os.Build;
import android.webkit.WebView;

public class BaseApplication extends Application {
	
	private final String LOG_TAG = BaseApplication.class.getSimpleName();
	private static BaseApplication mInstance;
	private RequestQueue mRequestQueue;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
		Constants.APP_MODE = Utils.getCompileMode(this);
		
		HLog.d(LOG_TAG, "onCreate()...");
		
		HLog.d(LOG_TAG, Constants.APP_MODE == Constants.DEV_MODE ? "Debug Mode" : "Realease Mode");
		Constants.MAIN_URL = Constants.APP_MODE == Constants.DEV_MODE ? Constants.MAIN_URL_DEV : Constants.MAIN_URL_REL;//메인 URL 세팅
		
		HLog.d(LOG_TAG, "MAIN_URL ::: " + Constants.MAIN_URL);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			WebView.setWebContentsDebuggingEnabled(Constants.APP_MODE == Constants.DEV_MODE);
		}
	}
	
	public static synchronized BaseApplication getInstance(){
		return mInstance;
	}
	
	private RequestQueue getRequestQueue(){
		if(mRequestQueue == null){
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}
		return mRequestQueue;
	}
	
	public <T> void addToRequestQueue(Request<T> req){
		getRequestQueue().add(req);
	}
	
	public void cancelPendingRequests(Object tag){
		if(mRequestQueue != null){
			mRequestQueue.cancelAll(tag);
		}
	}
}
