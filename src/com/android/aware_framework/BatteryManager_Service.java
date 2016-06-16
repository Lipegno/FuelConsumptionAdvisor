/*
 * Created by Denzil Ferreira
 * dferreir@andrew.cmu.edu
 * 
 * Battery Manager service for Android Aware framework
 * Last updated: 30-Jun-2010
 * Modified by: Denzil Ferreira
 */
package com.android.aware_framework;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.Log;

// 	Example of usage
//	BatteryManager_Service batterySrv = BatteryManager_Service.getService();
//	batterySrv.setApplicationContext(getApplicationContext());
//	batterySrv.setOnBatteryChangedListener(new BatteryManager_Service.OnBatteryChangedListener() {
//		@Override
//		public void onBatteryUnpluggedNotFull(Battery battery) {
//			Log.d(TAG,"Battery unplugged and not full! "+battery.dumpBattery());
//		}
//		@Override
//		public void onBatteryUnplugged(Battery battery) {
//			Log.d(TAG,"Battery charged and unplugged now! "+battery.dumpBattery());
//		}
//		@Override
//		public void onBatteryFull(Battery battery) {
//			Log.d(TAG,"Battery finished charging! "+battery.dumpBattery());
//		}
//		@Override
//		public void onBatteryDischarging(Battery battery) {
//			Log.d(TAG,"Phone running on batteries, which is normal usage! "+battery.dumpBattery());
//		}
//		@Override
//		public void onBatteryCharging(Battery battery) {
//			Log.d(TAG,"Battery is being charged now! "+battery.dumpBattery());
//		}
//	});
//	Intent batteryIntent = new Intent(this, BatteryManager_Service.class);
//	startService(batteryIntent);

public class BatteryManager_Service extends Service {

	//Battery observer
	private static OnBatteryChangedListener batteryListener;
	public interface OnBatteryChangedListener{
		public abstract void onBatteryDischarging(Battery battery);
		public abstract void onBatteryUnplugged(Battery battery);
		public abstract void onBatteryFull(Battery battery);
		public abstract void onBatteryCharging(Battery battery);
		public abstract void onBatteryUnpluggedNotFull(Battery battery);
		public abstract void onBatteryStartup(Battery battery);
	}
	
	public void setOnBatteryChangedListener(OnBatteryChangedListener batteryLis) {
		BatteryManager_Service.batteryListener = batteryLis;
	}
	
	public OnBatteryChangedListener getOnBatteryChangedListener() {
		return BatteryManager_Service.batteryListener;
	}
	
	//Battery object encapsulation
	public static class Battery {
		
		private static int batteryPercentage;
		private static int batteryTemperature;
		private static int batteryHealth;
		private static int batteryStatus;
		private static int lastBatteryStatus;
		private static int batteryVoltage;
		private static int batteryPlugged;
		private static int batteryLevel;
		private static int batteryScale;
		private static String batteryTechnology;

		private static long batteryUptime = 0;
		private static long batteryUptimeUsage = 0;

		private static Battery battery = new Battery();
		
		//Singleton battery
		private Battery() {
			batteryPercentage = 0;
			batteryTemperature = 0;
			batteryHealth = 0;
			batteryStatus = 0;
			batteryVoltage = 0;
			batteryPlugged = 0;
		}
		
		public static Battery getBattery() {
			return battery;
		}
		
		public int getBatteryPercentage() {
			return batteryPercentage;
		}

		public void setBatteryPercentage(int bp) {
			batteryPercentage = bp;
		}

		public int getBatteryTemperatureCelsius() {
			return batteryTemperature/10;
		}
		
		public int getBatteryTemperatureFahrenheit() {
			return ((getBatteryTemperatureCelsius()*9)/5+32);
		}

		public void setBatteryTemperature(int bt) {
			batteryTemperature = bt;
		}

		public String getBatteryHealth() {
			String output = "";
			
			switch(batteryHealth) {
				case BatteryManager.BATTERY_HEALTH_DEAD: output = "Dead"; break;
				case BatteryManager.BATTERY_HEALTH_GOOD: output = "Good"; break;
				case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE: output = "Overvoltage"; break;
				case BatteryManager.BATTERY_HEALTH_OVERHEAT: output = "Overheat"; break;
			}
			
			return output;
		}

