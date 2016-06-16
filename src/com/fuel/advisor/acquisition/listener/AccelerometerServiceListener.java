package com.fuel.advisor.acquisition.listener;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.android.aware_framework.Accelerometer_Service;
import com.android.aware_framework.Accelerometer_Service.AccelerometerInfo;

public final class AccelerometerServiceListener extends Observable	{

	private static final String MODULE = "AccelerometerServiceListener";
	
	private final Accelerometer_Service as = new Accelerometer_Service();
	private final Context ctx;
	private final Handler handler;
	private final double[] acc = new double[3];
	/**
	 * Default constructor. Builds an observable accelerometer listener that
	 * adds the caller as an observer
	 * @param ctx - The current application context
	 * @param handler - The optional handler if running on a different thread
	 * @param obs - The observer object
	 */
	public AccelerometerServiceListener(Context ctx, Handler handler, Observer obs) {

		this.ctx = ctx;
		this.handler = (handler != null)
			? handler : new Handler(ctx.getMainLooper());
		this.addObserver(obs);
	}
	
	/**
	 * Initialize the components
	 */
	public void start()	{

		// Set up delay and sensitivity settings and ensure we are registering
		// the sensor with this Thread
		as.setHandler(handler);

		// Set up the observer implementation
		as.setAccelerometerObserver(new Accelerometer_Service.AccelerometerObserver() {

			@Override
			public void onAccelerometerChanged(AccelerometerInfo accelerometer) {

				try {
					// Get the acceleration values
					acc[0] = accelerometer.getX();
					acc[1] = accelerometer.getY();
					acc[2] = accelerometer.getZ();
					Log.d(MODULE, "Acc x: " + acc[0] + " Acc y: " + acc[1] + " Acc z: " + acc[2]);
					setChanged();
					//Log.e(MODULE,"x acc:"+accelerometer.getX()+" y acc:"+accelerometer.getY()+" z acc:"+accelerometer.getZ());
					notifyObservers(acc);
				} catch (Exception e) {
					Log.e(MODULE, "Failure while inserting values on accelerometer change: "
							+ e.getMessage());
				}
			}
		});
		
		// Start the actual accelerometer service
		ctx.startService(new Intent(ctx, Accelerometer_Service.class));
	}
	
	/**
	 * Clean up the listener
	 */
	public void stop()	{
		
		try {
			ctx.stopService(new Intent(ctx, Accelerometer_Service.class));
		} catch (Exception e) {
			Log.e(MODULE, "Error shutting down accelerometer service: " + e.getMessage());
		}
	}
	
	/**
	 * Sets up the update period for the accelerometer
	 * @param period - The period in milliseconds
	 */
	public void setAccelerometerUpdatePeriod(long period)	{
		
		Accelerometer_Service.setAccelerometerUpdatePeriod(period);
	}
	
	/**
	 * Chnage the minium value of acceleration that triggers
	 * acceleration reading propagation
	 * @param min_acc - The minimum value of the acceleration
	 */
	public void setAccelerometerMinimumSensitivity(double min_acc)	{
		
		Accelerometer_Service.setAccelerometerSensitivy(min_acc);
	}
}