package com.flyingbuffalo.wfdmanager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.net.wifi.p2p.WifiP2pInfo;
import android.os.AsyncTask;

public class WFDPairInfo {

	public WifiP2pInfo info;
	
	public final static int PORT = 8988;
	public final static int SOCKET_TIMEOUT = 10000;
	public int port = PORT;
	public int socket_timeout = SOCKET_TIMEOUT;
	
	protected PairSocketConnectedListener pairSocketConnectedListener;
	
	public WFDPairInfo(WifiP2pInfo i) {
		this.info = i;
	}

	public void getSocket() {
		ConnectionAsyncTask conTask = new ConnectionAsyncTask(info);
        conTask.execute();        
	}
	
	public void setPairSocketConnectedListener(PairSocketConnectedListener l) {
		this.pairSocketConnectedListener = l;
	}
	
	public interface PairSocketConnectedListener {
        public void onSocketConnected(Socket s);
	}
	
	private class ConnectionAsyncTask extends AsyncTask<Void, Void, String> {
		
		private WifiP2pInfo info;
		
		public ConnectionAsyncTask(WifiP2pInfo i) {
			this.info = i;
        }

		@Override
		protected String doInBackground(Void... params) {
			try {
				if (info.groupFormed && info.isGroupOwner) {
		        	ServerSocket serverSocket = new ServerSocket(PORT);
		        	Socket client = serverSocket.accept();
		        	pairSocketConnectedListener.onSocketConnected(client);
		        } else if (info.groupFormed) {
		        	String host = info.groupOwnerAddress.getHostAddress();		        
		        	
		        	Socket socket = new Socket();		        
					socket.bind(null);
		            socket.connect((new InetSocketAddress(host, port)), socket_timeout);
		            pairSocketConnectedListener.onSocketConnected(socket);
		        }	
	        } catch (IOException e) {				
				e.printStackTrace();
			}
			return null;
		}				
	}
}
