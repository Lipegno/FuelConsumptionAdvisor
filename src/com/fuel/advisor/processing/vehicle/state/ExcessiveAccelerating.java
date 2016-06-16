package com.fuel.advisor.processing.vehicle.state;

import com.fuel.advisor.processing.vehicle.VehicleContext;

public class ExcessiveAccelerating extends Accelerating	{

	public ExcessiveAccelerating(VehicleContext ctx, VehicleState prevState) {

		super(ctx, prevState);
	}
	
	@Override
	public String getState() {

		return "ExcessiveAccelerating";
	}
}