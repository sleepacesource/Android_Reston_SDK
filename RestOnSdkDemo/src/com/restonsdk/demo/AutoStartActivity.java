package com.restonsdk.demo;

import com.restonsdk.demo.view.wheelview.NumericWheelAdapter;
import com.restonsdk.demo.view.wheelview.OnItemSelectedListener;
import com.restonsdk.demo.view.wheelview.WheelAdapter;
import com.restonsdk.demo.view.wheelview.WheelView;
import com.sleepace.sdk.core.heartbreath.domain.AutoStartConfig;
import com.sleepace.sdk.interfs.IResultCallback;
import com.sleepace.sdk.manager.CallbackData;
import com.sleepace.sdk.reston.RestOnHelper;
import com.sleepace.sdk.util.SdkLog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class AutoStartActivity extends BaseActivity {
    private WheelView wvHour, wvMinute;
    private WheelAdapter hourAdapter, minuteAdapter;
    private Button btnSave;
    private RestOnHelper restonHelper;
    private AutoStartConfig mConfig = new AutoStartConfig(true, (byte)22, (byte)0, (byte)127);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restonHelper = RestOnHelper.getInstance(this);
        setContentView(R.layout.activity_autostart);
        findView();
        initListener();
        initUI();
    }


    public void findView() {
    	super.findView();
        wvHour = (WheelView) findViewById(R.id.hour);
        wvMinute = (WheelView) findViewById(R.id.minute);
        btnSave = (Button) findViewById(R.id.btn_save);
    }

    public void initListener() {
    	super.initListener();
    	btnSave.setOnClickListener(this);
    }

    public void initUI() {
    	super.initUI();
        tvTitle.setText(R.string.set_auto_monitor);
        
        wvHour.setAdapter(new NumericWheelAdapter(0, 23));
        wvHour.setTextSize(20);
        wvHour.setCyclic(true);
        wvHour.setOnItemSelectedListener(onHourItemSelectedListener);

        wvMinute.setAdapter(new NumericWheelAdapter(0, 59));
        wvMinute.setTextSize(20);
        wvMinute.setCyclic(true);
        wvMinute.setOnItemSelectedListener(onMiniteItemSelectedListener);

        wvHour.setRate(5 / 4.0f);
        wvMinute.setRate(1 / 2.0f);
        
        initView();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if(restonHelper.isConnected()) {
        	restonHelper.getAutoCollection(3000, new IResultCallback<AutoStartConfig>() {
				@Override
				public void onResultCallback(final CallbackData<AutoStartConfig> cd) {
					// TODO Auto-generated method stub
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if(cd.isSuccess()){
								AutoStartConfig config = cd.getResult();
								mConfig.setEnable(config.isEnable());
								mConfig.setHour(config.getHour());
								mConfig.setMinute(config.getMinute());
								mConfig.setRepeat(config.getRepeat());
								initView();
							}
						}
					});
				}
			});
        }
    }
    
    private void initView() {
    	wvHour.setCurrentItem(mConfig.getHour());
        wvMinute.setCurrentItem(mConfig.getMinute());
    }


    @Override
    public void onClick(View v) {
    	super.onClick(v);
    	if(v == btnSave){
    		final int hour = wvHour.getCurrentItem();
			final int minute = wvMinute.getCurrentItem();
			printLog(getString(R.string.writing_automatically_monitor_device, String.format("%02d:%02d", hour,minute)));
			int repeat = 127; //转车二进制 ：01111111，从右到左，分别表示周一，周二，周三，如果该位是1，表示当天重复，否则不重复。故127表示，周一到周日重复
			restonHelper.setAutoCollection(true, hour, minute, repeat, 1000, new IResultCallback<Void>() {
				@Override
				public void onResultCallback(final CallbackData<Void> cd) {
					// TODO Auto-generated method stub
					SdkLog.log(TAG+" setAutoCollection hour:" + hour+",minute:" + minute + " " + cd);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							String msg = null;
							if(cd.isSuccess()){
								msg = getString(R.string.write_success);
							}else{
								msg = getErrMsg(cd.getStatus());
							}
							//Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
							printLog(msg);
							finish();
						}
					});
				}
			});
    	}
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
    }



    //更新控件快速滑动
    private OnItemSelectedListener onHourItemSelectedListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(int index) {
            
        }
    };

    private OnItemSelectedListener onMiniteItemSelectedListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(int index) {
            
        }
    };
    
}












