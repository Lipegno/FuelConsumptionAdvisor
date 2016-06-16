package com.fuel.advisor.ui.vehicle_management;

import java.util.List;

import com.fuel.advisor.R;
import com.fuel.advisor.storage.DBManager;
import com.vehicle.vehicle.Vehicle;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * The class responsible for listing the information about the vehicles which are
 * registered in the database
 * 
 * @author Tiago Camacho
 */
public class VehicleListing extends ListActivity	{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		try {
			super.onCreate(savedInstanceState);
			setTitle("Vehicle Listing");
			setListAdapter(new VehicleArrayAdapter(
					getApplicationContext(), 
					R.layout.vehicle_list_view, 
					DBManager.getDBManager().getVehicles()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * The class that populates a layout with three TextView objects and one ImageView.
	 * It receives a List of Vehicle objects, and sets up the corresponding fields accordingly
	 */
	private class VehicleArrayAdapter extends ArrayAdapter<Vehicle>	{

		private final List<Vehicle> list;
		
		public VehicleArrayAdapter(Context context, int textViewResourceId,
				List<Vehicle> objects) {
			super(context, textViewResourceId, objects);
			this.list = objects;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			View row = null;
			
			try {
				// Inflate the corresponding layout and retrieve TextView pointers
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.vehicle_list_view, parent, false);
				TextView plateValue = (TextView)row.findViewById(R.id.vehicle_list_view_plate);
				TextView brandInfo = (TextView)row.findViewById(R.id.vehicle_list_view_brand);
				TextView yearValue = (TextView)row.findViewById(R.id.vehicle_list_view_year);
				ImageView image = (ImageView)row.findViewById(R.id.vehicle_list_view_type);
				
				// Get the Vehicle information and determine which image to show (light or heavy vehicle)
				// Also, populate TextViews with the corresponding information
				Vehicle v = list.get(position);
				if (v.getType().equals("light"))
					image.setImageResource(R.drawable.icn_light_car);
				else
					image.setImageResource(R.drawable.icn_heavy_car);
				plateValue.setText(v.getPlate());
				brandInfo.setText(v.getBrand() + " " + v.getModel());
				yearValue.setText(Integer.toString(v.getYear()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return row;
		}
	}
}