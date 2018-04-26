package com.restonsdk.demo.fragment;

import com.restonsdk.demo.AutoStartActivity;
import com.restonsdk.demo.MainActivity;
import com.restonsdk.demo.R;
import com.restonsdk.demo.RawDataActivity;
import com.sleepace.sdk.constant.StatusCode;
import com.sleepace.sdk.core.heartbreath.domain.RealTimeData;
import com.sleepace.sdk.core.heartbreath.util.SleepStatusType;
import com.sleepace.sdk.interfs.IConnectionStateCallback;
import com.sleepace.sdk.interfs.IDeviceManager;
import com.sleepace.sdk.interfs.IMonitorManager;
import com.sleepace.sdk.interfs.IResultCallback;
import com.sleepace.sdk.manager.CONNECTION_STATE;
import com.sleepace.sdk.manager.CallbackData;
import com.sleepace.sdk.util.LogUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
public class ControlFragment extends BaseFragment {
	
	private Button btnCollectStatus, btnAutoStart, btnStartCollect, btnStopCollect, btnStartRealtimeData, btnStopRealtimeData, btnSignal;
	private TextView tvCollectStatus, tvSleepStatus, tvHeartRate, tvBreathRate;
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_control, null);
//		LogUtil.log(TAG+" onCreateView-----------");
		findView(view);
		initListener();
		initUI();
		return view;
	}
	
	
	protected void findView(View root) {
		// TODO Auto-generated method stub
		super.findView(root);
		btnCollectStatus = (Button) root.findViewById(R.id.btn_collect_status);
		btnAutoStart = (Button) root.findViewById(R.id.btn_set_auto_collect);
		btnStartCollect = (Button) root.findViewById(R.id.btn_start_collect);
		btnStopCollect = (Button) root.findViewById(R.id.btn_stop_collect);
		btnStartRealtimeData = (Button) root.findViewById(R.id.btn_realtime_data);
		btnStopRealtimeData = (Button) root.findViewById(R.id.btn_stop_realtime_data);
		btnSignal = (Button) root.findViewById(R.id.btn_signal_strength);
		
		tvCollectStatus = (TextView) root.findViewById(R.id.tv_collect_status);
		tvSleepStatus = (TextView) root.findViewById(R.id.tv_sleep_status);
		tvHeartRate = (TextView) root.findViewById(R.id.tv_heartrate);
		tvBreathRate = (TextView) root.findViewById(R.id.tv_breathrate);
	}


	protected void initListener() {
		// TODO Auto-generated method stub
		super.initListener();
		getRestonHelper().addConnectionStateCallback(stateCallback);
		btnCollectStatus.setOnClickListener(this);
		btnAutoStart.setOnClickListener(this);
		btnStartCollect.setOnClickListener(this);
		btnStopCollect.setOnClickListener(this);
		btnStartRealtimeData.setOnClickListener(this);
		btnStopRealtimeData.setOnClickListener(this);
		btnSignal.setOnClickListener(this);
	}


	protected void initUI() {
		// TODO Auto-generated method stub
		mActivity.setTitle(R.string.control);
		btnCollectStatus.setEnabled(getRestonHelper().isConnected());
		btnAutoStart.setEnabled(getRestonHelper().isConnected());
		btnStartCollect.setEnabled(getRestonHelper().isConnected());
		btnStopCollect.setEnabled(false);
		btnStartRealtimeData.setEnabled(false);
		btnStopRealtimeData.setEnabled(false);
		btnSignal.setEnabled(false);
	}
	
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		printLog(null);
		LogUtil.log(TAG+" onResume collectStatus:" + MainActivity.collectStatus);
		if(MainActivity.collectStatus == 1){
			tvCollectStatus.setText(R.string.working_state_ing);
			btnStartCollect.setEnabled(false);
			btnStopCollect.setEnabled(true);
			btnStartRealtimeData.setEnabled(true);
			btnSignal.setEnabled(true);
		}else if(MainActivity.collectStatus == 0){
			tvCollectStatus.setText(R.string.working_state_not);
			
		}else if(MainActivity.collectStatus == -1){
			tvCollectStatus.setText(R.string.working_state_no);
		}
	}
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		LogUtil.log(TAG+" onDestroyView----------------");
		getRestonHelper().removeConnectionStateCallback(stateCallback);
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
						btnCollectStatus.setEnabled(false);
						btnAutoStart.setEnabled(false);
						btnStartCollect.setEnabled(false);
						btnStopCollect.setEnabled(false);
						btnStartRealtimeData.setEnabled(false);
						btnStopRealtimeData.setEnabled(false);
						btnSignal.setEnabled(false);
						tvCollectStatus.setText(R.string.working_state_no);
						tvSleepStatus.setText(null);
						tvHeartRate.setText(null);
						tvBreathRate.setText(null);
						printLog(R.string.connection_broken);
					}else if(state == CONNECTION_STATE.CONNECTED){
						
					}
				}
			});
		}
	};
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		if(v == btnStartRealtimeData){
			LogUtil.log(TAG+" startRealtime collS:" + MainActivity.collectStatus);
			printLog(R.string.view_data);
			getRestonHelper().startRealTimeData(1000, realtimeCB);
		}else if(v == btnStopRealtimeData){
			printLog(R.string.stopping_data);
			getRestonHelper().stopRealTimeData(1000, new IResultCallback<Void>() {
				@Override
				public void onResultCallback(final CallbackData<Void> cd) {
					// TODO Auto-generated method stub
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if(checkStatus(cd)){
								printLog(R.string.stop_data_successfully);
								btnStartRealtimeData.setEnabled(true);
								btnStopRealtimeData.setEnabled(false);
								tvHeartRate.setText("--");
								tvBreathRate.setText("--");
							}
						}
					});
				}
			});
		}else if(v == btnStopCollect){
			printLog(R.string.notified_acquisition_off);
			getRestonHelper().stopCollection(1000, new IResultCallback<Void>() {
				@Override
				public void onResultCallback(final CallbackData<Void> cd) {
					// TODO Auto-generated method stub
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if(checkStatus(cd)){
								MainActivity.collectStatus = 0;
								printLog(R.string.close_acquisition_success);
								//tvCollectStatus.setText(R.string.working_state_not);
								btnStartCollect.setEnabled(true);
								btnStopCollect.setEnabled(false);
								btnSignal.setEnabled(false);
								btnStartRealtimeData.setEnabled(false);
								btnStopRealtimeData.setEnabled(false);
								
								tvSleepStatus.setText(null);
								tvHeartRate.setText("--");
								tvBreathRate.setText("--");
							}
						}
					});
				}
			});
		}else if(v == btnSignal){
			Intent intent = new Intent(mActivity, RawDataActivity.class);
        	startActivity(intent);
		}else if(v == btnCollectStatus){
			printLog(R.string.getting_device_status);
			getRestonHelper().getCollectionStatus(1000, new IResultCallback<Byte>() {
				@Override
				public void onResultCallback(final CallbackData<Byte> cd) {
					// TODO Auto-generated method stub
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if(checkStatus(cd)){
								MainActivity.collectStatus =  cd.getResult();
								LogUtil.log(TAG+" getCollectionStatus collS:" + MainActivity.collectStatus);
								int textRes = MainActivity.collectStatus == 1 ? R.string.working_state_ing : R.string.working_state_not;
								tvCollectStatus.setText(textRes);
								printLog(textRes);
								
								if(MainActivity.collectStatus == 1){//采集中
									btnStartCollect.setEnabled(false);
									btnStopCollect.setEnabled(true);
									btnStartRealtimeData.setEnabled(true);
									btnStopRealtimeData.setEnabled(false);
									btnSignal.setEnabled(true);
								}else{//非采集
									btnStartCollect.setEnabled(true);
									btnStopCollect.setEnabled(false);
									btnStartRealtimeData.setEnabled(false);
									btnStopRealtimeData.setEnabled(false);
									btnSignal.setEnabled(false);
								}
							}else{
								if(cd.getStatus() == StatusCode.STATUS_DISCONNECT){
									tvCollectStatus.setText(R.string.working_state_no);
								}
							}
						}
					});
				}
			});
		}else if(v == btnAutoStart){
			
			Intent intent = new Intent(mActivity, AutoStartActivity.class);
        	startActivity(intent);
			
		}else if(v == btnStartCollect){
			printLog(R.string.informing_device_collecting);
			getRestonHelper().startCollection(1000, new IResultCallback<Void>() {
				@Override
				public void onResultCallback(final CallbackData<Void> cd) {
					// TODO Auto-generated method stub
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							LogUtil.log(TAG+" startCollection cd:" + cd);
							if(checkStatus(cd)){
								MainActivity.collectStatus = 1;
								printLog(R.string.began_collect_success);
								tvCollectStatus.setText(R.string.working_state_ing);
								btnStartCollect.setEnabled(false);
								btnStopCollect.setEnabled(true);
								btnSignal.setEnabled(true);
								btnStartRealtimeData.setEnabled(true);
								btnStopRealtimeData.setEnabled(false);
							}
						}
					});
				}
			});
		}
	}
	

	 private IResultCallback<RealTimeData> realtimeCB = new IResultCallback<RealTimeData>() {
			@Override
			public void onResultCallback(final CallbackData<RealTimeData> cd) {
				// TODO Auto-generated method stub
//				LogUtil.log(TAG+" realtimeCB cd:" + cd +",isAdd:" + isAdded());
				if(!isAdded()){
					return;
				}
				
				mActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(checkStatus(cd)){
							if(cd.getCallbackType() == IMonitorManager.METHOD_REALTIME_DATA_OPEN){
								printLog(R.string.get_success);
								btnStartRealtimeData.setEnabled(false);
								btnStopRealtimeData.setEnabled(true);
							}/*else if(cd.getType() == IMonitorManager.TYPE_SLEEP_STATUS){//睡眠状态，只有状态发生变化时，才会推送
								RealTimeData realTimeData = (RealTimeData) cd.getResult();
								if(realTimeData.getSleepFlag() == 1){//入睡
									tvSleepStatus.setText(R.string.sleep_);
									printLog(getString(R.string.get_sleep, getString(R.string.sleep_)));
								}else if(realTimeData.getWakeFlag() == 1){//清醒
									tvSleepStatus.setText(R.string.wake_);
									printLog(getString(R.string.get_sleep, getString(R.string.wake_)));
								}else{
									tvSleepStatus.setText(null);
									printLog(getString(R.string.get_sleep, getString(R.string.sleep_) +":" + realTimeData.getSleepFlag()+","+getString(R.string.wake_)+":"+realTimeData.getWakeFlag()));
								}
							}*/else if(cd.getCallbackType() == IMonitorManager.METHOD_REALTIME_DATA){//实时数据
								btnStartCollect.setEnabled(false);
								btnStopCollect.setEnabled(true);
								btnStartRealtimeData.setEnabled(false);
								btnStopRealtimeData.setEnabled(true);
								btnSignal.setEnabled(true);
								
								RealTimeData realTimeData = cd.getResult();
								int sleepStatus = realTimeData.getStatus();
								int statusRes = getSleepStatus(sleepStatus);
								if(statusRes > 0){
									tvSleepStatus.setText(statusRes);
								}else{
									tvSleepStatus.setText(null);
								}
								if(sleepStatus == SleepStatusType.SLEEP_LEAVE){//离床
									tvHeartRate.setText("--");
									tvBreathRate.setText("--");
								}else{
									tvHeartRate.setText(realTimeData.getHeartRate() + getString(R.string.unit_heart));
									tvBreathRate.setText(realTimeData.getBreathRate() + getString(R.string.unit_respiration));
								}
								
								printLog(getString(R.string.get_heart_rate, String.valueOf(realTimeData.getHeartRate())));
								printLog(getString(R.string.get_breath_rate, String.valueOf(realTimeData.getBreathRate())));
								printLog(getString(R.string.get_sleep, getString(statusRes)));
							}
						}
					}
				});
			}
		};
		
		private int getSleepStatus(int status){
			int statusRes = 0;
			switch(status){
			case SleepStatusType.SLEEP_OK:
				statusRes = R.string.normal;
				break;
			case SleepStatusType.SLEEP_INIT:
				statusRes = R.string.initialization;
				break;
			case SleepStatusType.SLEEP_B_STOP:
				statusRes = R.string.respiration_pause;
				break;
			case SleepStatusType.SLEEP_H_STOP:
				statusRes = R.string.heartbeat_pause;
				break;
			case SleepStatusType.SLEEP_BODYMOVE:
				statusRes = R.string.body_movement;
				break;
			case SleepStatusType.SLEEP_LEAVE:
				statusRes = R.string.out_bed;
				break;
			case SleepStatusType.SLEEP_TURN_OVER:
				statusRes = R.string.label_turn_over;
				break;
			}
			
			return statusRes;
		}
}



