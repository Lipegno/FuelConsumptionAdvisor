package com.fuel.advisor.storage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Vector;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;
import android.util.Log;

import com.vehicle.driver.Driver;
import com.vehicle.vehicle.Vehicle;

/**
 * The class that talks directly with the database. It acts as a Facade, as it holds the methods
 * that other objects call to proceed to database insert, update, and removal, as well as for
 * querying the database. This class is also a Singleton in that only one instance can exist. Call
 * the {@link DBManager#getDBManager()} to get the unique reference for this class
 * 
 * @author Tiago Camacho
 */
public final class DBManager {

	public static final String DB_NAME = "fuel_db.sqlite";
	
	private static final String MODULE = "DBManager";
	private static final boolean DEBUG = true;
	private static final String VEHICLE_TABLE = "Vehicle";
	private static final String DRIVER_TABLE = "Driver";
	private static final String DRIVER_VEHICLE_TABLE = "Driver_Vehicle";
	private static final String TRIP_TABLE = "Trip";
	private static final String VEHICLE_TYPE_TABLE = "Vehicle_Type";
	private static final String VEHICLE_BRAND_TABLE = "Vehicle_Brand";
	private static final String TRIP_DATA_TABLE = "Trip_Data";
	private static final String VEHICLE_CALIBRATION_TABLE = "Vehicle_Calibration";
	private static final String CALIBRATION_SAMPLE_TABLE = "Calibration_Sample";
	
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final ContentValues values = new ContentValues();
	
	private static SQLiteDatabase db;
	
	private String formatedDate;
	
