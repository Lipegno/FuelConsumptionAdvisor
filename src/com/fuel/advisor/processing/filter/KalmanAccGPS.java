package com.fuel.advisor.processing.filter;

import java.io.PipedOutputStream;

import Jama.Matrix;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.android.aware_framework.Accelerometer_Service;
import com.fuel.advisor.processing.custom.CoordinateCalculator;

/**
 * The filter that implements a Kalman Filter so that integration of both
 * accelerometer and GPS is possible. The filter estimates velocity readings
 * using the accelerometer as a control input, and provides correction when
 * GPS is made available. This way, velocity estimation (2-D level; vertical and horizontal)
 * is made possible even when there is an outage of the GPS signal (very common when light of sight is lost)
 * 
 * @author
 * Tiago Camacho
 */
@SuppressWarnings("unused")
public final class KalmanAccGPS extends PipeFilter	{

	private static final String MODULE = "KalmanAccGPS";
	private static final double weight_factor = 0.90;	// The weight factor the low-pass filter
	private static final double dt = 
		Accelerometer_Service.getAccelerometerUpdatePeriod() / 1000.0f;	// The integration step for the Kalman

	// Define the Kalman filter related structures
	// Px, Py, Pz, Vx, Vy, Vz, bax, bay, baz
	private final Matrix X0 = new Matrix(new double[][]{
			{	-1564269.0702	},
			{	3422400.3830	},
			{	5142355.5654	},
			{	0.0		},
			{	0.0		},
			{	0.0		},
			{	0.05	},
			{	0.05	},
			{	0.05	}});
	private final Matrix P0 = Matrix.identity(9, 9);
	private final Matrix F = new Matrix(new double[][]{
			{	1,	0,	0,	dt,	0,	0,	-0.5*Math.pow(dt, 2),	0,	0	},
			{	0,	1,	0,	0,	dt,	0,	0,	-0.5*Math.pow(dt, 2),	0	},
			{	0,	0,	1,	0,	0,	dt,	0,	0,	-0.5*Math.pow(dt, 2)	},
			{	0,	0,	0,	1,	0,	0,	-dt,	0,	0	},
			{	0,	0,	0,	0,	1,	0,	0,	-dt,	0	},
			{	0,	0,	0,	0,	0,	1,	0,	0,	-dt		},
			{	0,	0,	0,	0,	0,	0,	1,	0,	0		},
			{	0,	0,	0,	0,	0,	0,	0,	1,	0		},
			{	0,	0,	0,	0,	0,	0,	0,	0,	1		}});
	private final Matrix U = new Matrix(new double[][]{
			{	0.0		},
			{	0.0		},
			{	0.0		}});
	private final Matrix G = new Matrix(new double[][]{
			{	0.5*Math.pow(dt, 2),	0,	0	},
			{	0,	0.5*Math.pow(dt, 2),	0	},
			{	0,	0,	0.5*Math.pow(dt,2)		},
			{	dt,	0,	0	},
			{	0,	dt,	0	},
			{	0,	0,	dt	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	}});
	private final Matrix H = new Matrix(new double[][]{
			{	1,	0,	0,	0,	0,	0,	0,	0,	0},
			{	0,	1,	0,	0,	0,	0,	0,	0,	0},
			{	0,	0,	1,	0,	0,	0,	0,	0,	0},
			{	0,	0,	0,	1,	0,	0,	0,	0,	0},
			{	0,	0,	0,	0,	1,	0,	0,	0,	0},
			{	0,	0,	0,	0,	0,	1,	0,	0,	0}});
	private final double processNoiseVariance = Math.pow(0.01, 2);	// m/s^2 -- accelerometer noise
	private final double processNoisePSD = processNoiseVariance/2;
	private final double biasNoiseVariance = Math.pow(0.05, 2);	// m/s^2 -- bias noise (white noise)
	private final double biasNoisePSD = biasNoiseVariance/2;
	private final Matrix measurementNoiseVariance = new Matrix(new double[][]{ // 20m for vert. pos, 10m for hor. pos;
			{	Math.pow(10, 2),	0,	0,	0,	0,	0	},	// 2.0m/s for vert.speed and 1.0m/s for hor.speed
			{	0,	Math.pow(20, 2),	0,	0,	0,	0	},
			{	0,	0,	Math.pow(10, 2),	0,	0,	0	},
			{	0,	0,	0,	Math.pow(1.0, 2),	0,	0	},
			{	0,	0,	0,	0,	Math.pow(2.0, 2),	0	},
			{	0,	0,	0,	0,	0,	Math.pow(1.0, 2)	}});
	private final Matrix Q = new Matrix(new double[][]{
			{	Math.pow(dt, 5)/4*processNoisePSD,	0,	0,	Math.pow(dt, 4)/2*processNoisePSD,	0,	0,	Math.pow(dt, 3)/2*biasNoisePSD,	0,	0	},
			{	0,	Math.pow(dt, 5)/4*processNoisePSD,	0,	0,	Math.pow(dt, 4)/2*processNoisePSD,	0,	0,	Math.pow(dt, 3)/2*biasNoisePSD,	0		},
			{	0,	0,	Math.pow(dt, 5)/4*processNoisePSD,	0,	0,	Math.pow(dt, 4)/2*processNoisePSD,	0,	0,	Math.pow(dt, 3)/2*biasNoisePSD	},
			{	Math.pow(dt, 4)/2*processNoisePSD,	0,	0,	Math.pow(dt, 3)*processNoisePSD,	0,	0,	Math.pow(dt, 2),	0,	0		},
			{	0,	Math.pow(dt, 4)/2*processNoisePSD,	0,	0,	Math.pow(dt, 3)*processNoisePSD,	0,	0,	Math.pow(dt, 2),	0		},
			{	0,	0,	Math.pow(dt, 4)/2*processNoisePSD,	0,	0,	Math.pow(dt, 3)*processNoisePSD,	0,	0,	Math.pow(dt, 2)				},
			{	Math.pow(dt, 3)/2*processNoisePSD,	0,	0,	Math.pow(dt, 2)*processNoisePSD,	0,	0,	dt*biasNoisePSD,	0,	0	},
			{	0,	Math.pow(dt, 3)/2*processNoisePSD,	0,	0,	Math.pow(dt, 2)*processNoisePSD,	0,	0,	dt*biasNoisePSD,	0	},
			{	0,	0,	Math.pow(dt, 3)/2*processNoisePSD,	0,	0,	Math.pow(dt, 2)*processNoisePSD,	0,	0,	dt*biasNoisePSD	}});
	private final Matrix R = measurementNoiseVariance.times(1);
	
