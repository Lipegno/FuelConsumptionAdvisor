/**
 * The class that listens to changes on the magnetometer
 * 
 */
package com.android.aware_framework;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class Magnetometer_Service extends Service	{

	private static final String MODULE = "Magnetometer_Service";

	private static SensorEventListener magnetometerListener;
	private static MagnetometerObserver magnetometerObs;
	
	private Sensor magnetometer;
	private Handler handler;
	private int sensorDelay = 3;	// Default value
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {

		try {
			super.onCreate();
			initEventListener();
			SensorManager sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
			magnetometer = sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			if (handler != null)
				sManager.registerListener(
						magnetometerListener,
						magnetometer,
						sensorDelay,
						handler);
			else
				sManager.registerListener(
						magnetometerListener,
						magnetometer,
						sensorDelay);
		} catch (Exception e) {
			Log.e(MODULE, "Error while creating service: " + e.getMessage());
		}
	}
	
	public MagnetometerObserver getMagnetometerObserver()	{
		
		return magnetometerObs; 
	}
	
	public void setMagnetometerObserver(MagnetometerObserver mObs)	{
		
		magnetometerObs = mObs;
	}
	
	public Handler getHandler()	{
		
		return this.handler;
	}
	
	public void setHandler(Handler handler)	{
		
		this.handler = handler;
	}
	
	public int getSensorDelay()	{
		
		return this.sensorDelay;
	}
	
	public void setSensorDelay(int sensorDelay)	{
		
		this.sensorDelay = sensorDelay;
	}
	
	/**
	 * Initialize the magnetometer listener
	 */
	private void initEventListener() throws Exception	{
		
		magnetometerListener = new SensorEventListener() {
			
			@Override
			public void onSensorChanged(SensorEvent event) {

				try {
					// Get the info object
					MagnetometerInfo mInfo = MagnetometerInfo.getMagnetometerInfo();
					
					// Test event type. We are only interested in magnetic field change
					switch (event.sensor.getType()) {
					case Sensor.TYPE_MAGNETIC_FIELD:
						mInfo.setValues(event.values);
						magnetometerObs.onMagnetometerChange(mInfo);
						break;

					default:
						break;
					}
				} catch (Exception e) {
					Log.e(MODULE, "Error on dealing with sensor change: " + e.getMessage());
				}
			}
			
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// TODO Auto-generated method stub
				
			}
		};
	}
	
	
	/**
	 *	The class that holds the actual magnetometer information 
	 */
	public static class MagnetometerInfo	{
		
		private static final MagnetometerInfo magnetometerInfo = new MagnetometerInfo();
		
		private float[] values = new float[3];
		
		/**
		 * Default constructor
		 */
		private MagnetometerInfo()	{}
		
		/**
		 * Get the singleton object
		 * @return - the MagnetometerInfo object
		 */
		public static MagnetometerInfo getMagnetometerInfo()	{
			
			return magnetometerInfo;
		}
		
		public float[] getValues()	{
			
			return this.values;
		}
		
		public void setValues(float[] values)	{
			
			this.values = values;
		}
	}
	
	/**
	 *	The interface that gets called back when a change on the magnetometer happens 
	 */
	public interface MagnetometerObserver	{
		
		public void onMagnetometerChange(MagnetometerInfo mInfo);
	}
}
