/*
 * Created by Denzil Ferreira
 * dferreir@andrew.cmu.edu
 * 
 * Settings service for Android Aware framework
 * Last updated: 3-Aug-2010
 * Modified by: Denzil Ferreira
 */
package com.android.aware_framework;

import java.util.Hashtable;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

//	Example

//	Settings_Service settingsSrv = Settings_Service.getService();
//	settingsSrv.setApplicationContext(getApplicationContext());
//	settingsSrv.setSettingsContentResolver(getContentResolver());
//	
//	settingsSrv.setOnSettingsChangedListener(new Settings_Service.OnSettingsChangedListener() {
//		@Override
//		public void onSettingsChanged(SettingsInfo settingsInfo) {
//			
//		}
//	});
//	
//	Intent settingsIntent = new Intent(this, Settings_Service.class);
//	startService(settingsIntent);

public class Settings_Service extends Service {

	public static class SettingsInfo {
		private static Hashtable<String, String> settings = new Hashtable<String, String>();
		
		private SettingsInfo() {
			Cursor set = settingsResolver.query(Settings.System.CONTENT_URI, null, null, null, null);
			
			if(set.moveToFirst()) {
				do{
					settings.put(set.getString(set.getColumnIndex("name")), ""+set.getString(set.getColumnIndex("value")));
				}while(set.moveToNext());
			}
		}
		
		//Singleton
		private static SettingsInfo settingsInfo = new SettingsInfo();
		public static SettingsInfo getSettingsInfo() {
			return settingsInfo;
		}
		
		public Hashtable<String, String> getSettings() {
			return settings;
		}
	}
	
	//Interface for other classes to decide what to do with the data
	private static OnSettingsChangedListener settingsListener;
	public interface OnSettingsChangedListener {
		public abstract void onSettingsChanged(SettingsInfo settingsInfo);
	}
	public void setOnSettingsChangedListener(OnSettingsChangedListener settingsL) {
		settingsListener = settingsL;
	}
	public OnSettingsChangedListener getOnSettingsChangedListener() {
		return settingsListener;
	}
	
	//Settings Observer
	private static SettingsObserver settingsObs = new SettingsObserver(null);
	public void setSettingsObserver(SettingsObserver setObs) {
		settingsObs = setObs;
	}
	public SettingsObserver getSettingsObserver() {
		return settingsObs;
	}
	public static class SettingsObserver extends ContentObserver {
		public SettingsObserver(Handler handler) {
			super(handler);
		}
		
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			
			if(settingsListener!=null)
				settingsListener.onSettingsChanged(SettingsInfo.getSettingsInfo());
		}
	}
	
	//Settings Content Resolver
	private static ContentResolver settingsResolver;
	public void setSettingsContentResolver(ContentResolver sR) {
		settingsResolver = sR;
	}
	public ContentResolver getSettingsContentResolver() {
		return settingsResolver;
	}
	
	//Activity binder
	private final IBinder settingBinder = new SettingsBinder();
	
    private static Context context;
	
    //Singleton pattern
    private static final Settings_Service settingsService = new Settings_Service();
    public static Settings_Service getService() {
    	return settingsService;
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		return settingBinder;
	}
	
	public class SettingsBinder extends Binder {
		Settings_Service getService() {
			return Settings_Service.getService();
		}
	}
	
	public void setApplicationContext(Context mContext) {
		Settings_Service.context = mContext;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if(context == null) 
			context = this.getApplicationContext();
		
		if(settingsResolver == null)
			settingsResolver = this.getContentResolver();
		
		settingsResolver.registerContentObserver(Settings.System.CONTENT_URI, true, settingsObs);
		
		Log.d("AWARE", "Settings Service running!");
	}

	 
	
	@Override
	public void onDestroy() {
		
		Log.d("AWARE","Settings Service terminated...");
		
		super.onDestroy();
	}
}
