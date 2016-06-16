package com.fuel.advisor.ui.driver_management;

import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.fuel.advisor.R;
import com.fuel.advisor.storage.DBManager;
import com.vehicle.driver.Driver;

/**
 * The class that lists the information about the currently registered drivers in the database
 * 
 * @author Tiago Camacho
 */
public class DriverListing extends ListActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		try {
			// Fetch the ArrayList of Driver objects from the database. We are going to use
			// our own ArrayAdapter class so we can populate correctly the ListView interface
			super.onCreate(savedInstanceState);
			setTitle("Driver Listing");
			setListAdapter(new DriverArrayAdapter(
					getApplicationContext(), 
					R.layout.list_view, 
					DBManager.getDBManager().getDrivers()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Use this class to set up a custom adapter so that we can populate two TextViews.
	 * For now, the first indicates the full name of driver, and the second one indicates the
	 * current passcode that uniquely identifies the driver
	 */
	private class DriverArrayAdapter extends ArrayAdapter<Driver>	{

		private final List<Driver> list;
	
		public DriverArrayAdapter(Context context, int textViewResourceId, List<Driver> objects) {
			
			super(context, textViewResourceId, objects);
			this.list = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			View row = null;
			
			try {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.list_view, parent, false);
				TextView tv_first = (TextView)row.findViewById(R.id.list_view_first_field);
				TextView tv_second = (TextView)row.findViewById(R.id.list_view_second_field);
				Driver d = list.get(position);
				tv_first.setText(d.getFirst_name() + " " + d.getLast_name());
				tv_second.setText("Passcode: " + d.getPasscode());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return row;
		}
	}
}