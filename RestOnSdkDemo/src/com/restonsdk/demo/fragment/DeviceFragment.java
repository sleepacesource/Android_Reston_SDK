package com.restonsdk.demo.fragment;

import java.io.IOException;
import java.io.InputStream;

import com.restonsdk.demo.MainActivity;
import com.restonsdk.demo.R;
import com.sleepace.sdk.core.heartbreath.domain.BatteryBean;
import com.sleepace.sdk.interfs.IConnectionStateCallback;
import com.sleepace.sdk.interfs.IDeviceManager;
import com.sleepace.sdk.interfs.IResultCallback;
import com.sleepace.sdk.manager.CONNECTION_STATE;
import com.sleepace.sdk.manager.CallbackData;
import com.sleepace.sdk.manager.DeviceMaterial;
import com.sleepace.sdk.manager.DeviceType;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class DeviceFragment extends BaseFragment {
	private Button btnDeviceName, btnDeviceId, btnPower, btnVersion, btnUpgrade;
	private TextView tvDeviceName, tvDeviceId, tvPower, tvVersion, tvUpgrade;
	private TextView tvDisconnect;
	private boolean upgrading = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View root = inflater.inflate(R.layout.fragment_device, null);
//		LogUtil.log(TAG+" onCreateView-----------");
		findView(root);
		initListener();
		initUI();
		return root;
	}
	
	protected void findView(View root) {
		// TODO Auto-generated method stub
		super.findView(root);
		tvDeviceName = (TextView) root.findViewById(R.id.tv_device_name);
		tvDeviceId = (TextView) root.findViewById(R.id.tv_device_id);
		tvPower = (TextView) root.findViewById(R.id.tv_device_battery);
		tvVersion = (TextView) root.findViewById(R.id.tv_device_version);
		tvUpgrade = (TextView) root.findViewById(R.id.tv_upgrade_fireware);
		btnDeviceName = (Button) root.findViewById(R.id.btn_get_device_name);
		btnDeviceId = (Button) root.findViewById(R.id.btn_get_device_id);
		btnPower = (Button) root.findViewById(R.id.btn_get_device_battery);
		btnVersion = (Button) root.findViewById(R.id.btn_device_version);
		btnUpgrade = (Button) root.findViewById(R.id.btn_upgrade_fireware);
		tvDisconnect = (TextView) root.findViewById(R.id.tv_disconnect);
	}


	protected void initListener() {
		// TODO Auto-generated method stub
		super.initListener();
		getRestonHelper().addConnectionStateCallback(stateCallback);
		btnDeviceName.setOnClickListener(this);
		btnDeviceId.setOnClickListener(this);
		btnPower.setOnClickListener(this);
		btnVersion.setOnClickListener(this);
		btnUpgrade.setOnClickListener(this);
		tvDisconnect.setOnClickListener(this);
	}


	protected void initUI() {
		// TODO Auto-generated method stub
		mActivity.setTitle(R.string.device);
		tvDisconnect.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); 
		setPageEnable(getRestonHelper().isConnected());
		tvDeviceName.setText(MainActivity.deviceName);
		tvDeviceId.setText(MainActivity.deviceId);
		tvPower.setText(MainActivity.power);
		tvVersion.setText(MainActivity.version);
		printLog(null);
	}
	
	
	private void setPageEnable(boolean enable){
		btnDeviceName.setEnabled(enable);
		btnDeviceId.setEnabled(enable);
		btnPower.setEnabled(enable);
		btnVersion.setEnabled(enable);
		btnUpgrade.setEnabled(enable);
		tvDisconnect.setEnabled(enable);
	}
	
	private IConnectionStateCallback stateCallback = new IConnectionStateCallback() {
		@Override
		public void onStateChanged(IDeviceManager manager, final CONNECTION_STATE state) {
			// TODO Auto-generated method stub
			
			if(!isAdded()){
				return;
			}
			
			mActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(state == CONNECTION_STATE.DISCONNECT){
						
						if(upgrading){
							upgrading = false;
							mActivity.hideUpgradeDialog();
							printLog(R.string.update_completed);
							tvUpgrade.setText(R.string.update_completed);
						}
						
						setPageEnable(false);
						printLog(R.string.connection_broken);
						
					}else if(state == CONNECTION_STATE.CONNECTED){
						
						if(upgrading){
							upgrading = false;
							btnUpgrade.setEnabled(true);
							mActivity.hideUpgradeDialog();
							printLog(R.string.update_completed);
							tvUpgrade.setText(R.string.update_completed);
						}
						
					}
				}
			});
		}
	};
	

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		getRestonHelper().removeConnectionStateCallback(stateCallback);
	}

	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		if(v == btnUpgrade){
			FirmwareBean bean = getFirmwareBean();
			if(bean == null){
				return;
			}
				
			btnUpgrade.setEnabled(false);
			mActivity.showUpgradeDialog();
			upgrading = true;
//			InputStream is = getResources().getAssets().open("Z2_V1.11.des");
			getRestonHelper().upgradeDevice(bean.crcDes, bean.crcBin, bean.is, new IResultCallback<Integer>() {
				@Override
				public void onResultCallback(final CallbackData<Integer> cd) {
					// TODO Auto-generated method stub
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if(checkStatus(cd)){
								int progress =  cd.getResult();
								tvUpgrade.setText(getString(R.string.upgrading_device, progress+"%"));
								
								printLog(tvUpgrade.getText().toString());
								
								if(progress == 100){
									printLog(getString(R.string.reboot_device, getString(R.string.device_name)));
									tvUpgrade.setText(getString(R.string.reboot_device, getString(R.string.device_name)));
								}
							}else{
								upgrading = false;
								btnUpgrade.setEnabled(true);
								mActivity.hideUpgradeDialog();
							}
						}
					});
				}
			});
		}else if(v == btnDeviceName){
			printLog(R.string.getting_device_name);
			MainActivity.deviceName = mActivity.getDevice().getDeviceName();
			tvDeviceName.setText(MainActivity.deviceName);
			printLog(getString(R.string.receive_device_name, mActivity.getDevice().getDeviceName()));
		}else if(v == btnDeviceId){
			printLog(R.string.getting_device_id);
			MainActivity.deviceId = mActivity.getDevice().getDeviceId();
			tvDeviceId.setText(MainActivity.deviceId);
			printLog(getString(R.string.get_device_id, mActivity.getDevice().getDeviceId()));
		}else if(v == btnPower){
			printLog(R.string.getting_power);
			getRestonHelper().getBattery(1000, new IResultCallback<BatteryBean>() {
				@Override
				public void onResultCallback(final CallbackData<BatteryBean> cd) {
					// TODO Auto-generated method stub
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if(checkStatus(cd)){
								BatteryBean bean =  cd.getResult();
								MainActivity.power = bean.getQuantity()+"%";
								tvPower.setText(MainActivity.power);
								printLog(getString(R.string.get_power, MainActivity.power));
							}
						}
					});
				}
			});
		}else if(v == btnVersion){
			printLog(R.string.getting_current_version);
			getRestonHelper().getDeviceVersion(1000, new IResultCallback<String>() {
				@Override
				public void onResultCallback(final CallbackData<String> cd) {
					// TODO Auto-generated method stub
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if(checkStatus(cd)){
								MainActivity.version =  cd.getResult();
								tvVersion.setText(MainActivity.version);
								printLog(getString(R.string.get_the_current_version, MainActivity.version));
							}
						}
					});
				}
			});
		}else if(v == tvDisconnect){
			printLog(R.string.disconnected_successfully);
			setPageEnable(false);
			
			mActivity.exit();
		}
	}
	
	
	class FirmwareBean{
		InputStream is;
		long crcBin;
		long crcDes;
	}
	
	
	private FirmwareBean getFirmwareBean(){
		
		DeviceType deviceType = mActivity.getDevice().getDeviceType();
		InputStream is = null;
		long crcBin = 0, crcDes = 0;
		
		try {
			switch(deviceType){
			case DEVICE_TYPE_Z2:
				is = getResources().getAssets().open("9-0_1.14.des");
				crcBin = 3855732355l;
				crcDes = 2539419916l;
				break;
			case DEVICE_TYPE_Z4:
//				if(deviceType.getMaterial() == DeviceMaterial.Z4.MATERIAL_NO_TEMPERATURE_HUMIDITY){
//					is = getResources().getAssets().open("22-4_1.15.des");
//					crcBin = 3430449546l;
//					crcDes = 2909393882l;
//				}else{
//					is = getResources().getAssets().open("22-3_1.15.des");
//					crcBin = 3430449546l;
//					crcDes = 2909393882l;
//				}
				
				is = getResources().getAssets().open("Z400T-2(SW)-v1.31r(v2.0.2r)-p-20201231.des");
				crcBin = 3048971386l;
				crcDes = 1309530705l;
				
				break;
			case DEVICE_TYPE_PILLOW:
				if(deviceType.getMaterial() == DeviceMaterial.Pillow.MATERIAL_DEFAULT){
					is = getResources().getAssets().open("3-0_1.12.des");
					crcBin = 3178547150l;
					crcDes = 4013397468l;
				}else if(deviceType.getMaterial() == DeviceMaterial.Pillow.MATERIAL_CHEMICAL_FIBER_PIEZOELECTRIC_SOLID){
					is = getResources().getAssets().open("3-1_1.04.des");
					crcBin = 709647589l;
					crcDes = 542788408l;
				}else if(deviceType.getMaterial() == DeviceMaterial.Pillow.MATERIAL_MEMORY_COTTON_CABLE){
					is = getResources().getAssets().open("3-2_1.12.des");
					crcBin = 3178547150l;
					crcDes = 4013397468l;
				}else if(deviceType.getMaterial() == DeviceMaterial.Pillow.MATERIAL_CHEMICAL_FIBER_PIEZOELECTRIC_PIECE){
					is = getResources().getAssets().open("3-3_1.04.des");
					crcBin = 709647589l;
					crcDes = 542788408l;
				}else if(deviceType.getMaterial() == DeviceMaterial.Pillow.MATERIAL_CHEMICAL_FIBER_PIEZOELECTRIC_PERFUME){
					is = getResources().getAssets().open("3-4_1.04.des");
					crcBin = 709647589l;
					crcDes = 542788408l;
				}else if(deviceType.getMaterial() == DeviceMaterial.Pillow.MATERIAL_REMOVABLE_POLYESTER){
					is = getResources().getAssets().open("3-5_1.04.des");
					crcBin = 709647589l;
					crcDes = 542788408l;
				}else if(deviceType.getMaterial() == DeviceMaterial.Pillow.MATERIAL_REMOVABLE_MEMORY_SLIVER){
					is = getResources().getAssets().open("3-6_1.04.des");
					crcBin = 709647589l;
					crcDes = 542788408l;
				}
				break;
			default:
				break;
			}
			
			FirmwareBean bean = new FirmwareBean();
			bean.is = is;
			bean.crcBin = crcBin;
			bean.crcDes = crcDes;
			return bean;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}










