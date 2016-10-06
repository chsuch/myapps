package com.csc.hybridbase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

public class HttpUtils {
	
	private static final String TAG = HttpUtils.class.getSimpleName();

	public static void excuteCmd(String urlStr, HashMap<String, String> params, Response.Listener<String> response, Response.ErrorListener error){
		AthenaStringRequest stringRequest = new AthenaStringRequest(Request.Method.POST, urlStr, params, response, error);
		BaseApplication.getInstance().addToRequestQueue(stringRequest);
	}
	
	public static void excuteCmd(int cmd, String urlStr, byte[] body, Response.Listener<byte[]> response, Response.ErrorListener error){
		HLog.d("", "body length ::: " + body.length);
		ByteRequest byteRequest = new ByteRequest(Request.Method.POST, urlStr, body, response, error);
		BaseApplication.getInstance().addToRequestQueue(byteRequest);
	}
	
	static class AthenaStringRequest extends StringRequest{
		HashMap<String, String> params;
		public AthenaStringRequest(int method, String url, HashMap<String, String> params, Listener<String> listener, ErrorListener errorListener){
			this(method, url, listener, errorListener);
			this.params = params;
		}
		
		public AthenaStringRequest(int method, String url, Listener<String> listener, ErrorListener errorListener){
			super(method, url, listener, errorListener);
			setRetryPolicy(new DefaultRetryPolicy(
					Constants.SERVER_TIMEOUT, 
			        DefaultRetryPolicy.DEFAULT_MAX_RETRIES, 
			        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		}
		
		@Override
		protected Map<String, String> getParams() throws AuthFailureError {
			if(this.params != null){
				HLog.d(TAG, params.toString());
				return this.params;
			}else{
				return super.getParams();
			}
		}
		
	}
	
	static class ByteRequest extends Request<byte[]> {
	    
		private byte[] mBody;
		private Listener<byte[]> mListener;

	    public ByteRequest(int method, String url, byte[] body, Listener<byte[]> listener,
	                       Response.ErrorListener errorListener) {
	        super(method, url, errorListener);
	        
	        mBody = body;
	        mListener = listener;
	        
	        setRetryPolicy(new DefaultRetryPolicy(
					Constants.SERVER_TIMEOUT, 
			        DefaultRetryPolicy.DEFAULT_MAX_RETRIES, 
			        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
	    }

	    @Override
		public byte[] getBody() throws AuthFailureError {
			if(mBody != null){
				return mBody;
			}else{
				return super.getBody();
			}
		}

		@Override
	    protected void deliverResponse(byte[] response) {
	        if(null != mListener){
	            mListener.onResponse(response);
	        }
	    }

	    @Override
	    protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
	        return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
	    }

//	    @Override
//		protected String getParamsEncoding() {
//			// TODO Auto-generated method stub
//			return "euc-kr";
//		}

		@Override
	    public String getBodyContentType() {
	        return "application/octet-stream";
	    }
	}
	
	public static String doPost(String urlStr, JSONObject params) throws IOException, HttpConnectionException{
		StringBuffer reVal = new StringBuffer();
		HttpURLConnection conn = (HttpURLConnection)new URL(urlStr).openConnection();
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Accept", "application/json");
		conn.setRequestProperty("Cache-Control", "no-cache");
		conn.setRequestMethod("POST");
		conn.setConnectTimeout(3 * 1000);
		conn.setReadTimeout(5 * 1000);
		conn.setDoOutput(true);
		
		OutputStream os = conn.getOutputStream();
		os.write(params.toString().getBytes("UTF-8"));
		os.flush();
		os.close();
		
		if(conn.getResponseCode() != HttpURLConnection.HTTP_OK){
			throw new HttpConnectionException(String.format("HTTP 통신에 실패했습니다.[%d]", conn.getResponseCode()));
		}
		
		String buffer = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		while((buffer = in.readLine()) != null){
			reVal.append(buffer);
		}
		in.close();
		conn.disconnect();
		return reVal.toString();
	}

	public static String doGet(String urlStr) throws IOException, HttpConnectionException{
		StringBuffer reVal = new StringBuffer();
		HttpURLConnection conn = (HttpURLConnection)new URL(urlStr).openConnection();
		conn.setRequestProperty("Cache-Control", "no-cache");
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(3 * 1000);
		conn.setReadTimeout(5 * 1000);
		
		conn.connect();
		
		if(conn.getResponseCode() != HttpURLConnection.HTTP_OK){
			throw new HttpConnectionException(String.format("HTTP 통신에 실패했습니다.[%d]", conn.getResponseCode()));
		}
		
		String buffer = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		while((buffer = in.readLine()) != null){
			reVal.append(buffer);
		}
		in.close();
		conn.disconnect();
		return reVal.toString();
	}
	
	static class HttpConnectionException extends Exception {
		private static final long serialVersionUID = 1L;

		public HttpConnectionException(String msg){
			super(msg);
		}
	}
}
