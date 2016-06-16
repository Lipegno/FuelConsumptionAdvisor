package com.fuel.advisor.processing.vehicle;

import com.fuel.advisor.processing.vehicle.state.Halted;
import com.fuel.advisor.processing.vehicle.state.VehicleState;

/**
 * A class that holds the representation of the current state of the vehicle.
 * External classes need only to call the {@link VehicleContext#updateDynamics(double, double, double, double)}
 * method to issue a pontetial change of the current state. The initial state is set up as 'Halted'.
 * Clients can query for current state at any time by issuing the {@link VehicleContext#getState()}
 * method call
 * 
 * @author Tiago Camacho
 */
@SuppressWarnings("unused")
public class VehicleContext {

	private static final String MODULE = "VehicleContext";
	private static final boolean DEBUG = true;
	
	private VehicleState state;
	
	/**
	 * Default constructor. Sets up the initial vehicle state
	 * We start by setting the state as 'Halted'
	 */
	public VehicleContext() {

		state = new Halted(this, null);
	}
	
	/**
	 * Issue a change on state. Usually this is only called by the State
	 * implementations
	 * @param newState - The new state
	 */
	public void setState(VehicleState newState)	{
		
		this.state = newState;
	}
	
	/**
	 * Update the vehicle dynamics
	 * @param avg_speed - The recent historical speed mean
	 * @param avg_acc - The recent historical acceleration mean
	 * @param avg_grade - The recent historical grade mean
	 * @param grade - The current instantaneous value of the grade
	 */
	public void updateDynamics(double avgSpeed, double avgAcc, 
			double avgGrade, double currentGrade)	{
		
		state.updateDynamics(avgSpeed, avgAcc, avgGrade, currentGrade);
	}
	
	/**
	 * Get the current state of the vehicle
	 * @return - The string that represents the current state of the vehicle
	 */
	public String getState()	{
		
		return state.getState();
	}
}