/*
 * Created by Denzil Ferreira
 * dferreir@andrew.cmu.edu
 * 
 * Network Information service for Android Aware framework
 * Last updated: 30-Jul-2010
 * Modified by: Denzil Ferreira
 */
package com.android.aware_framework;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Hashtable;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

//	Example
//	NetworkInfo_Service networkSrv = NetworkInfo_Service.getService();
//	networkSrv.setApplicationContext(getApplicationContext());
//	networkSrv.setOnNetworkInfoLisnener(new NetworkInfo_Service.OnNetworkInfoChanged() {
//		@Override
//		public void onNetworkInfoChanged(NetworkInfo networkInfo) {
//			Log.d("CSS",networkInfo.getNetInterfaces().toString());
//		}
//	});
//	
//	Intent networkIntent = new Intent(this, NetworkInfo_Service.class);
//	startService(networkIntent);

public class NetworkInfo_Service extends Service {

	public interface OnNetworkInfoChanged{
		public abstract void onNetworkInfoChanged(NetworkInfo networkInfo);
	}
	
	private static OnNetworkInfoChanged netinfoListener;
	public void setOnNetworkInfoLisnener(OnNetworkInfoChanged netInfo) {
		netinfoListener = netInfo;
	}
	public OnNetworkInfoChanged getOnNetworkInfoListener() {
		return netinfoListener;
	}
	
	public static class NetworkInfo {
		
		//network object that stores interface, ip, rx and tx
		public class NetObj {
			private String netInterface;
			private String ip;
			private String received;
			private String transmitted;
			
			public NetObj(String name, String IP, String RX, String TX) {
				netInterface = name;
				ip = IP;
				received = RX;
				transmitted = TX;
			}

			public String getNetInterface() {
				return netInterface;
			}

			public String getIp() {
				return ip;
			}

			public String getReceived() {
				return received;
			}

			public String getTransmitted() {
				return transmitted;
			}
		}
		
		private Hashtable<String, NetObj> netInterfaces = new Hashtable<String, NetObj>();
		
		private NetworkInfo() {
			try {
				for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
					NetworkInterface netInt = en.nextElement();
					
					for (Enumeration<InetAddress> enumIpAddr = netInt.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
		                
						if (!inetAddress.isLoopbackAddress()) {
							
							RandomAccessFile rxFile = new RandomAccessFile(
									new File("/sys/class/net/" + netInt.getName() + "/statistics/rx_bytes"), "r");
							RandomAccessFile txFile = new RandomAccessFile(
									new File("/sys/class/net/" + netInt.getName() + "/statistics/tx_bytes"), "r");
							
							if(rxFile!=null && txFile!=null)
							{
								netInterfaces.put(netInt.getName(), new NetObj(netInt.getName(), inetAddress.getHostAddress().toString(), rxFile.readLine(), txFile.readLine()) );
							}
		                }
					}
					
				}
			} catch (SocketException e) {
				Log.w("AWARE",e.toString());
			} catch (FileNotFoundException e) {
				Log.w("AWARE",e.toString());
			} catch (IOException e) {
				Log.w("AWARE",e.toString());
			}
		}
		
		
		private static NetworkInfo netInfo;
		public static NetworkInfo getNetworkInfo() {
			netInfo = new NetworkInfo();
			return netInfo;
		}
		
		public Hashtable<String, NetObj> getNetInterfaces() {
			return netInterfaces;
		}
	}
	
	private static BroadcastReceiver netInfoReceiver = new NetworkInfo_Monitor();
	public static class NetworkInfo_Monitor extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(netinfoListener!=null)
				netinfoListener.onNetworkInfoChanged(NetworkInfo.getNetworkInfo());
		}
	}
	
	//Activity binder
	private final IBinder netInfoBinder = new NetworkInfoBinder();
	
    private static Context context;
	
    //Singleton pattern
    private static final NetworkInfo_Service netInfoService = new NetworkInfo_Service();
    public static NetworkInfo_Service getService() {
    	return netInfoService;
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		return netInfoBinder;
	}
	
	public class NetworkInfoBinder extends Binder {
		NetworkInfo_Service getService() {
			return NetworkInfo_Service.getService();
		}
	}
	
	public void setApplicationContext(Context mContext) {
		NetworkInfo_Service.context = mContext;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if(context == null) 
			context = this.getApplicationContext();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		
		if(netInfoReceiver != null)
			registerReceiver(netInfoReceiver, filter);
		
		Log.d("AWARE", "Network Information Service running!");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		//clean-up
		unregisterReceiver(netInfoReceiver);
		
		Log.d("AWARE","Network Information Service terminated...");
		
	}
}
