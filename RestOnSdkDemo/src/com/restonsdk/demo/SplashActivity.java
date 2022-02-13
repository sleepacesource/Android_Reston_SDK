package com.restonsdk.demo;


import java.util.ArrayList;
import java.util.List;

import com.sleepace.sdk.util.SdkLog;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SplashActivity extends BaseActivity {
	
	private TextView tvVer;
	private Button btnSkip, btnSearch;
	
	private final int requestCode = 101;//权限请求码
    private boolean hasPermissionDismiss = false;//有权限没有通过
    private String dismissPermission = "";
    private List<String> unauthoPersssions = new ArrayList<String>();
    private String[] permissions = new String[] {Manifest.permission.ACCESS_FINE_LOCATION/*, Manifest.permission.WRITE_EXTERNAL_STORAGE*/ };
    private byte[] ssidRaw;
    private SharedPreferences mSetting;
    private boolean granted = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		findView();
		initListener();
		initUI();
		checkPermissions();
	}
	
	
	@Override
	protected void findView() {
		// TODO Auto-generated method stub
		super.findView();
		tvVer = (TextView) findViewById(R.id.tv_ver);
		btnSkip = (Button) findViewById(R.id.btn_skip);
		btnSearch = (Button) findViewById(R.id.btn_search_device);
	}


	@Override
	protected void initListener() {
		// TODO Auto-generated method stub
		super.initListener();
		btnSkip.setOnClickListener(this);
		btnSearch.setOnClickListener(this);
	}


	@Override
	protected void initUI() {
		// TODO Auto-generated method stub
		super.initUI();
		int len = DemoApp.getInstance().logBuf.length();
		SdkLog.log(TAG+" initUI log len:" + len);
		if(len > 0){//清除log缓存
			DemoApp.getInstance().logBuf.delete(0, len);
		}
		tvVer.setText(getString(R.string.cur_app_version, getAppVersionName()));
	}
	
	
	private String getAppVersionName() {  
	    String versionName = "";  
	    try {  
	        // ---get the package info---  
	        PackageManager pm = getPackageManager();  
	        PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);  
	        versionName = pi.versionName;  
	        if (versionName == null || versionName.length() <= 0) {  
	            return "";  
	        }  
	    } catch (Exception e) {  
	        e.printStackTrace();
	    }  
	    return versionName;  
	}  


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		if(v == btnSkip){
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
		}else if(v == btnSearch){
			Intent intent = new Intent(this, SearchBleDeviceActivity.class);
			startActivity(intent);
		}
	}



	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	
	private void checkPermissions() {
		granted = false;
		if(Build.VERSION.SDK_INT >= 23) {
			unauthoPersssions.clear();
			//逐个判断你要的权限是否已经通过
			for (int i = 0; i < permissions.length; i++) {
				if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
					unauthoPersssions.add(permissions[i]);//添加还未授予的权限
				}
			}
			//申请权限
			if (unauthoPersssions.size() > 0) {//有权限没有通过，需要申请
				ActivityCompat.requestPermissions(this, new String[]{unauthoPersssions.get(0)}, requestCode);
			}else {
				granted = true;
			}
		}else {
			granted = true;
		}
    }
	
	@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        hasPermissionDismiss = false;
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (this.requestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    hasPermissionDismiss = true;
                    dismissPermission = permissions[i];
                    if(!ActivityCompat.shouldShowRequestPermissionRationale(SplashActivity.this, dismissPermission)) {
                    	
                    }
                    break;
                }
            }

            //如果有权限没有被允许
            if (hasPermissionDismiss) {
            	
            }else{
                checkPermissions();
            }
        }
    }
}








































