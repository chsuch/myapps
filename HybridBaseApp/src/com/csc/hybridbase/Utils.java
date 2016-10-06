package com.csc.hybridbase;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View.OnClickListener;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.widget.Toast;

public class Utils {
	
	public static final String LOG_TAG = Utils.class.getSimpleName();
	
	/**
	 * WebView를 통해 자바스크립트를 호출
	 * @param wv 웹뷰
	 * @param functionNm 자바스크립트명(괄호제외)
	 * @param params 자바스크립트에 들어갈 파라메터
	 */
	public static void callJavaScript(WebView wv, String functionNm, String... params){
		StringBuffer param = new StringBuffer();
		for(int i=0; i<params.length; i++){
			param.append("'" + params[i] + "'");
			if(i < params.length-1){
				param.append(",");
			}
		}
		String javascript = "";
		if(param.length() == 0){
			javascript = String.format("javascript:%s()", functionNm);
		}else{
			javascript = String.format("javascript:%s(%s)", functionNm, param.toString());
		}
		HLog.d(LOG_TAG, javascript);
		wv.loadUrl(javascript);
	}

	/**
	 * 앱 모드를 리턴
	 * 
	 * @param context
	 * @return REL_MODE = 0, DEV_MODE = 2
	 */
	public static int getCompileMode(Context context) {
		return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE);
	}
	
	/**
	 * 핸드폰 번호를 리턴
	 * @param context
	 * @return 핸드폰 번호
	 */
	public static String getMobileNumber(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String mobileNo = telephonyManager.getLine1Number();
		if (!TextUtils.isEmpty(mobileNo)) {
			mobileNo = mobileNo.replace("+82", "0");
		}
		HLog.d("Utils", "mobileNo :::" + mobileNo + ":::");
		return mobileNo;
	}
	
	/**
	 * 네트워크에 연결 되어있는지를 확인
	 * @param context
	 * @return connected true otherwise false
	 */
	public static boolean isNetConnected(Context context) {
		ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
		
		return (netInfo != null && netInfo.isConnected() && netInfo.isAvailable());
	}

    /**
     * "DEFAULT_PREF"에 String preference를 저장함
     * @param context
     * @param key
     * @param value
     */
    public static void setStringToPref(Context context, String key, String value) {
    	SharedPreferences pref = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    	Editor editor = pref.edit();
    	editor.putString(key, value);
    	editor.commit();
    }
    
    /**
     * "DEFAULT_PREF"에 Boolean preference를 저장함
     * @param context
     * @param key
     * @param value
     */
    public static void setBooleanToPref(Context context, String key, boolean value) {
    	SharedPreferences pref = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    	Editor editor = pref.edit();
    	editor.putBoolean(key, value);
    	editor.commit();
    }
    
    /**
     * "DEFAULT_PREF"로 부터 preference Boolean 값을 가져옴
     * @param context
     * @param key
     * @param defValue
     * @return
     */
    public static boolean getBooleanFromPref(Context context, String key, boolean defValue) {
    	SharedPreferences pref = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    	return pref.getBoolean(key, defValue);
    }
    
    /**
     * "DEFAULT_PREF"로 부터 preference String 값을 가져옴
     * @param context
     * @param key
     * @param defValue
     * @return
     */
    public static String getStringFromPref(Context context, String key, String defValue) {
    	SharedPreferences pref = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    	return pref.getString(key, defValue);
    }
    
	public static void deleteFiles(String dirPath){
		File dir = new File(dirPath);
		if(dir.exists()){
			File[] list = dir.listFiles();
			if(list != null){
				for(File f : list){
					f.delete();
				}
			}
		}
	}
	
	/**
	 * packageName 패키지가 존재하는지 확인
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static boolean hasPackage(Context context, String packageName){
		PackageManager mgr = (PackageManager)context.getPackageManager();
		Intent i = mgr.getLaunchIntentForPackage(packageName);
		return i != null;
	}
	
	/**
	 * 브라우저 열기
	 * @param context
	 * @param url
	 */
	public static void openBrowser(Context context, String url){
		if(URLUtil.isValidUrl(url)){
			Uri uri = Uri.parse(url);
			Intent i = new Intent(Intent.ACTION_VIEW);
			if(hasPackage(context, "com.android.chrome")){
				i.setPackage("com.android.chrome");
			}
			i.setData(uri);
			try {
				context.startActivity(i);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(context, "실행 할 브라우저가 단말에 없습니다.", Toast.LENGTH_SHORT).show();
			}
		}else{
			Toast.makeText(context, "Url 형식에 맞지 않습니다.", Toast.LENGTH_SHORT).show();
		}
	}
	
	public static void sendSMS(Context context, String receiverNum, String content){
		PendingIntent sentPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(Constants.ACTION_SMS_SENT), 0);
