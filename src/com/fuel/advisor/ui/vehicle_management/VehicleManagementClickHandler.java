package com.fuel.advisor.ui.vehicle_management;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.fuel.advisor.R;
import com.fuel.advisor.ui.custom.ButtonClickHandler;
import com.fuel.advisor.ui.custom.popupwindow.AddVehiclePopup;
import com.fuel.advisor.ui.custom.popupwindow.RemoveVehiclePopup;

public class VehicleManagementClickHandler extends ButtonClickHandler	{

	@SuppressWarnings("unused")
	private static final String MODULE = "VehicleManagementClickHandler";
	private static final int[] view_ids = new int[]{
		R.id.vehicle_management_add,
		R.id.vehicle_management_list,
		R.id.vehicle_management_remove,
		R.id.vehicle_management_vsp
	};
	
	public VehicleManagementClickHandler(Activity a) {
		
		super(a, view_ids);
	}
	
	@Override
	protected void handle(View v) {
		
		try {
			int v_id = v.getId();
			
			switch (v_id) {
			case R.id.vehicle_management_add:
				new AddVehiclePopup(v, R.id.vehicle_management_center_view);
				break;
			case R.id.vehicle_management_remove:
				new RemoveVehiclePopup(v, R.id.vehicle_management_center_view);
				break;
			case R.id.vehicle_management_list:
				getActivity().startActivity(new Intent(getActivity(), VehicleListing.class));
				break;
			case R.id.vehicle_management_vsp:
				getActivity().startActivity(new Intent(getActivity(), VehicleVSPCalibration.class));
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}