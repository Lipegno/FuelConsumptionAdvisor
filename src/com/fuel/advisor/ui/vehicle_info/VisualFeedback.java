package com.fuel.advisor.ui.vehicle_info;

import java.text.DecimalFormat;
import java.util.Vector;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.fuel.advisor.R;
import com.fuel.advisor.processing.SensorDataProcessor;
import com.fuel.advisor.ui.vehicle_info.openGLStuff.OpenGLRenderer;
import com.fuel.advisor.ui.vehicle_info.openGLStuff.SimplePlane;

public class VisualFeedback extends Activity {
	
	private static final String MODULE = "VisualFeedback";
	private final DecimalFormat df = new DecimalFormat("#.###");
	
	private TextView accValue;
	private TextView speedValue;
	private TextView gradeValue;
	//private TextView stateValue;
	private double VSP;
	private boolean finished; 
	
	private OpenGLRenderer _velGaugeView;
	private OpenGLRenderer _accGaugeView;
	private OpenGLRenderer _barsView;
	private OpenGLRenderer _gradeView;

	private GLSurfaceView _velGaugeSView;
	private GLSurfaceView _accGaugeSView;
	private GLSurfaceView _barsSView;
	private GLSurfaceView _gradeSView;

	private Vector<SimplePlane> _planes 	   = new Vector<SimplePlane>();	
	private Vector<SimplePlane> _velGaugeComp  = new Vector<SimplePlane>();  
	private Vector<SimplePlane> _accGaugeComp  = new Vector<SimplePlane>();
	private SimplePlane _grade;

