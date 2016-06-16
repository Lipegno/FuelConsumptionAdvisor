package com.fuel.advisor.processing.vehicle.state;

import com.fuel.advisor.processing.vehicle.VehicleContext;

public class Accelerating implements VehicleState	{

	private final VehicleContext ctx;
	
	public Accelerating(VehicleContext ctx, VehicleState prevState) {

		this.ctx = ctx;
	}
	
	@Override
	public void updateDynamics(double avgSpeed, double avgAcc, double avgGrade,
			double currentGrade) {
		
		double sb_threshold = ThresholdCalculator.calculateSmoothBrakingThreshold(avgGrade, currentGrade);
		double eb_threshold = ThresholdCalculator.calculateEmergencyBrakingThreshold(avgGrade, currentGrade);
		
		// Smooth braking
		if (avgAcc < 0.0 && avgAcc >= sb_threshold)
			ctx.setState(new Braking(ctx, this));
		// Excessive braking
		else if (avgAcc < sb_threshold && avgAcc >= eb_threshold)
			ctx.setState(new ExcessiveBraking(ctx, this));
		// Emergency braking
		else if (avgAcc < eb_threshold)
			ctx.setState(new EmergencyBraking(ctx, this));
	}
	
	@Override
	public String getState() {

		return "Accelerating";
	}
}