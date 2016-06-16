/*
 * Created by Denzil Ferreira
 * dferreir@andrew.cmu.edu
 * 
 * Screen service for Android Aware framework
 * Last updated: 14-Jul-2010
 * Modified by: Denzil Ferreira
 */
package com.android.aware_framework;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/*
 * Example of usage:
 * Screen_Service screenSrv = Screen_Service.getService();
 * 		
 * 		//Initialize Screen manager
		screenSrv.setApplicationContext(getApplicationContext());
		screenSrv.setOnScreenChangedObserver(new Screen_Service.OnScreenChangedObserver() {
			@Override
			public void onScreenChanged() {
				Log.d(TAG,"Screen is "+screenSrv.getScreenStatus());
			}
		});
		
		Intent startScreen = new Intent(this,Screen_Service.class);
		startService(startScreen);
		
		//get current status of the screen
		Boolean screenOn = screenSrv.getScreenStatus(); //returns true if screen is on, false otherwise
 */

public class Screen_Service extends Service {

	private static BroadcastReceiver mScreenReceiver = new ScreenReceiver();
	public static class ScreenReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				Screen_Service.screenOn = false;
			}else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				Screen_Service.screenOn = true;
			}
			if(mScreenObserver!=null)
				mScreenObserver.onScreenChanged(getScreenStatus());
			else
				Log.w("AWARE","Forgot to set OnScreenChangedListener");
		}
	}
	
	//Screen observer
	private static OnScreenChangedObserver mScreenObserver;
	public interface OnScreenChangedObserver {
		public abstract void onScreenChanged(boolean screenOn);
	}
	
	public OnScreenChangedObserver getOnScreenChangedObserver() {
		return mScreenObserver;
	}
	
	public void setOnScreenChangedObserver(OnScreenChangedObserver obs) {
		mScreenObserver = obs;
	}
	
	private static boolean screenOn = true;
	public static boolean getScreenStatus() {
		return Screen_Service.screenOn;
	}
	
	//Activity binder
	private final IBinder screenBinder = new ScreenBinder();
	
    private static Context context;
	
    //Singleton pattern
    private static Screen_Service screenService = new Screen_Service();
    public static Screen_Service getService() {
    	return screenService;
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		return screenBinder;
	}
	
	public class ScreenBinder extends Binder {
		Screen_Service getService() {
			return Screen_Service.getService();
		}
	}
	
	public void setApplicationContext(Context mContext) {
		Screen_Service.context = mContext;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if(context == null) 
			context = this.getApplicationContext();
		
		//Monitor SCREEN ON
		IntentFilter filterOn = new IntentFilter(Intent.ACTION_SCREEN_ON);
		if(mScreenReceiver!=null)
			registerReceiver(mScreenReceiver, filterOn);
		
		//Monitor SCREEN OFF
		IntentFilter filterOff = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		if(mScreenReceiver!=null)
			registerReceiver(mScreenReceiver, filterOff);
		
		if(mScreenObserver!=null)
			mScreenObserver.onScreenChanged(getScreenStatus());
		
		Log.d("AWARE", "Screen Service running!");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("AWARE","Screen Service terminated...");
		unregisterReceiver(mScreenReceiver); //clean after ourselves
	}
}
