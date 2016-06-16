/*
 * Created by Denzil Ferreira
 * dferreir@andrew.cmu.edu
 * 
 * Activity Manager service for Android Aware framework
 * Last updated: 8-Jul-2010
 * Modified by: Denzil Ferreira
 */
package com.android.aware_framework;

import java.util.List;

import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/*
 * Example of usage:
 * 
 * ActivityManager_Service activityService = ActivityManager_Service.getService();
 * activityMngSrv.setApplicationContext(getApplicationContext());
 * activityMngSrv.setActivityManager((ActivityManager)getSystemService(ACTIVITY_SERVICE));
 *		
 * List<ActivityManager.RunningAppProcessInfo> runningApps = activityMngSrv.getRunningApps();
 * List<ActivityManager.RunningServiceInfo> runningServices = activityMngSrv.getRunningServices();
 * 		
 * RunningAppProcessInfo active = activityMngSrv.getActiveApp();
 * 
 * ActivityManagerBroadcaster is a BroadcastReceiver that will allow us to trigger this service using an AlarmManager
 * BroadcastAction is the interface that allows the user to configure what to be done when the Repeater is called
 * 
 * Example to connect this service to Repeater
 * 		
  		//What to do if we use the Repeater_Service, which we will
		activityMngSrv.setBroadcastAction(new ActivityManager_Service.BroadcastAction() {
			@Override
			public void onBroadcastAction() {
				RunningAppProcessInfo active = activityMngSrv.getActiveApp();
				Log.d(TAG,"Active application: " + active.processName + activityMngSrv.getLabel(active));
			}
		});
  
  		//What will be ran when Repeater is fired
		Intent intentToFire = new Intent(ActivityManagerBroadcaster.ACTION_ACTIVITYMANAGER);
		PendingIntent activityUpdater = PendingIntent.getBroadcast(this, 0, intentToFire, 0);
		
		repeater.setRepeaterIntent(activityUpdater);
		repeater.setRepetitionInterval(1000); //1 second interval
		repeater.start();
 */

public class ActivityManager_Service extends Service {

	private static ActivityManager activityMng;
	private static PackageManager pm;
	
	public void setPackageManager(PackageManager pkgManager) {
		pm = pkgManager;
	}
	
	public PackageManager getPackageManager() {
		return pm;
	}
	
	public static class ActivityManagerBroadcaster extends BroadcastReceiver {
		public static final String ACTION_ACTIVITYMANAGER = "ACTION_ACTIVITYMANAGER";
		
		@Override
		public void onReceive(Context context, Intent intent) {
			//Run what the user ordered, needs to be performed in 5 seconds.
			if(broadcastAction!=null)
				broadcastAction.onBroadcastAction();
			else 
				Log.w("AWARE","Forgot to initialize BroadcastAction in ActivityManagerBroadcaster");
		}
	}
	
	//BroadcastAction interface
	private static BroadcastAction broadcastAction;
	public interface BroadcastAction {
		public abstract void onBroadcastAction();
	}
	
	public void setBroadcastAction(BroadcastAction anAction) {
		ActivityManager_Service.broadcastAction = anAction;
	}
	
	public BroadcastAction getBroadcastAction() {
		return ActivityManager_Service.broadcastAction;
	}
	//----
	
	//Activity binder
	private final IBinder activityMngBinder = new ActivityManagerBinder();
	
    private static Context context;
	
    //Singleton pattern
    private static final ActivityManager_Service activityMngSrv = new ActivityManager_Service();
    public static ActivityManager_Service getService() {
    	return activityMngSrv;
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		return activityMngBinder;
	}
	
	public class ActivityManagerBinder extends Binder {
		ActivityManager_Service getService() {
			return ActivityManager_Service.getService();
		}
	}
	
	public void setApplicationContext(Context mContext) {
		ActivityManager_Service.context = mContext;
	}
	
	public Context getApplicationContext() {
		return ActivityManager_Service.context;
	}
	
	public void setActivityManager(ActivityManager mng) {
		ActivityManager_Service.activityMng = mng;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if(context == null) 
			context = this.getApplicationContext();
		
		if(pm == null)
			pm = this.getPackageManager();
		
		Log.d("AWARE", "Activity Manager Service running!");
	}

	@Override
	public void onDestroy() {
		
		Log.d("AWARE","Activity Manager Service terminated...");
		
		super.onDestroy();
	}
	
	public List<RunningAppProcessInfo> getRunningApps() {
		return activityMng.getRunningAppProcesses();
	}
	
	public List<RunningServiceInfo> getRunningServices() {
		return activityMng.getRunningServices(0);
	}
	
	public RunningAppProcessInfo getActiveApp() {
		List<RunningAppProcessInfo> apps = getRunningApps();
		
		for(RunningAppProcessInfo app : apps) {
			if(app.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				if(app.processName.compareToIgnoreCase("com.android.phone")!=0
						&& app.processName.compareToIgnoreCase("system")!=0) {
					return app;
				}
			}
		}
		
		return null;
	}
	
	//Tries to give the name of the application
	public String getLabel(RunningAppProcessInfo app) {
		PackageManager pm = this.getPackageManager();
		
        try {
            ApplicationInfo ai = pm.getApplicationInfo(app.processName, PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);
            return ai.loadLabel(pm).toString();
        } catch (PackageManager.NameNotFoundException e) {
        	
        } catch (NullPointerException e) {}
        
        if(app!= null) return app.processName;
        return "";
    }
}
