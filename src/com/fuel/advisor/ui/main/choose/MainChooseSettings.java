package com.fuel.advisor.ui.main.choose;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.fuel.advisor.R;
import com.fuel.advisor.storage.DBManager;
import com.fuel.advisor.storage.TripDataStorage;
import com.fuel.advisor.ui.custom.ButtonClickHandler;
import com.fuel.advisor.ui.custom.TextViewBorder;
import com.fuel.advisor.ui.custom.popupwindow.ChooseDriverPopup;
import com.fuel.advisor.ui.custom.popupwindow.ChooseVehiclePopup;
import com.fuel.advisor.ui.custom.popupwindow.PopupWindowScreen.OnPopupWindowCompletionListener;
import com.fuel.advisor.ui.vehicle_info.HistoricalFeedback;
import com.fuel.advisor.ui.vehicle_info.VehicleInfoFeedback;
import com.fuel.advisor.ui.vehicle_info.VisualFeedback;
import com.vehicle.driver.Driver;
import com.vehicle.vehicle.Vehicle;

/**
 * The activity that is called after the user chooses the Start button
 * on the main activity. The user must choose the vehicle and driver settings
 * before starting the information/advisory system
 * 
 * @author Tiago Camacho
 */
@SuppressWarnings("unused")
public class MainChooseSettings extends Activity {

	private static final String MODULE = "MainChooseSettings";
	private static final int[] view_ids = new int[]	{
		R.id.main_choose_driver_button,
		R.id.main_choose_vehicle_button
	};
	
	private boolean loggingEnabled = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		try {
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.main_choose_settings);
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			loggingEnabled = sp.getBoolean("trip_logging", false);
			long d_id = sp.getLong("driver_id", 0);
			long v_id = sp.getLong("vehicle_id", 0);
			if (d_id != 0)
				setUpLastDriverInfo(DBManager.getDBManager().getDriver(d_id));
			if (v_id != 0)
				setUpLastVehicleInfo(DBManager.getDBManager().getVehicle(v_id));
			if (d_id != 0 && v_id != 0)
				enableVisualizationButtons();
			new MainChooseClickHandler(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onDestroy() {

		super.onDestroy();
	}
	
	/**
	 * Handles the start button click
	 * @param v - The view which was selected
	 */
	public void handleButtonClick(View v)	{
		
		try {
			switch (v.getId()) {
			case R.id.main_choose_settings_start_normal_button:
				startActivity(new Intent(this, VehicleInfoFeedback.class));
				if (loggingEnabled)
					startService(new Intent(this, TripDataStorage.class));
				break;
			case R.id.main_choose_settings_start_viz_button:
				startActivity(new Intent(this, VisualFeedback.class));
				if (loggingEnabled)
					startService(new Intent(this, TripDataStorage.class));
				break;
			case R.id.main_choose_settings_start_historical_data:
				startActivity(new Intent(this, HistoricalFeedback.class));
				if (loggingEnabled)
					startService(new Intent(this, TripDataStorage.class));
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets up the last choosen driver information
	 * @param d - The driver object
	 */
	private void setUpLastDriverInfo(Driver d)	{
	
		try {
			((TextViewBorder)findViewById(R.id.main_choose_first_name)).setText(d.getFirst_name());
			((TextViewBorder)findViewById(R.id.main_choose_last_name)).setText(d.getLast_name());
			((TextViewBorder)findViewById(R.id.main_choose_user_id)).setText(d.getPasscode());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets up the last choosen driver information
	 * @param v - The vehicle object
	 */
	private void setUpLastVehicleInfo(Vehicle v)	{
		
		try {
			((TextViewBorder)findViewById(R.id.main_choose_vehicle_brand)).setText(v.getBrand());
			((TextViewBorder)findViewById(R.id.main_choose_vehicle_model)).setText(v.getModel());
			((TextViewBorder)findViewById(R.id.main_choose_vehicle_year)).setText(
					Integer.toString(v.getYear()));
			((TextViewBorder)findViewById(R.id.main_choose_vehicle_plate)).setText(v.getPlate());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Enables the buttons used to start the visualizations
	 */
	private void enableVisualizationButtons()	{
		
		try {
			Button b = (Button)findViewById(R.id.main_choose_settings_start_normal_button);
			b.setEnabled(true);
			b = (Button)findViewById(R.id.main_choose_settings_start_viz_button);
			b.setEnabled(true);
			b = (Button)findViewById(R.id.main_choose_settings_start_historical_data);
			b.setEnabled(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *	Inner class responsible for handling icon clicks 
	 */
	private class MainChooseClickHandler extends ButtonClickHandler	{
		
		private ChooseDriverPopup cdp;
		private ChooseVehiclePopup cvp;
		
		public MainChooseClickHandler(Activity a) {

			super(a, view_ids);
		}
		
		/**
		 * Handle the button click
		 * @param v
		 */
		protected void handle(View v)	{
			
			try {
				switch (v.getId()) {
				case R.id.main_choose_driver_button:
					cdp = new ChooseDriverPopup(v, R.id.main_choose_center_view);
					cdp.setOnCompletionListener(new OnPopupWindowCompletionListener() {
						@Override
						public void onOperationCompletion() {
							SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
							long d_id = sp.getLong("driver_id", 0);
							populateDriverInfo(DBManager.getDBManager().getDriver(d_id));
							if (sp.getLong("vehicle_id", 0) != 0)
								enableVisualizationButtons();
						}
					});
					break;
				case R.id.main_choose_vehicle_button:
					cvp = new ChooseVehiclePopup(v, R.id.main_choose_center_view);
					cvp.setOnCompletionListener(new OnPopupWindowCompletionListener() {
						@Override
						public void onOperationCompletion() {
							SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
							long v_id = sp.getLong("vehicle_id", 0);
							populateVehicleInfo(DBManager.getDBManager().getVehicle(v_id));
							if (sp.getLong("driver_id", 0) != 0)
								enableVisualizationButtons();
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
		 * Set up the driver information after the user has chosen it
		 */
		private void populateDriverInfo(Driver d)	{
			
			try {
				// Update the fields
				TextViewBorder tv = (TextViewBorder)getActivity().findViewById(R.id.main_choose_first_name);
				tv.setText(d.getFirst_name());
				tv = (TextViewBorder)getActivity().findViewById(R.id.main_choose_last_name);
				tv.setText(d.getLast_name());
				tv = (TextViewBorder)getActivity().findViewById(R.id.main_choose_user_id);
				tv.setText(d.getPasscode());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * Set up the vehicle information after the user has chosen it
		 */
		private void populateVehicleInfo(Vehicle v)	{
			
			try {
				// Update the fields
				TextViewBorder tv = (TextViewBorder)getActivity().findViewById(R.id.main_choose_vehicle_brand);
				tv.setText(v.getBrand());
				tv = (TextViewBorder)getActivity().findViewById(R.id.main_choose_vehicle_model);
				tv.setText(v.getModel());
				tv = (TextViewBorder)getActivity().findViewById(R.id.main_choose_vehicle_year);
				tv.setText(Integer.toString(v.getYear()));
				tv = (TextViewBorder)getActivity().findViewById(R.id.main_choose_vehicle_plate);
				tv.setText(v.getPlate());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}