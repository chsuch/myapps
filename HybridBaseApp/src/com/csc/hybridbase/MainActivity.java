package com.csc.hybridbase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class MainActivity extends CommonWebViewActivity {
	
	final String LOG_TAG = getClass().getSimpleName();
	
	/** SMS 리시버 */
//	private IntentFilter mSmsReceiverFilter;
	
	private Handler mHandler;
	private Handler mPermissionHandler;;
	private Uri mMediaUri;
//	private Uri mVideoUri;
	private String callBackFunc;
	private LocationManager lm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		lm = (LocationManager)getSystemService(LOCATION_SERVICE);
		mHandler = new Handler();
		mPermissionHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case Constants.REQ_PERMISSION_CODE_TAKE_PICTURE:
					startCamera(false);
					break;
				case Constants.REQ_PERMISSION_CODE_TAKE_PICTURE_FROM_GALLERY:
					startCamera(true);
					break;
				case Constants.REQ_PERMISSION_CODE_TAKE_VIDEO:
					startCameraForVideo(false);
					break;
				case Constants.REQ_PERMISSION_CODE_TAKE_VIDEO_FROM_GALLERY:
					startCameraForVideo(true);
					break;
				case Constants.REQ_PERMISSION_CODE_CHECK_LOCATION_ON_OFF:
					checkLocationOnOff();
					break;
				case Constants.REQ_PERMISSION_CODE_GET_LOCATION:
					getLocationInfo();
					break;
				default:
					HLog.e(LOG_TAG, "요청한 작업이 존해하지 않습니다..." + msg.what);
					break;
				}
			}
			
		};
		mWebView.addJavascriptInterface(new CallApp(), "callApp");
		mWebView.loadUrl(Constants.HOME_URL);
		