//		PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(Constants.ACTION_SMS_DELIVERED), 0);
		SmsManager smsMgr = SmsManager.getDefault();
		
		smsMgr.sendTextMessage(receiverNum, null, content, sentPendingIntent, null);
	}
	
	public static void sendEmail(Activity activity, String addr, String subject, String body){
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{addr});
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(Intent.EXTRA_TEXT, body);
		
		emailIntent.setType("message/rfc822");
//		emailIntent.setType("application/octet-stream");
		activity.startActivity(Intent.createChooser(emailIntent, "E-mail client를 선택하세요."));
	}
	
	public static float convertDpToPixel(Context c, float dp){
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, c.getResources().getDisplayMetrics());
	}
	
	public static float convertPixelToDp(Context c, float pixel){
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, pixel, c.getResources().getDisplayMetrics());
	}

	/**
	 * 머쉬멜로우 버전부터는 퍼미션을 체크하는 로직이 필요하여
	 * 퍼미션이 없는 경우 안내 팝업 및 앱정보 화면으로 이동 시키기 위한 메소드
	 * @param activity
	 * @param permissionHandler
	 * @param permissions
	 * @param reqCode
	 */
	public static void requestPermission(final CommonActivity activity, final Handler permissionHandler, final String[] permissions, final int reqCode){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
			if(isAllPermissionGranted(activity, permissions)){
				permissionHandler.sendEmptyMessage(reqCode);
			}else{
				ActivityCompat.requestPermissions(activity, permissions, reqCode);
			}
		}else{
			permissionHandler.sendEmptyMessage(reqCode);
		}
	}
	
	public static String[] getDeniedPermissions(Context context){
		List<String> permList = new ArrayList<String>();
		for(String permission : getPermissions(context)){
			if(ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED){
				permList.add(permission);
			}
		}
		return permList.toArray(new String[permList.size()]);
	}
	
	public static String[] getPermissions(Context context){
		String[] permissions = null;
		PackageManager pmgr = context.getPackageManager();
		try {
			PackageInfo info = pmgr.getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
			permissions = info.requestedPermissions;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return permissions;
	}
	
	public static boolean isAllPermissionGranted(Context context, String[] permissions){
		boolean isAllPermissionGranted = true;
		for(String permission : permissions){
			if(ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED){
				HLog.d("", permission + " PERMISSION_GRANTED");
			}else{
				isAllPermissionGranted = false;
				HLog.d("", permission + " PERMISSION_DENIED");
				break;
			}
		}
		
		return isAllPermissionGranted;
	}

	public static void showAppPermisionDialog(final Context context, String msg, OnClickListener listener) {
		String message = context.getString(R.string.forward_permission_manager, msg);
		
		CommonDialog.showDialog(context, message, new String[]{"취소", "확인"}, listener);
	}
	
	public static PackageInfo getPackageInfo(Context context, String packageName){
		PackageManager mgr = context.getPackageManager();
		try {
			return mgr.getPackageInfo(packageName, 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static PackageInfo getPackageArchiveInfo(Context context, String apkPath){
		PackageManager mgr = context.getPackageManager();
		return mgr.getPackageArchiveInfo(apkPath, 0);
	}
	
	/**
	 * path에 있는 apk파일과 현재 설치된 앱의 버전 정보를 체트하여 apk파일이 새로운 버전인지<br>
	 * 체크하는 메소드
	 * 
	 * @param context
	 * @param path
	 * @return
	 */
	public static boolean isNewApkVersion(Context context, String path){
		PackageManager mgr = context.getPackageManager();
		PackageInfo newApp = mgr.getPackageArchiveInfo(path, 0);
		PackageInfo installedApp = null;
		try {
			installedApp = mgr.getPackageInfo(context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newApp.versionCode > installedApp.versionCode;
	}
	
	/**
	 * 사진 전송시에 바이너리 형태로 전달하기 위한 메소드
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static byte[] toByteArray(File file) throws IOException{
		DataInputStream is = new DataInputStream(new FileInputStream(file));
		byte[] data = new byte[(int) file.length()];
		is.readFully(data);
		is.close();
		return data;
	}
	
	public static String makeUserAgent(Context context) {
		PackageInfo pkgInfo = null;
		try {
			pkgInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String userAgentStr = new WebView(context).getSettings().getUserAgentString();
		String appleWebKitVer = userAgentStr.substring(userAgentStr.lastIndexOf("/")+1, userAgentStr.length());
		String userAgent = "COMPANY=LOTTECM;DEVICE_OS=ANDROID;OS_VERSION=" + Build.VERSION.RELEASE + ";DEVICE_APP_VER=" + pkgInfo.versionName + ";DEVICE_MODEL=" + Build.MODEL + ";AppleWebKit=" + appleWebKitVer;
		
		HLog.d(LOG_TAG, "UserAgent : " + userAgent);
		return userAgent;
	}

	public static boolean copyFile(File src, File dest){
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(src);
			out = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			while(in.read(buffer) != -1){
				out.write(buffer);
			}
			out.flush();
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} 
		return true;
	}
	
}