	private DBManager()	{
		
		try {
			if (db == null)
				db = SQLiteDatabase.openDatabase(
						Environment.getExternalStorageDirectory() + "/fuel_consumption/" + DB_NAME,
						null,
						0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static class DBManagerHolder	{
		
		public static final DBManager INSTANCE = new DBManager();
	}
	
	/**
	 * Constructor
	 * @return - The single instance of this class
	 */
	public static DBManager getDBManager()	{
		
		return DBManagerHolder.INSTANCE;
	}
	
	/**
	 * Close the database connection
	 */
	public void close()	{
		
		try {
			if (db != null)
				db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the list of vehicles brands currently registered
	 * @return - The ArrayList that holds the brands of the vehicles
	 */
	public synchronized ArrayList<String> getVehicleBrands()	{
		
		ArrayList<String> list = new ArrayList<String>();
		Cursor c = null;
		
		try {
			c = db.query(
					VEHICLE_BRAND_TABLE,
					new String[]{"brand"},
					null,
					null,
					null,
					null, 
					null);
			while (c.moveToNext())
				list.add(c.getString(0));
		} catch (Exception e) {
			e.printStackTrace();
		} finally	{
			if (c != null)
				c.close();
		}
		
		return list;
	}
	
	/**
	 * Inserts a new vehicle brand in the database
	 * @param brand - The name of the brand
	 */
	public synchronized boolean insertVehicleBrand(String brand)	{
		
		boolean res = false;
		
		try {
			if (db.query(
					VEHICLE_BRAND_TABLE,
					null, 
					"brand='" + brand + "'",
					null,
					null, 
					null,
					null) == null)	{
				values.clear();
				values.put("brand", brand);
				res = (db.insert(VEHICLE_BRAND_TABLE, null, values) == -1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	/**
	 * Retreive the list of registered drivers
	 * @return - An ArrayList of Driver objects
	 */
	public synchronized ArrayList<Driver> getDrivers()	{
		
		ArrayList<Driver> list = new ArrayList<Driver>();
		Cursor c = null;
		
		try {
			c = db.query(
					DRIVER_TABLE,
					new String[]{"first_name", "last_name", "passcode"},
					null,
					null,
					null,
					null,
					null);
			int first_name_index = c.getColumnIndex("first_name");
			int last_name_index = c.getColumnIndex("last_name");
			int passcode_index = c.getColumnIndex("passcode");
			while (c.moveToNext())	{
				Driver d = new Driver(
						c.getString(first_name_index), 
						c.getString(last_name_index), 
						c.getString(passcode_index));
				list.add(d);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally	{
			if (c != null)
				c.close();
		}
		
		return list;
	}
	
	/**
	 * Insert a new driver into the database
	 * @param d - The Driver object that holds the information about the driver
	 * @return - The driver's passcode
	 * @throws DriverAlreadyRegisteredException - If a driver is already registered in the database
	 */
	public synchronized String insertDriver(Driver d) 
		throws DriverAlreadyRegisteredException	{
		
		String passcode = null;
		Cursor c = null;
		
		try {
			// Insert only if driver does not exists yet
			if (getDriverID(d) <= 0)	{
				// Create a new 3-digit passcode, making sure it is unique in
				// the database, so that we can uniquely identify each driver by
				// this number
				Vector<String> vec = new Vector<String>();
				c = db.query(
						DRIVER_TABLE,
						new String[]{"passcode"},
						null,
						null, 
						null, 
						null, 
						null);
				while (c.moveToNext())	{
					if (DEBUG)
						Log.d(MODULE, "Passcode: " + c.getString(0));
					vec.add(c.getString(0));
				}
				do	{
					passcode = createPasscode();
				}	while (vec.contains((String)passcode));
				if (DEBUG)
					Log.d(MODULE, "Created driver with code: " + passcode);
				
				// Proceed to insertion
				values.clear();
				values.put("first_name", d.getFirst_name());
				values.put("last_name", d.getLast_name());
				values.put("passcode", passcode);
				
				// Failed to insert. Return null pointer
				if (db.insert(DRIVER_TABLE, null, values) == -1)
					return null;
			}
			// Driver already registered
			else
				passcode = null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally	{
			if (c != null)
				c.close();
			if (passcode == null)
				throw new DriverAlreadyRegisteredException();
		}
		
		return passcode;
	}
	
	/**
	 * Removes a driver from the database
	 * @param passcode - The passcode that identifies the driver
	 * @throws DriverNotRegisteredException - Thrown when no driver with given passcode exists
	 */
	public synchronized void removeDriver(String passcode)
		throws DriverNotRegisteredException	{
		
		int affectedRows = 0;
		
		try {
			affectedRows = db.delete(DRIVER_TABLE, "passcode='" + passcode + "'", null);
		} catch (Exception e) {
			e.printStackTrace();
		} finally	{
			if (affectedRows == 0)
				throw new DriverNotRegisteredException();
		}
	}
	
	/**
	 * Get the driver's ID
	 * @param d - The Driver object
	 * @return - The newly added ID for the driver. Returns 0 if no driver exists and -1 on error
	 */
	public synchronized long getDriverID(Driver d)	{
		
		long id = -1;
		Cursor c = null;
		
		try {
			String first_name = d.getFirst_name();
			String last_name = d.getLast_name();
			String stm = 
				"select" +
				"	_id " +
				"from" +
				"	" + DRIVER_TABLE + " as d " +
				"where " +
				"	d.first_name='" + first_name + "'" +
				"	and" +
				"	d.last_name='" + last_name + "'";
			c = db.rawQuery(stm, null);
			if (c.moveToFirst())
				id = c.getInt(0);
		} catch (Exception e) {
			e.printStackTrace();
		} finally	{
			if (c != null)
				c.close();
		}
		
		return id;
	}
	
	/**
	 * Fetches the Driver object given the driver's unique passcode number. This is a 4-digit
	 * number that a driver receives upon first registration with the system 
	 * @param passcode - The passcode number
	 * @return - The Driver object. Return null if driver does not exist
	 */
	public synchronized Driver getDriver(String passcode)	{
		
		Driver d = null;
		Cursor c = null;
		
		try {
			c = db.query(
					DRIVER_TABLE, 
					new String[]{"first_name", "last_name"},
					"passcode='" + passcode + "'",
					null,
					null,
					null,
					null);
			if (c.moveToFirst())	{
				int first_name_index = c.getColumnIndex("first_name");
				int last_name_index = c.getColumnIndex("last_name");
				d = new Driver(
						c.getString(first_name_index),
						c.getString(last_name_index),
						passcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally	{
			if (c != null)
				c.close();
		}
		
		return d;
	}
	
	/**
	 * Returns a Driver object given the driver ID
	 * @param d_id - The driver ID
	 * @return - The Driver object
	 */
	public synchronized Driver getDriver(long d_id)	{
		
		Driver d = null;
		Cursor c = null;
		
		try {
			String stm = 
				"select" +
				"	passcode " +
				"from" +
				"	" + DRIVER_TABLE + " " +
				"where" +
				"	_id=" + d_id;
			c = db.rawQuery(stm, null);
			if (c.moveToFirst())
				d = getDriver(c.getString(0));
		} catch (Exception e) {
			e.printStackTrace();
		} finally	{
			if (c != null)
				c.close();
		}
		
		return d;
	}
	
	/**
	 * Retrieve a vehicle object given a plate number
	 * @param plate - The vehicle's plate number
	 * @return - The Vehicle object. Returns null if vehicle not found
	 */
	public synchronized Vehicle getVehicle(String plate)	{
		
		Vehicle v = null;
		Cursor c = null;
		
		try {
			String stm = 
				"select" +
				"	vb.brand," +
				"	vt.model," +
				"	vt.year," +
				"	vt.air_coef," +
				"	vt.roll_coef," +
				"	vt.class " +
				"from" +
				"	" + VEHICLE_TABLE + " as v " +
				"	inner join " + VEHICLE_TYPE_TABLE + " as vt on vt._id = v.type_id" +
				"	inner join " + VEHICLE_BRAND_TABLE + " as vb on vb._id = vt.b_id " +
				"where" +
				"	v.plate='" + plate + "'";
			c = db.rawQuery(stm, null);
			if (! c.moveToFirst())
				return null;
			
			// Get the vehicle data and populate the object
			int brand_index = c.getColumnIndex("brand");
			int model_index = c.getColumnIndex("model");
			int year_index = c.getColumnIndex("year");
			int air_coef_index = c.getColumnIndex("air_coef");
			int roll_coef_index = c.getColumnIndex("roll_coef");
			int class_index = c.getColumnIndex("class");
			v = new Vehicle(
					c.getString(brand_index),
					c.getString(model_index),
					c.getInt(year_index));
			v.setType(c.getString(class_index));
			v.setAirCoef(c.getDouble(air_coef_index));
			v.setRollCoef(c.getDouble(roll_coef_index));
			v.setPlate(plate);
		} catch (Exception e) {
			e.printStackTrace();
		} finally	{
			if (c != null)
				c.close();
		}
	
		return v;
	}
	
	/**
	 * Fetches a Vehicle object given the vehicle ID
	 * @param v_id - The vehicle ID
	 * @return - The Vehicle object
	 */
	public synchronized Vehicle getVehicle(long v_id)	{
		
		Vehicle v = null;
		Cursor c = null;
		
		try {
			String stm = 
				"select" +
				"	plate " +
				"from" +
				"	" + VEHICLE_TABLE + " " +
				"where" +
				"	_id=" + v_id;
			c = db.rawQuery(stm, null);
			if (c.moveToFirst())
				v = getVehicle(c.getString(0));
		} catch (Exception e) {
			e.printStackTrace();
		} finally	{
			if (c != null)
				c.close();
		}
		
		return v;
	}
	
	/**
	 * Retrieve the ArrayList of Vehicle objects that are currently registered in
	 * the database
	 * @return - The ArrayList of Vehicle objects
	 */
	public synchronized ArrayList<Vehicle> getVehicles()	{
		
		ArrayList<Vehicle> list = new ArrayList<Vehicle>();
		Cursor c = null;
		
		try {
			String stm = 
				"select" +
				"	v.plate," +
				"	vb.brand," +
				"	vt.model," +
				"	vt.year," +
				"	vt.air_coef," +
				"	vt.roll_coef," +
				"	vt.class " +
				"from" +
				"	" + VEHICLE_TABLE + " as v " +
				"	inner join " + VEHICLE_TYPE_TABLE + " as vt on vt._id = v.type_id" +
				"	inner join " + VEHICLE_BRAND_TABLE + " as vb on vb._id = vt.b_id ";
			c = db.rawQuery(stm, null);
			int plate_index = c.getColumnIndex("plate");
			int brand_index = c.getColumnIndex("brand");
			int model_index = c.getColumnIndex("model");
			int year_index = c.getColumnIndex("year");
			int air_coef_index = c.getColumnIndex("air_coef");
			int roll_coef_index = c.getColumnIndex("roll_coef");
			int class_index = c.getColumnIndex("class");
			
			// Iterate over rows and populate the result list
			while (c.moveToNext())	{
				Vehicle v = new Vehicle(c.getString(brand_index),c.getString(model_index), c.getInt(year_index));
				v.setPlate(c.getString(plate_index));
				v.setType(c.getString(class_index));
				v.setAirCoef(c.getDouble(air_coef_index));
				v.setRollCoef(c.getDouble(roll_coef_index));
				list.add(v);
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally	{
			if (c != null)
				c.close();
		}
		
		return list;
	}
	
	/**
	 * Get the ID of a vehicle
	 * @param v - The Vehicle object
	 * @return - The ID of the vehicle. Returns -1 on error, 0 if it doesn't exist
	 */
	public synchronized long getVehicleID(Vehicle v)	{
		
		long id = -1;
		Cursor c = null;
		
		try {
			String plate = v.getPlate();
			String stm = 
				"select" +
				"	_id " +
				"from" +
				"	" + VEHICLE_TABLE + " " +
				"where" +
				"	plate='" + plate + "'";
			c = db.rawQuery(stm, null);
			if (c.moveToFirst())
				id = c.getInt(0);
		} catch (Exception e) {
			e.printStackTrace();
		} finally	{
			if (c != null)
				c.close();
		}
		
		return id;
	}
	
	/**
	 * Fetches the vehicle type ID given the vehicle ID
	 * @param v_id - The vehicle ID
	 * @return - The vehicle type ID
	 */
	public synchronized long getVehicleTypeID(long v_id)	{
		
		long id = -1;
		Cursor c = null;
		
		try {
			String stm = 
				"select" +
				"	type_id " +
				"from" +
				"	" + VEHICLE_TABLE + " " +
				"where" +
				"	_id=" + v_id;
			c = db.rawQuery(stm, null);
			if (c.moveToFirst())
				id = c.getLong(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return id;
	}
	
	/**
	 * Determine if the current vehicle tpye has already been calibrated
	 * @param vt_id - The vehicle type ID
	 * @return - True if the vehicle type has already been calibrated, false otherwise
	 */
	public synchronized boolean isVehicleTypeCalibrated(long vt_id)	{
		
		boolean res = false;
		Cursor c = null;
		
		try {
			String stm = 
				"select" +
				"	count(*) " +
				"from " + VEHICLE_CALIBRATION_TABLE + " " +
				"where" +
				"	vt_id=" + vt_id;
			c = db.rawQuery(stm, null);
			if (c.moveToFirst())
				if (c.getLong(0) > 0)
					res = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	/**
	 * Insert a new Vehicle object in the database
	 * @param v - The vehicle object
	 * @return - True on success, false otherwise
	 */
	public synchronized boolean insertVehicle(Vehicle v)
		throws VehicleAlreadyRegisteredException	{
		
		boolean res = false;
		Cursor c = null;
		
		try {
			// Proceed to insertion only if Vehicle object does not yet exists
			if (getVehicle(v.getPlate()) == null)	{
				// First we need to get the vehicle's brand ID
				String stm = 
					"select" + 
					"	_id " +
					"from" +
					"	" + VEHICLE_BRAND_TABLE + " " +
					"where" +
					"	brand='" + v.getBrand() + "'";
				c = db.rawQuery(stm, null);
				if (! c.moveToFirst())
					return false;
				long b_id = c.getLong(0);
				
				// Now check to see if this type of vehicle is already in database
				stm = 
					"select" +
					"	_id " +
					"from" +
					"	" + VEHICLE_TYPE_TABLE + " " +
					"where" +
					"	model='" + v.getModel() + "'" +
					"	and year=" + v.getYear() +
					"	and air_coef=" + v.getAirCoef() +
					"	and roll_coef=" + v.getRollCoef() +
					"	and class='" + v.getType() + "'";
				c.close();
				c = db.rawQuery(stm, null);
				long t_id = 0;
				if (c.moveToFirst())
					t_id = c.getLong(0);
				
				// This indicates that we have a new type of vehicle. Add it to the database
				if (t_id == 0)	{
					values.clear();
					values.put("b_id", b_id);
					values.put("model", v.getModel());
					values.put("year", v.getYear());
					values.put("air_coef", v.getAirCoef());
					values.put("roll_coef", v.getRollCoef());
					values.put("class", v.getType());
					if ((t_id = db.insert(VEHICLE_TYPE_TABLE, null, values)) == -1)
						return false;
				}
				
				// Associate a Vehicle with a certain type
				values.clear();
				values.put("type_id", t_id);
				values.put("plate", v.getPlate());
				res = (db.insert(VEHICLE_TABLE, null, values) != -1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally	{
			if (c != null)
				c.close();
			else
				throw new VehicleAlreadyRegisteredException();
		}
		
		return res;
	}
	
	/**
	 * Removes a vehicle from the database
	 * @param v - The Vehicle object
	 * @throws VehicleNotRegisteredException - When the Vehicle is not registered in
	 * the database
	 */
	public synchronized void removeVehicle(Vehicle v)
		throws VehicleNotRegisteredException	{
		
		boolean res = false;
		
		try {
			res = (db.delete(VEHICLE_TABLE, "plate='" + v.getPlate() + "'", null) == 1);
		} catch (Exception e) {
			e.printStackTrace();
			throw new VehicleNotRegisteredException(e.getMessage());
		}
		if (! res)
			throw new VehicleNotRegisteredException();
	}
	
	/**
	 * Associates a driver with a given vehicle, if not already associated
	 * @param d_id - The driver's ID
	 * @param v_id - The vehicle's ID
	 * @return - The ID that associates a driver to a vehicle
	 */
	public synchronized long associateDriverVehicle(long d_id, long v_id)	{
		
		long id = -1;
		Cursor c = null;
		if (d_id == 0 || v_id == 0)
			return id;
		
		try {
			c = db.query(DRIVER_VEHICLE_TABLE, new String[]{"_id"}, null, null, null, null, null);
			if (c.moveToNext())
				id = c.getLong(0);
			else	{
				values.clear();
				values.put("d_id", d_id);
				values.put("v_id", v_id);
				id = db.insert(DRIVER_VEHICLE_TABLE, null, values);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally	{
			if (c != null)
				c.close();
		}
		
		return id;
	}
	
	/**
	 * Inserts a new trip into the database
	 * @param dv_id - The driver vehicle ID
	 * @return - The newly created ID for the trip. Returns -1 on error
	 */
	public synchronized long startTrip(long dv_id)	{
		
		long id = -1;
		
		try {
			formatedDate = sdf.format(new Date());
			values.clear();
			values.put("dv_id", dv_id);
			values.put("start_date", "'" + formatedDate + "'");
			id = db.insert(TRIP_TABLE, null, values);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return id;
	}
	
	/**
	 * Signals the end of a trip. This method simply adds the end date of the trip into the database
	 * @param trip_id - The trip ID
	 */
	public synchronized void endTrip(long trip_id)	{
		
		try {
			formatedDate = sdf.format(new Date());
			values.clear();
			values.put("end_date", formatedDate);
			db.update(TRIP_TABLE, values, "_id=" + trip_id, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Inserts the gathered values of a specific trip. It uses transactions to avoid high frequency inserts
	 * into the database
	 * @param values - The ArrayList that contains the ContentValues objects that hold the information
	 * for a specific instant of a concrete trip
	 * @return - True if the insertion transaction has been successful, false otherwise
	 */
	public synchronized boolean insertTripData(ArrayList<ContentValues> values)	{
		
		boolean res = false;
		SQLiteStatement stm = null;

		db.beginTransaction();
		try {
			// Create the insert statement
			String sql = 
				"insert into" +
				"	" + TRIP_DATA_TABLE + " (trip_id, latitude, longitude, altitude, speed, acceleration, grade, vsp, timestamp)" +
				"	values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
			stm = db.compileStatement(sql);
			
			// Iterate over the array, extracting values as necessary
			for (ContentValues row : values) {
				stm.bindLong(1, row.getAsLong("trip_id"));
				stm.bindDouble(2, row.getAsDouble("latitude"));
				stm.bindDouble(3, row.getAsDouble("longitude"));
				stm.bindDouble(4, row.getAsDouble("altitude"));
				stm.bindDouble(5, row.getAsDouble("speed"));
				stm.bindDouble(6, row.getAsDouble("acceleration"));
				stm.bindDouble(7, row.getAsDouble("grade"));
				stm.bindDouble(8, row.getAsDouble("vsp"));
				stm.bindString(9, row.getAsString("timestamp"));
				if (stm.executeInsert() <= 0)
					Log.d(MODULE, "Failed insertion of trip data into database");
			}
			
			// Signal success and update result value
			db.setTransactionSuccessful();
			res = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally	{
			stm.close();
			db.endTransaction();
		}
		
		return res;
	}
	
	/**
	 * Start the beginning of the calibration process
	 * @param vt_id - The vehicle type ID
	 * @return - The vehicle calibration ID. Returns -1 on failure
	 */
	public synchronized long startCalibration(long vt_id)	{
		
		long id = -1;
		
		try {
			formatedDate = sdf.format(new Date());
			values.clear();
			values.put("vt_id", vt_id);
			values.put("start_date", formatedDate);
			id = db.insert(VEHICLE_CALIBRATION_TABLE, null, values);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return id;
	}
	
	/**
	 * Flags the end of the calibration process
	 * @param vc_id - The vehicle calibration ID
	 */
	public synchronized void endCalibration(long vc_id)	{
		
		try {
			formatedDate = sdf.format(new Date());
			values.clear();
			values.put("end_date", formatedDate);
			db.update(VEHICLE_CALIBRATION_TABLE, values, "_id=" + vc_id, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Removes the calibration info for a specific type of vehicle
	 * @param vc_id - The vehicle type ID
	 */
	public synchronized void removeCalibration(long vt_id)	{
		
		Cursor c = null;
		long vc_id = 0;
		
		try {
			String stm = 
				"select " +
				"	_id " +
				"from " + VEHICLE_CALIBRATION_TABLE + " " +
				"where " +
				"	vt_id=" + vt_id;
			c = db.rawQuery(stm, null);
			if (c.moveToFirst())
				vc_id = c.getLong(0);
			db.delete(CALIBRATION_SAMPLE_TABLE, "vc_id=" + vc_id, null);
			db.delete(VEHICLE_CALIBRATION_TABLE, "vt_id=" + vt_id, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Inserts calibration data into the database
	 * @param values - The rows that contain the data
	 * @return - True on success, false otherwise
	 */
	public synchronized boolean insertCalibrationSample(ArrayList<ContentValues> values)	{
		
		boolean res = false;
		SQLiteStatement stm = null;

		db.beginTransaction();
		try {
			// Create the insertion SQL
			String sql = 
				"insert into " +
				CALIBRATION_SAMPLE_TABLE + " (vc_id, latitude, longitude, altitude, speed, acceleration, grade, timestamp)" +
				"	values (?, ?, ?, ?, ?, ?, ?, ?)";
			stm = db.compileStatement(sql);
			
			// Iterate over values and insert rows individually
			for (ContentValues row : values)	{
				stm.bindLong(1, row.getAsLong("vc_id"));
				stm.bindDouble(2, row.getAsDouble("latitude"));
				stm.bindDouble(3, row.getAsDouble("longitude"));
				stm.bindDouble(4, row.getAsDouble("altitude"));
				stm.bindDouble(5, row.getAsDouble("speed"));
				stm.bindDouble(6, row.getAsDouble("acceleration"));
				stm.bindDouble(7, row.getAsDouble("grade"));
				stm.bindString(8, row.getAsString("timestamp"));
				if (stm.executeInsert() <= 0)
					Log.d(MODULE, "Failed insertion of calibration sampling data into database");
			}
			db.setTransactionSuccessful();
			res = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally	{
			stm.close();
			db.endTransaction();
		}
	
		return res;
	}
	
	/**
	 * Determine if a database already exists
	 * @return
	 */
	public static boolean databaseExists()	{
		
		File storage_file = new File(Environment.getExternalStorageDirectory(), "/fuel_consumption/" + DB_NAME);
		
		return storage_file.exists();
	}
	
	/**
	 * Initializes the database, creating it if necessary
	 */
	public static void initDatabase()	{
		
		try {
			// Set up the necessary directory and file. Create them only if necessary
			File storage_file = new File(Environment.getExternalStorageDirectory(), "/fuel_consumption");
			if (! storage_file.exists())
				if (! storage_file.mkdirs())	{
					Log.e(MODULE, "Failed to make directory");
					return;
				}
			storage_file = new File(storage_file, DB_NAME);
			
			// The database does not exist yet. We are going to create a file and
			// execute all necessary SQL statements to populate it
			if (! storage_file.exists())	{
				storage_file.createNewFile();
				createDatabase(storage_file.getAbsolutePath());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates the database and initially populates it
	 * @param path - The absoulte path of the database
	 */
	private static void createDatabase(String path)	{
		
		try {
			Log.d(MODULE, "Creating new database");
			db = SQLiteDatabase.openDatabase(
					path, 
					null, 
					SQLiteDatabase.CREATE_IF_NECESSARY);
			String stm = 
				"CREATE TABLE if not exists " + DRIVER_TABLE +
					"(_id integer NOT NULL PRIMARY KEY AUTOINCREMENT," +
					"first_name text NOT NULL," +
					"last_name text NOT NULL," +
					"passcode text NOT NULL)";
			db.execSQL(stm);
			stm = 
				"CREATE TABLE if not exists " + DRIVER_VEHICLE_TABLE +
					"(_id integer NOT NULL PRIMARY KEY AUTOINCREMENT," +
					"d_id integer NOT NULL," +
					"v_id integer NOT NULL," +
					"CONSTRAINT driver_id FOREIGN KEY (d_id) " +
					"	REFERENCES " + DRIVER_TABLE + " (_id) ON DELETE CASCADE ON UPDATE CASCADE," +
					"CONSTRAINT vehicle_id FOREIGN KEY (v_id) " +
					"	REFERENCES " + VEHICLE_TABLE + " (_id) ON DELETE CASCADE ON UPDATE CASCADE)";
			db.execSQL(stm);
			stm = 
				"CREATE TABLE if not exists " + TRIP_TABLE + 
					"(_id integer NOT NULL PRIMARY KEY AUTOINCREMENT," +
					"dv_id integer NOT NULL," +
					"start_date text," +
					"end_date text," +
					"CONSTRAINT driver_vehicle_id FOREIGN KEY (dv_id) " +
						"REFERENCES " + DRIVER_VEHICLE_TABLE + " (_id) ON DELETE CASCADE ON UPDATE CASCADE)";
			db.execSQL(stm);
			stm =
				"CREATE TABLE if not exists " + TRIP_DATA_TABLE +
					"(_id integer NOT NULL PRIMARY KEY AUTOINCREMENT,"+
					"trip_id integer NOT NULL," +
					"latitude real NOT NULL," +
					"longitude real NOT NULL," +
					"altitude real NOT NULL," +
					"speed real NOT NULL," +
					"acceleration real NOT NULL," +
					"grade real NOT NULL," +
					"vsp real NOT NULL," +
					"timestamp text NOT NULL," +
					"CONSTRAINT trip_id FOREIGN KEY (trip_id) " +
						"REFERENCES " + TRIP_TABLE + " (_id) ON DELETE CASCADE ON UPDATE CASCADE)";
			db.execSQL(stm);
			stm = 
				"CREATE TABLE if not exists " + VEHICLE_TABLE +
					"(_id integer NOT NULL PRIMARY KEY AUTOINCREMENT," +
					"type_id integer NOT NULL," +
					"plate text NOT NULL," +
					"CONSTRAINT vehicle_type_id FOREIGN KEY (type_id) " +
						"REFERENCES " + VEHICLE_TYPE_TABLE + " (_id) ON DELETE CASCADE ON UPDATE CASCADE)";
			db.execSQL(stm);
			stm = 
				"CREATE TABLE if not exists " + VEHICLE_BRAND_TABLE +
					"(_id integer NOT NULL PRIMARY KEY AUTOINCREMENT," +
					"brand text NOT NULL)";
			db.execSQL(stm);
			stm = 
				"CREATE TABLE if not exists " + VEHICLE_TYPE_TABLE + 
					"(_id integer NOT NULL PRIMARY KEY AUTOINCREMENT," +
					"model text NOT NULL," +
					"year text NOT NULL," +
					"air_coef real NOT NULL," +
					"roll_coef real NOT NULL," +
					"class text NOT NULL," +
					"b_id integer NOT NULL," +
					"CONSTRAINT brand_id FOREIGN KEY (b_id) " +
						"REFERENCES " + VEHICLE_BRAND_TABLE + " (_id) ON DELETE CASCADE ON UPDATE CASCADE)";
			db.execSQL(stm);
			stm = 
				"CREATE TABLE if not exists " + VEHICLE_CALIBRATION_TABLE + 
					"(_id integer NOT NULL PRIMARY KEY AUTOINCREMENT," +
					 "vt_id integer NOT NULL," + 
					 "start_date text," +
					 "end_date text," +
					 "CONSTRAINT vt_id FOREIGN KEY (vt_id) " +
					 	"REFERENCES " + VEHICLE_TYPE_TABLE + " (_id) ON DELETE CASCADE ON UPDATE CASCADE)";
			db.execSQL(stm);
			stm = 
				"CREATE TABLE if not exists " + CALIBRATION_SAMPLE_TABLE +
					"(_id integer NOT NULL PRIMARY KEY AUTOINCREMENT," +
					"vc_id integer NOT NULL," +
					"latitude real NOT NULL," +
					"longitude real NOT NULL," +
					"altitude real NOT NULL," +
					"speed real NOT NULL," +
					"acceleration real NOT NULL," +
					"grade real NOT NULL," +
					"timestamp text NOT NULL," +
					"CONSTRAINT vc_id FOREIGN KEY (vc_id) " + 
						"REFERENCES " + VEHICLE_CALIBRATION_TABLE + " (_id) ON DELETE CASCADE ON UPDATE CASCADE)";
			db.execSQL(stm);
			String[] brands = new String[]{"Audi", "Volkswagen", "Porsche", "Renault", "Volvo", 
					"Mercedes", "Scania", "Camo", "Nissan", "Mini", "BMW"};
			for (String brand : brands)	{
				ContentValues value = new ContentValues();
				value.put("brand", brand);
				db.insert(VEHICLE_BRAND_TABLE, null, value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method that creates a 3-digit passcode for a given driver
	 * @return - A 3-digit String that contains a passcode
	 */
	private String createPasscode()	{
		
		String content = "0123456789";
		StringBuffer passcode = new StringBuffer();
		Random rand = new Random();
		
		for (int i = 0; i < 3; i++)
			passcode.append(content.charAt(rand.nextInt(content.length())));
		
		return passcode.toString();
	}
	
	/**
	 * An exception thrown when an attempt to register a driver which already exists
	 * in the database happens
	 */
	public class DriverAlreadyRegisteredException extends Exception	{
		
		private static final long serialVersionUID = -5700453889283499846L;

		public DriverAlreadyRegisteredException() {}
		
		public DriverAlreadyRegisteredException(String message) {
			
			super(message);
		}
	}
	
	/**
	 * An exception thrown when an attempt to remove a driver which is not
	 * yet registered in the database happens 
	 */
	public class DriverNotRegisteredException extends Exception	{

		private static final long serialVersionUID = 6679865490802654746L;
		
		public DriverNotRegisteredException() { }
		
		public DriverNotRegisteredException(String message)	{
			
			super(message);
		}
	}
	
	/**
	 * An exception thrown when an attempt to register an already registered
	 * vehicle is done
	 */
	public class VehicleAlreadyRegisteredException extends Exception	{

		private static final long serialVersionUID = -8599079405568820483L;
		
		public VehicleAlreadyRegisteredException()	{ }
		
		public VehicleAlreadyRegisteredException(String message)	{
			
			super(message);
		}
	}
	
	/**
	 * An exception thrown when an attempt to gather information about
	 * an unregistered vehicle is done
	 */
	public class VehicleNotRegisteredException extends Exception	{

		private static final long serialVersionUID = -5244108331915551092L;
		
		public VehicleNotRegisteredException() {		}
		
		public VehicleNotRegisteredException(String message)	{
			
			super(message);
		}
	}
}