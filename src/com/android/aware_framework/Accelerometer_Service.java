/*
 * Created by Denzil Ferreira
 * dferreir@andrew.cmu.edu
 * 
 * Accelerometer service and listener for Android Aware framework
 * Last updated: 30-Jun-2010
 * Modified by: Denzil Ferreira
 */
package com.android.aware_framework;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

// 	Example of usage:
// 
// 
// 	Accelerometer_Service accService = Accelerometer_Service.getService();
//  
// 
// 	accelerometerSrv.setSensorDelay(SensorManager.SENSOR_DELAY_GAME);
// 	accelerometerSrv.setAccelerometerSensitivity(10);
//	//Create an accelerometer observer
//	accelerometerSrv.setAccelerometerObserver(new Accelerometer_Service.AccelerometerObserver() {
//		@Override
//		public void onAccelerometerChanged(AccelerometerInfo accelerometer) {
//			Log.d("AWARE",accelerometer.getSensorName());
//		}
//	});
//	
//	Intent accelIntent = new Intent(this,Accelerometer_Service.class);
//	startService(accelIntent);
// 
// 
//	Don't forget to add a Receiver in the manifest for detecting if the screen is off
//	ACTION_SCREEN_ON & ACTION_SCREEN_OFF
public class Accelerometer_Service extends Service {

	public static class AccelerometerInfo{
		
		private float sensorPower;
		private float sensorResolution;

		private float x;
		private float y;
		private float z;
		
		private AccelerometerInfo() {} //populated with the listener.
		
		private static AccelerometerInfo accelerometerInfo = new AccelerometerInfo();
		public static AccelerometerInfo getAccelerometerInfo() {
			return accelerometerInfo;
		}
		
		public float getSensorPower() {
			return sensorPower;
		}
		public void setSensorPower(float sensorPower) {
			this.sensorPower = sensorPower;
		}
		public float getSensorResolution() {
			return sensorResolution;
		}
		public void setSensorResolution(float sensorResolution) {
			this.sensorResolution = sensorResolution;
		}
		public float getX() {
			return x;
		}
		public void setX(float x) {
			this.x = x;
		}
		public float getY() {
			return y;
		}
		public void setY(float y) {
			this.y = y;
		}
		public float getZ() {
			return z;
		}
		public void setZ(float z) {
			this.z = z;
		}
	}
	
	// Define the acceleration and orientation observers
	private static AccelerometerObserver accelerometerObs;
	
	public interface AccelerometerObserver {
		public abstract void onAccelerometerChanged(AccelerometerInfo accelerometer);
	}
	
	public void setAccelerometerObserver(AccelerometerObserver observer) {
		Accelerometer_Service.accelerometerObs = observer;
	}
	
	public AccelerometerObserver getAccelerometerObserver() {
		return Accelerometer_Service.accelerometerObs;
	}
	
	// Set up the accelerometers threholds
	private static double accelerometerSensitivity = 0.1;	// m/s^2
	private static long updatePeriod = 200;	// miliseconds
	
	public static double getAccelerometerSensitivity()	{
		
		return accelerometerSensitivity;
	}
	
	public static void setAccelerometerSensitivy(double sensitivity)	{
		
		Accelerometer_Service.accelerometerSensitivity = sensitivity;
	}
	
	public static long getAccelerometerUpdatePeriod()	{
		
		return updatePeriod;
	}
	
	public static void setAccelerometerUpdatePeriod(long period)	{
		
		updatePeriod = period;
	}
	
	//SensorEventListener
	private static SensorEventListener sensorListener = new SensorEventListener() {
		
		private final float[] acc = new float[3];
		private final float[] last_acc = new float[3];
		
		private long lastUpdate = 0;
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			
			// Enforce the sampling period
			if (System.currentTimeMillis() - lastUpdate < updatePeriod)
				return;
			
			// If a change since last reading hasn't occured, then reset the current
			// acceleration values. This avoids getting very small fluctations on the output
			AccelerometerInfo accelerometer = AccelerometerInfo.getAccelerometerInfo();
			for (int i = 0; i < event.values.length; i++)
				acc[i] = event.values[i];
			if (! hasAccelerationChanged(acc, last_acc))
				for (int i = 0; i < acc.length; i++)
					acc[i] = last_acc[i];
			else
				for (int i = 0; i < acc.length; i++)
					last_acc[i] = acc[i];				
			accelerometer.setX(acc[0]);
			accelerometer.setY(acc[1]);
			accelerometer.setZ(acc[2]);
			accelerometerObs.onAccelerometerChanged(accelerometer);
			lastUpdate = System.currentTimeMillis();
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
		
		/*
		 * Determine if a reasonable acceleration "change" has occured since our
		 * last reading
		 */
		private boolean hasAccelerationChanged(float[] acc, float[] last_acc)	{
			
			return ((Math.abs(last_acc[0] - acc[0]) > accelerometerSensitivity) ||
					(Math.abs(last_acc[1] - acc[1]) > accelerometerSensitivity) || 
					(Math.abs(last_acc[2] - acc[2]) > accelerometerSensitivity));
		}
	};
	
	// The Handler object
	private Handler handler = null;
	
	//Activity binder
	private final IBinder accelerometerBinder = new AccelerometerBinder();
	
	private static SensorManager sensorManager;
	private static Sensor accelerometer;
    private static Context context;
	
    //Singleton pattern
    private static final Accelerometer_Service accelerometerService = new Accelerometer_Service();
    public static Accelerometer_Service getService() {
    	return accelerometerService;
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		return accelerometerBinder;
	}
	
	public class AccelerometerBinder extends Binder {
		Accelerometer_Service getService() {
			return Accelerometer_Service.getService();
		}
	}
	
	public void setApplicationContext(Context mContext) {
		Accelerometer_Service.context = mContext;
	}
	
	/**
	 * Sets up the Handler object to which the sensor values are going to be sent to.
	 * Failure to call this will send all data to the main Thread, damaging the application
	 * responsiveness
	 * @param handler - The Handler object
	 */
	public void setHandler(Handler handler)	{
		
		this.handler = handler;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if(context == null) 
			context = this.getApplicationContext();
		
		//Start accelerometer sensors
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		// If handler is set, then ensure that the registration is made taking into account
		// this information
		if (handler != null)
			sensorManager.registerListener(
					sensorListener,
					accelerometer,
					SensorManager.SENSOR_DELAY_NORMAL, 
					handler);
		else
			sensorManager.registerListener(
					sensorListener,
					accelerometer, 
					SensorManager.SENSOR_DELAY_NORMAL);
		Log.d("AWARE", "Accelerometer Service running!");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		sensorManager.unregisterListener(sensorListener); //kill sensor listener
		Log.d("AWARE","Accelerometer Service terminated...");
	}
}
