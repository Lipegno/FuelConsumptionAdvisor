/*
 * Created by Denzil Ferreira
 * dferreir@andrew.cmu.edu
 * 
 * Devices Info service for Android Aware framework
 * Last updated: 30-Jun-2010
 * Modified by: Denzil Ferreira
 */
package com.android.aware_framework;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;

//	Example

//	DevicesInfo_Service deviceSrv = DevicesInfo_Service.getService();
//	deviceSrv.setApplicationContext(getApplicationContext());
//	deviceSrv.setTelephonyManager((TelephonyManager) getApplicationContext().getSystemService(TELEPHONY_SERVICE));
//	
//	Intent deviceInfoIntent = new Intent(this,DevicesInfo_Service.class);
//	startService(deviceInfoIntent);
//	
//	DeviceInfo device = DeviceInfo.getDeviceInfo(); //singleton
//	Log.d("AWARE","Network Operator: " +device.getDeviceNetworkOperatorName());

public class DevicesInfo_Service extends Service {

	public static class DeviceInfo {
		
		private String deviceID="";
		private String deviceSWVersion="";
		private String deviceSimCountryIso="";	
		private String deviceNetworkOperatorID="";
		private String deviceNetworkOperatorName="";
		private int deviceNetworkType=0;
		private boolean isDeviceRoaming=false;
		private int devicePhoneType=0;
		private String devicePhoneNumber="";
		private String deviceSIMCardSerialNumber="";
		private String deviceSubscriberID="";
		private int deviceServiceState=0;
		private int deviceDataState=0;
		private Build deviceBuild;

		private static DeviceInfo deviceInfo = new DeviceInfo(getTelephonyManager());
		public static DeviceInfo getDeviceInfo() {
			return deviceInfo;
		}
		
		//Singleton
		private DeviceInfo(TelephonyManager teleManager) {
			deviceID = teleManager.getDeviceId();
			deviceSWVersion = teleManager.getDeviceSoftwareVersion();
			deviceSimCountryIso = teleManager.getSimCountryIso();	
			deviceNetworkOperatorID = teleManager.getNetworkOperator();
			deviceNetworkOperatorName = teleManager.getNetworkOperatorName();
			deviceNetworkType = teleManager.getNetworkType();
			isDeviceRoaming = teleManager.isNetworkRoaming();
			devicePhoneType = teleManager.getPhoneType();
			devicePhoneNumber = teleManager.getLine1Number();
			deviceSIMCardSerialNumber = teleManager.getSimSerialNumber();
			deviceSubscriberID = teleManager.getSubscriberId();
			deviceServiceState=new ServiceState().getState();
			deviceDataState = teleManager.getDataState();
			deviceBuild = new Build();
		}
		
		public String getDeviceID() {
			return deviceID;
		}

		public String getDeviceSWVersion() {
			return deviceSWVersion;
		}

		public String getDeviceSimCountryIso() {
			return deviceSimCountryIso;
		}

		public String getDeviceNetworkOperatorID() {
			return deviceNetworkOperatorID;
		}

		public String getDeviceNetworkOperatorName() {
			return deviceNetworkOperatorName;
		}

		public int getDeviceNetworkType() {
			return deviceNetworkType;
		}

		public boolean isDeviceRoaming() {
			return isDeviceRoaming;
		}

		public int getDevicePhoneType() {
			return devicePhoneType;
		}

		public String getDevicePhoneNumber() {
			return devicePhoneNumber;
		}

		public String getDeviceSIMCardSerialNumber() {
			return deviceSIMCardSerialNumber;
		}

		public String getDeviceSubscriberID() {
			return deviceSubscriberID;
		}

		public int getDeviceServiceState() {
			return deviceServiceState;
		}
		
		public int getDeviceDataState() {
			return deviceDataState;
		}
		
		public String getIpAddress() {
		    try {
		        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
		            NetworkInterface intf = en.nextElement();
		            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
		                InetAddress inetAddress = enumIpAddr.nextElement();
		                if (!inetAddress.isLoopbackAddress()) {
		                	return inetAddress.getHostAddress().toString();
		                }
		            }
		        }
		    } catch (SocketException ex) {}
		    return "";
		}

		public Build getDeviceBuild() {
			return deviceBuild;
		}

		public String getDeviceBoard() {
			return Build.BOARD;
		}

		public String getDeviceManufacturer() {
			return Build.MANUFACTURER;
		}

		public String getDeviceModel() {
			return Build.MODEL;
		}

		public String getAndroid_version() {
			String android_id="";
			switch(Build.VERSION.SDK_INT) {
				case 4:
					android_id = "Donut (Android 1.6)";break;
				case 5:
					android_id = "Eclair (Android 2.0)";break;
				case 6:
					android_id = "Eclair (Android 2.0.1)";break;
				case 7:
					android_id = "Eclair (Android 2.1)";break;
				case 8:
					android_id = "Froyo (Android 2.2)";break;
			}
			return android_id;
		}

		public String getAndroid_build() {
			return Build.DISPLAY;
		}
	}
	
	private static TelephonyManager teleManager;
	public static TelephonyManager getTelephonyManager() {
		return teleManager;
	}

	public void setTelephonyManager(TelephonyManager teleMng) {
		teleManager = teleMng;
	}

	//Activity binder
	private final IBinder deviceInfoBinder = new DeviceInfoBinder();
	
    private static Context context;
	
    //Singleton pattern
    private static final DevicesInfo_Service deviceInfoService = new DevicesInfo_Service();
    public static DevicesInfo_Service getService() {
    	return deviceInfoService;
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		return deviceInfoBinder;
	}
	
	public class DeviceInfoBinder extends Binder {
		DevicesInfo_Service getService() {
			return DevicesInfo_Service.getService();
		}
	}
	
	public void setApplicationContext(Context mContext) {
		DevicesInfo_Service.context = mContext;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if(context == null) 
			context = this.getApplicationContext();
		
		if(teleManager == null) 
			teleManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
		
		Log.d("AWARE", "Device Info Service running!");
	}

	@Override
	public void onDestroy() {
		
		Log.d("AWARE","Device Info terminated...");
		
		//Don't allow it to die, as we need it for getting device_id for database storage
		Intent deviceInfo = new Intent(this,DevicesInfo_Service.class);
		startService(deviceInfo);
		
		super.onDestroy();
	}
}