//		mSmsReceiverFilter = new IntentFilter();
//		mSmsReceiverFilter.addAction(Constants.ACTION_SMS_SENT);
//		mSmsReceiverFilter.addAction(Constants.ACTION_SMS_RECEIVED);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		String action =  intent.getAction();
		HLog.d(LOG_TAG, "onNewIntent()..." + action);
		super.onNewIntent(intent);
		if(!TextUtils.isEmpty(action) && action.equals("GO_TO_HOME")){
			mWebView.loadUrl(Constants.HOME_URL);
			clearHistory = true;
		}
	}

	@Override
	protected void onPause() {
		HLog.d(LOG_TAG, "onPause()...");
		super.onPause();
//		unregisterReceiver(mSmsReceiver);
	}

	@Override
	protected void onResume() {
		HLog.d(LOG_TAG, "onResume()...");
		super.onResume();
//		registerReceiver(mSmsReceiver, mSmsReceiverFilter);
	}

	@Override
	protected void onDestroy() {
		HLog.d(LOG_TAG, "onDestroy()...");
		super.onDestroy();
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case Constants.REQ_CODE_TAKE_PICTURE_FROM_GALLERY:
			if(resultCode == RESULT_OK){
				mMediaUri = data.getData();
				//겔러리에서 사진 선택하는 경우, 사진 선택 후 로직은 카메라로 촬영한 로직과 동일
				//Constants.REQ_CODE_TAKE_PICTURE 실행
			}else{
				break;
			}
		case Constants.REQ_CODE_TAKE_PICTURE:
			if(resultCode == RESULT_OK){
				afterTakePicture(requestCode);
			}
			break;
		case Constants.REQ_CODE_TAKE_VIDEO_FROM_GALLERY:
			if(resultCode == RESULT_OK){
				mMediaUri = data.getData();
			}else{
				break;
			}
		case Constants.REQ_CODE_TAKE_VIDEO:
			if(resultCode == RESULT_OK){
				afterTakeVideo(requestCode);
			}
			break;
		default:
			break;
		}
	}
	
	private OnClickListener requestPermissionsListener  = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			CommonDialog.closeDialog();
			if(v.getId() == R.id.btnOk){
				Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				i.addCategory(Intent.CATEGORY_DEFAULT);
				i.setData(Uri.parse("package:" + getPackageName()));
				startActivity(i);
			}
		}
	};
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		// TODO Auto-generated method stub
		switch (requestCode) {
		case Constants.REQ_PERMISSION_CODE_TAKE_PICTURE:
			if(grantResults.length == 2 && (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)){
				mPermissionHandler.sendEmptyMessage(requestCode);
			}else{
				Utils.showAppPermisionDialog(mContext, getString(R.string.noti_permission_photo), requestPermissionsListener);
			}
			break;
		case Constants.REQ_PERMISSION_CODE_TAKE_PICTURE_FROM_GALLERY:
			if(grantResults.length == 2 && (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)){
				mPermissionHandler.sendEmptyMessage(requestCode);
			}else{
				Utils.showAppPermisionDialog(mContext, getString(R.string.noti_permission_photo_form_gallery), requestPermissionsListener);
			}
			break;
		case Constants.REQ_PERMISSION_CODE_TAKE_VIDEO:
			if(grantResults.length == 2 && (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)){
				mPermissionHandler.sendEmptyMessage(requestCode);
			}else{
				Utils.showAppPermisionDialog(mContext, getString(R.string.noti_permission_video), requestPermissionsListener);
			}
			break;
		case Constants.REQ_PERMISSION_CODE_TAKE_VIDEO_FROM_GALLERY:
			if(grantResults.length == 2 && (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)){
				mPermissionHandler.sendEmptyMessage(requestCode);
			}else{
				Utils.showAppPermisionDialog(mContext, getString(R.string.noti_permission_video_from_gallery), requestPermissionsListener);
			}
			break;
		case Constants.REQ_PERMISSION_CODE_CHECK_LOCATION_ON_OFF:
			if(grantResults.length == 2 && (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)){
				mPermissionHandler.sendEmptyMessage(requestCode);
			}else{
				Utils.showAppPermisionDialog(mContext, getString(R.string.noti_permission_location), requestPermissionsListener);
			}
			break;
		case Constants.REQ_PERMISSION_CODE_GET_LOCATION:
			if(grantResults.length == 2 && (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)){
				mPermissionHandler.sendEmptyMessage(requestCode);
			}else{
				Utils.showAppPermisionDialog(mContext, getString(R.string.forward_location_manager), requestPermissionsListener);
			}
			break;
		default:
			break;
		}
	}
	
	/**
	 * contentUri로 파일 찾기
	 * 
	 * @param contentUri
	 * @return
	 */
	private String getRealPathFromUri(Uri contentUri, String type) {
//		  String[] proj = {MediaStore.Images.Media.DATA};
		String[] proj = {type};
		CursorLoader cursorLoader = new CursorLoader(this, contentUri, proj, null, null, null);        
		Cursor cursor = cursorLoader.loadInBackground();
		int column_index = cursor.getColumnIndexOrThrow(type);
		cursor.moveToFirst();
		String realPath = cursor.getString(column_index);
		HLog.d(LOG_TAG, "getRealPathFromUri ::: " + realPath);
		return realPath; 
	}
	
	private Bitmap rotate(Bitmap bitmap, int degrees){
		HLog.d(LOG_TAG, "rotated image to " + degrees);
		if(degrees != 0 && bitmap != null){
			Matrix m = new Matrix();
			m.setRotate(degrees, (float)bitmap.getWidth()/2, (float)bitmap.getHeight()/2);
			try {
				Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
				if(bitmap != converted){
					bitmap.recycle();
					bitmap = converted;
				}
			} catch (OutOfMemoryError e) {
				Toast.makeText(this, "메모리 부족으로 사진을 회전하지 못했습니다.", Toast.LENGTH_SHORT).show();
			}
		}
		return bitmap;
	}
	
	/**
	 * 사진 정보로 부터 회전 정보를 가져와 원래 상태로 회전 시킴
	 * 
	 * @param imgPath
	 * @param bitmap
	 * @return
	 */
	private Bitmap rotateImage(String imgPath, Bitmap bitmap) {
		Bitmap b = null;
		try {
			ExifInterface exif = new ExifInterface(imgPath);
			int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			int exifDegree = exifOrientationToDegrees(exifOrientation);
			b = rotate(bitmap, exifDegree);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return b;
	}
	
	private int exifOrientationToDegrees(int exifOrientation){
		if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_90){
			return 90;
		}else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_180){
			return 180;
		}if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_270){
			return 270;
		}else{
			return 0;
		}
	}
	
	/**
	 * 동영상은 현재 사용되지 않음
	 * 
	 * @param reqCode
	 */
	private void afterTakeVideo(int reqCode) {
		HLog.d(LOG_TAG, "동영상 촬영 후 처리 시작");
//		Bitmap bitmap = null;

		File videoFile = null;
		if(reqCode==Constants.REQ_CODE_TAKE_VIDEO){
			videoFile = new File(Environment.getExternalStorageDirectory() + Constants.BITMAP_DIR_PATH + "/" + fileName + Constants.MP4);
		}else{
			videoFile = new File(getRealPathFromUri(mMediaUri, MediaStore.Video.Media.DATA));
		}
		// 사진 생성경로에 파일이 없거나 사이즈가0일경우(제대로 생성안됨)
		if(videoFile == null || videoFile.length() <= 0 || mMediaUri == null) {
			Toast.makeText(this, "동영상 파일이 존재하지 않습니다.\n잠시 후 다시 실행하여 주세요.", Toast.LENGTH_SHORT).show();
			return;
		}
		
		String physical_path = videoFile.getAbsolutePath();
		
		HLog.d(LOG_TAG, "physical_path : " + physical_path);

		File newFile = new File(Environment.getExternalStorageDirectory() + Constants.BITMAP_DIR_PATH + "/" + fileName + Constants.MP4);
		boolean copyResult = true;
		if(reqCode == Constants.REQ_CODE_TAKE_VIDEO_FROM_GALLERY){
			copyResult = Utils.copyFile(videoFile, newFile);
		}
		
		mMediaUri = null;
		if(copyResult){
			//콜백함수
			JSONObject jObj = new JSONObject();
			try {
				jObj.put("filePath", newFile.getAbsolutePath());
				jObj.put("inputId", inputId);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Utils.callJavaScript(mWebView, callBackFunc, jObj.toString());
		}else{
			Toast.makeText(this, "동영상 파일 생성에 실패했습니다.\n잠시 후 다시 실행하여 주세요.", Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * 겔러리에서 사진 선택 후 또는 촬영 후에 저장버튼 클릭 후 사진을 생성하는 메소드
	 * 
	 * @param reqCode 카메라로 촬영한 경우와 겔러리에서 사진을 선택한 경우를 구분함
	 */
	private void afterTakePicture(int reqCode) {
		HLog.d(LOG_TAG, "사진 촬영 후 처리 시작");
		Bitmap bitmap = null;

		File photoFile = null;
		if(reqCode==Constants.REQ_CODE_TAKE_PICTURE){
			photoFile = new File(Environment.getExternalStorageDirectory() + Constants.BITMAP_DIR_PATH + "/" + fileName + Constants.BITMAP_JPG);
		}else{
			photoFile = new File(getRealPathFromUri(mMediaUri, MediaStore.Images.Media.DATA));
		}
		// 사진 생성경로에 파일이 없거나 사이즈가0일경우(제대로 생성안됨)
		if(photoFile == null || photoFile.length() <= 0 || mMediaUri == null) {
			Toast.makeText(this, "사진 파일이 존재하지 않습니다.\n잠시 후 다시 실행하여 주세요.", Toast.LENGTH_SHORT).show();
			return;
		}
		long lastModify = photoFile.lastModified();
		String physical_path = photoFile.getAbsolutePath();
		
		HLog.d(LOG_TAG, "physical_path : " + physical_path);

		// outofmemory 방지
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 2;	// 샘플링 사이즈.숫자가 클수록 속도는 증가 화질은 떨어짐..
		options.inDither = true;	// 이미지 처리가 깔끔하게..

//		bitmap = BitmapFactory.decodeFile(physical_path, options);//가져오는 기능
		
		bitmap = rotateImage(physical_path, BitmapFactory.decodeFile(physical_path, options));
		
//		//해상도 설정-start ==> 
//		//해상도 설정이 필요한 경우 사용
//		//JPEG압축 시에 bitmap.compress(CompressFormat.JPEG, 100, fosObj); 대신 bitmap2.compress(CompressFormat.JPEG, 100, fosObj); 사용
//		int maxResolution = 2048;
//		
//		int iWidth = bitmap.getWidth();
//		int iHeight = bitmap.getHeight();
//		int newWidth = iWidth;
//		int newHeight = iHeight;
//		float rate = 0.0f;
//		
//		if(iWidth > iHeight){
//			if(maxResolution < iWidth){
//				rate = maxResolution / (float)iWidth;
//				newHeight = (int)(iHeight * rate);
//				newWidth = maxResolution;
//			}
//		}else{
//			if(maxResolution < iHeight){
//				rate = maxResolution / (float)iHeight;
//				newWidth = (int)(iWidth * rate);
//				newHeight = maxResolution;
//			}
//		}
//		Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
//		//해상도 설정-end
		
		FileOutputStream fosObj = null;
		
		HLog.d(LOG_TAG, "pull fileName : " +  fileName);
		//기존파일 지우기
		String aPath = Environment.getExternalStorageDirectory() + Constants.BITMAP_DIR_PATH + "/" + fileName + Constants.BITMAP_JPG;
		File checkImg = new File(aPath);
		
		if(checkImg.exists()){
			checkImg.delete();
		}
		try {
			fosObj = new FileOutputStream(aPath);
			bitmap.compress(CompressFormat.JPEG, 100, fosObj);
		} catch (FileNotFoundException e) {
			Toast.makeText(this, "현재 저장공간에 파일을 생성할 수 없습니다.\n잠시 후 다시 실행하여 주세요.", Toast.LENGTH_SHORT).show();
			return;
		}
		
		mMediaUri = null;
		
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		String lastModifyStr = format.format(new Date(lastModify));

//		JSONObject사용시에 \문자에 의해 원하지 않는 스트링으로 변형됨에 따라 아래와 같이 수동으로 json형태의 스트링 생성
		String param = "{\"filePath\":\"%s\",\"inputId\":\"%s\",\"date\":\"%s\"}";
		try {
			byte[] data = Utils.toByteArray(new File(aPath));
			String strData = Base64.encodeToString(data, Base64.NO_WRAP);
			param = String.format(param, strData, inputId, lastModifyStr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Utils.callJavaScript(mWebView, callBackFunc, param);
		
		//일부 카메라에서 recycle이후에 압축을 한다고 죽는 경우가 있어 코드 마지막으로 이동
		bitmap.recycle();
		bitmap = null;
//		bitmap2.recycle();
//		bitmap2 = null;
		
//		sendPhoto(aPath);
	}

	/* 
	 * 카메라 촬영 시 특정 단말에서 화면이 돌아가는 경우가 발생하여, 방지하고자 오버라이딩함.
	 * (non-Javadoc)
	 * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		HLog.d(LOG_TAG, "onConfigurationChanged");
	}
	
	private boolean externalMemoryAvailable() {
		return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
	}
	
	private void startCameraForVideo(boolean isFromGallery){//미사용
		if(externalMemoryAvailable()) {
			if(Environment.getExternalStorageDirectory().canWrite() && mkLotteInsDir(false)) {
				if(isFromGallery){//갤러리에서 가져옴
					Intent intent_Gallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.INTERNAL_CONTENT_URI);
					startActivityForResult(intent_Gallery, Constants.REQ_CODE_TAKE_VIDEO_FROM_GALLERY);
				}else{//카메라로 찍어서 가져옴
					Intent intent_Camera = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
					intent_Camera.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
					startActivityForResult(intent_Camera, Constants.REQ_CODE_TAKE_VIDEO);
				}
			}else {
				Toast.makeText(this, "메모리를 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
			}
		}else{
			Toast.makeText(this, "외장 메모리를 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
		}
	}
	
	//카메라 사진 찍기
	private void startCamera(boolean isFromGallery){
		if(externalMemoryAvailable()) {
			if(Environment.getExternalStorageDirectory().canWrite() && mkLotteInsDir(true)) {
				if(isFromGallery){//갤러리에서 가져옴
					Intent intent_Gallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
					startActivityForResult(intent_Gallery, Constants.REQ_CODE_TAKE_PICTURE_FROM_GALLERY);
				}else{//카메라로 찍어서 가져옴
					Intent intent_Camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intent_Camera.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
					startActivityForResult(intent_Camera, Constants.REQ_CODE_TAKE_PICTURE);
				}
				
			}else {
				Toast.makeText(this, "메모리를 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
			}
		}else{
			Toast.makeText(this, "외장 메모리를 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void checkLocationOnOff(){
		LocationProvider gps = lm.getProvider(LocationManager.GPS_PROVIDER);
		LocationProvider network = lm.getProvider(LocationManager.NETWORK_PROVIDER);
		boolean isGpsOn = (gps != null && lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) || (network != null && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
		if( isGpsOn == false ){
			CommonDialog.showDialog(mContext, "", "위치정보를 사용 할 수 없습니다.\n설정 화면으로 이동 하시겠습니까?", new String[]{"취소", "확인"}, new OnClickListener() {
				@Override
				public void onClick(View v) {
					CommonDialog.closeDialog();
					if(v.getId() == R.id.btnOk){
						Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						i.addCategory(Intent.CATEGORY_DEFAULT);
						startActivity(i);
					}
				}
			});
		}
		JSONObject jObj = new JSONObject();
		try {
			jObj.put("gpsFlag", isGpsOn ? "Y" : "N");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Utils.callJavaScript(mWebView, callBackFunc, jObj.toString());
	}
	
	private void getLocationInfo(){
		LocationProvider gps = lm.getProvider(LocationManager.GPS_PROVIDER);
		LocationProvider network = lm.getProvider(LocationManager.NETWORK_PROVIDER);
		boolean isGpsOn = 
				(gps != null && lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) || 
				(network != null && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
		if( isGpsOn == false ){
			CommonDialog.showDialog(mContext, "", "위치정보를 사용 할 수 없습니다.\n설정 화면으로 이동 하시겠습니까?", new String[]{"취소", "확인"}, new OnClickListener() {
				@Override
				public void onClick(View v) {
					CommonDialog.closeDialog();
					if(v.getId() == R.id.btnOk){
						Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						i.addCategory(Intent.CATEGORY_DEFAULT);
						startActivity(i);
					}
				}
			});
		}else{
			if(network != null && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
				HLog.d(LOG_TAG, "Start network provider...");
				lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, mLocationListener);
			}
			if(gps != null && lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
				HLog.d(LOG_TAG, "Start gps provider...");
				lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, mLocationListener);
			}
		}
	}
	
	/**
	 * 사진 촬영 후 저장 되는 디렉토리 생성
	 */
	private boolean mkLotteInsDir(boolean isPhoto) {
		String SD_path = Environment.getExternalStorageDirectory().getAbsolutePath();
		if (SD_path != null) {
			try {
				File photoFile = new File(SD_path + Constants.BITMAP_DIR_PATH);
				if(!photoFile.exists()) {
					if(photoFile.mkdirs() == false) {
						return false;
					}
				}
				
				if(isPhoto){
					photoFile = new File(SD_path + Constants.BITMAP_DIR_PATH + "/" + fileName + Constants.BITMAP_JPG);
					mMediaUri = Uri.fromFile(photoFile);
				}else{
					photoFile = new File(SD_path + Constants.BITMAP_DIR_PATH + "/" + fileName + Constants.MP4);
					mMediaUri = Uri.fromFile(photoFile);
				}
			}
			catch(Exception e) {
				e.printStackTrace();
				mMediaUri = null;
				return false;
			}
	
			return true;
		}else{
			return false;
		}
	}
	
	
	String fileName;
	String inputId;
	private class CallApp{//javascript에서 콜함
		@JavascriptInterface
		public void appBridge(final String str){
    		mHandler.post(new Runnable(){
				@Override
				public void run() {
					try {
						JSONObject obj = new JSONObject(str);
						String api = obj.optString("api");
						callBackFunc = obj.optString("callBackFunc");

						Iterator<String> keys = obj.keys();
						while(keys.hasNext()){
							String key = keys.next();
							HLog.d(LOG_TAG, key + " ::: " + obj.optString(key));
						}
						
						if(api.equals("116")){//외부 브라우저 오픈
							Utils.openBrowser(mContext, obj.optString("SiteAddr"));
						}else if(api.equals("120")){//촬영 후 사진 가져오기
							fileName = obj.optString("fileName");
							inputId = obj.optString("inputId");
							if(TextUtils.isEmpty(fileName) || TextUtils.isEmpty(inputId) || TextUtils.isEmpty(callBackFunc)){
								Toast.makeText(mContext, "서버로 부터 생성 할 사진파일 정보를 받지 못했습니다.", Toast.LENGTH_SHORT).show();
							}else{
								Utils.requestPermission(mActivity, 
										mPermissionHandler, 
										new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
										Constants.REQ_PERMISSION_CODE_TAKE_PICTURE);
							}
						}else if(api.equals("121")){//겔러리에서 사진 가져오기
							fileName = obj.optString("fileName");
							inputId = obj.optString("inputId");
							if(TextUtils.isEmpty(fileName) || TextUtils.isEmpty(inputId) || TextUtils.isEmpty(callBackFunc)){
								Toast.makeText(mContext, "서버로 부터 생성 할 사진파일 정보를 받지 못했습니다.", Toast.LENGTH_SHORT).show();
							}else{
								Utils.requestPermission(mActivity, 
										mPermissionHandler, 
										new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
										Constants.REQ_PERMISSION_CODE_TAKE_PICTURE_FROM_GALLERY);
							}
						}else if(api.equals("123")){//동영상 촬영 후 가져오기//미사용
							fileName = obj.optString("fileName");
							inputId = obj.optString("inputId");
							if(TextUtils.isEmpty(fileName) || TextUtils.isEmpty(inputId) || TextUtils.isEmpty(callBackFunc)){
								Toast.makeText(mContext, "서버로 부터 생성 할 사진파일 정보를 받지 못했습니다.", Toast.LENGTH_SHORT).show();
							}else{
								Utils.requestPermission(mActivity, 
										mPermissionHandler, 
										new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
										Constants.REQ_PERMISSION_CODE_TAKE_VIDEO);
							}
						}else if(api.equals("124")){//겔러리에서 동영상 가져오기//미사용
							fileName = obj.optString("fileName");
							inputId = obj.optString("inputId");
							if(TextUtils.isEmpty(fileName) || TextUtils.isEmpty(inputId) || TextUtils.isEmpty(callBackFunc)){
								Toast.makeText(mContext, "서버로 부터 생성 할 사진파일 정보를 받지 못했습니다.", Toast.LENGTH_SHORT).show();
							}else{
								Utils.requestPermission(mActivity, 
										mPermissionHandler, 
										new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
										Constants.REQ_PERMISSION_CODE_TAKE_VIDEO_FROM_GALLERY);
							}
						}else if(api.equals("122")){//임시 사진 저장 폴더 삭제
							Utils.deleteFiles(Environment.getExternalStorageDirectory() + Constants.BITMAP_DIR_PATH);
						}else if(api.equals("131")){//GPS on/off 확인
							Utils.requestPermission(mActivity, 
									mPermissionHandler, 
									new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 
									Constants.REQ_PERMISSION_CODE_CHECK_LOCATION_ON_OFF);
						}else if(api.equals("130")){//GPS 정보 가져오기
							Utils.requestPermission(mActivity, 
									mPermissionHandler, 
									new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 
									Constants.REQ_PERMISSION_CODE_GET_LOCATION);
						}else{
							HLog.e(LOG_TAG, String.format("요청한 api[%s]는 존재하지 않습니다.", api));
						}
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
    		});
		}
	}
	
//	BroadcastReceiver mSmsReceiver = new BroadcastReceiver(){
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			HLog.d(LOG_TAG, "SmsReceiver action : " + intent.getAction());
//			if(intent.getAction().equals(Constants.ACTION_SMS_SENT)){
//				switch (getResultCode()) {
//				case Activity.RESULT_OK:
//					Toast.makeText(mContext, "SMS sent successfully...", Toast.LENGTH_SHORT).show();
//					break;
//				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
//					Toast.makeText(mContext, "SMS sent failure..." + "Generic failure cause", Toast.LENGTH_SHORT).show();
//					break;
//				case SmsManager.RESULT_ERROR_NO_SERVICE:
//					Toast.makeText(mContext, "SMS sent failure..." + "Service is currently unavailable", Toast.LENGTH_SHORT).show();
//					break;
//				case SmsManager.RESULT_ERROR_NULL_PDU:
//					Toast.makeText(mContext, "SMS sent failure..." + "No pdu provided", Toast.LENGTH_SHORT).show();
//					break;
//				case SmsManager.RESULT_ERROR_RADIO_OFF:
//					Toast.makeText(mContext, "SMS sent failure..." + "Radio was explicitly turned off", Toast.LENGTH_SHORT).show();
//					break;
//				default:
//					break;
//				}
//			} else if(intent.getAction().equals(Constants.ACTION_SMS_RECEIVED)) {
//				HLog.d(this.getClass().getSimpleName(), "Received message................");
//				String message = "";
//				String sender = "";
//				Bundle bundle = intent.getExtras();
//				if (bundle != null) {
//					Object[] pdus = (Object[]) bundle.get("pdus");
//					SmsMessage[] smsMessage = new SmsMessage[pdus.length];
//					for (int i = 0; i < pdus.length; i++) {
//						smsMessage[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
//					}
//					message = smsMessage[0].getMessageBody();
//					sender = smsMessage[0].getOriginatingAddress();
//					HLog.d(LOG_TAG, "Received SMS : " + message);
////					String authNo = message.substring(18, 24);
////					String senderHeader = message.substring(0, message.indexOf("]")+1);
////					if(sender.equals("15771006") && senderHeader.equals("[서울신용평가정보]")){
////						webview.loadUrl("javascript:fn_SetPhoneCertificationNum('" + authNo + "')");
////					}
//				}
//			}
//		}
//	};
	
	private LocationListener mLocationListener = new LocationListener() {
		
		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
		
		@Override
		public void onProviderEnabled(String arg0) {
			HLog.d(LOG_TAG, "onProviderEnabled()..." + arg0);
		}
		
		@Override
		public void onProviderDisabled(String arg0) {
			HLog.d(LOG_TAG, "onProviderDisabled()..." + arg0);
		}
		
		@Override
		public void onLocationChanged(Location location) {
			HLog.d(LOG_TAG, "onLocationChanged()...");
			HLog.d(LOG_TAG, location.getLatitude() + ", " + location.getLongitude() + ", " + location.getProvider());
			JSONObject jObj = new JSONObject();
			try {
				jObj.put("sLatitude", location.getLatitude());
				jObj.put("sLongitude", location.getLongitude());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Utils.callJavaScript(mWebView, callBackFunc, jObj.toString());
			lm.removeUpdates(this);
		}
	};
}
