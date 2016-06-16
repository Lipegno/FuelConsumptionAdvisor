package com.fuel.advisor.ui.custom.popupwindow;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.fuel.advisor.R;
import com.fuel.advisor.storage.DBManager;
import com.fuel.advisor.ui.custom.CustomDialog;
import com.fuel.advisor.ui.custom.CustomDialog.DialogType;
import com.vehicle.vehicle.Vehicle;

public class ChooseVehiclePopup extends PopupWindowScreen	{

	public ChooseVehiclePopup(View view, int centerLayoutID) {
		
		super(view, R.layout.main_choose_vehicle, centerLayoutID);
	}
	
	@Override
	protected void setButtonsClickListeners() {

		// Set up confirmation button
		Button b = (Button)findPopupWidget(R.id.main_choose_vehicle_confirm_button);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Vehicle vec;
					if ((vec = getVehicle()) == null)
						return;
					setVehicleTripInfo(vec);
					dismissPopupWindow();
					if (completion_listener != null)
						completion_listener.onOperationCompletion();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		// Set up cancelation button
		b = (Button)findPopupWidget(R.id.main_choose_vehicle_cancel_button);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismissPopupWindow();
				if (cancelation_listener != null)
					cancelation_listener.onOperationCancelation();
			}
		});
	}
	
	/**
	 * Fetches the Vehicle object
	 * @return - The Vehicle object or null if not found
	 * @throws Exception
	 */
	private Vehicle getVehicle() throws Exception	{
		
		String plate = ((AutoCompleteTextView)findPopupWidget(
				R.id.main_choose_vehicle_choose_plate)).getText().toString();
		plate = plate.replaceAll("\\s", "");
		Vehicle v = null;
		
		// Ensure input correctness
		if (plate.equals(""))	{
			CustomDialog.createDialog(getApplicationContext(), DialogType.ERROR).show(
					"Input Error", 
					"Plate number is empty");
			return null;
		}
		
		// Fetch the actual object from the database
		if ((v = DBManager.getDBManager().getVehicle(plate)) == null)
			CustomDialog.createDialog(getApplicationContext(), DialogType.ERROR).show(
					"Search Error", 
					"Couldn't find vehicle with plate '" + plate + "' in database");
		
		return v;
	}

	/**
	 * Sets up the vehicle information regarding the current trip
	 * @throws Exception
	 */
	private void setVehicleTripInfo(Vehicle v) throws Exception	{
		
		long v_id = 0;
		
		if ((v_id = DBManager.getDBManager().getVehicleID(v)) <= 0)	{
			CustomDialog.createDialog(getApplicationContext(), DialogType.ERROR).show(
					"Search Error",
					"Unable to set vehicle. Can't find database ID");
			return;
		}
		SharedPreferences.Editor edit = 
			PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
		edit.putLong("vehicle_id", v_id);
		edit.commit();
	}
}