	// Define auxilary variables
	private final double[] acc_g = new double[3];
	private final double[] real_acc = new double[3];
	private final double[] last_acc = new double[3];
	private final double[] pos = new double[3];
	private final double[] vec = new double[3];
	
	private final double lat_1 =  32.660492;
	private final double lon_1 = -16.919399;
	private final double lat_2 =  32.660955;
	private final double lon_2 = -16.919592;
	
	private boolean accelerationFirstReading = true;
	private Location lastLocation;
	private Location location;
	private double[] raw_acc = new double[3];
	private Matrix estimation = new Matrix(9, 1);
	private Matrix input = new Matrix(3, 1);
	private KalmanFilter kf;
	
	/**
	 * Default constructor for this filter
	 * @param output - The outpustream to receive content from
	 * @param pitch_offset - The value of the pitch offset
	 */
	public KalmanAccGPS(PipedOutputStream output, double pitch_offset)	{
		
		super(output);
		setFilterName(MODULE);
		try {
			kf = new KalmanFilter(X0, P0, F, U, G, Q, H, R);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onDataReceived(Bundle data) {

		try {
			// Read the raw sensor measurements. Determine if we have change
			// of acceleration and/or GPS position
			Location l;
			if ((raw_acc = data.getDoubleArray("acceleration")) != null)	{
				// If we have only input from the accelerometer, then
				// our best estimation is given by accelerometer only data
				if (updateAccelerationReading())	{
					input.set(0, 0, real_acc[0]);
					input.set(1, 0, real_acc[1]);
					input.set(2, 0, real_acc[2]);
					kf.setStateEstimationMatrix(kf.predict(input));
				}
				else	{
					input.set(0, 0, 0.0);
					input.set(1, 0, 0.0);
					input.set(2, 0, 0.0);
					kf.setStateEstimationMatrix(kf.predict(input));
				}
			}
			if ((l = (Location)data.getParcelable("location")) != null)	{
				// If we have GPS data available, then use this as a measurement
				// to improve estimation
				location = l;
				if (hasLocationChanged(location))	{
					kf.correct(getKalmanMeasurement());
					lastLocation = location;
				}
			}
			
			// Update the values and forward them
			estimation = kf.getStateEstimationMatrix();
			pos[0] = estimation.get(0, 0);
			pos[1] = estimation.get(1, 0);
			pos[2] = estimation.get(2, 0);
			vec[0] = estimation.get(3, 0);
			vec[1] = estimation.get(4, 0);
			vec[2] = estimation.get(5, 0);
			real_acc[0] -= estimation.get(6, 0);
			real_acc[1] -= estimation.get(7, 0);
			real_acc[2] -= estimation.get(8, 0);
			data.putDoubleArray("position", pos);
			data.putDoubleArray("real_acceleration", real_acc);
			data.putDoubleArray("gravity_acceleration", acc_g);
			data.putDoubleArray("velocity", vec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Update the acceleration readings by applying the high-pass filter so that
	 * acceleration due vehicle motion is isolated from acceleration due force of gravity
	 */
	private boolean updateAccelerationReading()	{
		
		// Reset acceleration if no change has happened since last reading
		if (raw_acc == null || (raw_acc[0] == last_acc[0] && raw_acc[1] == last_acc[1] && raw_acc[2] == last_acc[2]))	{
			real_acc[0] = real_acc[1] = real_acc[2] = 0.0;
			return false;
		}
		
		// If this is our first reading, then we want to say that all the acceleration
		// that is being read by the device is solely due to gravity
		if (accelerationFirstReading)	{
			acc_g[0] = 0.0;
			acc_g[1] = raw_acc[1];
			acc_g[2] = raw_acc[2];
			real_acc[1] = 0.0;
			real_acc[2] = 0.0;
			accelerationFirstReading = false;
		}
		
		// Apply the low pass for gravity acceleration isolation
		for (int i = 0; i < acc_g.length; i++)	{
			acc_g[i] = weight_factor * acc_g[i] + (1 - weight_factor) * raw_acc[i];
			real_acc[i] = raw_acc[i] - acc_g[i];
		}

		// Update last raw acceleration readings
		for (int i = 0; i < raw_acc.length; i++)
			last_acc[i] = raw_acc[i];
		
		return true;
	}
	
	/**
	 * Determines if there has been a change of global position from the last
	 * GPS reading
	 * @param l - The current Location object
	 * @return - True if location has changed, false otherwise
	 */
	private boolean hasLocationChanged(Location l)	{
		
		return lastLocation == null || (l.getLatitude() != lastLocation.getLatitude() 
			&& l.getLongitude() != lastLocation.getLongitude());
	}
	
	/**
	 * Determine measurement by calculating position in cartesian coordinates and
	 * determining the speed vector
	 * @return - The measurement Matrix (6x1)
	 */
	private Matrix getKalmanMeasurement()	{
		
		double V_x;
		double V_y;
		double V_z;
		double teta;
		double phi;
		
		double[] lastPos = CoordinateCalculator.convertToCartesian(lastLocation.getLatitude(),
				lastLocation.getLongitude(), lastLocation.getAltitude());
		double[] currentPos = CoordinateCalculator.convertToCartesian(location.getLatitude(),
				location.getLongitude(), location.getAltitude());
		teta = Math.atan2(currentPos[0] - lastPos[0], currentPos[2] - lastPos[2]);
		phi = Math.atan2(currentPos[1] - lastPos[1] , currentPos[2] - lastPos[2]);
		V_z = location.getSpeed();
		V_x = V_z * Math.tan(phi);
		V_y = V_z * Math.tan(teta);
		
		return new Matrix(new double[][]{
				{	currentPos[1]	},
				{	currentPos[2]	},
				{	currentPos[0]	},
				{	V_x	},
				{	V_y	},
				{	V_z	}
		});
	}
}