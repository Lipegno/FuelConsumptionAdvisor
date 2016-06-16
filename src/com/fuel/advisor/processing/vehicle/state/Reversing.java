package com.fuel.advisor.processing.vehicle.state;

import com.fuel.advisor.processing.vehicle.VehicleContext;

public class Reversing implements VehicleState	{

	private final VehicleContext ctx;
	
	public Reversing(VehicleContext ctx, VehicleState prevState) {

		this.ctx = ctx;
	}
	
	@Override
	public void updateDynamics(double avgSpeed, double avgAcc, double avgGrade,
			double currentGrade) {

		double sb_threshold = ThresholdCalculator.calculateEmergencyBrakingThreshold(avgGrade, currentGrade);
		
		// Normal braking
		if (avgAcc > 0.0 && avgAcc <= Math.abs(sb_threshold))
			ctx.setState(new Braking(ctx, this));
		// Excessive braking
		else if (avgAcc > 0.0 && avgAcc > Math.abs(sb_threshold))
			ctx.setState(new ExcessiveBraking(ctx, this));
	}
	
	@Override
	public String getState() {

		return "Reversing";
	}
}