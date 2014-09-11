package com.example.wfdmanagersample;

import java.net.Socket;
import java.util.List;

import com.flyingbuffalo.wfdmanager.WFDDevice;
import com.flyingbuffalo.wfdmanager.WFDManager;
import com.flyingbuffalo.wfdmanager.WFDManager.WFDDeviceConnectedListener;
import com.flyingbuffalo.wfdmanager.WFDManager.WFDDeviceDiscoveredListener;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements WFDDeviceDiscoveredListener, WFDDeviceConnectedListener{

	WFDManager manager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		manager = new WFDManager(getApplicationContext());
		manager.setWFDDeviceDiscoveredListener(this);
		manager.setWFDDeviceConnectedListener(this);			
		
		Button btn_find = (Button) findViewById(R.id.btn_find);
		btn_find.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d("TEST", "Click");
				manager.getDevicesAsync();
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		manager.resisterReceiver();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		manager.unregisterReceiver();
	}

	@Override
	public void onDeviceConnected(Socket s, WifiP2pInfo info) {
		// TODO Auto-generated method stub
		Log.d("TEST", "onDeviceConnected");
	}

	@Override
	public void onDeviceConnectFailed(int reason) {
		// TODO Auto-generated method stub
		Log.d("TEST", "onDeviceConnectFailed");
	}

	@Override
	public void onUpdateThisDevice(WFDDevice d) {
		// TODO Auto-generated method stub
		Log.d("TEST", "onUpdateThisDevice");
	}

	@Override
	public void onDevicesDiscovered(List<WFDDevice> deviceList) {
		// TODO Auto-generated method stub
		Log.d("TEST", "onDevicesDiscovered - count : " + deviceList.size());
		if(deviceList.size() > 0) {
			WFDDevice device = null;
			for(WFDDevice d : deviceList) {
				Log.d("devicelist", d.device.deviceName + "  " + d.device.deviceAddress + "  " + d.device.isGroupOwner() + "  " + d.device.status);
				device = d;
			}
			
			manager.connectAsync(device);
		}
	}

	@Override
	public void onDevicesDiscoverFailed(int reasonCode) {
		// TODO Auto-generated method stub
		Log.d("TEST", "onDevicesDiscoverFailed");
	}

	@Override
	public void onChannelLost() {
		// TODO Auto-generated method stub
		Log.d("TEST", "onChannelLost");
	}

	@Override
	public void onWFDdisabled() {
		// TODO Auto-generated method stub
		Log.d("TEST", "onWFDdisabled");
	}

	@Override
	public void onDevicesReset() {
		// TODO Auto-generated method stub
		Log.d("TEST", "onDevicesReset");
	}

	@Override
	public void onDeviceDisconnected() {
		// TODO Auto-generated method stub
		Log.d("TEST", "onDeviceDisconnected");
	}
	
}