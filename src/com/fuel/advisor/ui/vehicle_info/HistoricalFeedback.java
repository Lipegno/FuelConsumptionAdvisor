package com.fuel.advisor.ui.vehicle_info;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.fuel.advisor.R;

@SuppressWarnings("unused")
public class HistoricalFeedback extends Activity {
	public final int FINISH_TRIP = 1;
	public final String MODULE = "Historical feedback";
	public ImageView[] _images;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.historical_layout);
		ImageView bar1 = (ImageView) findViewById(R.id.imageBar1);
		ImageView bar2 = (ImageView) findViewById(R.id.imageBar2);
		ImageView bar3 = (ImageView) findViewById(R.id.imageBar3);
		ImageView bar4 = (ImageView) findViewById(R.id.imageBar4);
		ImageView bar5 = (ImageView) findViewById(R.id.imageBar5);
		ImageView bar6 = (ImageView) findViewById(R.id.imageBar6);
		ImageView bar7 = (ImageView) findViewById(R.id.imageBar7);
		ImageView bar8 = (ImageView) findViewById(R.id.imageBar8);
		ImageView bar9 = (ImageView) findViewById(R.id.imageBar9);
		ImageView bar10 = (ImageView) findViewById(R.id.imageBar10);
		ImageView bar11 = (ImageView) findViewById(R.id.imageBar11);
		ImageView bar12 = (ImageView) findViewById(R.id.imageBar12);
		ImageView bar13 = (ImageView) findViewById(R.id.imageBar13);
		ImageView bar14 = (ImageView) findViewById(R.id.imageBar14);
		ImageView bar15 = (ImageView) findViewById(R.id.imageBar15);
		
		bar1.setBackgroundResource(R.drawable.bar1);
		bar2.setBackgroundResource(R.drawable.bar2);
		bar3.setBackgroundResource(R.drawable.bar3);
		bar4.setBackgroundResource(R.drawable.bar4);
		bar5.setBackgroundResource(R.drawable.bar5);
		bar6.setBackgroundResource(R.drawable.bar6);
		bar7.setBackgroundResource(R.drawable.bar7);
		bar8.setBackgroundResource(R.drawable.bar8);
		bar9.setBackgroundResource(R.drawable.bar9);
		bar10.setBackgroundResource(R.drawable.bar10);
		bar11.setBackgroundResource(R.drawable.bar11);
		bar12.setBackgroundResource(R.drawable.bar12);
		bar13.setBackgroundResource(R.drawable.bar13);
		bar14.setBackgroundResource(R.drawable.bar14);
		bar15.setBackgroundResource(R.drawable.bar15);
		_images = new ImageView[]{bar1,bar2,bar3,bar4,bar5,bar6,bar7,bar8,bar9,bar10,bar11,bar12,bar13,bar14,bar15};
		
		for(int i=0;i<15;i++){
			_images[i].setVisibility(View.INVISIBLE);
		}
		updateBars(5);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		menu.add(0, FINISH_TRIP, Menu.NONE, "finish trip");
		return true;

	}
	/**
	 * Closes the options menu after a while
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Timer timing = new Timer();
		timing.schedule(new TimerTask() {

			@Override
			public void run() {
				closeOptionsMenu();
			}
		}, 10000);
		return super.onPrepareOptionsMenu(menu);
	}
	/**
	 * Return a number between 0 and 15 to describe the number of VSP bars to draw
	 * @param instantaneous_fuel
	 * @return
	 */
	public int calculateVSPBars(double instantaneous_fuel)	{
		
		return (int)Math.min(Math.abs(Math.floor(instantaneous_fuel)), 15.0);
	}
	
	public void cleanBars(){
		
		for(int i=0;i<15;i++){
			_images[i].setVisibility(View.INVISIBLE);
		}
	}
	
	public void updateBars(int barnum){
		cleanBars();
		for(int i=0;i<barnum;i++){
			_images[i].setVisibility(View.VISIBLE);
		}
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		Log.e(MODULE, "finish trip selected");
		Intent test = new Intent(this, HistoricalFeedbackMap.class);
		startActivity(test);
		return super.onOptionsItemSelected(item);
	}
}
