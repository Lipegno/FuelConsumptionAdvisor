package com.fuel.advisor.ui.custom.popupwindow;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.fuel.advisor.R;
import com.fuel.advisor.storage.DBManager;
import com.fuel.advisor.storage.DBManager.DriverNotRegisteredException;
import com.fuel.advisor.ui.custom.CustomDialog;
import com.fuel.advisor.ui.custom.CustomDialog.DialogType;

/**
 * The PopupWindow responsible for collecting input for removing a driver from
 * the database
 * 
 * @author Tiago Camacho
 */
public class DriverRemovalPopup extends PopupWindowScreen	{

	public DriverRemovalPopup(View view, int centerLayoutID) {

		super(view, R.layout.driver_removal, centerLayoutID);
	}
	
	@Override
	protected void setButtonsClickListeners() {
		
		// Set up the confirmation button
		Button b = (Button)findPopupWidget(R.id.driver_removal_confirm_button);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					removeDriver();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		// Set up the cancelation button
		b = (Button)findPopupWidget(R.id.driver_removal_cancel_button);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismissPopupWindow();
			}
		});
	}
	
	/**
	 * Try driver removal from the database
	 * @throws Exception
	 */
	private void removeDriver() throws Exception	{
		
		DBManager db_man = DBManager.getDBManager();
		String passcode = ((EditText)findPopupWidget(R.id.driver_removal_code)).getText().toString();
		passcode = passcode.replaceAll("\\s", "");
		
		// Invalid input
		if (passcode.equals("") || passcode.length() != 3)	{
			CustomDialog cd = CustomDialog.createDialog(getApplicationContext(), DialogType.ERROR);
			cd.show(
					"Input Error",
					"Invalid input. Indicate a 3-digit numerical passcode");
			return;
		}

		// Try driver removal. Catch specific exception that indicates that the driver
		// is not in the database
		try {
			db_man.removeDriver(passcode);
			CustomDialog cd = CustomDialog.createDialog(getApplicationContext(), DialogType.SUCCESS);
			cd.show(
					"Removal Successful", 
					"Driver with passcode " + passcode + " succesfully removed from database");
			cd.setOnConfirmationListener(new CustomDialog.OnConfirmationListener() {
				@Override
				public void onConfirmation() {
					dismissPopupWindow();
				}
			});
		} catch (DriverNotRegisteredException dnre) {
			CustomDialog cd = CustomDialog.createDialog(getApplicationContext(), DialogType.ERROR);
			cd.show(
					"Search Error", 
					"Couldn't find driver with passcode " + passcode + " in database");
		}
	}
}