/*
 * Created by Denzil Ferreira
 * dferreir@andrew.cmu.edu
 * 
 * Aware Dog (WatchDog) service for Android Aware framework
 * Last updated: 22-Jul-2010
 * Modified by: Denzil Ferreira
 */
package com.android.aware_framework;

import java.util.ArrayList;
import java.util.Date;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.android.aware_framework.DevicesInfo_Service.DeviceInfo;

// Example
// Note: Add Receiver AwareDogPing to manifest, with intent filter ACTION_AWAREDOG
// 		 Add uses-permission android.permission.INTERNET

//		AwareDog_Service awareDog = AwareDog_Service.getService();
//		awareDog.setApplicationContext(getApplicationContext());
//		awareDog.setDeviceID(deviceInfo.getDeviceID());
//		awareDog.setReportHQ("http://kettle.ubiq.cs.cmu.edu/~lizned/index.php/aware/alive");
//		awareDog.setResponsible("lizned.arierref@gmail.com");
//		
//		//Create list of services to be monitored
//		ArrayList<Object> servicesMonitored = new ArrayList<Object>();
//		servicesMonitored.add(appMngSrv);
//		servicesMonitored.add(activityMngSrv);
//		servicesMonitored.add(screenSrv);
//		servicesMonitored.add(smsSrv);
//		servicesMonitored.add(callSrv);
//		servicesMonitored.add(batterySrv);
//		servicesMonitored.add(deviceSrv);
//		
//		awareDog.setMonitoredServices(servicesMonitored);
//		
//		//Start monitoring the services
//		Intent awareDogIntent = new Intent(this, AwareDog_Service.class);
//		startService(awareDogIntent);
//		
//		//Let the server know we are alive every 30 minutes
//		Intent pingServer = new Intent(AwareDogPing.ACTION_AWAREDOG);
//		PendingIntent pingUpdater = PendingIntent.getBroadcast(this, 0, pingServer, 0);
//		
//		Repeater_Service aRepeater = Repeater_Service.getService();
//		aRepeater.setApplicationContext(getApplicationContext());
//		aRepeater.setAlarmManager((AlarmManager)getSystemService(ALARM_SERVICE));
//		
//		aRepeater.setRepeaterIntent(pingUpdater);
//		aRepeater.setRepetitionInterval(30*60*60*1000); //30 minutes pings to server
//		aRepeater.start();

public class AwareDog_Service extends Service {

	//-- Ping to HQ linked to repeater
	private static BroadcastReceiver awareDogPing = new AwareDogPing();
	public static class AwareDogPing extends BroadcastReceiver {
		public static final String ACTION_AWAREDOG = "ACTION_AWAREDOG";
	
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(ACTION_AWAREDOG)) {
				Runnable pingServer = new Runnable() {
					@Override
					public void run() {
						if(DeviceInfo.getDeviceInfo().getIpAddress()!="") {
						
							ArrayList<NameValuePair> data = new ArrayList<NameValuePair>();
							data.add(new BasicNameValuePair("device_id", getDeviceID()));
							data.add(new BasicNameValuePair("responsible",getResponsible()));
							data.add(new BasicNameValuePair("last_ping",""+new Date().getTime()/1000)); //PHP is in seconds vs millis in Java
						
							//if we publish successfully to server, we delete the local record.
							if(!HTMLObj.dataPOST(getReportHQ(), data)) {
								Log.w("AWARE","Warning: timed-out on the server...");
							}
							Log.d("AWARE","AwareDog just ping'ed HQ");
						}else{
							Log.e("AWARE","Error: No IP address!");
						}
					}
				};
				Thread bg = new Thread(null,pingServer,"AwareDog");
				bg.start();
			}
		}
	}

	//-- Services to monitor
	private static ArrayList<Object> services = new ArrayList<Object>();
	public void setMonitoredServices(ArrayList<Object> servicesMonitored) {
		services = servicesMonitored;
	}
	public ArrayList<Object> getMonitoredServices() {
		return services;
	}
	
	//AwareDog is going to be monitoring ACTION_TIME_TICK, which is triggered every minute to keep services alive
	private static BroadcastReceiver awareReceiver = new AwareDogTicker();
	public static class AwareDogTicker extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("AWARE","AwareDog is awake");
			
			for(Object service : services) 
			{
				
				Intent checkService = new Intent(context,service.getClass());
				context.startService(checkService);
				Log.d("AWARE","AwareDog check: "+service.getClass().getName() + " OK");
			
			}
			
			Log.d("AWARE","AwareDog is back to sleep");
		}	
	}
	
	//-- Report to HeadQuarters for letting it know we are still kickin'
	private static String HQ_URL = "";
	public void setReportHQ(String hqURL) {
		AwareDog_Service.HQ_URL = hqURL;
	}
	
	public static String getReportHQ() {
		return AwareDog_Service.HQ_URL;
	}
	
	//-- Device's ID
	private static String deviceID = "";
	public void setDeviceID(String device) {
		deviceID = device;
	}
	public static String getDeviceID() {
		return deviceID;
	}
	
	//-- Study responsible
	private static String studyResponsible = "";
	public void setResponsible(String responsible) {
		studyResponsible = responsible;
	}
	
	public static String getResponsible() {
		return studyResponsible;
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		
		//Don't let AwareDog die!
		Intent startAwareDog = new Intent(context, AwareDog_Service.class);
		startService(startAwareDog);
	}
	
	//Activity binder
	private final IBinder awareDogBinder = new AwareDogBinder();
	
    private static Context context;
	
    //Singleton pattern
    private static final AwareDog_Service awareDogService = new AwareDog_Service();
    public static AwareDog_Service getService() {
    	return awareDogService;
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		return awareDogBinder;
	}
	
	public class AwareDogBinder extends Binder {
		AwareDog_Service getService() {
			return AwareDog_Service.getService();
		}
	}
	
	public void setApplicationContext(Context mContext) {
		AwareDog_Service.context = mContext;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if(context == null) 
			context = this.getApplicationContext();
		
		//Check for services if they are running every minute
		IntentFilter serviceCheck = new IntentFilter();
		serviceCheck.addAction(Intent.ACTION_TIME_TICK);
		
		if(awareReceiver!=null)
			registerReceiver(awareReceiver, serviceCheck);
		
		IntentFilter ping = new IntentFilter();
		ping.addAction("ACTION_AWAREDOG");
		
		if(awareDogPing!=null)
			registerReceiver(awareDogPing, ping);
		
		Log.d("AWARE", "AwareDog Service running!");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("AWARE","AwareDog Service terminated...");
		unregisterReceiver(awareReceiver);
		unregisterReceiver(awareDogPing);
		
		//Don't let it die
		Intent awareDog = new Intent(this, AwareDog_Service.class);
		startService(awareDog);
	}
}
