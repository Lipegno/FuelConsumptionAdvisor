/*
 * Created by Denzil Ferreira
 * dferreir@andrew.cmu.edu
 * 
 * Calendar service for Android Aware framework
 * Last updated: 30-Jun-2010
 * Modified by: Denzil Ferreira
 */
package com.android.aware_framework;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/*
 * Example of usage:
 * 
 * INSIDE ACTIVITY:
 * 
 * //we are using Singleton, always returns same instance in memory
 * Accelerometer_Service accService = Accelerometer_Service.getService();
 * 
 * //we can change the accelerometer reading rate
 * // SENSOR_DELAY_FASTEST; SENSOR_DELAY_GAME; SENSOR_DELAY_NORMAL; SENSOR_DELAY_UI 
 * accService.setSensorDelay(SensorManager.SENSOR_DELAY_XXX);
 * 
 * Intent accelIntent = new Intent(this,Accelerometer_Service.class);
 * startService(accelIntent);
 * 
 * //Get the current vectorial speed
 * float currentVS = accService.getVectorialSpeed();
 * float XX = accService.getXX();
 * float YY = accService.getYY();
 * float ZZ = accService.getZZ();
 */

public class TEMPLATE_Service extends Service {

	//Activity binder
	private final IBinder calendarBinder = new CalendarBinder();
	
    private static Context context;
	
    //Singleton pattern
    private static final TEMPLATE_Service calendarService = new TEMPLATE_Service();
    public static TEMPLATE_Service getService() {
    	return calendarService;
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		return calendarBinder;
	}
	
	public class CalendarBinder extends Binder {
		TEMPLATE_Service getService() {
			return TEMPLATE_Service.getService();
		}
	}
	
	public void setApplicationContext(Context mContext) {
		TEMPLATE_Service.context = mContext;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if(context == null) context = getApplicationContext();
		
		Log.d("AWARE", "Calendar Service running!");
	}

	 
	
	@Override
	public void onDestroy() {
		
		Log.d("AWARE","Calendar Service terminated...");
		
		super.onDestroy();
	}
}
