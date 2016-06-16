package com.fuel.advisor.processing.vehicle.state;

import com.fuel.advisor.processing.vehicle.VehicleContext;

public class ExcessiveBraking implements VehicleState	{
	
	private final VehicleContext ctx;
	private final VehicleState prevState;
	
	public ExcessiveBraking(VehicleContext ctx, VehicleState prevState) {

		this.ctx = ctx;
		this.prevState = prevState;
	}
	
	@Override
	public void updateDynamics(double avgSpeed, double avgAcc, double avgGrade,
			double currentGrade) {
		
		double sb_threshold = ThresholdCalculator.calculateSmoothBrakingThreshold(avgGrade, currentGrade);
		
		// Go to Halted state
		if (avgSpeed >= LOWER_SPEED_LIMIT && avgSpeed <= HIGHER_SPEED_LIMIT)
			ctx.setState(new Halted(ctx, null));
		// Go to Braking state if deceleration values are smooth enough
		else if (avgAcc < 0.0 && avgAcc > sb_threshold)
			ctx.setState(new Braking(ctx, this));
		else if (avgAcc > 0.0)	{
			if (prevState.getState().equals("ExcessiveAccelerating"))
				ctx.setState(new Accelerating(ctx, this));
			else
				ctx.setState(prevState);
		}
	}
	
	@Override
	public String getState() {

		return "ExcessiveBraking";
	}
}