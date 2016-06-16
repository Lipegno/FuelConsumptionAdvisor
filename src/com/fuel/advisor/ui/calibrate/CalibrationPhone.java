package com.fuel.advisor.ui.calibrate;

import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fuel.advisor.R;
import com.fuel.advisor.acquisition.listener.AccelerometerServiceListener;
import com.fuel.advisor.ui.custom.CustomDialog;
import com.fuel.advisor.ui.custom.CustomDialog.DialogType;

/**
 * The Activity responsible for calibrating the device's accelerometer
 * 
 * @author Tiago Camacho
 */
@SuppressWarnings("unused")
public class CalibrationPhone extends Activity implements Observer	{

	private static final String MODULE = "CalibrationPhone";
	// Define the neutral values that are expected when the phone is
	// laying flat on the table
	private static final double ACC_X_AXIS_NEUTRAL = 0.0;
	private static final double ACC_Y_AXIS_NEUTRAL = 0.0;
	private static final double ACC_Z_AXIS_NEUTRAL = 9.807;
	
	private final DecimalFormat df = new DecimalFormat("#.##");
	
	private int totalTime;
	private Handler handler;
	private double last_acc[] = new double[3];
	private double acc[] = new double[3];
	private double a_off_x;
	private double a_off_y;
	private double a_off_z;
	private boolean update = false;
	private ProgressBar bar;
	private CountDownTimer cdt;
	private AccelerometerServiceListener asl;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		try {
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.calibrate_phone);
			totalTime = new Integer(getString(R.string.phone_calib_time));
			bar = (ProgressBar) findViewById(R.id.phone_calib_progress);
			CustomDialog.createDialog(this, DialogType.INFO).show(
					"Phone Calibration", 
					"Lay down the phone on a table in a straight portrait orientation. " +
					"Ideally, the phone should be agaisnt a wall, so that the calibration is sucessful. After " +
					"ready press 'Start' and wait for " + totalTime + " seconds.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onDestroy() {

		try {
			if (update)
				cancelCalibration();
			super.onDestroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void update(Observable observable, Object data) {

		// When there is accelerometer movement, we want to reset
		// the counter to its original value. Start updating values
		// only when the update variable is set
		if (update)	{
			acc = (double[]) data;
			if (last_acc[0] == acc[0] && last_acc[1] == acc[1] && last_acc[2] == acc[2])
				return;
			TextView acc_x = (TextView)findViewById(R.id.phone_calib_acc_x);
			TextView acc_y = (TextView)findViewById(R.id.phone_calib_acc_y);
			TextView acc_z = (TextView)findViewById(R.id.phone_calib_acc_z);
			acc_x.setText(df.format(acc[0]));
			acc_y.setText(df.format(acc[1]));
			acc_z.setText(df.format(acc[2]));
			acc_x.invalidate();
			acc_y.invalidate();
			acc_z.invalidate();
			if (cdt != null)	{
				cdt.cancel();
				cdt.start();
				bar.clearAnimation();
			}
			for (int i = 0; i < acc.length; i++)
				last_acc[i] = acc[i];
		}
	}
	
	/**
	 * Handle the button click
	 * @param v - The view which has been selected
	 */
	public void handleButtonClick(View v)	{
		
		try {
			switch (v.getId()) {
			case R.id.start_calib_phone_button:
				v.setEnabled(false);
				findViewById(R.id.cancel_calib_phone_button).setEnabled(true);
				findViewById(R.id.phone_calibrate_layout).invalidate();
				startCalibration();
				break;
			case R.id.cancel_calib_phone_button:
				v.setEnabled(false);
				findViewById(R.id.start_calib_phone_button).setEnabled(true);
				findViewById(R.id.phone_calibrate_layout).invalidate();
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
	 * Starts the calibration process
	 * @throws Exception
	 */
	private void startCalibration() throws Exception	{
		
		asl = new AccelerometerServiceListener(getApplicationContext(), null, this);
		findViewById(R.id.phone_calib_progress).setVisibility(View.VISIBLE);
		update = true;
		asl.start();
		initCountDownTimer();
	}
	
	/**
	 * Cancel the calibration process
	 * @throws Exception
	 */
	private void cancelCalibration() throws Exception	{
		
		asl.stop();
		cdt.cancel();
		update = false;
		((TextView)findViewById(R.id.phone_calib_acc_x)).setText("0.00");
		((TextView)findViewById(R.id.phone_calib_acc_y)).setText("0.00");
		((TextView)findViewById(R.id.phone_calib_acc_z)).setText("0.00");
		findViewById(R.id.phone_calibrate_layout).invalidate();
		Toast t = Toast.makeText(getApplicationContext(),
				"Canceled Phone Calibration Process",
				Toast.LENGTH_LONG);
		t.show();
	}
	
	/**
	 * Stores the preferences
	 * @throws Exception
	 */
	private void storePreferences() throws Exception	{
		
		SharedPreferences.Editor edit = getSharedPreferences(
				getString(R.string.phone_calib_file), 0).edit();
		edit.putFloat("a_off_x", (float) this.a_off_x);
		edit.putFloat("a_off_y", (float) this.a_off_y);
		edit.putFloat("a_off_z", (float) this.a_off_z);
		edit.commit();
	}
	
	/**
	 * Finalize the calibration by setting up the offset values and
	 * storing them into the SharedPreferences file
	 */
	private void finishCalibration()	{
		
		try {
			// Stop the accelerometer service and update layout
			bar.setProgress(bar.getMax());
			update = false;
			asl.stop();
			this.a_off_x = ACC_X_AXIS_NEUTRAL - acc[0];
			this.a_off_y = ACC_Y_AXIS_NEUTRAL - acc[1];
			this.a_off_z = ACC_Z_AXIS_NEUTRAL - acc[2];
			TextView tv_aof_x = (TextView)findViewById(R.id.phone_calib_acc_x);
			TextView tv_aof_y = (TextView)findViewById(R.id.phone_calib_acc_x);
			TextView tv_aof_z = (TextView)findViewById(R.id.phone_calib_acc_z);
			tv_aof_x.setText(df.format(this.a_off_x));
			tv_aof_y.setText(df.format(this.a_off_y));
			tv_aof_z.setText(df.format(this.a_off_z));
			tv_aof_x.setTextColor(Color.RED);
			tv_aof_y.setTextColor(Color.RED);
			tv_aof_z.setTextColor(Color.RED);
			
			// Show notification and store preferences
			Toast t = Toast.makeText(getApplicationContext(),
					"Calibration process finished",
					Toast.LENGTH_LONG);
			t.show();
			findViewById(R.id.cancel_calib_phone_button).setEnabled(false);
			findViewById(R.id.phone_calibrate_layout).invalidate();
			storePreferences();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Initialize the count-down timer
	 */
	private void initCountDownTimer()	{
		
		cdt = new CountDownTimer(
				(totalTime + 1) * 1000,
				1000) {
			
			@Override
			public void onTick(long millisUntilFinished) {
				
				try {
					// Update progress bar
					int progress = (int)(totalTime - (millisUntilFinished / 1000));
					Log.d(MODULE, "Progress: " + progress);
					bar.setProgress(progress);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFinish() {

				try {
					// Set the finalization text and call last calibration method
					finishCalibration();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}
}
