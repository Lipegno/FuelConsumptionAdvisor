package com.fuel.advisor.processing.vehicle.state;

import com.fuel.advisor.processing.vehicle.VehicleContext;

public class Halted implements VehicleState	{

	private final VehicleContext ctx;
	
	public Halted(VehicleContext ctx, VehicleState prevState) {

		this.ctx = ctx;
	}
	
	@Override
	public void updateDynamics(double avgSpeed, double avgAcc, double avgGrade,
			double currentGrade) {

		double sa_threshold = ThresholdCalculator.calculateSmoothAccelerationThreshold(avgGrade, currentGrade);
		
		// Normal positive acceleration values
		if ((avgAcc > 0.0 && avgAcc <= sa_threshold) && avgSpeed > HIGHER_SPEED_LIMIT)
			ctx.setState(new Accelerating(ctx, this));
		// Excessive positive acceleration values
		else if ((avgAcc > 0.0 && avgAcc > sa_threshold) && avgSpeed > HIGHER_SPEED_LIMIT)
			ctx.setState(new ExcessiveAccelerating(ctx, this));
		// Reversing. For now we ignore excessive reversing
		else if (avgSpeed < LOWER_SPEED_LIMIT)
			ctx.setState(new Reversing(ctx, this));
	}
	
	@Override
	public String getState() {

		return "Halted";
	}
}