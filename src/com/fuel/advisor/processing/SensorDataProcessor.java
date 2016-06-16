package com.fuel.advisor.processing;

import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observer;
import java.util.Vector;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fuel.advisor.R;
import com.fuel.advisor.acquisition.listener.AccelerometerServiceListener;
import com.fuel.advisor.acquisition.listener.LocationServiceListener;
import com.fuel.advisor.misc.LooperRunnable;
import com.fuel.advisor.processing.filter.ContextSpecifier;
import com.fuel.advisor.processing.filter.DynamicsCalculator;
import com.fuel.advisor.processing.filter.KalmanAccGPS;
import com.fuel.advisor.processing.filter.PipeFilter;
import com.fuel.advisor.storage.DBManager;
import com.vehicle.vehicle.Vehicle;

/**
 * The Service that performs background data processing and manages the filters to apply to that data. This
 * module is not responsible for the final destination of the transformed data. That is usually the worry of the
 * last filter component, or more concretely of the Data Sink component
 * 
 * @author Tiago Camacho
 */
@SuppressWarnings("unused")
public final class SensorDataProcessor extends Service implements LooperRunnable, Observer	{

	private static final String MODULE = "SensorDataProcessor";
	private static final boolean DEBUG = true;
	
	private final Bundle msgContent = new Bundle();
	private final double[] acc = new double[3];
	private final double[] acc_offset = new double[3];
	
	private long msgCounter = 0;
	private PipedOutputStream pos;
	private AccelerometerServiceListener asl;
	private LocationServiceListener lsl;
	private Handler handler;
	private PipeFilter filter;
	
	@Override
	public void onCreate() {
		
		try {
			super.onCreate();
			// Let's start by reading the preferences stored by
			// the calibration process
			SharedPreferences sp = getSharedPreferences(
					getString(R.string.phone_calib_file), 0);
			acc_offset[0] = (double)sp.getFloat("acc_off_x", 0);
			acc_offset[1] = (double)sp.getFloat("acc_off_y", 0);
			acc_offset[2] = (double)sp.getFloat("acc_off_z", 0);
			sp = getSharedPreferences(getString(R.string.driver_calib_file), 0);
			double roll_offset = (double)sp.getFloat("roll_off", 0);
			double pitch_offset = (double)sp.getFloat("pitch_off", 0);
			
			// Now get the vehicle related data
			sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			Vehicle v = DBManager.getDBManager().getVehicle(sp.getLong("vehicle_id", 0));
			double air_coef = v.getAirCoef();
			double roll_coef = v.getRollCoef();
			
			// Create necessary filters and connect streams accordingly
			pos = new PipedOutputStream();
			filter = new ContextSpecifier(
						new DynamicsCalculator(
								new KalmanAccGPS(pos, pitch_offset), 
								pitch_offset),
						getApplicationContext(),
						getString(R.string.vehicle_info_broadcast));
			new Thread(this, "Sensor data procesor").start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDestroy() {
		
		try {
			filter.stop();
			asl.deleteObserver(this);
			lsl.deleteObserver(this);
			asl.stop();
			lsl.stop();
			handler.getLooper().quit();
			super.onDestroy();
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
	public void update(java.util.Observable observable, Object data) {

		try {
			// Accelerometer data
			if (observable instanceof AccelerometerServiceListener)	{
				double[] d = (double[])data;
				acc[0] = d[0] + acc_offset[0];
				acc[1] = d[1] + acc_offset[1];
				acc[2] = d[2] + acc_offset[2];
				msgContent.putDoubleArray("acceleration", acc);
			}
			// GPS data
			else if (observable instanceof LocationServiceListener)	{
				// Actual data location
				if (data instanceof Location)
					msgContent.putParcelable("location", (Location)data);
				// Current GPS status data
				else if (data instanceof int[])
					msgContent.putIntArray("gps_status", (int[])data);
			}
			msgContent.putLong("counter", ++msgCounter);
			msgContent.putLong("time", System.currentTimeMillis());
			
			// Put the Bundle object on the Parcel and ensure that
			// we have correct parcel size on the other end of the pipe
			Parcel p = Parcel.obtain();
			p.writeBundle(msgContent);
			p.setDataSize(PipeFilter.PARCELABLE_SIZE);
			pos.write(p.marshall());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {

		try {
			Looper.prepare();
			Context ctx = getApplicationContext();
			handler = new Handler(Looper.myLooper());
			filter.start();
			asl = new AccelerometerServiceListener(ctx, handler, this);
			// Sets the sampling period (5 samples per second in this case)
			asl.setAccelerometerUpdatePeriod(200);
			asl.setAccelerometerMinimumSensitivity(0.1);
			asl.start();
			(lsl = new LocationServiceListener(ctx, handler, this)).start();
			Looper.loop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Handler getHandler() {

		return this.handler;
	}
}