package com.flyingbuffalo.wfdmanager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.AsyncTask;

public class WFDManager implements ChannelListener, PeerListListener, ConnectionInfoListener {
	
	/** members **/
	private Context context;
	private WifiP2pManager manager;
	private Channel channel;
	private WifiP2pInfo info;
	private IntentFilter intentFilter = new IntentFilter();
	private WiFiDirectBroadcastReceiver receiver;
	
	/** flags **/
	private boolean retryChannel = false;
	private boolean isWifiP2pEnabled = false;	
	
	/** listeners **/
	public WFDDeviceDiscoveredListener wfdDiscoveredListener;
	public WFDDeviceConnectedListener wfdConnectedListener;
	
	/** ERROR NUM **/
	protected final int WFD_DISABLED = 999;
	protected final int CHANNEL_LOST = 998;
	protected final int DEVICES_RESET = 997;
	protected final int UPDATE_THIS_DEVICE = 996;	
	
	public WFDManager(Context context) {
		this.context = context;
		manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(context, context.getMainLooper(), null);
		
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
	}
	
	public void registerReceiver() {
		receiver = new WiFiDirectBroadcastReceiver(this);
		context.registerReceiver(receiver, intentFilter);
	}
	
	public void unregisterReceiver() {
		context.unregisterReceiver(receiver);
	}
	
	
	public void getDevicesAsync() {
		if(!isWifiP2pEnabled) {
			// WFD OFF
			wfdDiscoveredListener.onDevicesDiscoverFailed(WFD_DISABLED);
		}
		manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
            	// TODO -> onDevicesDiscovered
            }

            @Override
            public void onFailure(int reasonCode) {            	
            	// TODO - need to check it will be need
            	wfdDiscoveredListener.onDevicesDiscoverFailed(reasonCode);            	
            }
        });

	}
	
	public void pairAsync(WFDDevice d) {		
			
		WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = d.device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        manager.connect(channel, config, new ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            	// TODO -> onDeviceConnected
            }

            @Override
            public void onFailure(int reasonCode) {
            	wfdConnectedListener.onDeviceConnectFailed(reasonCode);
            }
        });
	}
	
	public void unpair() {
		manager.removeGroup(channel, new ActionListener() {
			
			@Override
			public void onSuccess() {
				wfdConnectedListener.onDeviceDisconnected();				
			}
			
			@Override
			public void onFailure(int reason) {
				// TODO ?
			}
		});
	}
	
	/**
	 * set Listener
	 * @param listner
	 */
	public void setWFDDeviceDiscoveredListener(WFDDeviceDiscoveredListener l) {
		this.wfdDiscoveredListener = l;
	}
	
	public void setWFDDeviceConnectedListener(WFDDeviceConnectedListener l) {
		this.wfdConnectedListener = l;
	}

	public interface WFDDeviceDiscoveredListener {
		
		public void onDevicesDiscovered(List<WFDDevice> deviceList);
		
		public void onDevicesDiscoverFailed(int reasonCode);
		
		
		/** onDevicesDiscoverFailed로 통합 **/
		
		//public void onChannelLost();	// When channel lost, called.
		
		//public void onWFDdisabled();	// When WFD disabled, called.
		
		/**
	     * Remove all peers and clear all fields. This is called on
	     * BroadcastReceiver receiving a state change event.
	     */
		// public void onDevicesReset();
	}
	
	public interface WFDDeviceConnectedListener {
		
	    public void onDeviceConnected(WFDPairInfo info);
	    
	    public void onDeviceConnectFailed(int reason);
	    
	    //public void onUpdateThisDevice(WFDDevice d);   // Update this device data;
	    
	    public void onDeviceDisconnected();
	    
	}

	@Override
	public void onPeersAvailable(WifiP2pDeviceList peers) {
		List<WifiP2pDevice> device_list = new ArrayList<>();
		device_list.addAll(peers.getDeviceList());
		
		List<WFDDevice> list = new ArrayList<WFDDevice>();
		for(WifiP2pDevice device : device_list) {
			list.add(new WFDDevice(device));
		}
		
		wfdDiscoveredListener.onDevicesDiscovered(list);	
	}
	
	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		this.info = info;
		
        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        wfdConnectedListener.onDeviceConnected(new WFDPairInfo(info));
	}
	
	@Override
	public void onChannelDisconnected() {
		// we will try once more
		if (manager != null && !retryChannel) {
			// Channel lost. Trying again
			wfdDiscoveredListener.onDevicesDiscoverFailed(CHANNEL_LOST);
			// TODO - data reset
			retryChannel = true;
			manager.initialize(context, context.getMainLooper(), this);
		} else {
			// Severe! Channel is probably lost permanently. Try Disable/Re-Enable P2P.
			wfdDiscoveredListener.onDevicesDiscoverFailed(WFD_DISABLED);
		}
	}	
	
	
	/**
	 * @param isWifiP2pEnabled the isWifiP2pEnabled to set
	 */
	protected void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
		this.isWifiP2pEnabled = isWifiP2pEnabled;
	}
	
	protected void requestConnectionInfo() {
		manager.requestConnectionInfo(channel, this);		
	}
	
	protected void requestPeers() {
		manager.requestPeers(channel, this);
	}
	
}
