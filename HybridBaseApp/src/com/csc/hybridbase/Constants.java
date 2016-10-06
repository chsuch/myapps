package com.csc.hybridbase;

public class Constants {
	
	public static int APP_MODE;
	public static int SERVER_TIMEOUT = 3000;
	
	/** Debug Mode */
	public static final int DEV_MODE = 2;
	/** Release Mode */
	public static final int REL_MODE = 0;
	
	/** 메인 URL */
	public static String MAIN_URL = "";
	public static final String MAIN_URL_REL = "https://m.lottehowmuch.com";
	public static final String MAIN_URL_DEV = "https://10.150.1.96:8064";
	
	public static String HOME_URL = "";
	public static String HOME_URL_REL = "https://m.lottehowmuch.com";
	public static String HOME_URL_DEV = "https://10.150.1.96:8064/web/C/M/MAIN/index.jsp";
	
	public static int MAIN_PORT = 0;
	public static final int MAIN_PORT_REL = 443;
	public static final int MAIN_PORT_DEV = 8064;
	
	/** 인트로 화면 최소 로딩 시간(모듈 구동 여부에 따라 설정된 시간 이상일 수 있다) */
	public static final int INTRO_LAUNCH_TIME = 3000;
	
	/* 통신사 정보 */
//	public static final String SKT = "45005";
//	public static final String LGT = "45006";
//	public static final String KT = "45008";
	
	/* SharedPreference */
	public static final String PREF_NAME = "DEFAULT_PREF";
	public static final String PREF_KEY_IS_FIRST_LAUNCH = "IS_FIRST_LAUNCH";
	
	/* startActivityForResult request/response code */
	public static final int REQ_CODE_TAKE_PICTURE = 10001;
	public static final int REQ_CODE_TAKE_PICTURE_FROM_GALLERY = 10002;
	public static final int REQ_CODE_TAKE_VIDEO = 10003;
	public static final int REQ_CODE_TAKE_VIDEO_FROM_GALLERY = 10004;
	
	/* ActivityCompat.requestPermissions requset code */
	public static final int REQ_PERMISSION_CODE_TAKE_PICTURE = 20001;
	public static final int REQ_PERMISSION_CODE_TAKE_PICTURE_FROM_GALLERY = 20002;
	public static final int REQ_PERMISSION_CODE_CHECK_LOCATION_ON_OFF = 20003;
	public static final int REQ_PERMISSION_CODE_GET_LOCATION = 20004;
	public static final int REQ_PERMISSION_CODE_TAKE_VIDEO = 20005;
	public static final int REQ_PERMISSION_CODE_TAKE_VIDEO_FROM_GALLERY = 20006;
	
	/* 사진 저장 관련  */
	public static final String BITMAP_JPG = ".jpg";
	public static final String MP4 = ".mp4";
	public static final String BITMAP_TEMP = "/lotteInsTemp";
	public static final String BITMAP_DIR_PATH = "/LOTTEHM";
	
	/* Intent Action */
	public static final String ACTION_SMS_SENT = "ACTION_SMS_SENT";
	public static final String ACTION_SMS_DELIVERED = "ACTION_SMS_DELIVERED";
	public static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
}
