package com.fuel.advisor.processing.filter;

import com.android.aware_framework.Accelerometer_Service;
import com.fuel.advisor.acquisition.listener.AccelerometerServiceListener;
import com.fuel.advisor.processing.vehicle.VehicleContext;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * The class that receives the transformed content from the processing pipeline, and uses it
 * to determine the current context of the vehicle/driver. This class is then responsible for
 * not only the determination of the context (and consequent addition into the data structure)
 * but also for broadcasting the content to all interested parties (usually UI and/or storage)
 * 
 * @author Tiago Camacho
 */
@SuppressWarnings("unused")
public final class ContextSpecifier extends PipeFilter	{

	private static final String MODULE = "ContextSpecifier";
	private static final boolean DEBUG = true;
	private static final double dT =
		Accelerometer_Service.getAccelerometerUpdatePeriod() / 1000.0f;
	private static final int WINDOW_PERIOD = 3;	// The number of seconds to remember the readings
	private static final int ARRAY_SIZE = (int)(WINDOW_PERIOD / dT);
	
	private final Context ctx;
	private final Intent i;
	private final VehicleContext v_ctx;
	
	private static int counter = 0;
	
	private double[] linear_acceleration = new double[ARRAY_SIZE];
	private double[] linear_velocity = new double[ARRAY_SIZE];
	private double[] grade = new double[ARRAY_SIZE];
	private double current_grade = 0.0;
	
	/**
	 * Default constructor for the context specifier filter
	 * @param filter - The inner filter
	 * @param ctx - The current application context
	 * @param broadCastType - The type of broadcast to use
	 */
	public ContextSpecifier(PipeFilter filter, Context ctx, String broadCastType) {

		super(filter);
		this.ctx = ctx;
		this.i = new Intent(broadCastType);
		this.v_ctx = new VehicleContext();
		setFilterName(MODULE);
	}
	
	@Override
	protected void onDataReceived(Bundle data) {
		
		try {
			// Update the velocity, acceleration and grade values.
			// Determine current context if necessary
			current_grade = data.getDouble("grade");
			if (counter == ARRAY_SIZE - 1)	{
				determineCurrentState();
				for (int i = 0; i < counter; i++)	{
					linear_acceleration[i] = 0.0;
					linear_velocity[i] = 0.0;
					grade[i] = 0.0;
				}
				counter = 0;
			}
			else	{
				linear_acceleration[counter] = data.getDouble("linear_acceleration");
				linear_velocity[counter] = data.getDouble("linear_velocity");
				grade[counter] = data.getDouble("grade");
				counter++;
			}
			data.putString("state", v_ctx.getState());
			
			// Broadcast data to interested parties
			i.putExtras(data);
			ctx.sendOrderedBroadcast(i, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Determines the current vehicle context
	 */
	private void determineCurrentState()	{
		
		double avg_speed = 0.0;
		double avg_acc = 0.0;
		double avg_grade = 0.0;

		// Compute moving average for the last sampling window
		for (int i = 0; i < counter; i++)	{
			avg_acc += linear_acceleration[i];
			avg_speed += linear_velocity[i];
			avg_grade += grade[i];
		}
		avg_acc /= linear_acceleration.length;
		avg_speed /= linear_velocity.length;
		avg_grade /= grade.length;
		
		// Update the vehicle dynamics
		v_ctx.updateDynamics(avg_speed, avg_acc, avg_grade, current_grade);
	}
}