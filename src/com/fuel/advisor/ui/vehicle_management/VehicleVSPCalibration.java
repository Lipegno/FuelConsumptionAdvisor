package com.fuel.advisor.ui.vehicle_management;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;

import com.fuel.advisor.R;
import com.fuel.advisor.storage.DBManager;
import com.fuel.advisor.ui.calibrate.CalibrationPhone;
import com.fuel.advisor.ui.calibrate.CalibrationVehicle;
import com.fuel.advisor.ui.custom.ButtonClickHandler;
import com.fuel.advisor.ui.custom.CustomDialog;
import com.fuel.advisor.ui.custom.TextViewBorder;
import com.fuel.advisor.ui.custom.CustomDialog.DialogType;
import com.fuel.advisor.ui.custom.popupwindow.ChooseVehiclePopup;
import com.fuel.advisor.ui.custom.popupwindow.PopupWindowScreen;
import com.vehicle.vehicle.Vehicle;

/**
 * The Activity used to fetch the details about the calibration process. It eventually calls
 * {@link CalibrationPhone} to start the calibration process
 * 
 * @author Tiago Camacho
 */
@SuppressWarnings("unused")
public class VehicleVSPCalibration extends Activity {
	
	private static final String MODULE = "VehicleVSPCalibration";
	private static final int[] view_ids = new int[]{
		R.id.vehicle_calibration_choose_vehicle_button
	};
	
	private long vt_id;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		try {
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.vehicle_calibration);
			CustomDialog.createDialog(this, DialogType.INFO).show(
					"Vehicle Calibration Info",
					"This enables VSP model calibration for specific vehicles. Press 'Start' " +
					"and 'Finish' to start and end the calibration process, respectively. " +
					"Try getting both low and high levels for speed and acceleration while in calibration mode. " +
					"WARNING: This process does not measure real fuel consumption in any way!");
			new VehicleVSPClickHandler(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Handle the 'Start' button click
	 * @param v - The view that has been clicked
	 */
	public void handleButtonClick(View v)	{
		
		try {
			switch (v.getId()) {
			case R.id.vehicle_calibration_start:
				startCalibration();
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Starts the calibration process
	 * @throws Exception
	 */
	private void startCalibration() throws Exception	{
		
		String plate = 
			((TextViewBorder)findViewById(R.id.vehicle_calibration_vehicle_plate)).getText().toString();
		
		// No vehicle chosen. Cannot initiate the calibration process
		if (plate == null)	{
			CustomDialog.createDialog(this, DialogType.ERROR).show(
					"No Vehicle Choosen",
					"You have to choose a vehicle before starting the calibration process");
			return;
		}
		
		// Determine if we already perform calibration for this device. If so present a dialog
		// to make sure that the user wants to override current data
		Vehicle v = new Vehicle(null, null, 0);
		v.setPlate(plate);
		final DBManager db_man = DBManager.getDBManager();
		vt_id = db_man.getVehicleTypeID(db_man.getVehicleID(v));
		if (db_man.isVehicleTypeCalibrated(vt_id))	{
			CustomDialog cd = CustomDialog.createDialog(this, DialogType.OPTION);
			cd.show(
				"Calibration Already Performed",
				"It seems that this vehicle type has already been calibrated. If you decide " +
				"to continue, all calibration information will be lost. Start calibration and " +
				"overwrite previous data?");
			cd.setOnConfirmationListener(new CustomDialog.OnConfirmationListener() {
				@Override
				public void onConfirmation() {
					db_man.removeCalibration(vt_id);
					startActivity();
				}
			});
		}
		else	{
			startActivity();
		}
	}
	
	/**
	 * Starts the calibration activity
	 */
	private void startActivity()	{
		
		startActivity(new Intent(this, CalibrationVehicle.class));
	}
	
	/**
	 * Inner class for taking care of center view button clicks
	 */
	private final class VehicleVSPClickHandler extends ButtonClickHandler	{
		
		private ChooseVehiclePopup cvp;
		
		public VehicleVSPClickHandler(Activity a) {

			super(a, view_ids);
		}
		
		@Override
		protected void handle(View v) {

			try {
				switch (v.getId()) {
				case R.id.vehicle_calibration_choose_vehicle_button:
					cvp = new ChooseVehiclePopup(v, R.id.vehicle_calibration_center_view);
					cvp.setOnCompletionListener(new PopupWindowScreen.OnPopupWindowCompletionListener() {
						@Override
						public void onOperationCompletion() {
							Vehicle v = null;
							SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
							v = DBManager.getDBManager().getVehicle(sp.getLong("vehicle_id", 0));
							if (v == null)
								return;
							populateVehicleInfo(v);
						}
					});
					break;
				default:
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * Populates the fields with the vehicle information after the user has chosen
		 * @param v - The Vehicle object
		 */
		private void populateVehicleInfo(Vehicle v)	{
			
			try {
				TextViewBorder tv = (TextViewBorder) findViewById(R.id.vehicle_calibration_vehicle_brand);
				tv.setText(v.getBrand());
				tv = (TextViewBorder) findViewById(R.id.vehicle_calibration_vehicle_model);
				tv.setText(v.getModel());
				tv = (TextViewBorder) findViewById(R.id.vehicle_calibration_vehicle_year);
				tv.setText(new Integer(v.getYear()).toString());
				tv = (TextViewBorder) findViewById(R.id.vehicle_calibration_vehicle_plate);
				tv.setText(v.getPlate());
				findViewById(R.id.vehicle_calibration_start).setEnabled(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}