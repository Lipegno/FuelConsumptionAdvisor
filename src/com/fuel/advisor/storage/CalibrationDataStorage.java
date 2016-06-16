package com.fuel.advisor.storage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.fuel.advisor.R;

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

/**
 * The class that collects and stores calibration samples. Similarly to {@link TripDataStorage}, it
 * runs as a background service that continuously fetches broadcasted content and stores it into the
 * databaase
 * 
 * @author Tiago Camacho
 */
@SuppressWarnings("unused")
public class CalibrationDataStorage extends Service implements Runnable	{
	
	private static final boolean DEBUG = true;
	private static final String MODULE = "CalibrationDataStorage"; 
	private static final int FLUSHING_PERIOD = 10 * 1000;	// seconds

	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	private final ArrayList<ContentValues> buffer = new ArrayList<ContentValues>();
	
	private long vc_id;
	private DBManager db_man;
	private Thread t;
	private CalibrationSamplingBroadcastReceiver broadcastRcv;
	
	@Override
	public void onCreate() {
		
		try {
			super.onCreate();
			broadcastRcv = new CalibrationSamplingBroadcastReceiver();
			IntentFilter filter = new IntentFilter(
				getString(R.string.vehicle_info_broadcast));
			getApplicationContext().registerReceiver(broadcastRcv, filter);
			
			// Fetch the vehicle ID from the shared preferences
			db_man = DBManager.getDBManager();
			long v_id = 
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getLong("vehicle_id", -1);
			long vt_id = db_man.getVehicleTypeID(v_id);
			if (v_id == -1 || vt_id == -1)	{
				Log.e(MODULE, "Failed to fetch Vehicle Type ID. Cannot start calibration sampling storage");
				stopSelf();
				return;
			}
			// Issue start of the calibration sampling storage process
			vc_id = db_man.startCalibration(vt_id);
			t = new Thread(this, "Calibration Sampling Storage");
			t.start();
			Log.d(MODULE, "Start calibration storage procedure");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDestroy() {
		
		try {
			if (! buffer.isEmpty())
				flushBuffer();
			if (vc_id > 0)
				db_man.endCalibration(vc_id);
			getApplicationContext().unregisterReceiver(broadcastRcv);
			broadcastRcv.abortBroadcast();
			t.interrupt();
			super.onDestroy();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}

	@Override
	public void run() {

		while (true)	{
			try {
				if (! buffer.isEmpty())
					flushBuffer();
				Thread.sleep(FLUSHING_PERIOD);
			} catch (InterruptedException ie)	{
				Thread.currentThread().interrupt();
				break;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Updates the buffer's content
	 * @param latitude
	 * @param longitude
	 * @param altitude
	 * @param speed
	 * @param acceleration
	 * @param grade
	 */
	private void updateBuffer(double latitude, double longitude, double altitude, 
			double speed, double acceleration, double grade) throws Exception	{
		
		ContentValues values = new ContentValues();
		values.put("vc_id", vc_id);
		values.put("latitude", latitude);
		values.put("longitude", longitude);
		values.put("altitude", altitude);
		values.put("speed", speed);
		values.put("acceleration", acceleration);
		values.put("grade", grade);
		values.put("timestamp", sdf.format(new Date()));
		synchronized (buffer) {
			buffer.add(values);
		}
	}
	
	private void flushBuffer() throws Exception	{
		
		ArrayList<ContentValues> tempBuffer;
		
		// Let's perform a buffer copy so that we can release
		// the buffer as quickly as possible
		synchronized (buffer) {
			tempBuffer = new ArrayList<ContentValues>(buffer);
			buffer.clear();
		}
		
		// Issue insertion into database
		db_man.insertCalibrationSample(tempBuffer);
	}
	
	/**
	 *	Inner class responsible for receiving the broadcast from the service 
	 */
	private class CalibrationSamplingBroadcastReceiver extends BroadcastReceiver	{
		
		private Bundle msgContent = new Bundle();
		
		@Override
		public void onReceive(Context context, Intent intent) {

			try {
				// Extract the data from the Bundle object
				msgContent = intent.getExtras();
				updateBuffer(
						msgContent.getDouble("latitude"),
						msgContent.getDouble("longitude"),
						msgContent.getDouble("altitude"), 
						msgContent.getDouble("speed"), 
						msgContent.getDouble("linear_acceleration"), 
						msgContent.getDouble("grade"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}