package com.example.wfdmanagersample;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import com.flyingbuffalo.wfdmanager.WFDDevice;
import com.flyingbuffalo.wfdmanager.WFDManager;
import com.flyingbuffalo.wfdmanager.WFDManager.WFDDeviceConnectedListener;
import com.flyingbuffalo.wfdmanager.WFDManager.WFDDeviceDiscoveredListener;
import com.flyingbuffalo.wfdmanager.WFDPairInfo;
import com.flyingbuffalo.wfdmanager.WFDPairInfo.PairSocketConnectedListener;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements WFDDeviceDiscoveredListener, WFDDeviceConnectedListener{

	WFDManager manager;
	int flag = 0;
	
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
		manager.registerReceiver();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		manager.unregisterReceiver();
	}

	@Override
	public void onDeviceConnected(final WFDPairInfo info) {
		// TODO Auto-generated method stub
		Log.d("TEST", "onDeviceConnected");
		flag = 1;
		info.setPairSocketConnectedListener(new PairSocketConnectedListener() {
			
			@Override
			public void onSocketConnected(Socket s) {
				try{
					if (info.info.groupFormed && info.info.isGroupOwner) {
						Log.d("TEST", "Server: connection done");
						MessageAsyncTask m = new MessageAsyncTask(s);
						m.execute();
					} else if (info.info.groupFormed) {
						Log.d("TEST", "Client: ready to send message");
						while (true) {
							BufferedWriter w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
							PrintWriter out = new PrintWriter(w, true);
		                    String return_msg = "PING";
		                    out.println(return_msg);
		                    Log.d("TEST", "result :" + return_msg);
		                    
		                    Thread.sleep(1000);
						}
					}
				} catch(IOException e) {
					Log.e("TEST ERROR", e.getMessage());
				} catch (InterruptedException e) {					
					e.printStackTrace();
				}
			}
		});
		Log.d("TEST", "get socket call");
		info.getSocket();
		Log.d("TEST", "get socket done");
	}

	@Override
	public void onDeviceConnectFailed(int reason) {
		// TODO Auto-generated method stub
		Log.d("TEST", "onDeviceConnectFailed");
	}

//	@Override
//	public void onUpdateThisDevice(WFDDevice d) {
//		// TODO Auto-generated method stub
//		Log.d("TEST", "onUpdateThisDevice");
//	}

	@Override
	public void onDevicesDiscovered(List<WFDDevice> deviceList) {
		// TODO Auto-generated method stub		
		if(flag == 0 && deviceList.size() > 0) {
			Log.d("TEST", "onDevicesDiscovered - count : " + deviceList.size());
			WFDDevice device = null;
			for(WFDDevice d : deviceList) {
				Log.d("devicelist", d.device.deviceName + "  " + d.device.deviceAddress + "  " + d.device.isGroupOwner() + "  " + d.device.status);
				device = d;
			}
			
			manager.pairAsync(device);
		}
	}

	@Override
	public void onDevicesDiscoverFailed(int reasonCode) {
		// TODO Auto-generated method stub
		Log.d("TEST", "onDevicesDiscoverFailed");
	}

//	@Override
//	public void onChannelLost() {
//		// TODO Auto-generated method stub
//		Log.d("TEST", "onChannelLost");
//	}
//
//	@Override
//	public void onWFDdisabled() {
//		// TODO Auto-generated method stub
//		Log.d("TEST", "onWFDdisabled");
//	}
//
//	@Override
//	public void onDevicesReset() {
//		// TODO Auto-generated method stub
//		Log.d("TEST", "onDevicesReset");
//	}

	@Override
	public void onDeviceDisconnected() {
		// TODO Auto-generated method stub
		Log.d("TEST", "onDeviceDisconnected");
	}
	
	public class MessageAsyncTask extends AsyncTask<Void,Void,Void> {
		private Socket client = null;
		
		public MessageAsyncTask(Socket s) {
			client = s;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			try {
				while(true) {
					BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    String str = in.readLine();
                    Log.d("TEST","S: Received: '" + str + "'");
//                    try {
//                        Thread.sleep(1000);         
//                    } catch (InterruptedException e) {
//                       e.printStackTrace();
//                    }
//                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
//                    out.println("Server Received " + str);
				}
			} catch (Exception e) {
				Log.d("TEST", "Server: error");
			}
			return null;
		}
		
	}
}
