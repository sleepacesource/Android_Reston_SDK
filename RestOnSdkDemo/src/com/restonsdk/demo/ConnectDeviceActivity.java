package com.restonsdk.demo;

import com.sleepace.sdk.constant.DeviceCode;
import com.sleepace.sdk.core.heartbreath.domain.LoginBean;
import com.sleepace.sdk.domain.BleDevice;
import com.sleepace.sdk.interfs.IResultCallback;
import com.sleepace.sdk.manager.CallbackData;
import com.sleepace.sdk.manager.DeviceType;
import com.sleepace.sdk.reston.RestOnHelper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class ConnectDeviceActivity extends BaseActivity {
    private EditText etUserId;
    private TextView tvDeviceCode;
    private Button btnConnect;
    
    private BleDevice device;
    private RestOnHelper restonHelper;
    //private SharedPreferences mSetting;
    
    private static final String[] DEVICE_CODE = new String[]{
    		DeviceCode.Z2_9_0,//Reston Z2
    		DeviceCode.Z4_22_3,//Z400T 带温湿度
    		DeviceCode.Z4_22_4 //Z400 不带温湿度
    };
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_device);
        restonHelper = RestOnHelper.getInstance(this);
        //mSetting = getSharedPreferences("config", Context.MODE_PRIVATE);
        findView();
        initListener();
        initUI();
    }


    public void findView() {
    	super.findView();
    	etUserId = (EditText) findViewById(R.id.et_userid);
    	tvDeviceCode = (TextView) findViewById(R.id.tv_device_code);
    	btnConnect = (Button) findViewById(R.id.btn_connect_device);
    }

    public void initListener() {
    	super.initListener();
    	tvDeviceCode.setOnClickListener(this);
    	btnConnect.setOnClickListener(this);
    }

    public void initUI() {
    	device = (BleDevice) getIntent().getSerializableExtra("device");
        tvTitle.setText(R.string.connect_device);
        printLog(null);
//        String uid = mSetting.getString("uid", "100");
//        etUserId.setText(uid);
//        etUserId.setSelection(etUserId.length());
        
//        String code = DEVICE_CODE[0];
//		tvDeviceCode.setText(code);
//		DeviceType deviceType = SleepUtil.getDeviceTypeFromCode(code);
//		device.setDeviceType(deviceType);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public void onClick(View v) {
    	super.onClick(v);
    	if(v == tvDeviceCode){
    		new AlertDialog.Builder(this)
    		.setIcon(android.R.drawable.ic_dialog_info)
    		.setTitle(R.string.device_code)
    		.setItems(DEVICE_CODE, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					String code = DEVICE_CODE[which];
					tvDeviceCode.setText(code);
//					DeviceType deviceType = SleepUtil.getDeviceTypeFromCode(code);
//					device.setDeviceType(deviceType);
				}
			})
    		.setNegativeButton(android.R.string.cancel, null)
    		.show();
    	}else if (v == btnConnect) {
    		final String deviceCode = tvDeviceCode.getText().toString();
    		if(TextUtils.isEmpty(deviceCode)){
    			Toast.makeText(this, R.string.device_code, Toast.LENGTH_SHORT).show();
    			return;
    		}
    		
        	printLog(R.string.userid_judgment);
        	String uid = etUserId.getText().toString().trim();
        	
        	if(!TextUtils.isEmpty(uid)){
        		btnConnect.setEnabled(false);
        		
        		printLog(R.string.non_empty);
        		printLog(R.string.connecting_device);
        		
//        		mSetting.edit().putString("uid", uid).commit();
            	
        		int userId = Integer.valueOf(uid);
        		restonHelper.login(device.getDeviceName(), device.getAddress(), deviceCode, userId, 10 * 1000, new IResultCallback<LoginBean>() {
					@Override
					public void onResultCallback(final CallbackData<LoginBean> cd) {
						// TODO Auto-generated method stub
						
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								btnConnect.setEnabled(true);
								if(cd.isSuccess()){
									printLog(R.string.connect_device_successfully);
									LoginBean bean =  cd.getResult();
									device.setDeviceType(DeviceType.getDeviceType(deviceCode));
									device.setDeviceId(bean.getDeviceId());
									Intent intent = new Intent(mContext, MainActivity.class);
									intent.putExtra("device", device);
									startActivity(intent);
								}else{
									printLog(R.string.failure);
								}
							}
						});
						
					}
				});
        	}
        }
    }
    
}












