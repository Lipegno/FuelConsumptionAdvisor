package com.fuel.advisor.processing.vehicle.state;

import com.fuel.advisor.processing.vehicle.VehicleContext;

public class EmergencyBraking extends Braking	{

	public EmergencyBraking(VehicleContext ctx, VehicleState prevState) {

		super(ctx, prevState);
	}
	
	@Override
	public String getState() {

		return "EmergencyBraking";
	}
}
