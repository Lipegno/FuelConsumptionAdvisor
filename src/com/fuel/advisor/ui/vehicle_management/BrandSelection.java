package com.fuel.advisor.ui.vehicle_management;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.fuel.advisor.R;
import com.fuel.advisor.storage.DBManager;

public class BrandSelection extends ListActivity	{
	
	@SuppressWarnings("unused")
	private static final String MODULE = "BrandSelection";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		try {
			super.onCreate(savedInstanceState);
			setTitle("Brand selection");
			setListAdapter(new ArrayAdapter<String>(
					getApplicationContext(), 
					R.layout.list_view, 
					R.id.list_view_first_field, 
					DBManager.getDBManager().getVehicleBrands()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		// Upon first selection, populate the EditText field and exit listing activity
		try {
			String selectedBrand = l.getItemAtPosition(position).toString();
			SharedPreferences.Editor editor = getSharedPreferences("brand", 0).edit();
			editor.putString("brand", selectedBrand);
			editor.commit();
			finish();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}