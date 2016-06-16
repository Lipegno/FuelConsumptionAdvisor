package com.fuel.advisor.processing.vehicle.state;

import com.vehicle.vehicle.Vehicle;

import android.util.Log;


/**
 * Class responsible for calculating several threshold values used to determine
 * state transitions 
 * 
 * @author Tiago Camacho
 */
@SuppressWarnings("unused")
public class ThresholdCalculator {

	private static final boolean DEBUG = false;
	private static final String MODULE = "ThresholdCalculator";
	
	/**
	 * Determine the threshold for a smooth acceleration
	 * @param grade_mean
	 * @param current_grade
	 * @return
	 */
	public static double calculateSmoothAccelerationThreshold(double grade_mean, double current_grade)	{
		
		double dG = current_grade - grade_mean;
		double threshold;
		
		if (current_grade >= 0)
			threshold = 0.045 * (current_grade - grade_mean) + 0.0005 * current_grade + VehicleState.SMOOTH_ACC_THRESHOLD;
		else
			threshold = Math.max(0.025 * current_grade + VehicleState.SMOOTH_ACC_THRESHOLD, 0);

		if (DEBUG)
			Log.d(MODULE, "Smooth acc threshold: " + threshold);
		return threshold; 
	}
	
	/**
	 * Determine the threshold for a smooth braking
	 * @param grade_mean
	 * @param current_grade
	 * @return
	 */
	public static double calculateSmoothBrakingThreshold(double grade_mean, double current_grade)	{
		
		double threshold = 0.005 * (current_grade - grade_mean) + 0.015 * current_grade + VehicleState.SMOOTH_BRAKING_THRESHOLD; 
		
		if (DEBUG)
			Log.d(MODULE, "Smooth Brk Threshold: " + threshold);
		return threshold;
	}
	
	/**
	 * Determine the threshold for an emergency braking
	 * @param grade_mean
	 * @param current_grade
	 * @return
	 */
	public static double calculateEmergencyBrakingThreshold(double grade_mean, double current_grade)	{

		double threshold = 0.005 * (current_grade - grade_mean) + 0.015 * current_grade + VehicleState.EMERGENCY_BRAKING_THRESHOLD; 
		
		if (DEBUG)
			Log.d(MODULE, "Emerg Brk Threshold: " + threshold);
		return threshold; 
	}
}