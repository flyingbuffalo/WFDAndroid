package com.flyingbuffalo.wfdmanager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import android.net.wifi.p2p.WifiP2pInfo;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Paired WiFi direct device info. Wrapping origin object for library.
 * @author Shin
 *
 */
public class WFDPairInfo {

	public WifiP2pInfo info;
	
	public final static int PORT = 8988;
	public final static int SOCKET_TIMEOUT = 10000;
	public int port = PORT;
	public int socket_timeout = SOCKET_TIMEOUT;
	ServerSocket serverSocket = null;
	
	protected PairSocketConnectedListener pairSocketConnectedListener;
	
	/**
	 * Constructor WFDPairInfo
	 * @param i WifiP2pInfo
	 */
	public WFDPairInfo(WifiP2pInfo i) {
		this.info = i;
	}

	public String getLocalAddress() throws UnknownHostException {
		return InetAddress.getLocalHost().toString();
	}
	
	public String getRemoteAddress() {
		return info.groupOwnerAddress.getHostAddress();				
	}		
	
	/**
	 * Request to connect socket, to return socket.
	 * Socket is returned on onSocketConnected.
	 */	
	@Deprecated 
	public void getSocket() {
		ConnectionAsyncTask conTask = new ConnectionAsyncTask(info);
        conTask.execute();        
	}
	
	/**
	 * .
	 * Set PairSocketConnectedListener and Request to connect socket, to return socket through listener.
	 * Socket is returned on onSocketConnected.
	 * @param l PairSocketConnectedListener
	 */
	public void connectSocketAsync(PairSocketConnectedListener l) {
		this.pairSocketConnectedListener = l;
		
		Log.d("TEST", "connectSocketAsync");
		ConnectionAsyncTask conTask = new ConnectionAsyncTask(info);
        conTask.execute();
	}
	
	/**
	 * To getting socket, using this listener.
	 * @author Shin
	 *
	 */
	public interface PairSocketConnectedListener {
        public void onSocketConnected(Socket s);
	}
	
	 // After the group negotiation, we assign the group owner as the file
    // server. The file server is single threaded, single connection server
    // socket.
	private class ConnectionAsyncTask extends AsyncTask<Void, Void, String> {
		
		private WifiP2pInfo info;
		
		public ConnectionAsyncTask(WifiP2pInfo i) {
			this.info = i;
			Log.d("TEST", "Create ConnectAsync");
        }

		@Override
		protected String doInBackground(Void... params) {
			try {							
				if (info.groupFormed && info.isGroupOwner) {
					Log.d("TEST", "GROUPOWNER");
					serverSocket = new ServerSocket(PORT);
					Log.d("FILE_TEST", "Server socket wait connect request");
					serverSocket.setReuseAddress(true);
		        	Socket socket = serverSocket.accept();
		        	Log.d("FILE_TEST", "Server socket accept connect");
		        	pairSocketConnectedListener.onSocketConnected(socket);
		        	Log.d("FILE_TEST", "onSocketconnected call on server");
		        } else if (info.groupFormed) {       
		        	Log.d("TEST", "CLIENT");
		        	String host = info.groupOwnerAddress.getHostAddress();		 
		        	Socket socket = new Socket();		
	            	socket.bind(null);
	            	socket.connect(new InetSocketAddress(host, PORT), 5000);
	            			
//					socket.bind(null);
//		            socket.connect((new InetSocketAddress(host, port)), socket_timeout);
		            pairSocketConnectedListener.onSocketConnected(socket);
		            Log.d("FILE_TEST", "onSocketconnected call on client");
		        }	
	        } catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(serverSocket != null) {
					try {
						serverSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return null;
		}				
	}
}