		public void setBatteryHealth(int bh) {
			batteryHealth = bh;
		}

		public String getBatteryStatus() {
			String output = "";
			
			switch(batteryStatus) {
				case BatteryManager.BATTERY_STATUS_CHARGING: output = "Charging"; break;
				case BatteryManager.BATTERY_STATUS_DISCHARGING: output = "Discharging"; break;
				case BatteryManager.BATTERY_STATUS_FULL: output = "Full"; break;
				case BatteryManager.BATTERY_STATUS_NOT_CHARGING: output = "Not charging"; break;
			}
			
			return output;
		}
		
		public int getBatteryIntStatus() {
			return batteryStatus;
		}

		public void setBatteryStatus(int bs) {
			setLastBatteryStatus(batteryStatus); //keep record of last battery status
			batteryStatus = bs;
		}

		public int getBatteryVoltage() {
			return batteryVoltage;
		}

		public void setBatteryVoltage(int bv) {
			batteryVoltage = bv;
		}

		public String getBatteryPlugged() {
			String output = "";
			
			switch(batteryPlugged) {
				case BatteryManager.BATTERY_PLUGGED_AC: output = "AC"; break;
				case BatteryManager.BATTERY_PLUGGED_USB: output = "USB"; break;
			}
			
			return output;
		}
		
		public boolean isPhoneCharging() {
			return (batteryPlugged==0?false:true); //==0 battery running on battery
		}

		public void setBatteryPlugged(int bp) {
			batteryPlugged = bp;
		}
		
		public String dumpBattery() {
			return "Health:"+getBatteryHealth()+" %:"+getBatteryPercentage()+" Plugged:"+getBatteryPlugged()+" Status:"+getBatteryStatus()+" Temp:"+getBatteryTemperatureCelsius()+"C "+" "+getBatteryTemperatureFahrenheit()+"F"+" "+" Volt:"+getBatteryVoltage();
		}

		public void setLastBatteryStatus(int lbs) {
			lastBatteryStatus = lbs;
		}

		public int getLastBatteryStatus() {
			return lastBatteryStatus;
		}
		
		public String getUptime() {
			long elapsedTime = batteryUptime;  
		    elapsedTime = elapsedTime / 1000;  //transform from millis to seconds
		    return DateUtils.formatElapsedTime(elapsedTime); //using built-in date formating tools 
		}
		
		public String getUptimeUsage() {
			long upusage = batteryUptimeUsage;
			upusage = upusage / 1000; //transform from millis to seconds
			return DateUtils.formatElapsedTime(upusage);
		}
		
		public long getUptimeMillis() {
			return batteryUptime;
		}
		
		public long getUptimeUsageMillis() {
			return batteryUptimeUsage;
		}

		public void setUptime(long batteryUptime) {
			Battery.batteryUptime = batteryUptime;
		}
		
		public void setUptimeUsage(long batteryUptime) {
			Battery.batteryUptimeUsage = batteryUptime;
		}
		
		public int getBatteryLevel() {
			return batteryLevel;
		}

		public void setBatteryLevel(int batteryLevel) {
			Battery.batteryLevel = batteryLevel;
		}

		public int getBatteryScale() {
			return batteryScale;
		}

		public void setBatteryScale(int batteryScale) {
			Battery.batteryScale = batteryScale;
		}
		
		public String getBatteryTechnology() {
			return batteryTechnology;
		}

		public void setBatteryTechnology(String batteryTechnology) {
			Battery.batteryTechnology = batteryTechnology;
		}
	}
	
	//Battery BroadcastReceiver
	private static BroadcastReceiver batteryReceiver = new BatteryManagerReceiver();
	public static class BatteryManagerReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
				
				//Set Battery object
				Battery batteryInfo = Battery.getBattery();
				
				int reading = intent.getIntExtra("level", 0);
				int percentage = intent.getIntExtra("scale", 100);
				
