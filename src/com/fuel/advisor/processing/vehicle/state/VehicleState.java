package com.fuel.advisor.processing.vehicle.state;

/**
 * The Interface that represents the operations that are allowed by
 * the State objects
 * 
 * @author Tiago Camacho
 */
public interface VehicleState {

	// This defines some useful constants to be used by states
	public static final double LOWER_SPEED_LIMIT = -0.5;	// m/s
	public static final double HIGHER_SPEED_LIMIT = 1.0;	// m/s
	public static final double SMOOTH_ACC_THRESHOLD = 1.0;	// m/s^2
	public static final double SMOOTH_BRAKING_THRESHOLD = -1.0;	// m/s^2
	public static final double EMERGENCY_BRAKING_THRESHOLD = -2.0;	// m/s^2
	
	// This defines the public interface for all states
	public void updateDynamics(double avg_speed, double avg_acc, double avg_grade, double current_grade);
	public String getState();
}