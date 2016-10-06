package com.csc.hybridbase;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.MailTo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions.Callback;
import com.csc.hybridbase.R;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CommonWebViewActivity extends CommonActivity{
	
	private final String LOG_TAG = getClass().getSimpleName();
	boolean clearHistory = false;
	WebView mWebView;
//	CommonProgress mProgress;
//	Timer mTimeoutTimer;
//	boolean isPageFinished;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mWebView = (WebView)findViewById(R.id.wv);
		
//		mTimeoutTimer = new Timer();
		setWebviewSettings();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private void setWebviewSettings() {
		
		//block longClick
		mWebView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				return true;
			}
		});
		mWebView.setHapticFeedbackEnabled(false);//LongClick시 발생되는 진동 제거
		//block longClick
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);
		}
		
		WebSettings webSettings = mWebView.getSettings();
//		webSettings.setUseWideViewPort(true);
//		webSettings.setAllowContentAccess(true);
//		webSettings.setAllowFileAccess(true);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setSupportMultipleWindows(true);//onCreateWindow가 적용 되도록 설정-외부부라우저 오픈
		webSettings.setGeolocationEnabled(true);//위치정보를 사용 할 수 있도록 설정
		webSettings.setDomStorageEnabled(true);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		webSettings.setAllowFileAccessFromFileURLs(true);
		webSettings.setAllowUniversalAccessFromFileURLs(true);
		webSettings.setTextZoom(100);//설정에서 글자크기를 조정해도 고정되도록 100%로 설정
		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
//		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
		
		
		mWebView.setWebViewClient(new WebViewClient(){

			@Override
			public void onPageStarted(final WebView view, final String url, Bitmap favicon) {
				HLog.d(LOG_TAG, "onPageStarted()..." + url);
				if(Utils.isNetConnected(mContext)){
//					mProgress = CommonProgress.show(mContext, "", "", false, true);
					super.onPageStarted(view, url, favicon);
				}else{
					view.stopLoading();
					CommonDialog.showDialog(mContext, getString(R.string.can_not_use_network) + "\n네트워크 확인 후 다시 한번 시도해 주세요.");
				}
			}
			
			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				HLog.e(LOG_TAG, String.format("onReceivedError()... %s[%d] - %s", description, errorCode, failingUrl));
//				mProgress.dismiss();
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				HLog.d(LOG_TAG, "shouldOverrideUrlLoading()..." + url);
				if (url.startsWith("tel:")) {
	                Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse(url)); 
	                startActivity(tel);
	                return true;
	            } else if (url.startsWith("mailto:")) {
	            	MailTo mt = MailTo.parse(url);
	            	Utils.sendEmail(mActivity, mt.getTo(), mt.getSubject(), mt.getBody());
	                return true;
	            } else if (url.startsWith("market:")) {
	            	startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
					return true;
	            } else if (url.startsWith("sms:")) {
	            	startActivity(Intent.createChooser(new Intent(Intent.ACTION_SENDTO, Uri.parse(url)), "SMS 클라이언트를 선택하세요."));
//	            	startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
					return true;
	            } else if(URLUtil.isValidUrl(url) == false){
	            	CommonDialog.showDialog(mContext, "로드 할 수 없는 URL입니다.");
	            	return true;
	            } else{
	            	return false;
	            }
			}

			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
				String errorMsg = "";
				switch (error.getPrimaryError()) {
				case SslError.SSL_DATE_INVALID:
					errorMsg = "SSL_DATE_INVALID";
					break;
				case SslError. SSL_EXPIRED:
					errorMsg = "SSL_EXPIRED";
					break;
				case SslError.SSL_IDMISMATCH:
					errorMsg = "SSL_IDMISMATCH";
					break;
				case SslError.SSL_INVALID:
					errorMsg = "SSL_INVALID";
					break;
				case SslError.SSL_NOTYETVALID:
					errorMsg = "SSL_NOTYETVALID";
					break;
				case SslError.SSL_UNTRUSTED:
					errorMsg = "SSL_UNTRUSTED";
					break;
				default:
					break;
				}
				
				HLog.e(LOG_TAG, "onReceivedSslError : " + errorMsg);
				
				if(Constants.APP_MODE == Constants.DEV_MODE){
					handler.proceed();
				}else{
					CommonDialog.showDialog(mContext, "서버 SSL인증서에 문제가 있어 페이지에 접근 할 수 없습니다." + (TextUtils.isEmpty(errorMsg) ? "" : "\n"+errorMsg), "확인", new OnClickListener(){

						@Override
						public void onClick(View v) {
							CommonDialog.closeDialog();
							finish();
						}
					});
				}
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				HLog.d(LOG_TAG, "onPageFinished()..." + url);
				super.onPageFinished(view, url);
				if(clearHistory){
					mWebView.clearHistory();
					clearHistory = false;
				}
			}
		});
		
		mWebView.setWebChromeClient(new WebChromeClient(){
        	@Override
        	public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result){
        		CommonDialog.showDialog(mContext, message, "확인",  new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						CommonDialog.closeDialog();
						result.confirm();
					}
				});
        		return true;
        	};
        	
        	@Override
			public boolean onConsoleMessage(ConsoleMessage cm) {
				HLog.d("Console message", cm.message() + " -- From line " +  cm.lineNumber() + " of " + cm.sourceId());
				return true;
			}

			@Override
			public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
				WebView newWebView = new WebView(view.getContext());
				WebView.WebViewTransport transport = (WebView.WebViewTransport)resultMsg.obj;
				transport.setWebView(newWebView);
				resultMsg.sendToTarget();
				return true;
			}

			@Override
        	public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
				CommonDialog.showDialog(mContext, message, new String[]{"취소", "확인"},  new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						switch (v.getId()) {
						case R.id.btnOk:
							result.confirm();
							break;
						default:
							result.cancel();
							break;
						}
						CommonDialog.closeDialog();
					}
				});
        		return true;
        	}

			@Override
			public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
				super.onGeolocationPermissionsShowPrompt(origin, callback);
				callback.invoke(origin, true, false);
			}
        });
		
	}

	@Override
	public void onBackPressed() {
		//백키를 앱 종료로 사용
		if(this instanceof MainActivity){
			CommonDialog.showDialog(mContext, "알림", "종료하시겠습니까?", new String[]{"취소", "확인"}, new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					CommonDialog.closeDialog();
					switch (v.getId()) {
					case R.id.btnOk:
						finish();
						break;
					}
				}
			});
		}
		
		//백키를 뒤로 가기로 사용
//		if(mWebView.canGoBack()){
//			mWebView.goBack();
//		}else{
//			if(this instanceof MainActivity){
//				CommonDialog.showDialog(mContext, "알림", "종료하시겠습니까?", new String[]{"취소", "확인"}, new OnClickListener() {
//					
//					@Override
//					public void onClick(View v) {
//						CommonDialog.closeDialog();
//						switch (v.getId()) {
//						case R.id.btnOk:
//							finish();
//							break;
//						}
//					}
//				});
//			}else{
//				super.onBackPressed();
//			}
//		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mWebView.destroy();
		mWebView.destroyDrawingCache();
		mWebView.clearCache(false);
	}
	
}
