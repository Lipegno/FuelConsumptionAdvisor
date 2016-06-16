/*
 * Created by Denzil Ferreira
 * dferreir@andrew.cmu.edu
 * 
 * Memory service for Android Aware framework
 * Last updated: 11-Aug-2010
 * Modified by: Denzil Ferreira
 */
package com.android.aware_framework;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

//	EXAMPLE

//	Memory_Service memorySrv = Memory_Service.getService();
//	
//	memorySrv.setApplicationContext(getApplicationContext());
//	memorySrv.setActivityManager((ActivityManager) getSystemService(ACTIVITY_SERVICE));
//	
//	Intent memoryIntent = new Intent(this, Memory_Service.class);
//	startService(memoryIntent);

public class Memory_Service extends Service {

	private static ActivityManager activityManager;
	public static ActivityManager getActivityManager() {
		return activityManager;
	}
	public void setActivityManager(ActivityManager activityManager) {
		Memory_Service.activityManager = activityManager;
	}
	
	public long getAvailableMemoryBytes() {
		ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(memInfo);
		
		return memInfo.availMem;
	}
	
	public long getTotalMemoryBytes() {
		return Runtime.getRuntime().totalMemory();
	}
	
	public long getFreeMemoryBytes() {
		return Runtime.getRuntime().freeMemory();
	}
	
	public long getMaxMemoryBytes() {
		return Runtime.getRuntime().maxMemory();
	}
	
	//Activity binder
	private final IBinder memoryBinder = new MemoryBinder();
	
    private static Context context;
	
    //Singleton pattern
    private static final Memory_Service memoryService = new Memory_Service();
    public static Memory_Service getService() {
    	return memoryService;
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		return memoryBinder;
	}
	
	public class MemoryBinder extends Binder {
		Memory_Service getService() {
			return Memory_Service.getService();
		}
	}
	
	public void setApplicationContext(Context mContext) {
		Memory_Service.context = mContext;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if(context == null) 
			context = this.getApplicationContext();
		
		if(activityManager==null)
			activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		
		
		Log.d("AWARE", "Memory Service running!");
	}

	@Override
	public void onDestroy() {
		
		Log.d("AWARE","Memory Service terminated...");
		
		super.onDestroy();
	}
}
