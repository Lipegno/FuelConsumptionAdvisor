package com.fuel.advisor.ui.custom.popupwindow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import com.fuel.advisor.R;
import com.fuel.advisor.storage.DBManager;
import com.fuel.advisor.storage.DBManager.VehicleAlreadyRegisteredException;
import com.fuel.advisor.ui.custom.CustomDialog;
import com.fuel.advisor.ui.custom.CustomDialog.DialogType;
import com.fuel.advisor.ui.vehicle_management.BrandSelection;
import com.vehicle.vehicle.Vehicle;
import com.vehicle.vehicle.VehicleInfo;

/**
 * Concrete class reponsible for collecting vehicle information and adding it
 * to the database. It sub-classes PopupWindowScreen abstract class implementing
 * all necessary methods for input read from the user
 * 
 * @author Tiago Camacho
 */
public class AddVehiclePopup extends PopupWindowScreen	{
	
	@SuppressWarnings("unused")
	private static final String MODULE = "AddVehiclePopup";
	private static final int MINIMUM_YEAR = 1900;
	private static final int MAXIMUM_YEAR = 2100;
	
	public AddVehiclePopup(View view, int centerLayoutID) {
		
		super(view, R.layout.vehicle_addition, centerLayoutID);
		try {
			((EditText)findPopupWidget(R.id.vehicle_addition_air_coef)).setText(
					Double.toString(VehicleInfo.DEFAULT_AIR_COEF_LIGHT));
			((EditText)findPopupWidget(R.id.vehicle_addition_roll_coef)).setText(
					Double.toString(VehicleInfo.DEFAULT_ROLL_COEF_LIGHT));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void setButtonsClickListeners() {

		// Set up a listener for the Brand EditText. We want to call a new Activity
		// when this widget get's selected.
		findPopupWidget(R.id.vehicle_addition_brand).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				getApplicationContext().startActivity(new Intent(getApplicationContext(), BrandSelection.class));
				final SharedPreferences sp = getApplicationContext().getSharedPreferences("brand", 0);
				sp.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
					@Override
					public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
							String key) {
						EditText ed = (EditText)findPopupWidget(R.id.vehicle_addition_brand);
						ed.setText(sp.getString("brand", ""));
						ed.invalidate();
					}
				});
			}
		});
		
		// Set up the minus year button
		findPopupWidget(R.id.vehicle_addition_date_button_minus).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				decreaseYear();
			}
		});
		
		// Set up the plus year button
		findPopupWidget(R.id.vehicle_addition_date_button_plus).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				increaseYear();
			}
		});
		
		// Set up the light type radio button
		final RadioButton rb_light = (RadioButton)findPopupWidget(R.id.vehicle_addition_type_light);
		rb_light.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					if (rb_light.isChecked())	{
						((EditText)findPopupWidget(R.id.vehicle_addition_air_coef)).setText(
								Double.toString(VehicleInfo.DEFAULT_AIR_COEF_LIGHT));
						((EditText)findPopupWidget(R.id.vehicle_addition_roll_coef)).setText(
								Double.toString(VehicleInfo.DEFAULT_ROLL_COEF_LIGHT));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		// Set up heavy type radio button
		final RadioButton rb_heavy = (RadioButton)findPopupWidget(R.id.vehicle_addition_type_heavy);
		rb_heavy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (rb_heavy.isChecked())	{
					((EditText)findPopupWidget(R.id.vehicle_addition_air_coef)).setText(
							Double.toString(VehicleInfo.DEFAULT_AIR_COEF_HEAVY));
					((EditText)findPopupWidget(R.id.vehicle_addition_roll_coef)).setText(
							Double.toString(VehicleInfo.DEFAULT_ROLL_COEF_HEAVY));
				}
			}
		});
		
		// Set up the confirm addition button
		findPopupWidget(R.id.vehicle_addition_confirm_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					insertVehicle();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		// Set up the cancel addition button
		findPopupWidget(R.id.vehicle_addition_cancel_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismissPopupWindow();
			}
		});
	}
	
	/**
	 * Decreases the year of production of the vehicle
	 */
	private void decreaseYear()	{
		
		try {
			EditText ed = (EditText)findPopupWidget(R.id.vehicle_addition_date);
			int year = new Integer(ed.getText().toString());
			if (year > MINIMUM_YEAR)
				ed.setText(Integer.toString(--year));
			ed.invalidate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Increase the year of production of the vehicle
	 */
	private void increaseYear()	{
		
		try {
			EditText ed = (EditText)findPopupWidget(R.id.vehicle_addition_date);
			int year = new Integer(ed.getText().toString());
			if (year < MAXIMUM_YEAR)
				ed.setText(Integer.toString(++year));
			ed.invalidate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Try Vehicle insertion into database
	 * @throws Exception
	 */
	private void insertVehicle() throws Exception	{
		
		// First retrieve input values
		String brand = ((EditText)findPopupWidget(R.id.vehicle_addition_brand)).getText().toString();
		String model = ((EditText)findPopupWidget(R.id.vehicle_addition_model)).getText().toString();
		String year = ((EditText)findPopupWidget(R.id.vehicle_addition_date)).getText().toString();
		String type = ((RadioButton)findPopupWidget(R.id.vehicle_addition_type_light)).isChecked() ?
				"light" : "heavy";
		String air_coef = ((EditText)findPopupWidget(R.id.vehicle_addition_air_coef)).getText().toString();
		String roll_coef = ((EditText)findPopupWidget(R.id.vehicle_addition_roll_coef)).getText().toString();
		String plate = ((EditText)findPopupWidget(R.id.vehicle_addition_plate_number)).getText().toString();
		
		// Ensure correctness of input data
		if (! isBrandValid(brand))	{
			CustomDialog.createDialog(getApplicationContext(), DialogType.ERROR).show(
					"Input Error",
					"Brand selected is not valid. Please choose from brand list");
			return;
		}
		if (! isModelValid(model))	{
			CustomDialog.createDialog(getApplicationContext(), DialogType.ERROR).show(
					"Input Error", 
					"Model is not valid. Ensure that you specify only alpha-numeric characters");
			return;
		}
		if (! isCoefValid(air_coef))	{
			CustomDialog.createDialog(getApplicationContext(), DialogType.ERROR).show(
					"Input Error",
					"Air coef is not valid");
			return;
		}
		if ( ! isCoefValid(roll_coef))	{
			CustomDialog.createDialog(getApplicationContext(), DialogType.ERROR).show(
					"Input Error", 
					"Roll coef is not valid");
			return;
		}
		if (! isYearValid(new Integer(year)))	{
			CustomDialog.createDialog(getApplicationContext(), DialogType.ERROR).show(
					"Input Error", 
					"Year is not valid. Specify a year between 1900 and 2100");
			return;
		}
		
		if (! isPlateValid(plate))	{
			CustomDialog.createDialog(getApplicationContext(), DialogType.ERROR).show(
					"Input Error", 
					"Plate number is not valid");
			return;
		}
		
		// Now try insertion into database
		Vehicle v = new Vehicle(brand, model, new Integer(year));
		v.setType(type);
		v.setAirCoef(new Double(air_coef));
		v.setRollCoef(new Double(roll_coef));
		v.setPlate(plate);
		try {
			DBManager.getDBManager().insertVehicle(v);
		} catch (VehicleAlreadyRegisteredException vare) {
			CustomDialog.createDialog(getApplicationContext(), DialogType.ERROR).show(
					"Insertion Error", 
					"Vehicle is already registered into database");
			return;
		}

		// Show success message and set dismiss listener
		CustomDialog cd = CustomDialog.createDialog(getApplicationContext(), DialogType.SUCCESS);
		cd.show("Insertion Successful", 
				"Inserted new vehicle with plate '" + v.getPlate() + "' into database");
		cd.setOnConfirmationListener(new CustomDialog.OnConfirmationListener() {
			@Override
			public void onConfirmation() {
				dismissPopupWindow();
			}
		});
	}
	
	private boolean isBrandValid(String brand)	{
		
		return DBManager.getDBManager().getVehicleBrands().contains(brand);
	}
	
	private boolean isModelValid(String model)	{
		
		return ! model.matches("~[a-zA-Z0-9]");
	}
	
	private boolean isYearValid(int year)	{
		
		return year >= 1900 && year <= 2100;
	}
	
	private boolean isCoefValid(String coef)	{
		
		if ((coef = coef.replaceAll("\\s", "")).equals(""))
			return false;
		double c = new Double(coef);
		
		return c >= 0 && c <= 1;
	}
	
	private boolean isPlateValid(String plate)	{
		
		return ! ((plate = plate.replaceAll("\\s", "")).equals(""));
	}
}