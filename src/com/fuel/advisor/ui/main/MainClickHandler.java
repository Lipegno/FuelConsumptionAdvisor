/**
 * Class that sets up the touchListener for the main activity
 */
package com.fuel.advisor.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.fuel.advisor.R;
import com.fuel.advisor.ui.calibrate.CalibrationDriver;
import com.fuel.advisor.ui.calibrate.CalibrationPhone;
import com.fuel.advisor.ui.custom.ButtonClickHandler;
import com.fuel.advisor.ui.driver_management.DriverManagement;
import com.fuel.advisor.ui.main.choose.MainChooseSettings;
import com.fuel.advisor.ui.vehicle_management.VehicleManagement;

public class MainClickHandler extends ButtonClickHandler {

	private static final String MODULE = "MainClickHandler";
	private static final int[] button_ids = new int[]{
		R.id.main_vehicle_button,
		R.id.main_driver_button,
		R.id.main_calibrate_button,
		R.id.main_calibrate_orientation,
		R.id.main_start_button};

	public MainClickHandler(Activity a)	{
		
		super(a, button_ids);
	}
	
	/**
	 * Handle the request
	 */
	protected void handle(View v)	{

		try {
			int v_id = v.getId();
			
			switch (v_id) {
			case R.id.main_vehicle_button:
				startVehicleManagement();
				break;
			case R.id.main_driver_button:
				startDriverManagement();
				break;
			case R.id.main_calibrate_button:
				startCalibration();
				break;
			case R.id.main_calibrate_orientation:
				startOrientationCalibration();
				break;
			case R.id.main_start_button:
				startMain();
				break;
			default:
				Log.e(MODULE, "Unknown operation caught for button ID: " + v_id);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Starts the Vehicle management activity
	 * @throws Exception
	 */
	private void startVehicleManagement() throws Exception	{
	
		Activity a = getActivity();
		a.startActivity(new Intent(a, VehicleManagement.class));
	}
	
	private void startDriverManagement() throws Exception	{
		
		Activity a = getActivity();
		a.startActivity(new Intent(a, DriverManagement.class));
	}
	
	/**
	 * Start up the calibration activity
	 * 
	 */
	private void startCalibration()	throws Exception	{
		
		Activity a = getActivity();
		a.startActivity(new Intent(a, CalibrationPhone.class));
	}
	
	private void startOrientationCalibration() throws Exception	{
		
		Activity a = getActivity();
		a.startActivity(new Intent(a, CalibrationDriver.class));
	}
	
	/**
	 * Starts the main application. This sends us to the screen
	 * where the user has to choose the driver and vehicle information
	 */
	private void startMain() throws Exception	{
		
		Activity a = getActivity();
		a.startActivity(new Intent(a, MainChooseSettings.class));
	}
}