				batteryInfo.setBatteryPercentage(reading*100/percentage);
				
				batteryInfo.setBatteryHealth(intent.getIntExtra("health", -1));
				batteryInfo.setBatteryPlugged(intent.getIntExtra("plugged", -1));
				batteryInfo.setBatteryStatus(intent.getIntExtra("status", -1));
				batteryInfo.setBatteryTemperature(intent.getIntExtra("temperature", 999));
				batteryInfo.setBatteryVoltage(intent.getIntExtra("voltage", -1));
				batteryInfo.setBatteryLevel(intent.getIntExtra("level", 0));
				batteryInfo.setBatteryScale(intent.getIntExtra("scale", 100));
				batteryInfo.setBatteryTechnology(intent.getStringExtra("technology"));
				
				batteryInfo.setUptime(SystemClock.elapsedRealtime());
				batteryInfo.setUptimeUsage(SystemClock.uptimeMillis());
				
				//Charging, now full
				if(batteryInfo.getLastBatteryStatus() == BatteryManager.BATTERY_STATUS_CHARGING 
						&& batteryInfo.getBatteryIntStatus()==BatteryManager.BATTERY_STATUS_FULL) {
					if(batteryListener!=null)
						batteryListener.onBatteryFull(batteryInfo);
				}
				
				//Charged and we unplugged
				if(batteryInfo.getLastBatteryStatus() == BatteryManager.BATTERY_STATUS_FULL 
						&& ! batteryInfo.isPhoneCharging() && batteryInfo.getBatteryPercentage() == 100) {
					if(batteryListener!=null)
						batteryListener.onBatteryUnplugged(batteryInfo);
				}
				
				//Charging and we unplugged and it's not full - VERY BAD action
				if(batteryInfo.getLastBatteryStatus() == BatteryManager.BATTERY_STATUS_CHARGING && (batteryInfo.getBatteryIntStatus() == BatteryManager.BATTERY_STATUS_DISCHARGING 
						|| batteryInfo.getBatteryIntStatus() == BatteryManager.BATTERY_STATUS_NOT_CHARGING) 
						&& batteryInfo.getBatteryPercentage() < 100) {
					if(batteryListener!=null)
						batteryListener.onBatteryUnpluggedNotFull(batteryInfo);
				}
					
				//Discharging and not plugged
				if(batteryInfo.getBatteryIntStatus() == BatteryManager.BATTERY_STATUS_DISCHARGING 
						|| batteryInfo.getBatteryIntStatus() == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
					if(batteryListener!=null)
						batteryListener.onBatteryDischarging(batteryInfo);
				}
				
				//Was discharging or not charging and now it's charging
				if( (batteryInfo.getLastBatteryStatus() == BatteryManager.BATTERY_STATUS_DISCHARGING 
						|| batteryInfo.getLastBatteryStatus() == BatteryManager.BATTERY_STATUS_NOT_CHARGING )
						&& batteryInfo.isPhoneCharging() && batteryInfo.getBatteryPercentage() <= 100) {
					if(batteryListener!=null)
						batteryListener.onBatteryCharging(batteryInfo);
				}
			}
		}
	}
	
	//Activity binder
	private final IBinder batteryBinder = new BatteryBinder();
	
    private static Context context;
	
    //Singleton pattern
    private static final BatteryManager_Service batteryService = new BatteryManager_Service();
    public static BatteryManager_Service getService() {
    	return batteryService;
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		return batteryBinder;
	}
	
	public class BatteryBinder extends Binder {
		BatteryManager_Service getService() {
			return BatteryManager_Service.getService();
		}
	}
	
	public void setApplicationContext(Context mContext) {
		BatteryManager_Service.context = mContext;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if(context == null) 
			context = this.getApplicationContext();
		
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		if(batteryReceiver!=null)
			registerReceiver(batteryReceiver, filter);
		
		Log.d("AWARE", "Battery Manager Service running!");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("AWARE","Battery Manager Service terminated...");
		unregisterReceiver(batteryReceiver); //clean after ourselves
	}
}
