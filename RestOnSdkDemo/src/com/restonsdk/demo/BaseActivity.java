package com.restonsdk.demo;

import com.sleepace.sdk.manager.CallbackData;
import com.sleepace.sdk.util.StatusCode;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

public class BaseActivity extends Activity implements OnClickListener {
	protected final String TAG = this.getClass().getSimpleName();
	protected ImageView ivBack;
	protected TextView tvTitle;
	protected ImageView ivRight;
	protected ScrollView scrollView;
	protected TextView tvLog;
	protected BaseActivity mContext;
	private boolean isUserTouch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mContext = this;
	}

	protected void findView() {
		ivBack = (ImageView) findViewById(R.id.iv_back);
		tvTitle = (TextView) findViewById(R.id.tv_title);
    	tvLog = (TextView) findViewById(R.id.tv_log);
    	scrollView = (ScrollView) findViewById(R.id.scrollview);
	};

	protected void initListener() {
		if(ivBack != null){
			ivBack.setOnClickListener(this);
		}
		initTvLogTouchListener(scrollView, tvLog);
	};

	protected void initUI() {
		
	};
	
	
	public void initTvLogTouchListener(final ScrollView scrollView, TextView tvLog){
		if(tvLog != null){
			tvLog.setMovementMethod(ScrollingMovementMethod.getInstance());
			tvLog.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					switch(event.getAction()){
					case MotionEvent.ACTION_DOWN:
						isUserTouch = true;
						v.removeCallbacks(userTouchTimeoutTask);
//						LogUtil.log(TAG+" tvLog onTouch down-------------down");
						if(scrollView != null){
							scrollView.requestDisallowInterceptTouchEvent(true);
						}
						break;
					case MotionEvent.ACTION_MOVE:
//						LogUtil.log(TAG+" tvLog onTouch move-------------");
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
//						LogUtil.log(TAG+" tvLog onTouch------cancel or up-------" + event.getAction());
						if(scrollView != null){
							scrollView.requestDisallowInterceptTouchEvent(false);
						}
						v.postDelayed(userTouchTimeoutTask, userTouchTimeout);
						break;
					}
					return false;
				}
			});
		}
	}
	
	
	private int userTouchTimeout = 2000;
	private Runnable userTouchTimeoutTask = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			isUserTouch = false;
		}
	};
	

	@Override
	public void onClick(View v) {
		if(v == ivBack){
			finish();
		}
	}
	
	public void printLog(int strRes){
		String log = getString(strRes);
		printLog(log);
	}
	
	public void printLog(String log){
		printLog(log, tvLog);
	}

	public void printLog(int strRes, TextView tvLog){
		String log = getString(strRes);
		printLog(log, tvLog);
	}
	
	private static final int MAX_LOG_ROW_COUNT = 100;
	private static final int CACHE_LOG_ROW_COUNT = 50;
	
	public synchronized void printLog(String log, final TextView tvLog){
		if (!TextUtils.isEmpty(log)) {
			String[] ss = DemoApp.getInstance().logBuf.toString().split("\n");
			if(ss.length > MAX_LOG_ROW_COUNT + CACHE_LOG_ROW_COUNT){
				int clearCount = ss.length - MAX_LOG_ROW_COUNT;
				StringBuffer buf = new StringBuffer();
				for(int i=0;i<clearCount;i++){
					buf.append(ss[i] + "\n");
				}
				String tmp = buf.toString();
				int idx = DemoApp.getInstance().logBuf.indexOf(tmp);
				DemoApp.getInstance().logBuf.delete(0, idx+ tmp.length());
			}
			DemoApp.getInstance().logBuf.append(log + "\n");
		}
		
		if(tvLog == null){
			return;
		}
		
		String str = DemoApp.getInstance().logBuf.toString();
		if(str.lastIndexOf("\n") != -1){//去掉最后一行换行符
			str = str.substring(0, str.length()-1);
		}
		
		tvLog.setText(str);
		tvLog.postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(!isUserTouch){
					int offset = tvLog.getLineCount() * tvLog.getLineHeight() + getResources().getDimensionPixelSize(R.dimen.dp15);
					if (offset > tvLog.getHeight()) {
						tvLog.scrollTo(0, offset - tvLog.getHeight());
					}
				}
			}
		}, 200);
	}
	
	public boolean checkStatus(CallbackData cd){
		return checkStatus(cd, tvLog);
	}
	
	public boolean checkStatus(CallbackData cd, TextView tvLog){
		if(!cd.isSuccess()){
			String errMsg = getErrMsg(cd.getStatus());
			printLog(errMsg, tvLog);
			return false;
		}
		return true;
	}
	
	public String getErrMsg(int status){
		if(status == StatusCode.STATUS_BLUETOOTH_NOT_OPEN){
			return getString(R.string.not_bluetooth);
		}
		
		if(status == StatusCode.STATUS_DISCONNECT){
			return getString(R.string.reconnect_device);
		}
		
		if(status == StatusCode.STATUS_TIMEOUT){
			return getString(R.string.time_out);
		}
		
		if(status == StatusCode.STATUS_FAILED){
			return getString(R.string.failure);
		}
		
		return "";
	}
}


















