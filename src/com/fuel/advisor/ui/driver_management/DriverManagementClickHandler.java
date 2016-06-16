/**
 * Sets up the interaction mechanism for the driver management
 * activity
 */
package com.fuel.advisor.ui.driver_management;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.fuel.advisor.R;
import com.fuel.advisor.ui.custom.ButtonClickHandler;
import com.fuel.advisor.ui.custom.popupwindow.AddDriverPopup;
import com.fuel.advisor.ui.custom.popupwindow.DriverRemovalPopup;

public class DriverManagementClickHandler extends ButtonClickHandler	{
	
	private static final int[] view_ids = new int[]{
		R.id.driver_management_add,
		R.id.driver_management_remove,
		R.id.driver_management_list
	};

	public DriverManagementClickHandler(Activity a) {
		
		super(a, view_ids);
	}

	@Override
	protected void handle(View v) {

		try {
			int v_id = v.getId();
			
			switch (v_id) {
			case R.id.driver_management_add:
				new AddDriverPopup(v, R.id.driver_management_center_view);
				break;
			case R.id.driver_management_remove:
				new DriverRemovalPopup(v, R.id.driver_management_center_view);
				break;
			case R.id.driver_management_list:
				getActivity().startActivity(new Intent(getActivity(), DriverListing.class));
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}