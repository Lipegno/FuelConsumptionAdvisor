/*
 * Created by Denzil Ferreira
 * dferreir@andrew.cmu.edu
 * 
 * Carrier service for Android Aware framework
 * Last updated: 30-Jun-2010
 * Modified by: Denzil Ferreira
 */
package com.android.aware_framework;

import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;

//	Example

//	Carrier_Service carrierSrv = Carrier_Service.getService();
//	carrierSrv.setApplicationContext(getApplicationContext());
//	carrierSrv.setTelephonyManager((TelephonyManager) getSystemService(TELEPHONY_SERVICE));
//	
//	carrierSrv.setOnCarrierListener(new Carrier_Service.OnCarrierInfoListener() {
//		@Override
//		public void onCarrierInfo(CarrierInfo carrier) {
//			Log.d("CSS",carrier.toString());
//		}
//	}
//	
//	Intent carrierIntent = new Intent(this, Carrier_Service.class);
//	startService(carrierIntent);

public class Carrier_Service extends Service {

	private static OnCarrierInfoListener carrierListener;
	public interface OnCarrierInfoListener {
		public abstract void onCarrierInfo(CarrierInfo carrier);
	}
	public OnCarrierInfoListener getOnCarrierListener() {
		return carrierListener;
	}

	public void setOnCarrierListener(OnCarrierInfoListener carrierListener) {
		Carrier_Service.carrierListener = carrierListener;
	}
	
	public static class CarrierInfo {
		
		private int callState;
		private CellLocation cellLocation;
		private int dataActivity;
		private int dataState;
		private String deviceId;
		private String deviceSw;
		private String deviceNumber;
		private List<NeighboringCellInfo> neighbors;
		private String networkCountryIso;
		private String networkOperator;
		private int networkType;
		private int phoneType;
		private String simCountryIso;
		private String simOperator;
		private String simSerialNumber;
		private int simState;
		private String subscriberId;
		private String voiceMailTag;
		private String voiceMailNumber;
		private boolean networkRoaming;
		
		private CarrierInfo() {
			setCallState(telephonyManager.getCallState());
			setCellLocation(telephonyManager.getCellLocation());
			setDataActivity(telephonyManager.getDataActivity());
			setDataState(telephonyManager.getDataState());
			setDeviceId(telephonyManager.getDeviceId());
			setDeviceNumber(telephonyManager.getLine1Number());
			setDeviceSw(telephonyManager.getDeviceSoftwareVersion());
			setNeighbors(telephonyManager.getNeighboringCellInfo());
			setNetworkCountryIso(telephonyManager.getNetworkCountryIso());
			setNetworkOperator(telephonyManager.getNetworkOperator());
			setNetworkRoaming(telephonyManager.isNetworkRoaming());
			setNetworkType(telephonyManager.getNetworkType());
			setPhoneType(telephonyManager.getPhoneType());
			setSimCountryIso(telephonyManager.getSimCountryIso());
			setSimOperator(telephonyManager.getSimOperator());
			setSimSerialNumber(telephonyManager.getSimSerialNumber());
			setSimState(telephonyManager.getSimState());
			setSubscriberId(telephonyManager.getSubscriberId());
			setVoiceMailNumber(telephonyManager.getVoiceMailNumber());
			setVoiceMailTag(telephonyManager.getVoiceMailAlphaTag());
		}
		
		//Singleton
		private static CarrierInfo carrierInfo = new CarrierInfo();
		public static CarrierInfo getCarrierInfo() {
			return carrierInfo;
		}
		
