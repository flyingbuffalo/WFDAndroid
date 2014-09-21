package com.flyingbuffalo.wfdmanager;

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * WiFi device object. Using to pairing.
 * @author Shin
 *
 */
public class WFDDevice {

	public WifiP2pDevice device;
	public int platform;
	
	public WFDDevice(WifiP2pDevice d) {
		this.device = d;
	}
}
