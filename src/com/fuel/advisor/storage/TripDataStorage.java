package com.fuel.advisor.storage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ViewDebug.IntToString;

import com.fuel.advisor.R;

/**
 * A class that implements a service that runs on the background, receiving trip related data
 * and storing it into the database. This service runs on a separate process space
 * 
 * @author Tiago Camacho
 */
@SuppressWarnings("unused")
public class TripDataStorage extends Service implements Runnable	{

	private static final String MODULE = "TripDataStorage";
	private static final int FLUSHING_PERIOD = 30 * 1000;	// seconds

	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	private final ArrayList<ContentValues> buffer = new ArrayList<ContentValues>();
	
	private long trip_id = 0;
	private ArrayList<ContentValues> tempBuffer;
	private DBManager db_man;
	private Thread t;
	private TripDataBroadcastReceiver broadcastRcv;
	
	@Override
	public void onCreate() {
		
		try {
			super.onCreate();
			// Get the trip ID value and mark the trip beginning
			db_man = DBManager.getDBManager();
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			long d_id = sp.getLong("driver_id", 0);
			long v_id = sp.getLong("vehicle_id", 0);
			long dv_id = db_man.associateDriverVehicle(d_id, v_id);
			if (d_id == 0 || v_id == 0 || dv_id == -1)	{
				Log.e(MODULE, "Failed to fetch driver and vehicle IDs. Cannot store trip data in database");
				stopSelf();
				return;
			}
			if ((trip_id = db_man.startTrip(dv_id)) == -1)	{
				Log.e(MODULE, "Failed to set start trip. Cannot store trip data in database");
				stopSelf();
				return;
			}
			
			// Register the filter so we can receive content from the processing layer 
			IntentFilter filter = new IntentFilter(
					getString(R.string.vehicle_info_broadcast));
			broadcastRcv = new TripDataBroadcastReceiver();
			
			// Initiate the process
			getApplicationContext().registerReceiver(broadcastRcv, filter);
			db_man = DBManager.getDBManager();
			t = new Thread(this, "Trip data storage");
			t.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onDestroy() {
		
		try {
			if (! buffer.isEmpty())
				flushBuffer();
			if (trip_id > 0)
				db_man.endTrip(trip_id);
			getApplicationContext().unregisterReceiver(broadcastRcv);
			broadcastRcv.abortBroadcast();
			t.interrupt();
			super.onDestroy();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Continously check if buffer flushing is necessary
	 */
	@Override
	public void run()	{
		
		while (true)	{
			try {
				if (! buffer.isEmpty())
					flushBuffer();
				Thread.sleep(FLUSHING_PERIOD);
			} catch (InterruptedException ie)	{
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Updates the buffer that holds the data about the trip
	 * @param latitude - The latitude at a given instance
	 * @param longitude - The longitude at a given instance
	 * @param altitude - The altitude at a given instance
	 * @param speed - The linear speed value
	 * @param acceleration - The linear acceleration value
	 * @throws Exception
	 */
	private void updateBuffer(double latitude, double longitude,
			double altitude, double speed, double acceleration, double grade, double vsp) throws Exception	{
		
		ContentValues values = new ContentValues();
		values.put("trip_id", trip_id);
		values.put("latitude", latitude);
		values.put("longitude", longitude);
		values.put("altitude", altitude);
		values.put("speed", speed);
		values.put("acceleration", acceleration);
		values.put("grade", grade);
		values.put("vsp", vsp);
		values.put("timestamp", sdf.format(new Date()));
		synchronized (buffer) {
			buffer.add(values);
		}
	}
	
	/**
	 * Flushes the buffer's content
	 */
	private void flushBuffer()	{
		
		// Copy buffer content to temporary buffer,
		// releasing the buffer for additional operations
		synchronized (buffer) {
			tempBuffer = new ArrayList<ContentValues>(buffer);
			buffer.clear();
		}
		
		// Now we can safely issue for database addition
		db_man.insertTripData(tempBuffer);
	}
	
	/**
	 * An inner class that receives the broadcast data for the current trip
	 */
	private class TripDataBroadcastReceiver extends BroadcastReceiver	{
		
		private Bundle msgContent = new Bundle();
		
		@Override
		public void onReceive(Context context, Intent intent) {

			try {
				// Extract content from Intent and update the buffer
				msgContent = intent.getExtras();
				updateBuffer(
						msgContent.getDouble("latitude"), 
						msgContent.getDouble("longitude"), 
						msgContent.getDouble("altitude"), 
						msgContent.getDouble("speed"), 
						msgContent.getDouble("linear_acceleration"),
						msgContent.getDouble("grade"),
						msgContent.getDouble("vsp"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}