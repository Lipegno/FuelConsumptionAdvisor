package com.fuel.advisor.processing.vehicle.state;

import com.fuel.advisor.processing.vehicle.VehicleContext;

public class Braking implements VehicleState {

	protected final VehicleContext ctx;
	protected final VehicleState prevState;
	
	public Braking(VehicleContext ctx, VehicleState prevState) {

		this.ctx = ctx;
		this.prevState = prevState;
	}
	
	@Override
	public void updateDynamics(double avgSpeed, double avgAcc, double avgGrade,
			double currentGrade) {

		double sa_threshold = ThresholdCalculator.calculateSmoothAccelerationThreshold(avgGrade, currentGrade);
		double sb_threshold = ThresholdCalculator.calculateEmergencyBrakingThreshold(avgGrade, currentGrade);

		// Normal acceleration
		if (avgAcc > 0.0 && avgAcc <= sa_threshold && avgSpeed > HIGHER_SPEED_LIMIT)
			ctx.setState(new Accelerating(ctx, this));
		// Excessive acceleration
		else if (avgAcc > 0.0 && avgAcc > sa_threshold && avgSpeed > HIGHER_SPEED_LIMIT)
			ctx.setState(new ExcessiveAccelerating(ctx, this));
		// Excessive braking
		else if (avgAcc < sb_threshold)
			ctx.setState(new ExcessiveBraking(ctx, this));
		// We are reversing
		else if (avgAcc < 0.0 && avgSpeed < LOWER_SPEED_LIMIT)
			ctx.setState(new Reversing(ctx, this));
		// We are halted
		else if (avgSpeed >= LOWER_SPEED_LIMIT && avgSpeed <= HIGHER_SPEED_LIMIT)
			ctx.setState(new Halted(ctx, this));
	}
	
	@Override
	public String getState() {

		return "Braking";
	}
}