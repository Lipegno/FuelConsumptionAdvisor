package com.fuel.advisor.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

import com.android.aware_framework.Accelerometer_Service;
import com.android.aware_framework.Location_Service;
import com.fuel.advisor.R;
import com.fuel.advisor.processing.SensorDataProcessor;
import com.fuel.advisor.storage.DBManager;
import com.fuel.advisor.storage.TripDataStorage;

/**
 * The main Activity for the application
 * 
 * @author Tiago Camacho
 */
public class FuelConsumptionAdvisor extends Activity {
	
	public static final String VEHICLE_FILENAME = "vehicle_state";
	
	private static final String MODULE = "FuelConsumptionAdvisor";
	private static final boolean DEBUG = true;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	try {
    		super.onCreate(savedInstanceState);
    		requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.main);
            new MainClickHandler(this);
            
            // Need to create database if it doesn't exist yet
            if (! DBManager.databaseExists())
            	DBManager.initDatabase();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    @Override
    public void onBackPressed() {
    	
    	try {
			if (DEBUG)
				Log.d(MODULE, "Cleaning up application");
			super.onBackPressed();
			DBManager.getDBManager().close();
			cleanEnabledServices();
			finish();
			System.runFinalizersOnExit(true);
			System.exit(0);
		} catch (Exception e) {
			Log.e(MODULE, "Failure to clean up application: " + e.getMessage());
		}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	
    	try {
			MenuInflater mi = getMenuInflater();
			mi.inflate(R.menu.main_menu, menu);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	try {
    		switch (item.getItemId()) {
			case R.id.main_options:
				startActivity(new Intent(getApplicationContext(), MainOptions.class));
				break;
			default:
				break;
			}
    	} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return true;
    }
    
    @Override
    protected void onResume() {
    	
    	// Let's read possible changes on settings
    	try {
    		// First get the values stored on the default shared preferences
    		super.onResume();
    		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    		double acc_sensitivity = new Double(sp.getString("acc_sensitivity", "0.1"));
    		double acc_sampling_rate = new Double(sp.getString("acc_sampling", "5")); 
    		double gps_sampling_rate = new Double(sp.getString("gps_sampling", "1")); 
    		
    		// Query for current sensor settings
    		if (Accelerometer_Service.getAccelerometerSensitivity() != acc_sensitivity)
    			Accelerometer_Service.setAccelerometerSensitivy(acc_sensitivity);
    		long acc_sampling_period = (long) (1 / acc_sampling_rate * 1000);
    		if (Accelerometer_Service.getAccelerometerUpdatePeriod() != acc_sampling_period)
    			Accelerometer_Service.setAccelerometerUpdatePeriod(acc_sampling_period);
    		long gps_sampling_period = (long) (1 / gps_sampling_rate * 1000);
    		if (Location_Service.getUpdateIntervalGPS() != gps_sampling_period)
    			Location_Service.setUpdateIntervalGPS(gps_sampling_period);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
	/**
	 * Cleans services that may be enabled
	 * @throws Exception
	 */
	private void cleanEnabledServices() throws Exception	{
		
		stopService(new Intent(this, SensorDataProcessor.class));
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if (sp.getBoolean("trip_logging", false))
			stopService(new Intent(this, TripDataStorage.class));
	}
}