	@Override 
	protected void onCreate(Bundle savedInstanceState) {

		try {
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.visual_feedback);		

			accValue   = (TextView) findViewById(R.id.acceleration_feedback);
			speedValue = (TextView) findViewById(R.id.velocity_feeback);
			gradeValue = (TextView) findViewById(R.id.grade_feedback); 
			//stateValue = (TextView) findViewById(R.id.state);
			
			_barsView	  = new OpenGLRenderer();			
			_velGaugeView = new OpenGLRenderer();			
			_accGaugeView = new OpenGLRenderer();			
			_gradeView    = new OpenGLRenderer();
			
			_barsView._zoom=-4;
			_velGaugeView._zoom=-4;
			_accGaugeView._zoom=-4;
			_gradeView._zoom=-4;
			//bar animation stuff
			SimplePlane backgroundVSP = new SimplePlane(8f,6f);
			float offset = 0.156f;
			SimplePlane plane = new SimplePlane(0.145f, 1f);
			plane.x=-1.1f; 
			plane.z=2.3f; // brings plane (the matrix closer do the screen)
			SimplePlane plane2 = new SimplePlane(0.145f, 1f);
			plane2.x=offset;
			SimplePlane plane3 = new SimplePlane(0.145f, 1f);
			plane3.x= offset;
			SimplePlane plane4 = new SimplePlane(0.145f, 1f);
			plane4.x =offset;
			SimplePlane plane5 = new SimplePlane(0.145f, 1f);
			plane5.x=offset;
			SimplePlane plane6 = new SimplePlane(0.145f, 1f);
			plane6.x= offset;		
			SimplePlane plane7 = new SimplePlane(0.145f, 1f);
			plane7.x =offset;		
			SimplePlane plane8 = new SimplePlane(0.145f, 1f);
			plane8.x=offset;
			SimplePlane plane9 = new SimplePlane(0.145f, 1f);
			plane9.x= offset;
			SimplePlane plane10= new SimplePlane(0.145f,1f);
			plane10.x=offset;
			SimplePlane plane11= new SimplePlane(0.145f,1f);
			plane11.x=offset;
			SimplePlane plane12= new SimplePlane(0.145f,1f);
			plane12.x=offset;  
			SimplePlane plane13= new SimplePlane(0.145f,1f);
			plane13.x=offset;
			SimplePlane plane14 = new SimplePlane(0.145f,1f);
			plane14.x=offset;
			SimplePlane plane15 = new SimplePlane(0.145f,1f);
			plane15.x=offset;
			
			boolean colored=true;
			backgroundVSP.loadBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.background_vsp));
			if(colored){
				plane.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar1));
				plane2.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar2)); 
				plane3.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar3));
				plane4.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar4));
				plane5.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar5));
				plane6.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar6));
				plane7.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar7));
				plane8.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar8));
				plane9.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar9));
				plane10.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar10));
				plane11.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar11));
				plane12.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar12));
				plane13.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar13));
				plane14.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar14));
				plane15.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar15));
			}
			else{
				plane.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar_mon_1));
				plane2.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar_mon_2)); 
				plane3.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar_mon_3));
				plane4.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar_mon_4));
				plane5.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar_mon_5));
				plane6.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar_mon_6));
				plane7.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar_mon_7));
				plane8.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar_mon_8));
				plane9.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar_mon_9));
				plane10.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar_mon_10));
				plane11.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar_mon_11));
				plane12.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar_mon_12));
				plane13.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar_mon_13));
				plane14.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar_mon_14));
				plane15.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bar_mon_15));
			}
			//_planes.add(backgroundVSP);
		    _planes.add(plane);
		    _planes.add(plane2);
		    _planes.add(plane3);
		    _planes.add(plane4);
		    _planes.add(plane5);
		    _planes.add(plane6);
		    _planes.add(plane7);
		    _planes.add(plane8);
		    _planes.add(plane9);
		    _planes.add(plane10);
		    _planes.add(plane11);
		    _planes.add(plane12);
		    _planes.add(plane13);
		    _planes.add(plane14);
		    _planes.add(plane15);

		    // builds the planes to hold the velocity stuff, adds the bitmaps and the planes, 
			// brings the plane closer to the screen and adds the planes to the vector
		    SimplePlane gauge 	 = new SimplePlane(0.45f, 0.45f);	
			SimplePlane backVel  = new SimplePlane(1f, 1f);		
			gauge.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.gauge));
			backVel.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.velocity_background));
			backVel.z=2.6f; 
			_velGaugeComp.add(gauge);
			_velGaugeComp.add(backVel);		

			// builds the planes to hold the acceleration stuff, adds the bitmaps and the planes, 
			// brings the plane closer to the screen and adds the planes to the vector
			SimplePlane backAcc = new SimplePlane(1f,1f);
			SimplePlane accGauge = new SimplePlane(0.45f, 0.45f); 
			backAcc.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.accback));
			accGauge.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.gauge_acc));
			backAcc.z= 2.6f;		
			_accGaugeComp.add(accGauge);
			_accGaugeComp.add(backAcc);

			_grade = new SimplePlane(1f,1f);
			_grade.loadBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.gradewidget_car));
			
			this._barsSView	    = (GLSurfaceView) this.findViewById(R.id.glSurface);
			this._velGaugeSView = (GLSurfaceView) this.findViewById(R.id.glSurfaceVell);
			this._accGaugeSView = (GLSurfaceView) this.findViewById(R.id.glSurfaceAcc);
			this._gradeSView    = (GLSurfaceView) this.findViewById(R.id.glSurfaceGrade);

			if (this._barsSView != null)	{
				this._barsSView.setRenderer(_barsView);
				this._barsSView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);  // only renders on request
			}
			if (this._velGaugeSView != null)	{
				this._velGaugeSView.setRenderer(_velGaugeView);
				this._velGaugeSView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);  // only renders on request
			}		
			if (this._accGaugeSView != null)	{
				this._accGaugeSView.setRenderer(_accGaugeView);
				this._accGaugeSView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);  // only renders on request
			}
			if (this._gradeSView != null)	{
				this._gradeSView.setRenderer(_gradeView);
				this._gradeSView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);  // only renders on request
			}
			
			// not finished on resume
			finished=false;
			// Register the broadcast receiver. Add the necessary filters
			IntentFilter filter = 
				new IntentFilter(getResources().getString(R.string.vehicle_info_broadcast));
			filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
			getApplicationContext().registerReceiver(new VisualFeedbackReceiver(), filter);
			// Start the processing service that runs on the background and
			// show feedback toast message
			startService(new Intent(this, SensorDataProcessor.class));
			Toast t = Toast.makeText(this, "Started background services", Toast.LENGTH_LONG);
			t.show();
			//enables the finishtrip and finish button
			findViewById(R.id.finish_vehicle_info_visual).setEnabled(true);
			findViewById(R.id.go_to_agg).setEnabled(true);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Handles the finish button clicked from the View v
	 * @param v - The view that has been pressed
	 * */	
	public void handleButtonClick(View v)	{
		
		switch(v.getId()){
			case(R.id.finish_vehicle_info_visual):
				v.setEnabled(false);
				stopViz();
				break;
			
			case(R.id.go_to_agg):
				gotoAggregateViz();
				break;
		}
	}
	/**
	 * Stops the sensor processing service when the visualization is finished
	 */
	public void stopViz(){	
		try {					
			if ((finished = stopService(new Intent(this, SensorDataProcessor.class))))	{				
				Toast t = Toast.makeText(this, "Background services stopped", Toast.LENGTH_LONG);
				t.show();
			}
			else
				Log.e(MODULE, "Failed to stop sensor data processor");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	/**
	 * Navigates to the activity responsible for presenting the summary of the trip
	 */
	public void gotoAggregateViz(){
		int[] teste = {20,30,10,15,15,5,12,40};
		Intent consData = new Intent(this, VisuaFeedbackMap.class);
		consData.putExtra("DATA",teste);
		startActivity(consData);
	}
	/**
	 * Creates the Mesh to be rendererd by the gl views  according to the acceleration
	 * @param acceleration
	 */
	public void createOpenGLObj(float acceleration, float grade, float vel){
		
		//clean the meshes so we dont render everything again
		_barsView.clean();		
		_velGaugeView.clean();
		_accGaugeView.clean();
		_gradeView.clean();
		
		int bar_num = calculateVSPBars(calculateInstantaneousFuel(VSP));
		Log.d(MODULE, "VSP: " + VSP + "Bar number: " + bar_num);
		for (int i = 0; i < bar_num; i++)
			_barsView.addMesh(_planes.elementAt(i));
		
		float angle   = (meterPerSecontToKilometerPerHour(vel)*240)/120;
		
		//creates the velocity gauge animation
		double angleRad = ((-angle*Math.PI)/180);
		_velGaugeComp.elementAt(0).rz =-angle;
		_velGaugeComp.elementAt(0).x= -1*((float)(0.097*Math.cos(angleRad)) - (float)(0.05*Math.sin(angleRad)));	
		_velGaugeComp.elementAt(0).y= -1*((float)(0.097*Math.sin(angleRad)) + (float)(0.05*Math.cos(angleRad)));
		_velGaugeView.addMesh(_velGaugeComp.elementAt(1));
		_velGaugeView.addMesh(_velGaugeComp.elementAt(0));
		
		angle = acceleration*120/-6;
		//creates the acc gauge animation
		angleRad = ((angle*Math.PI)/180);
		_accGaugeComp.elementAt(0).rz = angle;
		_accGaugeComp.elementAt(0).x= -1*(- (float)(-0.11*Math.sin(angleRad)));	
		_accGaugeComp.elementAt(0).y= -1*((float)(-0.11*Math.cos(angleRad)));	
		_accGaugeView.addMesh(_accGaugeComp.elementAt(1));
		_accGaugeView.addMesh(_accGaugeComp.elementAt(0));
		
		//grade animation stuff
		_grade.rz=grade;
		_grade.z=2.8f;
		//_gradeView.addMesh(_frame);
		_gradeView.addMesh(_grade);

		//requests a render to the different surfaces on the view
		_barsSView.requestRender();		    
		_accGaugeSView.requestRender();
		_velGaugeSView.requestRender();
		_gradeSView.requestRender();

		
		//Log.e(MODULE, "Rendering");

	} 
	/**
	 * Given the VSP value, approximate the instantaneous fuel consumption (ml/s)
	 * @param VSP - The raw VSP value
	 * @return - The instantaneous fuel (ml/s)
	 */
	public double calculateInstantaneousFuel(double VSP)	{
		
		double fuel = 0.0;
		
		if (VSP <= 0)
			fuel = 1 + Math.sqrt(Math.abs(VSP));
		else
			fuel = 0.00075 * Math.pow(VSP, 3) - 0.05 * Math.pow(VSP, 2) + 1.35 * VSP + 0.05;
		
		return fuel;
	}
	/**
	 * Return a number between 0 and 15 to describe the number of VSP bars to draw
	 * @param instantaneous_fuel
	 * @return
	 */
	public int calculateVSPBars(double instantaneous_fuel)	{
		
		return (int)Math.min(Math.abs(Math.floor(instantaneous_fuel)), 15.0);
	}
	/**
	 * Converts the velocity from m/s to kw/h
	 * @param vel_ms
	 * @return
	 */
	public int meterPerSecontToKilometerPerHour(float vel_ms){
		
		return (int)Math.round((vel_ms/1000)*3600);
	}
	/**
	 *	The class that receives the broadcasts from the broadcaster
	 */
	private class VisualFeedbackReceiver extends BroadcastReceiver	{

		private Bundle msgContent = new Bundle();
		private double acc 		  = 0.0;
		private double speed 	  = 0.0;
		private double grade 	  = 0.0;
		private String state	  = "";
		@Override
		public void onReceive(Context context, Intent intent) {
			
			try {
				msgContent = intent.getExtras();
				acc 	   = msgContent.getDouble("linear_acceleration");
				speed	   = Math.abs(msgContent.getDouble("speed"));
				VSP 	   = msgContent.getDouble("vsp");
				grade	   = msgContent.getDouble("grade");
				state 	   = msgContent.getString("state");
				
				accValue.setText(df.format(acc)+"m/s^2");
				speedValue.setText(df.format(meterPerSecontToKilometerPerHour((float)speed))+"Km/h");
				gradeValue.setText(df.format(Math.round(grade))+"¼");
				accValue.invalidate();
				speedValue.invalidate();
				gradeValue.invalidate();
				
				if (! finished)
					createOpenGLObj((float)acc, (float)grade, (float)speed);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}