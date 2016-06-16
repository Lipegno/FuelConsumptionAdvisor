package com.fuel.advisor.ui.vehicle_info;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.fuel.advisor.R;
import com.fuel.advisor.processing.SensorDataProcessor;
import com.fuel.advisor.storage.DBManager;
import com.fuel.advisor.storage.TripDataStorage;
import com.fuel.advisor.ui.custom.TextViewBorder;

public final class VehicleInfoFeedback extends Activity {

	private static final String MODULE = "VehicleInfoFeedback";
	private static final boolean DEBUG = false;
	
	private final DecimalFormat df = new DecimalFormat("#.###");
	
	private Bundle msgContent = new Bundle();
	private TextViewBorder distance_value;
	private TextViewBorder speed_value;
	private TextViewBorder accl_value;
	private TextViewBorder grade_value;
	private TextViewBorder vsp_value;
	private TextViewBorder state;
	private TextView gps_sats;
	private TextView gps_fixed;
	private boolean finished = false;
	private boolean isGPSFixed = false;
	private boolean lastGPSStatus = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		try {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.vehicle_info);
			super.onCreate(savedInstanceState);
			
			// Get references to the layout elements
			distance_value = (TextViewBorder) findViewById(R.id.distance);
			speed_value = (TextViewBorder) findViewById(R.id.velocity);
			accl_value = (TextViewBorder) findViewById(R.id.acceleration);
			grade_value = (TextViewBorder) findViewById(R.id.grade);
			vsp_value = (TextViewBorder) findViewById(R.id.vsp);
			state = (TextViewBorder) findViewById(R.id.state);
			gps_sats = (TextView) findViewById(R.id.num_sat);
			gps_fixed = (TextView) findViewById(R.id.gps_status);
			
			// Register the broadcast receiver. Add the necessary filters
			IntentFilter filter = 
				new IntentFilter(getString(R.string.vehicle_info_broadcast));
			filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
			getApplicationContext().registerReceiver(new VehicleBroadcastReceiver(), filter);
			
			// Start the processing service that runs on the background and
			// show feedback toast message
			startService(new Intent(this, SensorDataProcessor.class));
			Toast t = Toast.makeText(this, "Started background services", Toast.LENGTH_LONG);
			t.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	/**
	 * Ignore back pressed key until we pressed the 
	 */
	public void onBackPressed() {
		
		if (finished)
			super.onBackPressed();
	}
	
	/**
	 * Listen for finish button click
	 * @param v - The view that has been pressed
	 */
	public void handleButtonClick(View v)	{
		
		try {
			finished = true;
			stopService(new Intent(this, TripDataStorage.class));
			stopService(new Intent(this, SensorDataProcessor.class));
			v.setEnabled(false);
			Toast t = Toast.makeText(this, "Background services stopped", Toast.LENGTH_LONG);
			t.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * The class responsible for receiving the broadcasts and updating the UI fields
	 * accordingly
	 */
	private class VehicleBroadcastReceiver extends BroadcastReceiver	{
		
		@Override
		public void onReceive(Context context, Intent intent) {

			// Update all UI values
			try {
				msgContent = intent.getExtras();
				if (DEBUG)
					Log.d(MODULE, "Time for message '" + msgContent.getLong("counter") + "' to travel: " +
							(System.currentTimeMillis() - msgContent.getLong("time")));
				grade_value.setText(df.format(msgContent.getDouble("grade")));
				speed_value.setText(df.format(msgContent.getDouble("linear_velocity")));
				vsp_value.setText(df.format(msgContent.getDouble("vsp")));
				state.setText(msgContent.getString("state"));
				gps_sats.setText(Integer.toString(msgContent.getInt("num_sats")));
				if ((isGPSFixed = msgContent.getBoolean("gps_fixed")) != lastGPSStatus)	{
					if (! isGPSFixed)	{
						gps_fixed.setText("NO FIX");
						gps_fixed.setTextColor(Color.RED);
						lastGPSStatus = false;
					}
					else	{
						gps_fixed.setText("FIXED");
						gps_fixed.setTextColor(Color.GREEN);
						lastGPSStatus = true;
					}
				}
				accl_value.invalidate();
				grade_value.invalidate();
				speed_value.invalidate();
				vsp_value.invalidate();
				state.invalidate();
				gps_sats.invalidate();
				gps_fixed.invalidate();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}