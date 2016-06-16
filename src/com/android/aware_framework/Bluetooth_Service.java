/*
 * Created by Denzil Ferreira
 * dferreir@andrew.cmu.edu
 * 
 * Bluetooth service for Android Aware framework
 * Last updated: 4-Aug-2010
 * Modified by: Denzil Ferreira
 */
package com.android.aware_framework;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

//	Example
//		Bluetooth_Service bluetoothSrv = Bluetooth_Service.getService();
//		bluetoothSrv.setApplicationContext(getApplicationContext());
//		
//		bluetoothSrv.setOnBluetoothListener(new Bluetooth_Service.OnBluetoothListener() {
//			@Override
//			public void onBluetoothScan(BluetoothInfo btInfo) {
//					Log.d("CSS","onBluetoothScan:"+btInfo.getScanResults());
//			}
//		});
//		
//		Intent bluetoothIntent = new Intent(this, Bluetooth_Service.class);
//		startService(bluetoothIntent);
//		
//		//What will be ran when Repeater is fired
//		Intent updateBluetooth = new Intent(BluetoothReceiver.ACTION_BLUETOOTHMANAGER);
//		PendingIntent bluetoothUpdater = PendingIntent.getBroadcast(this, 0, updateBluetooth, 0);
//		
//		Repeater_Service btRepeater = Repeater_Service.getService();
//		btRepeater.setApplicationContext(getApplicationContext());
//		btRepeater.setAlarmManager((AlarmManager)getSystemService(ALARM_SERVICE));
//		
//		btRepeater.setRepeaterIntent(bluetoothUpdater);
//		btRepeater.setRepetitionInterval(60000); //1 minute interval
//		btRepeater.start();

public class Bluetooth_Service extends Service {

	private static BluetoothAdapter bluetoothAdapter;
	
	public static class BluetoothInfo {
		private static String address;
		private static String name;
		private static boolean enabled;
		private static int state;
		private static int scanMode;
		private static String scanResults;
		
		private BluetoothInfo(){
			
			Runnable btInfo = new Runnable() {
				@Override
				public void run() {
					
					//ask for discovery at startup!
					if(bluetoothAdapter.isEnabled())
						bluetoothAdapter.startDiscovery();
					
					setAddress(bluetoothAdapter.getAddress());
					setName(bluetoothAdapter.getName());
					enabled = bluetoothAdapter.isEnabled();
					setState(bluetoothAdapter.getState());
					setScanMode(bluetoothAdapter.getScanMode());
					
					try {
						Process process = Runtime.getRuntime().exec("cat /sys/devices/virtual/bluetooth/hci0/inquiry_cache");
						DataOutputStream dataOutStream = new DataOutputStream(process.getOutputStream());
						DataInputStream dataInStream = new DataInputStream(process.getInputStream());
						
						dataOutStream.writeBytes("cat /sys/devices/virtual/bluetooth/hci0/inquiry_cache" + "\n");
						dataOutStream.flush();
						
						String inquiry = dataInStream.readLine()+"\n";
						scanResults = inquiry;
						while (dataInStream.available() > 0) {
							inquiry = dataInStream.readLine();
							scanResults += inquiry +"\n";
						}
						process.waitFor();
						
						//Kill process and streams
						dataOutStream.close();
						dataInStream.close();
						process.destroy();
						
					} catch (IOException e) {
						Log.d("AWARE","Bluetooth OutputStream was empty, retrying...");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
			
			Thread btThreadInfo = new Thread(null, btInfo,"btinfo");
			btThreadInfo.start();
			
		}
		
		private static BluetoothInfo btInfo;
		public static BluetoothInfo getBluetoothInfo() {
			btInfo = new BluetoothInfo();
			return btInfo;
		}
		public String getAddress() {
			return address;
		}
		public String getName() {
			return name;
		}
		public boolean isEnabled() {
			return enabled;
		}
		public int getState() {
			return state;
		}
		public int getScanMode() {
			return scanMode;
		}
		public String getScanResults() {
			return scanResults;
		}
		public void setAddress(String address) {
			BluetoothInfo.address = address;
		}
		public void setName(String name) {
			BluetoothInfo.name = name;
		}
		public void setEnabled(boolean enabled) {
			BluetoothInfo.enabled = enabled;
		}
		public void setState(int state) {
			BluetoothInfo.state = state;
		}
		public void setScanMode(int scanMode) {
			BluetoothInfo.scanMode = scanMode;
		}
		public void setScanResults(String scanResults) {
			BluetoothInfo.scanResults = scanResults;
		}
	}
	
	private static BroadcastReceiver bluetoothReceiver = new BluetoothReceiver();
	public static class BluetoothReceiver extends BroadcastReceiver {
		public static final String ACTION_BLUETOOTHMANAGER = "ACTION_BLUETOOTHMANAGER";
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(onBluetoothListener!=null)
			{
				onBluetoothListener.onBluetoothScan(BluetoothInfo.getBluetoothInfo());	
			}
		}		
	}
	
	private static OnBluetoothListener onBluetoothListener;
	public interface OnBluetoothListener {
		public abstract void onBluetoothScan(BluetoothInfo btInfo);
	}
	public void setOnBluetoothListener(OnBluetoothListener btListener) {
		onBluetoothListener = btListener;
	}
	public OnBluetoothListener getOnBluetoothListener() {
		return onBluetoothListener;
	}
	
	//Activity binder
	private final IBinder bluetoothBinder = new BluetoothBinder();
	
    private static Context context;
	
    //Singleton pattern
    private static final Bluetooth_Service bluetoothService = new Bluetooth_Service();
    public static Bluetooth_Service getService() {
    	return bluetoothService;
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		return bluetoothBinder;
	}
	
	public class BluetoothBinder extends Binder {
		Bluetooth_Service getService() {
			return Bluetooth_Service.getService();
		}
	}
	
	public void setApplicationContext(Context mContext) {
		Bluetooth_Service.context = mContext;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if(context == null) 
			context = this.getApplicationContext();
		
		if(bluetoothAdapter == null)
			bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothReceiver.ACTION_BLUETOOTHMANAGER);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		
		if(bluetoothReceiver!=null) {
			registerReceiver(bluetoothReceiver, filter);
		}
		
		if(onBluetoothListener!=null)
			onBluetoothListener.onBluetoothScan(BluetoothInfo.getBluetoothInfo());
		
		Log.d("AWARE", "Bluetooth Service running!");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("AWARE","Bluetooth Service terminated...");
		unregisterReceiver(bluetoothReceiver);
	}
}
