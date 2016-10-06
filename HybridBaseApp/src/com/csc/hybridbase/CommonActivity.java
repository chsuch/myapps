package com.csc.hybridbase;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

@SuppressWarnings("unused")
public class CommonActivity extends Activity{
	
	private final String LOG_TAG = getClass().getSimpleName();
	
	Context mContext;
	BaseApplication mApp;
	CommonActivity mActivity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mContext = this;
		mActivity = this;
		mApp = (BaseApplication)getApplication();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
}
