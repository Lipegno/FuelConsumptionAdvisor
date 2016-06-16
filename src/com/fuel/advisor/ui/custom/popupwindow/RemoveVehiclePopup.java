package com.fuel.advisor.ui.custom.popupwindow;

import java.util.ArrayList;
import java.util.Iterator;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.fuel.advisor.R;
import com.fuel.advisor.storage.DBManager;
import com.fuel.advisor.storage.DBManager.VehicleNotRegisteredException;
import com.fuel.advisor.ui.custom.CustomDialog;
import com.fuel.advisor.ui.custom.CustomDialog.DialogType;
import com.vehicle.vehicle.Vehicle;

/**
 * The class responsible for the removal of existing vehicle objects
 * 
 * @author Tiago Camacho
 */
public class RemoveVehiclePopup extends PopupWindowScreen	{

	@SuppressWarnings("unused")
	private static final String MODULE = "RemoveVehiclePopup";
	
	public RemoveVehiclePopup(View view, int centerLayoutID) {

		super(view, R.layout.vehicle_removal, centerLayoutID);
	}
	
	@Override
	protected void setButtonsClickListeners() {
		
//		initAutocompleteView();
		
		// Sets up the removal confirmation button
		Button b = (Button)findPopupWidget(R.id.vehicle_removal_confirm_button);
		if (b != null)	{
			b.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						removeVehicle();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		
		// Sets up the removal cancelation button
		b = (Button)findPopupWidget(R.id.vehicle_removal_cancel_button);
		if (b != null)	{
			b.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismissPopupWindow();
				}
			});
		}
	}
	
	/**
	 * Sets up the auto complete view so that suggestion are provided to the
	 * user, in this case for plate number insertion
	 */
	private void initAutocompleteView()	{
		
		try {
			// Fetch all possible plate values from database and set up the ArrayAdapter to feed
			// the autocompletion mechanism
			AutoCompleteTextView plateValue = (AutoCompleteTextView)findPopupWidget(R.id.vehicle_removal_plate);
			ArrayList<String> plates = new ArrayList<String>();
			ArrayList<Vehicle> list = DBManager.getDBManager().getVehicles();
			Iterator<Vehicle> i = list.iterator();
			while (i.hasNext())
				plates.add(i.next().getPlate());
			plateValue.setAdapter(new ArrayAdapter<String>(
					getApplicationContext(),
					R.layout.list_item, 
					plates));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Verifies correctness of input and consequently removes vehicle information
	 * from the database
	 * @throws Exception
	 */
	private void removeVehicle() throws Exception	{
		
		String plate = ((EditText)findPopupWidget(R.id.vehicle_removal_plate)).getText().toString();
		plate = plate.replaceAll("\\s", "");
		if (plate.equals(""))	{
			CustomDialog.createDialog(getApplicationContext(), DialogType.ERROR).show(
					"Input Error", 
					"Plate value is empty");
			return;
		}
		
		// Search for Vehicle
		Vehicle v = DBManager.getDBManager().getVehicle(plate);
		if (v == null)	{
			CustomDialog.createDialog(getApplicationContext(), DialogType.ERROR).show(
					"Search Error", 
					"Cannot find vehicle with plate '" + plate + "' in database");
			return;
		}
		
		// Try removal
		try {
			DBManager.getDBManager().removeVehicle(v);
		} catch (VehicleNotRegisteredException vnre) {
			CustomDialog.createDialog(getApplicationContext(), DialogType.ERROR).show(
					"Removal Error", 
					"Vehicle with plate '" + plate + "' not registered in the database");
			return;
		}
		// Show success message and set dismiss listener
		CustomDialog cd = CustomDialog.createDialog(getApplicationContext(), DialogType.SUCCESS);
		cd.show("Removal Success",
				"Vehicle with plate number '" + plate + "' successfuly removed from database");
		cd.setOnConfirmationListener(new CustomDialog.OnConfirmationListener() {
			@Override
			public void onConfirmation() {
				dismissPopupWindow();
			}
		});
	}
}