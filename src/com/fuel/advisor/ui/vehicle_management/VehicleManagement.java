/**
 * The activity for setting up vehicle data
 */
package com.fuel.advisor.ui.vehicle_management;

import com.fuel.advisor.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class VehicleManagement extends Activity	{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		try {
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.vehicle_management);
			new VehicleManagementClickHandler(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}
