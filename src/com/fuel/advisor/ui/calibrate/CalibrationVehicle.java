package com.fuel.advisor.ui.calibrate;

import com.fuel.advisor.R;
import com.fuel.advisor.processing.SensorDataProcessor;
import com.fuel.advisor.storage.CalibrationDataStorage;
import com.fuel.advisor.storage.DBManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

/**
 * The Activity used to show the information about the calibration of a specific kind
 * of vehicle
 * 
 * @author Tiago Camacho
 */
@SuppressWarnings("unused")
public class CalibrationVehicle extends Activity	{

	private static final String MODULE = "CalibrationVehicle";
	
	private boolean finished = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		try {
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.calibration_info);
			startService(new Intent(this, SensorDataProcessor.class));
			startService(new Intent(this, CalibrationDataStorage.class));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onBackPressed() {
		
		if (finished)
			super.onBackPressed();
	}
	
	/**
	 * Handle the buttons' click
	 * @param v - The view that has been clicked
	 */
	public void handleButtonClick(View v)	{
		
		try {
			switch (v.getId()) {
			case R.id.calibration_info_finish_button:
				finishCalibration();
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Finalizes the calibration process
	 * @throws Exception
	 */
	private void finishCalibration() throws Exception	{
		
		stopService(new Intent(this, CalibrationDataStorage.class));
		stopService(new Intent(this, SensorDataProcessor.class));
		finished = true;
		Button b = (Button)findViewById(R.id.calibration_info_finish_button);
		b.setEnabled(false);
		Toast t = Toast.makeText(getApplicationContext(), "Finished Calibration Process", Toast.LENGTH_LONG);
		t.show();
	}
}