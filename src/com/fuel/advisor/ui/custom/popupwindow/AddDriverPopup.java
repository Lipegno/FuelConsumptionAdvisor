package com.fuel.advisor.ui.custom.popupwindow;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.fuel.advisor.R;
import com.fuel.advisor.storage.DBManager;
import com.fuel.advisor.storage.DBManager.DriverAlreadyRegisteredException;
import com.fuel.advisor.ui.custom.CustomDialog;
import com.fuel.advisor.ui.custom.CustomDialog.DialogType;
import com.vehicle.driver.Driver;

public class AddDriverPopup extends PopupWindowScreen	{

	@SuppressWarnings("unused")
	private static final String MODULE = "AddDriverPopup";
	
	public AddDriverPopup(View view, int centerLayoutID) {

		super(view, R.layout.driver_addition, centerLayoutID);
	}
	
	@Override
	protected void setButtonsClickListeners() {

		// Set up confirmation button
		Button b = (Button)findPopupWidget(R.id.driver_addition_confirm_button);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					insertDriver();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		// Set up cancelation button
		b = (Button)findPopupWidget(R.id.driver_addition_cancel_button);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismissPopupWindow();
			}
		});
	}
	
	/**
	 * Inserts a new driver into the database
	 */
	private void insertDriver()	throws Exception	{
		
		String first_name = 
			((EditText)findPopupWidget(R.id.driver_addition_first_name)).getText().toString();
		String last_name = 
			((EditText)findPopupWidget(R.id.driver_addition_last_name)).getText().toString();
		first_name = first_name.replaceAll("\\s", "");
		last_name = last_name.replaceAll("\\s", "");
		
		// Ensure that we have valid input
		if (! isUserInputValid(first_name, last_name))	{
			CustomDialog cd = CustomDialog.createDialog(getApplicationContext(), DialogType.ERROR);
			cd.show("Invalid Input",
					"Must insert both first and last names.");
			return;
		}
		
		// Let's try driver insertion into database. Ensure we dismiss everything after
		// sucessful insertion and dismiss of the CustomDialog
		try {
			String passcode = DBManager.getDBManager().insertDriver(new Driver(first_name, last_name));
			CustomDialog cd = CustomDialog.createDialog(getApplicationContext(), DialogType.SUCCESS);
			cd.show("Addition Successful",
					"Driver successfuy added to database. Passcode is " + passcode);
			cd.setOnConfirmationListener(new CustomDialog.OnConfirmationListener() {
				@Override
				public void onConfirmation() {
					dismissPopupWindow();
				}
			});
		} catch (DriverAlreadyRegisteredException dare) {
			CustomDialog cd = CustomDialog.createDialog(getApplicationContext(), DialogType.ERROR);
			cd.show("Addition Failure",
					"Failure to add driver to database. Driver already registered");
		}
	}
	
	/**
	 * Validates the user input
	 * @return - True if valid, false otherwise
	 */
	private boolean isUserInputValid(String first_name, String last_name)	{
		
		return ! (first_name.equals("") || last_name.equals(""));
	}
}