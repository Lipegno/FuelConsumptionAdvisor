package com.fuel.advisor.ui.calibrate;

import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fuel.advisor.R;
import com.fuel.advisor.acquisition.listener.AccelerometerServiceListener;
import com.fuel.advisor.acquisition.listener.MagnetometerServiceListener;
import com.fuel.advisor.ui.custom.CustomDialog;
import com.fuel.advisor.ui.custom.CustomDialog.DialogType;

/**
 * The Activity used to perform the driver/orientation calibration of the device. With this, the
 * driver can choose the desired orientation inside the vehicle
 * 
 * @author Tiago Camacho
 */
@SuppressWarnings("unused")
public class CalibrationDriver extends Activity	implements Observer	{

	private static final String MODULE = "CalibrationDriver";
	
	private final DecimalFormat df = new DecimalFormat("#.##");
	
	private double[] acc = new double[3];
	private double[] acc_offset = new double[3];
	private float[] acc_float = new float[3];
	private double pitch;
	private double roll;
	private AccelerometerServiceListener asl;
	private MagnetometerServiceListener msl;
	private CountDownTimer cdt;
	private int totalTime;
	private ProgressBar bar;
	private boolean started = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		try {
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.calibrate_driver);
			totalTime = new Integer(getString(R.string.driver_calib_time));
			bar = (ProgressBar)findViewById(R.id.driver_calib_progress);
			CustomDialog.createDialog(this, DialogType.INFO).show(
					"Orientation Calibration", 
					"Place the phone according to desired orientation. When ready, press 'Start' and " +
					"wait for " + totalTime + " seconds");
			loadPreferences();
			asl = new AccelerometerServiceListener(getApplicationContext(), null, this);
			msl = new MagnetometerServiceListener(getApplicationContext(), null, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onDestroy() {
		
		try {
			if (started)
				cancelCalibration();
			else	{
				asl.stop();
				msl.stop();
				asl.deleteObserver(this);
				msl.deleteObserver(this);
			}
			super.onDestroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void update(Observable observable, Object data) {

		try {
			// Get the actual data
			if (observable instanceof AccelerometerServiceListener)	{
				acc = (double[])data;
				acc_float[0] = (float) (acc[0] + acc_offset[0]);
				acc_float[1] = (float) (acc[1] + acc_offset[1]);
				acc_float[2] = (float) (acc[2] + acc_offset[2]);
				pitch = Math.toDegrees(Math.atan2(acc_float[1], acc_float[2]));
				roll = Math.toDegrees(Math.atan2(acc_float[0], acc_float[2]));
				updateLayoutValues();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Handles the button clicks for this activity
	 * @param v - The View object that has been pressed
	 */
	public void handleButtonClick(View v)	{
		
		try {
			switch (v.getId()) {
			case com.fuel.advisor.R.id.start_calib_driver_button:
				startCalibration();
				break;
			case com.fuel.advisor.R.id.cancel_calib_driver_button:
				cancelCalibration();
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Update the layout values
	 * @throws Exception
	 */
	private void updateLayoutValues() throws Exception	{
		
		TextView tv = null;
		
		tv = (TextView)findViewById(R.id.driver_calib_pitch);
		tv.setText(df.format(pitch));
		tv.invalidate();
		tv = (TextView)findViewById(R.id.driver_calib_roll);
		tv.setText(df.format(roll));
		tv.invalidate();
	}
	
	/**
	 * Initiate the calibration process
	 * @throws Exception
	 */
	private void startCalibration() throws Exception	{
		
		asl.start();
		msl.start();
		initCountdownTimer();
		cdt.start();
		started = true;
		findViewById(com.fuel.advisor.R.id.start_calib_driver_button).setEnabled(false);
		findViewById(com.fuel.advisor.R.id.cancel_calib_driver_button).setEnabled(true);
		((TextView)findViewById(R.id.driver_calib_pitch_text)).setText("Pitch: ");
		((TextView)findViewById(R.id.driver_calib_roll_text)).setText("Roll: ");
	}
	
	/**
	 * Cancel the calibration
	 * @throws Exception
	 */
	private void cancelCalibration() throws Exception	{
		
		// Halt the timer and services
		started = false;
		cdt.cancel();
		asl.stop();
		msl.stop();
		
		// Update fields and show notification
		((TextView)findViewById(R.id.driver_calib_pitch)).setText("0.00");
		((TextView)findViewById(R.id.driver_calib_roll)).setText("0.00");
		findViewById(com.fuel.advisor.R.id.start_calib_driver_button).setEnabled(true);
		findViewById(com.fuel.advisor.R.id.cancel_calib_driver_button).setEnabled(false);
		Toast t = Toast.makeText(getApplicationContext(),
				"Calibration process canceled", 
				Toast.LENGTH_LONG);
		t.show();
	}
	
	/**
	 * Finish up the calibration
	 * @throws Exception
	 */
	private void finishCalibration() throws Exception	{
		
		// Run finalization steps
		TextView tv = null;
		bar.setProgress(bar.getMax());
		started = false;
		asl.stop();
		msl.stop();
		tv = (TextView)findViewById(R.id.driver_calib_pitch);
		tv.setText(df.format(pitch));
		tv.setTextColor(Color.RED);
		tv = (TextView)findViewById(R.id.driver_calib_roll);
		tv.setText(df.format(roll));
		tv.setTextColor(Color.RED);
		Toast t = Toast.makeText(getApplicationContext(),
				"Calibration process finished",
				Toast.LENGTH_LONG);
		t.show();
		findViewById(com.fuel.advisor.R.id.cancel_calib_driver_button).setEnabled(false);		
		storePreferences();
	}
	
	/**
	 * Load the stored preferences
	 * @throws Exception
	 */
	private void loadPreferences() throws Exception	{
		
		TextView tv = null;
		
		// Get both phone and driver calibration shared preferences
		SharedPreferences prefs = getSharedPreferences(
				getString(com.fuel.advisor.R.string.phone_calib_file),
				0);
		acc_offset[0] = (double)prefs.getFloat("a_off_x", 0);
		acc_offset[1] = (double)prefs.getFloat("a_off_y", 0);
		acc_offset[2] = (double)prefs.getFloat("a_off_z", 0);
		prefs = getSharedPreferences(
				getString(com.fuel.advisor.R.string.driver_calib_file),
				0);
		pitch = (double)prefs.getFloat("pitch_off", 0);
		roll = (double)prefs.getFloat("roll_off", 0);
		
		// Update the previous values for the driver calibration
		tv = (TextView)findViewById(R.id.driver_calib_pitch);
		tv.setText(df.format(pitch));
		tv = (TextView)findViewById(R.id.driver_calib_roll);
		tv.setText(df.format(roll));
	}
	
	/**
	 * Store the driver calibration values
	 * @throws Exception
	 */
	private void storePreferences() throws Exception	{
		
		SharedPreferences.Editor edit = getSharedPreferences(
				getString(com.fuel.advisor.R.string.driver_calib_file),
				0).edit();
		edit.putFloat("pitch_off", (float) pitch);
		edit.putFloat("roll_off", (float) roll);
		edit.commit();
	}
	
	/**
	 * Initialize the count down timer
	 */
	private void initCountdownTimer()	{
		
		cdt = new CountDownTimer(
				(new Integer(getString(com.fuel.advisor.R.string.driver_calib_time)) + 1) * 1000,
				1000) {
			
			@Override
			public void onTick(long millisUntilFinished) {

				try {
					// Update the progress bar
					int progress = (int)(totalTime - (millisUntilFinished / 1000));
					bar.setProgress(progress);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFinish() {
				
				try {
					finishCalibration();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}
}