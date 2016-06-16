package com.fuel.advisor.ui.main;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.fuel.advisor.R;

/**
 * The acitivity that sets up the main option for the application
 * 
 * @author Tiago Camacho
 */
public class MainOptions extends PreferenceActivity	{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		try	{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.main_preferences);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}