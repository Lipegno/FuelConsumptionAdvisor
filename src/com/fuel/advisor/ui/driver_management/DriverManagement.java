/**
 * The activity for setting up driver specific details
 */
package com.fuel.advisor.ui.driver_management;

import com.fuel.advisor.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class DriverManagement extends Activity	{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		try {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.driver_management);
			super.onCreate(savedInstanceState);
			new DriverManagementClickHandler(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onDestroy() {
		
		try {
			super.onDestroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}