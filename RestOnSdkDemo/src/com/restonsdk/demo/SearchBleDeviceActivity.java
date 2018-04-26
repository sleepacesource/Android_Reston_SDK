package com.restonsdk.demo;

import java.util.ArrayList;
import java.util.List;

import com.restonsdk.demo.util.BleDeviceNameUtil;
import com.sleepace.sdk.domain.BleDevice;
import com.sleepace.sdk.manager.ble.BleHelper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class SearchBleDeviceActivity extends BaseActivity {
    private TextView tvRefresh;
    private ImageView ivRefresh;
    private View vRefresh;
    private ListView listView;
    private LayoutInflater inflater;
    private BleAdapter adapter;

    private final Handler mHandler = new Handler();
	private int scanTime = 6 * 1000;
	private boolean mScanning;
    
    private RotateAnimation animation;
    
    private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_ble);
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        findView();
        initListener();
        initUI();
    }


    public void findView() {
    	super.findView();
        vRefresh = findViewById(R.id.layout_refresh);
        tvRefresh = (TextView) findViewById(R.id.tv_refresh);
        ivRefresh = (ImageView) findViewById(R.id.iv_refresh);
        listView = (ListView) findViewById(R.id.list);
    }

    public void initListener() {
    	super.initListener();
        vRefresh.setOnClickListener(this);
        listView.setOnItemClickListener(onItemClickListener);
    }

    public void initUI() {
        inflater = getLayoutInflater();

        animation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(2000);//设置动画持续时间
        animation.setRepeatCount(Animation.INFINITE);
        animation.setInterpolator(new LinearInterpolator());
        
        tvTitle.setText(R.string.ble_search);
        adapter = new BleAdapter();
        listView.setAdapter(adapter);
        
        vRefresh.performClick();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BleHelper.REQCODE_OPEN_BT) {
            
        }
    }

    private OnItemClickListener onItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	stopScan();
        	BleDevice device = adapter.getItem(position);
        	Intent intent = new Intent(mContext, ConnectDeviceActivity.class);
        	intent.putExtra("device", device);
        	startActivity(intent);
        }
    };


    private void initRefreshView() {
        tvRefresh.setText(R.string.refresh);
        ivRefresh.clearAnimation();
        ivRefresh.setImageResource(R.drawable.bg_refresh);
    }

    private void initSearchView() {
        tvRefresh.setText(R.string.refreshing);
        ivRefresh.setImageResource(R.drawable.device_loading);
        ivRefresh.startAnimation(animation);
    }

    @Override
    public void onClick(View v) {
    	super.onClick(v);
        if (v == vRefresh) {
        	
        	if(mScanning){
        		return;
        	}
        	
        	printLog(R.string.detects_bluetooth);
        	
        	if(mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()){
        		printLog(R.string.bluetooth_on);
        		printLog(R.string.loaded__list);
        		scanBleDevice();
        	}else{
        		printLog(R.string.not_bluetooth);
        		Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    			startActivityForResult(enabler, BleHelper.REQCODE_OPEN_BT);
        	}
        }
    }
    
    
    private boolean scanBleDevice(){
		if (!mScanning) {
            mScanning = true;
    		initSearchView();
    		adapter.clearData();
            mHandler.postDelayed(stopScanTask, scanTime);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            return true;
        }
		return false;
	}
    
    private final Runnable stopScanTask = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };
    
    public void stopScan() {
        if (mScanning) {
            mScanning = false;
    		initRefreshView();
    		
    		printLog(R.string.load_list_complete);
            
            mHandler.removeCallbacks(stopScanTask);
            //由于stopScan是延时后的操作，为避免断开或其他情况时把对象置空，所以以下2个对象都需要非空判断
            if (mBluetoothAdapter != null && mLeScanCallback != null) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }
    }
    
    
    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {// Z1-140900000
        	runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					String modelName = device.getName();
					if(modelName != null){
						modelName = modelName.trim();
					}
		            String deviceName = BleDeviceNameUtil.getBleDeviceName(0xff, scanRecord);
		            if(deviceName != null){
		            	deviceName = deviceName.trim();
		            }
		            
		            if(!TextUtils.isEmpty(modelName) && !TextUtils.isEmpty(deviceName)){
		            	BleDevice ble = new BleDevice();
		            	ble.setModelName(modelName);
		            	ble.setAddress(device.getAddress());
		            	ble.setDeviceName(deviceName);
		            	ble.setDeviceId(deviceName);
		            	adapter.addBleDevice(ble);
		            }
				}
			});
        }
    };
    

    class BleAdapter extends BaseAdapter {
        private List<BleDevice> list = new ArrayList<BleDevice>();
        

        class ViewHolder {
            TextView tvName;
            TextView tvDeviceId;
        }

        @Override
        public int getCount() {

            return list.size();
        }

        @Override
        public BleDevice getItem(int position) {

            return list.get(position);
        }

        @Override
        public long getItemId(int position) {

            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_reston_item, null);
                holder = new ViewHolder();
                holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
                holder.tvDeviceId = (TextView) convertView.findViewById(R.id.tv_deviceid);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            BleDevice item = getItem(position);
            holder.tvName.setText(item.getModelName());
            holder.tvDeviceId.setText(item.getDeviceName());
            return convertView;
        }

        public void addBleDevice(BleDevice bleDevice) {

            boolean exists = false;
            for (BleDevice d : list) {
                if (d.getAddress().equals(bleDevice.getAddress())) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                list.add(bleDevice);
                notifyDataSetChanged();
            }
        }

        public List<BleDevice> getData() {
            return list;
        }

        public void clearData() {
            list.clear();
            notifyDataSetChanged();
        }
    }
}
