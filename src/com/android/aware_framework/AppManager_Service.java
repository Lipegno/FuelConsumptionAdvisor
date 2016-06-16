/*
 * Created by Denzil Ferreira
 * dferreir@andrew.cmu.edu
 * 
 * Application Manager service for Android Aware framework
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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/*
 * Example of usage:
 * //Add receiver to manifest and add as intent filter: ACTION_PACKAGE_ADDED, ACTION_PACKAGE_REMOVED
 * 
 * AppManager_Service appMngSrv = AppManager_Service.getService();
 * appMngSrv.setApplicationContext(getApplicationContext());
 * appMngSrv.setPackageManager(getPackageManager());
 * 
 * //Get list of installed applications
 * List<ApplicationInfo> apps = appMngSrv.getInstalledApplications();
 * 
 * appMngSrv.setOnAppChangedListener(new AppManager_Service.OnAppChangedListener() {
		@Override
		public void onAppRemoved(String pkgName) {
			Log.d(TAG,"Removed: "+pkgName);
		}
		@Override
		public void onAppInstalled(String pkgName) {
			Log.d(TAG,"Installed: "+pkgName);
		}
		@Override
		public void onAppUpdated(String pkgName) {
			Log.d(TAG,"Updated: "+pkgName);
		}
	});
 * 
 * Intent appManagerIntent = new Intent(this,AppManager_Service.class);
   startService(appManagerIntent);
 */

public class AppManager_Service extends Service {

	//Package Manager
	private static PackageManager pkgManager;
	
	private static BroadcastReceiver appManagerReceiver = new AppManagerReceiver();
	public static class AppManagerReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Uri uri = intent.getData();
			if(uri == null) return;
			
			String pkgName = uri.getSchemeSpecificPart();
			if(pkgName == null) return;
			
			Bundle extras = intent.getExtras();
			if(appManagerListener != null) {
				if(intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
					if(extras != null && extras.getBoolean(Intent.EXTRA_REPLACING, false))
						appManagerListener.onAppUpdated(pkgName);
					else
						appManagerListener.onAppInstalled(pkgName);
				}
				if(intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
					if(extras != null && extras.getBoolean(Intent.EXTRA_REPLACING, false)) {
						//this is a package that will be replaced, we will receive a ACTION_PACKAGE_ADDED after this
					}else appManagerListener.onAppRemoved(pkgName);
				}
			}
		}
	}
	
	//Application Manager Listener interface
	private static OnAppChangedListener appManagerListener;
	public interface OnAppChangedListener {
		public abstract void onAppInstalled(String pkgName);
		public abstract void onAppRemoved(String pkgName);
		public abstract void onAppUpdated(String pkgName);
	} 
	
	public void setOnAppChangedListener(OnAppChangedListener listener) {
		appManagerListener = listener;
	}
	
	public OnAppChangedListener getOnAppChangedListener() {
		return appManagerListener;
	}
	
	//Activity binder
	private final IBinder appManagerBinder = new AppManagerBinder();
	
    private static Context context;
	
    //Singleton pattern
    private static final AppManager_Service appManagerSrv = new AppManager_Service();
    public static AppManager_Service getService() {
    	return appManagerSrv;
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		return appManagerBinder;
	}
	
	public class AppManagerBinder extends Binder {
		AppManager_Service getService() {
			return AppManager_Service.getService();
		}
	}
	
	public void setApplicationContext(Context mContext) {
		AppManager_Service.context = mContext;
	}
	
	public void setPackageManager(PackageManager pkgMng) {
		AppManager_Service.pkgManager = pkgMng;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if(context == null) 
			context = this.getApplicationContext();
		
		if(pkgManager == null) 
			pkgManager = this.getPackageManager();
		
		IntentFilter pkgMng = new IntentFilter();
		pkgMng.addAction(Intent.ACTION_PACKAGE_ADDED);
		pkgMng.addAction(Intent.ACTION_PACKAGE_REMOVED);
		pkgMng.addDataScheme("package");
		
		if(appManagerReceiver!=null)
			registerReceiver(appManagerReceiver, pkgMng);
		
		Log.d("AWARE", "Application Manager Service running!");
	} 
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		//clean-up
		unregisterReceiver(appManagerReceiver);
		
		Log.d("AWARE","Application Manager Service terminated...");
		
	}
	
	//Functionalities
	public List<ApplicationInfo> getInstalledApplications() {
		return pkgManager.getInstalledApplications(PackageManager.GET_META_DATA);
	}
	
}
