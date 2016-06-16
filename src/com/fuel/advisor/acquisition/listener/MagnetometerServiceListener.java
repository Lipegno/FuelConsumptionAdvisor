/**
 * The class that listens to all changes in the magnetic field
 */
package com.fuel.advisor.acquisition.listener;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.android.aware_framework.Magnetometer_Service;
import com.android.aware_framework.Magnetometer_Service.MagnetometerInfo;

public final class MagnetometerServiceListener extends Observable	{

	private static final String MODULE = "MagnetometerServiceListener";
	private static final boolean DEBUG = true;
	private static final int MAGNOMETER_SENSITIVITY = 3;

	private final Magnetometer_Service ms = new Magnetometer_Service();
	private final Handler handler;
	private final Context ctx;
	
	private float[] values = new float[3];
	
	/**
	 * Default constructor.Builds a Magnetometer service that notifies all
	 * interested observers when a change happens
	 * @param ctx - The current context of the application
	 * @param handler - The optional handler if running on a different thread
	 * @param obs - The observer object
	 */
	public MagnetometerServiceListener(Context ctx, Handler handler, Observer obs)	{

		this.ctx = ctx;
		this.handler = (handler != null)
			? handler : new Handler(ctx.getMainLooper());
		this.addObserver(obs);
	}
	
	/**
	 * Initialize the service
	 */
	public void start()	{
		
		// Set up the service parameters
		ms.setSensorDelay(MAGNOMETER_SENSITIVITY);
		ms.setHandler(handler);
		ms.setMagnetometerObserver(new Magnetometer_Service.MagnetometerObserver()	{
			
			@Override
			public void onMagnetometerChange(MagnetometerInfo mInfo) {

				try {
					System.arraycopy(mInfo.getValues(), 0, values, 0, mInfo.getValues().length);
					setChanged();
					notifyObservers(values);
				} catch (Exception e) {
					Log.e(MODULE, "Error while dealing with magnometer change: " + e.getMessage());
				}
			}
		});
	
		// Start the actual service
		ctx.startService(new Intent(ctx, Magnetometer_Service.class));
	}

	/**
	 * Halt the service execution
	 */
	public void stop()	{
		
		try {
			if (DEBUG)
				Log.d(MODULE, "Halting magnometer service");
			ctx.stopService(new Intent(ctx, Magnetometer_Service.class));
		} catch (Exception e) {
			Log.e(MODULE, "Error while stopping magnometer service: " + e.getMessage());
		}
	}
}
