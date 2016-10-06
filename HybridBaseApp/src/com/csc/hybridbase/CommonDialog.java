package com.csc.hybridbase;

import java.util.Iterator;
import java.util.Stack;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.csc.hybridbase.R;

@SuppressWarnings("unused")
public class CommonDialog extends Dialog implements android.view.View.OnClickListener {
	
	private static Stack<CommonDialog> mDialogStack;
	private LinearLayout mDialogArea;
	private LinearLayout mTitleArea;
	private LinearLayout mBtnArea;
	private TextView mTitle;
	private TextView mMessage;
	private Button mBtnOk; 
	private Button mBtnCancel;
	
	/**
	 * @param context
	 * @param title 다이얼로그 타이틀
	 * @param msg 다이얼로그 메세지
	 */
	private CommonDialog(Context context, String title, String msg) {
		super(context, R.style.AppDialogTheme);
		init(context);
		mTitleArea.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
		mTitle.setText(title);
		mMessage.setText(msg);
	}
	
	/**
	 * 마지막에 오픈 된 다이얼로그를 닫는다.
	 */
	public static void closeDialog(){
		CommonDialog d = mDialogStack.pop();
		if(d != null && d.isShowing()){
			d.dismiss();
		}
	}
	
	/**
	 * 오픈되어있는 모든 다이얼로그를 닫는다.
	 */
	public static void closeAllDialog(){
		if(mDialogStack != null){
			Iterator<CommonDialog> it = mDialogStack.iterator();
			while(it.hasNext()){
				CommonDialog d = it.next();
				if(d != null && d.isShowing()){
					d.dismiss();
				}
			}
		mDialogStack.clear();
		}
	}
	
	/**
	 * @param context
	 * @param title 다이얼로그 타이틀
	 * @param msg 다이얼로그 메세지
	 * @param button [0] Negative 버튼, [1] Positivie 버튼
	 * @param listener 버튼 클릭 리스너. null일 경우 다일얼로그 dismiss동작만 수행됨<br>
	 * R.id.btnOk 일 경우 positive 동작 등록.
	 */
	public static void showDialog(Context context, String title, String msg, String[] button, android.view.View.OnClickListener listener){
		CommonDialog d = new CommonDialog(context, title, msg);
		if(button != null){
			if(!TextUtils.isEmpty(button[1])){
				d.setPositiveButton(button[1], listener);
			}
			if(!TextUtils.isEmpty(button[0])){
				d.setNegativeButton(button[0], listener);
			}
		}else{
			d.setPositiveButton("확인", null);
		}
		mDialogStack.push(d).show();
	}
	
	/**
	 * Positive버튼만 있는 경우 사용
	 * @param context
	 * @param title 다이얼로그 타이틀
	 * @param msg 다이얼로그 메세지
	 * @param button Positivie 버튼
	 * @param listener 버튼 클릭 리스너. null일 경우 다일얼로그 dismiss동작만 수행됨<br>
	 * R.id.btnOk 일 경우 positive 동작 등록.
	 */
	public static void showDialog(Context context, String title, String msg, String button, android.view.View.OnClickListener listener){
		showDialog(context, title, msg, new String[]{"", button}, listener);
	}
	
	/**
	 * 타이틀이 없고, Positivie버튼만 있는 경우 사용
	 * @param context
	 * @param msg 다이얼로그 메세지
	 * @param button Positivie 버튼
	 * @param listener 버튼 클릭 리스너. null일 경우 다일얼로그 dismiss동작만 수행됨<br>
	 * R.id.btnOk 일 경우 positive 동작 등록.
	 */
	public static void showDialog(Context context, String msg, String button, android.view.View.OnClickListener listener){
		showDialog(context, msg, new String[]{"", button}, listener);
	}
	
	/**
	 * 타이틀이 없는 경우 사용
	 * @param context
	 * @param msg 다이얼로그 메세지
	 * @param button [0] Negative 버튼, [1] Positivie 버튼
	 * @param listener 버튼 클릭 리스너. null일 경우 다일얼로그 dismiss동작만 수행됨<br>
	 * R.id.btnOk 일 경우 positive 동작 등록.
	 */
	public static void showDialog(Context context, String msg, String[] button, android.view.View.OnClickListener listener){
		showDialog(context, "", msg, button, listener);
	}
	
	
	/**
	 * 확인 버튼만 있고, 클릭 시 닫기 이벤트만 적용 되는 경우 사용
	 * @param context
	 * @param title 다이얼로그 타이틀
	 * @param msg 다이얼로그 메세지
	 */
	public static void showDialog(Context context, String title, String msg){
		showDialog(context, title, msg, "확인", null);
	}
	
	/**
	 * 타이틀이 없고, 확인 버튼 클릭 시 닫기 이벤트만 적용되는 경우 사용
	 * @param context
	 * @param msg 다이얼로그 메세지
	 */
	public static void showDialog(Context context,String msg){
		showDialog(context, msg, "확인", null);
	}
	
	private void init(Context context) {
		if(mDialogStack == null){
			mDialogStack = new Stack<CommonDialog>();
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.dialog_common, null);
		
		mTitle = (TextView) v.findViewById(R.id.dialog_title);
		mMessage = (TextView) v.findViewById(R.id.dialog_msg);
		mBtnOk = (Button) v.findViewById(R.id.btnOk);
		mBtnCancel = (Button) v.findViewById(R.id.btnCancel);
		mDialogArea = (LinearLayout)v.findViewById(R.id.layout_dialog);
		mTitleArea = (LinearLayout)v.findViewById(R.id.titleArea); 
		mBtnArea = (LinearLayout)v.findViewById(R.id.dialog_btn_area);
		
		mBtnOk.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);

		v.invalidate();
		
		setContentView(v);
		setCancelable(false);
		setCanceledOnTouchOutside(false);
	}
	
	private void setPositiveButton(String btnNm, android.view.View.OnClickListener listener) {
		if (TextUtils.isEmpty(btnNm) == false) {
			mBtnOk.setText(btnNm);
			mBtnOk.setVisibility(View.VISIBLE);
			if (listener != null) {
				mBtnOk.setOnClickListener(listener);
			}
		}
	}
	
	private void setNegativeButton(String btnNm, android.view.View.OnClickListener listener) {
		if (TextUtils.isEmpty(btnNm) == false) {
			mBtnCancel.setText(btnNm);
			mBtnCancel.setVisibility(View.VISIBLE);
			if (listener != null)	{
				mBtnCancel.setOnClickListener(listener);
			}
		}
	}

	@Override
	public void onClick(View v) {
		closeDialog();
	}
}