		public String getCallState() {
			String output = "";
			switch (callState) 
			{
				case TelephonyManager.CALL_STATE_IDLE:
					output = "CALL_STATE_IDLE";
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					output = "CALL_STATE_OFFHOOK";
					break;
				case TelephonyManager.CALL_STATE_RINGING:
					output = "CALL_STATE_RINGING";
					break;
			}
			return output;
		}
		public void setCallState(int callState) {
			this.callState = callState;
		}
		public CellLocation getCellLocation() {
			return cellLocation;
		}
		public void setCellLocation(CellLocation cellLocation) {
			this.cellLocation = cellLocation;
		}
		public String getDataActivity() {
			String output = "";
			switch(dataActivity) {
				case TelephonyManager.DATA_ACTIVITY_DORMANT:
					output = "DATA_ACTIVITY_DORMANT";
				break;
				case TelephonyManager.DATA_ACTIVITY_IN:
					output = "DATA_ACTIVITY_IN";
				break;
				case TelephonyManager.DATA_ACTIVITY_INOUT:
					output = "DATA_ACTIVITY_INOUT";
				break;
				case TelephonyManager.DATA_ACTIVITY_NONE:
					output = "DATA_ACTIVITY_NONE";
				break;
				case TelephonyManager.DATA_ACTIVITY_OUT:
					output = "DATA_ACTIVITY_OUT";
				break;
			}
			
			return output;
		}
		public void setDataActivity(int dataActivity) {
			this.dataActivity = dataActivity;
		}
		public String getDataState() {
			String output = "";
			
			switch(dataState) 
			{
				case TelephonyManager.DATA_CONNECTED:
					output = "DATA_CONNECTED";
				break;
				case TelephonyManager.DATA_CONNECTING:
					output = "DATA_CONNECTING";
				break;
				case TelephonyManager.DATA_DISCONNECTED:
					output = "DATA_DISCONNECTED";
				break;
				case TelephonyManager.DATA_SUSPENDED:
					output = "DATA_SUSPENDED";
				break;
			}
			
			return output;
		}
		public void setDataState(int dataState) {
			this.dataState = dataState;
		}
		public String getDeviceId() {
			return deviceId;
		}
		public void setDeviceId(String deviceId) {
			this.deviceId = deviceId;
		}
		public String getDeviceSw() {
			return deviceSw;
		}
		public void setDeviceSw(String deviceSw) {
			this.deviceSw = deviceSw;
		}
		public String getDeviceNumber() {
			return deviceNumber;
		}
		public void setDeviceNumber(String deviceNumber) {
			this.deviceNumber = deviceNumber;
		}
		public List<NeighboringCellInfo> getNeighbors() {
			return neighbors;
		}
		public void setNeighbors(List<NeighboringCellInfo> neighbors) {
			this.neighbors = neighbors;
		}
		public String getNetworkCountryIso() {
			return networkCountryIso;
		}
		public void setNetworkCountryIso(String networkCountryIso) {
			this.networkCountryIso = networkCountryIso;
		}
		public String getNetworkOperator() {
			return networkOperator;
		}
		public void setNetworkOperator(String networkOperator) {
			this.networkOperator = networkOperator;
		}
		
		public String getNetworkTypeNeighbor(int networkType) {
			String output = "";
			
			switch(networkType)
			{
				case TelephonyManager.NETWORK_TYPE_1xRTT:
					output = "NETWORK_TYPE_1xRTT";
				break;
				case TelephonyManager.NETWORK_TYPE_CDMA:
					output = "NETWORK_TYPE_CDMA";
				break;
				case TelephonyManager.NETWORK_TYPE_EDGE:
					output = "NETWORK_TYPE_EDGE";
				break;
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
					output = "NETWORK_TYPE_EVDO_0";
				break;
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
					output = "NETWORK_TYPE_EVDO_A";
				break;
				case TelephonyManager.NETWORK_TYPE_GPRS:
					output = "NETWORK_TYPE_GPRS";
				break;
				case TelephonyManager.NETWORK_TYPE_HSDPA:
					output = "NETWORK_TYPE_HSDPA";
				break;
				case TelephonyManager.NETWORK_TYPE_HSPA:
					output = "NETWORK_TYPE_HSPA";
				break;
				case TelephonyManager.NETWORK_TYPE_HSUPA:
					output = "NETWORK_TYPE_HSUPA";
				break;
				case TelephonyManager.NETWORK_TYPE_UMTS:
					output = "NETWORK_TYPE_UMTS";
				break;
				case TelephonyManager.NETWORK_TYPE_UNKNOWN:
					output = "NETWORK_TYPE_UNKNOWN";
				break;
			}
			
			return output;
		}
		public String getNetworkType() {
			String output = "";
			
			switch(networkType)
			{
				case TelephonyManager.NETWORK_TYPE_1xRTT:
					output = "NETWORK_TYPE_1xRTT";
				break;
				case TelephonyManager.NETWORK_TYPE_CDMA:
					output = "NETWORK_TYPE_CDMA";
				break;
				case TelephonyManager.NETWORK_TYPE_EDGE:
					output = "NETWORK_TYPE_EDGE";
				break;
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
					output = "NETWORK_TYPE_EVDO_0";
				break;
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
					output = "NETWORK_TYPE_EVDO_A";
				break;
				case TelephonyManager.NETWORK_TYPE_GPRS:
					output = "NETWORK_TYPE_GPRS";
				break;
				case TelephonyManager.NETWORK_TYPE_HSDPA:
					output = "NETWORK_TYPE_HSDPA";
				break;
				case TelephonyManager.NETWORK_TYPE_HSPA:
					output = "NETWORK_TYPE_HSPA";
				break;
				case TelephonyManager.NETWORK_TYPE_HSUPA:
					output = "NETWORK_TYPE_HSUPA";
				break;
				case TelephonyManager.NETWORK_TYPE_UMTS:
					output = "NETWORK_TYPE_UMTS";
				break;
				case TelephonyManager.NETWORK_TYPE_UNKNOWN:
					output = "NETWORK_TYPE_UNKNOWN";
				break;
			}
			
			return output;
		}
		public void setNetworkType(int networkType) {
			this.networkType = networkType;
		}
		public String getPhoneType() {
			String output = "";
			
			switch(phoneType)
			{
				case TelephonyManager.PHONE_TYPE_CDMA:
					output = "PHONE_TYPE_CDMA";
				break;
				case TelephonyManager.PHONE_TYPE_GSM:
					output = "PHONE_TYPE_GSM";
				break;
				case TelephonyManager.PHONE_TYPE_NONE:
					output = "PHONE_TYPE_NONE";
				break;
			}
			
			return output;
		}
		public void setPhoneType(int phoneType) {
			this.phoneType = phoneType;
		}
		public String getSimCountryIso() {
			return simCountryIso;
		}
		public void setSimCountryIso(String simCountryIso) {
			this.simCountryIso = simCountryIso;
		}
		public String getSimOperator() {
			return simOperator;
		}
		public void setSimOperator(String simOperator) {
			this.simOperator = simOperator;
		}
		public String getSimSerialNumber() {
			return simSerialNumber;
		}
		public void setSimSerialNumber(String simSerialNumber) {
			this.simSerialNumber = simSerialNumber;
		}
		public String getSimState() {
			String output = "";
			switch(simState) 
			{
				case TelephonyManager.SIM_STATE_ABSENT:
					output = "SIM_STATE_ABSENT";
				break;
				case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
					output = "SIM_STATE_NETWORK_LOCKED";
				break;
				case TelephonyManager.SIM_STATE_PIN_REQUIRED:
					output = "SIM_STATE_PIN_REQUIRED";
				break;
				case TelephonyManager.SIM_STATE_PUK_REQUIRED:
					output = "SIM_STATE_PUK_REQUIRED";
				break;
				case TelephonyManager.SIM_STATE_READY:
					output = "SIM_STATE_READY";
				break;
				case TelephonyManager.SIM_STATE_UNKNOWN:
					output = "SIM_STATE_UNKNOWN";
				break;
			}

			return output;
		}
		public void setSimState(int simState) {
			this.simState = simState;
		}
		public String getSubscriberId() {
			return subscriberId;
		}
		public void setSubscriberId(String subscriberId) {
			this.subscriberId = subscriberId;
		}
		public String getVoiceMailTag() {
			return voiceMailTag;
		}
		public void setVoiceMailTag(String voiceMailTag) {
			this.voiceMailTag = voiceMailTag;
		}
		public String getVoiceMailNumber() {
			return voiceMailNumber;
		}
		public void setVoiceMailNumber(String voiceMailNumber) {
			this.voiceMailNumber = voiceMailNumber;
		}
		public boolean isNetworkRoaming() {
			return networkRoaming;
		}
		public void setNetworkRoaming(boolean networkRoaming) {
			this.networkRoaming = networkRoaming;
		}
	}
	
