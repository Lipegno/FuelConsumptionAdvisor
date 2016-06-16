package com.fuel.advisor.acquisition.listener;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.os.Handler;
import android.util.Log;

import com.android.aware_framework.Location_Service;

public final class LocationServiceListener extends Observable {
	
	private static final String MODULE = "LocationServiceListener";
	private static final boolean DEBUG = false;
	private static final int MINIMUM_SAT_NUM = 3;
	
	private final Location_Service ls = new Location_Service();
	private final Handler handler;
	private final Context ctx;
	
	/**
	 * Default constructor. 
	 * @param ctx
	 * @param handler
	 * @param obs
	 */
	public LocationServiceListener(Context ctx, Handler handler, Observer obs) {

		this.ctx = ctx;
		this.handler = (handler != null)
			? handler : new Handler(ctx.getMainLooper());
		this.addObserver(obs);
	}
	
	/**
	 * Initialize the components
	 */
	public void start()	{
		
		// Set up the location service parameters
		ls.setUpdateIntervalGPS(0);
		ls.setUpdateIntervalNetwork(0);
		ls.setMinDistanceGPS(5);
		ls.setMinDistanceNetwork(100);
		ls.setHandler(handler);
		
		// Set up the listener implementation
		ls.setLocationListener(new Location_Service.OnLocationChanged()  {
			
			private int gps_fixed = 0;
			private boolean got_first_fix = false;
			
			@Override
			public void onGpsStatusChanged(int event) {

				try {
					int num_sat = 0;
					
					// Get the GPS status information
					GpsStatus status = ls.getGPSStatus(null);
					for (@SuppressWarnings("unused") GpsSatellite sat
							: status.getSatellites()) {
						num_sat++;
					}
					// Determine if we have a fix or not
					if (event == GpsStatus.GPS_EVENT_FIRST_FIX)	{
						got_first_fix = true;
						gps_fixed = 1;
					}
					else
						gps_fixed = (got_first_fix && 
								event != GpsStatus.GPS_EVENT_STOPPED && num_sat >= MINIMUM_SAT_NUM)
						 	? 1
						 	: 0;
					// Notify observers
					setChanged();
					notifyObservers(new int[]{gps_fixed, num_sat});
				} catch (Exception e) {
					Log.e(MODULE, "Failure while dealing with GPS status change: " + e.getMessage());
				}
			}
			
			@Override
			public void onLocationGPSChanged(Location local) {
				
				try {
					if (DEBUG)
						Log.d(MODULE, "GPS location changed");
					// Notify observers
					setChanged();
					notifyObservers(local);
				} catch (Exception e) {
					Log.e(MODULE, "Failure while inserting values on GPS change: " + e.getMessage());
				}
			}
		});
		
		// Start the actual location service
		ctx.startService(new Intent(ctx, Location_Service.class));
	}
	
	/**
	 * Halt the location service
	 */
	public void stop()	{
		
		try {
			ctx.stopService(new Intent(ctx, Location_Service.class));
		} catch (Exception e) {
			Log.e(MODULE, "Error while stopping location service: " + e.getMessage());
		}
	}
}
