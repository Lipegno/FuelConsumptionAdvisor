/*
 * Created by Denzil Ferreira
 * dferreir@andrew.cmu.edu
 * 
 * Repeater service for Android Aware framework
 * Allows you to set a repeating activity or service to be performed at specified intervals.
 * Last updated: 9-Jul-2010
 * Modified by: Denzil Ferreira
 */
package com.android.aware_framework;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

/*
 * http://developer.android.com/reference/android/app/AlarmManager.html
 * Note: The Alarm Manager is intended for cases where you want to have your application code run at a specific time, even if your application is not currently running. For normal timing operations (ticks, timeouts, etc) it is easier and much more efficient to use Handler.
 * Example of usage:
 * 
 * Repeater_Service repeater = Repeater_Service.getService();
 * repeater.setApplicationContext(getApplicationContext());
 * repeater.setAlarmManager((AlarmManager) getSystemService(ALARM_SERVICE));
 * 
 * Intent intentToFire = new Intent(ActivityManagerBroadcaster.ACTION_ACTIVITYMANAGER); //need to add Receiver to manifest with this action
 * PendingIntent activityUpdater = PendingIntent.getBroadcast(this, 0, intentToFire, 0); //set the intent as a PendingIntent
 * repeater.setRepeaterIntent(activityUpdater); //tell the repeater what you want to run
 * repeater.setRepetitionInterval(1000); //1 second interval
 * //or if you prefer at a set datetime
 * repeater.setTimedAlarm(new Date().getTime()+DateUtils.DAY_IN_MILLIS); //tomorrow at this time
 * repeater.start();
 * //to stop the repeater
 * repeater.stop();
 */

public class Repeater_Service extends Service {

	private static AlarmManager repeatManager;
	private static PendingIntent repeaterIntent;
	
	private static long repetitionInterval = 0;
	private static long timedAlarm = 0;

	//Activity binder
	private final IBinder repeaterBinder = new RepeaterBinder();
	
    private static Context context;
	
    public Repeater_Service() {}
    
    public static Repeater_Service getService() {
    	return new Repeater_Service();
    }
    
    public PendingIntent getRepeaterIntent() {
		return repeaterIntent;
	}

	public void setRepeaterIntent(PendingIntent repeaterIntent) {
		Repeater_Service.repeaterIntent = repeaterIntent;
	}
    
	@Override
	public IBinder onBind(Intent intent) {
		return repeaterBinder;
	}
	
	public class RepeaterBinder extends Binder {
		Repeater_Service getService() {
			return Repeater_Service.getService();
		}
	}
	
	public void setApplicationContext(Context mContext) {
		Repeater_Service.context = mContext;
	}
	
	public void setAlarmManager(AlarmManager mManager) {
		Repeater_Service.repeatManager = mManager;
	}
	
	public AlarmManager getAlarmManager() {
		return Repeater_Service.repeatManager;
	}
	
	public long getRepetitionInterval() {
		return repetitionInterval;
	}

	public void setRepetitionInterval(long repetitionInterval) {
		Repeater_Service.repetitionInterval = repetitionInterval;
	}
	
	public long getTimedAlarm() {
		return timedAlarm;
	}

	public void setTimedAlarm(long timedAlarm) {
		Repeater_Service.timedAlarm = timedAlarm;
	}
	
	public void start() {
		if(repeaterIntent != null) {
			if(timedAlarm>0) {
				Log.d("AWARE","Setting up Repeater at "+timedAlarm+" ms");
				repeatManager.set(AlarmManager.RTC_WAKEUP, getTimedAlarm(), getRepeaterIntent());
			}
			if(repetitionInterval>0) {
				Log.d("AWARE","Setting up Repeater for every "+repetitionInterval+" ms");
				repeatManager.setRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime()+1000, getRepetitionInterval(), getRepeaterIntent());
			}
		}else
			Log.w("AWARE","Forgot to initialize Repeater's Intent");
	}
	
	public void stop() {
		repeatManager.cancel(getRepeaterIntent());
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if(context == null) 
			context = this.getApplicationContext();
		
		if(repeatManager == null) 
			repeatManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
		
		Log.d("AWARE", "Repeater Service running!");
	}
 	
	@Override
	public void onDestroy() {
		
		Log.d("AWARE","Repeater Service terminated...");
		
		super.onDestroy();
	}
}