	public static BroadcastReceiver carrierServiceReceiver = new CarrierServiceReceiver();
	public static class CarrierServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(carrierListener!=null) {
				CarrierInfo carrier = CarrierInfo.getCarrierInfo();
				carrierListener.onCarrierInfo(carrier);
			}
		}		
	}
	
	public static PhoneStateListener carrierStateListener = new CarrierPhoneState();
	public static class CarrierPhoneState extends PhoneStateListener {
		@Override
		public void onServiceStateChanged(ServiceState serviceState) {
			if(carrierListener!=null) {
				CarrierInfo carrier = CarrierInfo.getCarrierInfo();
				carrierListener.onCarrierInfo(carrier);
			}
			super.onServiceStateChanged(serviceState);
		}
	}
	
	//Activity binder
	private final IBinder carrierBinder = new CarrierBinder();
	
    private static Context context;
    private static TelephonyManager telephonyManager;

	public void setTelephonyManager(TelephonyManager telephonyManager) {
		Carrier_Service.telephonyManager = telephonyManager;
	}

	//Singleton pattern
    private static final Carrier_Service carrierService = new Carrier_Service();
    public static Carrier_Service getService() {
    	return carrierService;
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		return carrierBinder;
	}
	
	public class CarrierBinder extends Binder {
		Carrier_Service getService() {
			return Carrier_Service.getService();
		}
	}
	
	public void setApplicationContext(Context mContext) {
		Carrier_Service.context = mContext;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if(context == null) 
			context = this.getApplicationContext();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		
		if(carrierServiceReceiver!=null)
			registerReceiver(carrierServiceReceiver, filter);
		
		if(telephonyManager == null)
			telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
		
		if(telephonyManager!=null)
			telephonyManager.listen(carrierStateListener, PhoneStateListener.LISTEN_CELL_LOCATION);
		
		Log.d("AWARE", "Carrier Service running!");
	}

	@Override
	public void onDestroy() {
		
		//Clean-up
		telephonyManager.listen(carrierStateListener, PhoneStateListener.LISTEN_NONE);
		unregisterReceiver(carrierServiceReceiver);
		
		Log.d("AWARE","Carrier Service terminated...");
		
		super.onDestroy();
	}
}
