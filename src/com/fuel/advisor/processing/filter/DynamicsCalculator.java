package com.fuel.advisor.processing.filter;

import com.fuel.advisor.processing.custom.CoordinateCalculator;

import android.os.Bundle;
import android.util.Log;

/**
 * The DynamicsCalculator filter receives both GPS and Accelerometer data considered
 * feasible (usually cleaned by previous filters) so that final linear_velocity, acceleration,
 * grade, and Vehicle Specific Power (VSP) can be calculated.
 * 
 * @author
 *	Tiago Camacho
 */
@SuppressWarnings("unused")
public final class DynamicsCalculator extends PipeFilter	{

	private static final String MODULE = "DynamicsCalculator";
	private static final double g = -9.807;
	
	private Bundle data;
	private double[] acc_g = new double[3];
	private double[] real_acc = new double[3];
	private double[] last_real_acc = new double[3];
	private double[] vec = new double[3];
	private double[] last_pos = new double[3];
	private double[] pos = new double[3];
	private int[] gps_status = new int[2];
	private double pitch_offset = 0.0;
	private double roll_offset = 0.0;
	private double air_coef = 0.000302;
	private double roll_coef = 0.132;
	private double grade = 0.0;
	private double linear_acc = 0.0;	// The scalar quantity for the acceleration
	private double linear_velocity = 0.0;	// Includes direction of the velocity 
	private double speed = 0.0;	// The norm of the velocity
	private double distance = 0.0;
	private double vsp = 0.0;
	private long lastAccelerationUpdate = 0;
	private long lastSpeedUpdate = 0;
	
	/**
	 * Default constructor
	 * @param filter - The filter to receive content from
	 * @param ctx - The current application context
	 * @param broadcastType - The type of broadcast to perform
	 */
	public DynamicsCalculator(PipeFilter filter, double pitch_offset)	{
		
		super(filter);
		this.pitch_offset = pitch_offset;
		setFilterName(MODULE);
	}
	
	@Override
	protected void onDataReceived(Bundle data) {

		// Upon receiving the data, extract the acceleration vector and
		// GPS linear_velocity values. Consequently update the data with calculated values
		// for the linear acceleration, grade, and VSP
		try {
			this.data = data;
			pos = data.getDoubleArray("position");
			acc_g = data.getDoubleArray("gravity_acceleration");
			real_acc = data.getDoubleArray("real_acceleration");
			vec = data.getDoubleArray("velocity");
			calculateDistance();
			calculateLinearAcceleration();
			if (linear_acc != 0)	{
				calculateGrade();
				lastAccelerationUpdate = System.currentTimeMillis();
			}
			calculateLinearVelocity();
			calculateVSP();
			gps_status = data.getIntArray("gps_status");
			data.remove("gravity_acceleration");
			data.remove("real_acceleration");
			data.remove("velocity");
			data.remove("position");
			double[] loc = CoordinateCalculator.convertToEllipsoidal(pos[2], pos[0], pos[1]);
			data.putDouble("latitude", loc[0]);
			data.putDouble("longitude", loc[1]);
			data.putDouble("altitude", loc[2]);
			data.putDouble("distance", distance);
			data.putDouble("linear_acceleration", linear_acc);
			data.putDouble("linear_velocity", linear_velocity);
			data.putDouble("speed", speed);
			data.putDouble("grade", grade);
			data.putDouble("vsp", vsp);
			if (gps_status != null)	{
				data.putBoolean("gps_fixed", (gps_status[0] == 1) ? true : false);
				data.putInt("num_sats", gps_status[1]);
			}
			gps_status = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Updates the linear_velocity reading
	 * @param linear_velocity - The new linear_velocity value
	 */
	private void updateSpeedReading(double speed)	{
		
		this.linear_velocity = speed;
		lastSpeedUpdate = System.currentTimeMillis();
	}
	
	/**
	 * Determine linear acceleration value
	 */
	private void calculateLinearAcceleration()	{
		
		if (real_acc == null || (real_acc[1] == last_real_acc[1] && real_acc[2] == last_real_acc[2]))	{
			linear_acc = 0.0;
			return;
		}
		
		linear_acc = real_acc[1] * Math.cos(Math.toRadians(pitch_offset))
				+ real_acc[2] * Math.sin(Math.toRadians(pitch_offset));
		last_real_acc[1] = real_acc[1];
		last_real_acc[2] = real_acc[2];
	}
	
	/**
	 * Determine the linear velocity value
	 */
	private void calculateLinearVelocity()	{

		if (vec == null || (vec[1] == 0.0 && vec[2] == 0.0))	{
			linear_velocity = 0.0;
			speed = 0.0;
			return;
		}
		
		double vec_y = vec[1] * Math.cos(Math.toRadians(pitch_offset));
		double vec_z = vec[2] * Math.sin(Math.toRadians(pitch_offset));
		linear_velocity = vec_y + vec_z;
		speed = Math.sqrt(Math.pow(vec_y, 2) + Math.pow(vec_z, 2));
	}
	
	/**
	 * Determine the total distance travelled
	 */
	private void calculateDistance()	{
		
		if (pos == null || (pos[0] == 0.0 && pos[1] == 0.0 && pos[2] == 0.0))
			distance += 0.0;
		else if (last_pos == null || (last_pos[0] == 0.0 && last_pos[1] == 0.0 && last_pos[2] == 0.0))
			distance += 0.0;
		else
			distance += Math.sqrt(Math.pow(pos[0] - last_pos[0], 2) + Math.pow(pos[1] - last_pos[1], 2) +
					Math.pow(pos[2] - last_pos[2], 2));
		
		last_pos[0] = pos[0];
		last_pos[1] = pos[1];
		last_pos[2] = pos[2];
	}
	
	/**
	 * Calculate the grade value
	 */
	private void calculateGrade()	{
		
		grade = Math.toDegrees(Math.atan2(acc_g[1], acc_g[2])) - pitch_offset;
	}
	
	/**
	 * Calculates the Vehicle Specific Power
	 * @return - The VSP value in m^2/s^3
	 */
	private void calculateVSP()	{
		
		vsp = speed * (1.1 * linear_acc + g * Math.sin(Math.toRadians(grade)) 
				+ roll_coef) + air_coef * Math.pow(speed, 3);
	}
}