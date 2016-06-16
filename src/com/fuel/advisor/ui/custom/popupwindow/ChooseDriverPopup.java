package com.fuel.advisor.ui.custom.popupwindow;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.fuel.advisor.R;
import com.fuel.advisor.storage.DBManager;
import com.fuel.advisor.ui.custom.CustomDialog;
import com.fuel.advisor.ui.custom.CustomDialog.DialogType;
import com.vehicle.driver.Driver;

/**
 * Concrete class used to gather input from the user on the Vehicle object to use
 * for the trip about to start
 * 
 * @author Tiago Camacho
 */
public class ChooseDriverPopup extends PopupWindowScreen	{

	@SuppressWarnings("unused")
	private static final String MODULE = "ChooseDriverPopup";
	
	public ChooseDriverPopup(View view, int centerLayoutID) {

		super(view, R.layout.main_choose_driver, centerLayoutID);
	}
	
	@Override
	protected void setButtonsClickListeners() {
		
		// Sets up confirmation button
		Button b = (Button)findPopupWidget(R.id.main_choose_driver_confirm_button);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Driver d;
					if ((d = getDriver()) == null)
						return;
					setDriverTripInfo(d);
					dismissPopupWindow();
					if (completion_listener != null)
						completion_listener.onOperationCompletion();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		// Sets up cancelation button
		b = (Button)findPopupWidget(R.id.main_choose_driver_cancel_button);
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
	 * Searches for an existing driver within the database
	 * @return - The Driver object if found, null otherwise
	 */
	private Driver getDriver() throws Exception	{
		
		Driver d = null;
		String passcode = ((EditText)findPopupWidget(R.id.main_choose_driver_code)).getText().toString();
		passcode = passcode.replaceAll("\\s", "");
		
		// Invalid input
		if (passcode.equals("") || passcode.length() != 3)	{
			CustomDialog cd = CustomDialog.createDialog(getApplicationContext(), DialogType.ERROR);
			cd.show("Input Error", 
					"Invalid input. Indicate a 3-digit numerical passcode");
			return null;
		}
		
		// Try and fetch the Driver object from the database
		if ((d = DBManager.getDBManager().getDriver(passcode)) == null)	{
			CustomDialog cd = CustomDialog.createDialog(getApplicationContext(), DialogType.ERROR);
			cd.show("Search Error", 
					"Couldn't not find driver with passcode " + passcode);
			return null;
		}
		
		return d;
	}
	
	/**
	 * Sets up the driver trip information. Namely it saves the driver's ID
	 * in the default shrared preferences file, so that we can use this value
	 * to initiate a trip segment
	 * @param d - The driver object
	 */
	private void setDriverTripInfo(Driver d) throws Exception	{
		
		long d_id = 0;
		
		if ((d_id = DBManager.getDBManager().getDriverID(d)) <= 0)	{
			CustomDialog.createDialog(getApplicationContext(), DialogType.ERROR).show(
					"Search Error", 
					"Unable to set up driver. Can't find database ID");
			return;
		}
		SharedPreferences.Editor edit = 
			PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
		edit.putLong("driver_id", d_id);
		edit.commit();
	}
}