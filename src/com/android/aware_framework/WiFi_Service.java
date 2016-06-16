/*
 * Created by Denzil Ferreira
 * dferreir@andrew.cmu.edu
 * 
 * WiFi service for Android Aware framework
 * Last updated: 30-Jul-2010
 * Modified by: Denzil Ferreira
 */
package com.android.aware_framework;

//	EXAMPLE
//	WiFi_Service wifiSrv = WiFi_Service.getService();
//	wifiSrv.setApplicationContext(getApplicationContext());
//	wifiSrv.setWifiManager((WifiManager) getSystemService(WIFI_SERVICE));
//	
//	wifiSrv.setOnWiFiScanListener(new WiFi_Service.OnWiFiScanListener() {
//		@Override
//		public void onWiFiScan(WiFi_Info wifiInfo) {
//			Log.d("CSS",wifiInfo.getWiFiInfo().toString());
//		}
//	});
//	
//	Intent wifiIntent = new Intent(this, WiFi_Service.class);
//	startService(wifiIntent);

import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class WiFi_Service extends Service {

	public static class WiFi_Info {
		
		private WifiInfo wifiConnected;
		private List<ScanResult> wifiNeighbours;
		
		private WiFi_Info() {
			wifiConnected = wifiManager.getConnectionInfo();
			wifiNeighbours = wifiManager.getScanResults();
		}
		
		public List<ScanResult> getNeighbours() {
			return wifiNeighbours;
		}
		
		public WifiInfo getWiFiInfo() {
			return wifiConnected;
		}
		
		private static WiFi_Info wifiInfo;
		public static WiFi_Info getWiFi_Info() {
			wifiInfo = new WiFi_Info();
			return wifiInfo;
		}
	}
	
	private static OnWiFiScanListener wifiListener;
	public interface OnWiFiScanListener {
		public abstract void onWiFiScan(WiFi_Info wifiInfo);
	}
	public void setOnWiFiScanListener(OnWiFiScanListener wifi) {
		wifiListener = wifi;
	}
	public OnWiFiScanListener getOnWiFiScanListener() {
		return wifiListener;
	}
	
	private static BroadcastReceiver wifiReceiver = new WiFiServiceReceiver();
	public static class WiFiServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(wifiListener!=null)
				wifiListener.onWiFiScan(WiFi_Info.getWiFi_Info());
		}		
	}
	
	private static WifiManager wifiManager;
	public WifiManager getWifiManager() {
		return wifiManager;
	}
	public void setWifiManager(WifiManager wifiManager) {
		WiFi_Service.wifiManager = wifiManager;
	}

	//Activity binder
	private final IBinder wifiBinder = new WiFiBinder();
	
    private static Context context;
	
    //Singleton pattern
    private static final WiFi_Service wifiService = new WiFi_Service();
    public static WiFi_Service getService() {
    	return wifiService;
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		return wifiBinder;
	}
	
	public class WiFiBinder extends Binder {
		WiFi_Service getService() {
			return WiFi_Service.getService();
		}
	}
	
	public void setApplicationContext(Context mContext) {
		WiFi_Service.context = mContext;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if(context == null) 
			context = this.getApplicationContext();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		
		if(wifiReceiver!=null)
			registerReceiver(wifiReceiver, filter);
		
		Log.d("AWARE", "WiFi Service running!");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		//clean-up
		unregisterReceiver(wifiReceiver);
		
		Log.d("AWARE","WiFi Service terminated...");
		
	}
}
