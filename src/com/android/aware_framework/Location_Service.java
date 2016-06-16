/*
 * Created by Denzil Ferreira
 * dferreir@andrew.cmu.edu
 * 
 * Location service and listener for Android Aware framework
 * Last updated: 28-Jun-2010
 * Modified by: Denzil Ferreira
 */
package com.android.aware_framework;

import java.io.IOException;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

/*
 * Example of usage:
 * 
 * INSIDE ACTIVITY:
 * 
 * //we are using Singleton, always returns same instance in memory
 * Location_Service locationService = Location_Service.getService();
 * 
 * //we can change the service parameters like this. Attention, recommended minimum in UpdateInterval is 1 minute for battery's sake
 * locationService.setUpdateIntervalGPS(1); //in minutes
 * locationService.setUpdateIntervalNetwork(1); //in minutes
 * locationService.setMinDistanceGPS(10); //in meters
 * locationService.setMinDistanceNetwork(3000); //in meters
 * 
 * //Get the current best location data, based on accuracy:
 * Location currentLocation = locationService.getCurrentBestLocation();
 * 
 * //get current GPS fix
 * Location currentGPS = locationService.getGPSLocation();
 * 
 * //get current Network fix
 * Location currentNetwork = locationService.getNetworkLocation();
 * 
 * //get human readable address according to current best location
 * List<Address> address = locationService.getReadableLocations();
 * 
 * //get the location listener
		locationService.setLocationListener(new Location_Service.OnLocationChanged() {
			@Override
			public void onLocationGPSChanged(Location local) {
				
				List<Address> addresses = locationSrv.getReadableLocation(local);
				
				StringBuilder addressStr = new StringBuilder();
				
				for(Address street: addresses) {
					for(int i=0; i<street.getMaxAddressLineIndex();i++) {
						addressStr.append(street.getAddressLine(i)+"\n");
					}
					addressStr.append(street.getCountryName());
				}
				
				Log.e(TAG,"GPS provided: "+addressStr);
				
			}

			@Override
			public void onLocationNetworkChanged(Location local) {
				
				//If we didn't get a GPS fix on the last 5 minutes, assuming we are indoors.
				if(local.getTime() > (locationSrv.getGPSLocation().getTime()+5*60*1000)) 
				{
					List<Address> addresses = locationSrv.getReadableLocation(local);
					
					StringBuilder addressStr = new StringBuilder();
					
					for(Address street: addresses) {
						for(int i=0; i<street.getMaxAddressLineIndex();i++) {
							addressStr.append(street.getAddressLine(i)+"\n");
						}
						addressStr.append(street.getCountryName());
					}
					
					Log.e(TAG,"Network provided: "+addressStr);
					
				}
			}
		});
		
 * Intent locationIntent = new Intent(this,Location_Service.class);
 * startService(locationIntent);
 * 
 */
public class Location_Service extends Service {

	private static LocationManager locationManager;
	private static Geocoder geocoder;
	
	private static Location gpsLocation;
	private static Location netLocation;
	
//	private static LocationListener listenerNetwork;
	private static LocationListener listenerGPS;
	
	private static long updateIntervalGPS = 1; //refresh rate in minutes (only applies if > 0)
	private static int updateIntervalNetwork = 1;
	
	private static float minDistanceGPS = 10; //interval in meters
	private static float minDistanceNetwork = 3000;
	
	private static Context context;
	
	private Handler handler;
	
	// Let's add a new method to the OnLocationChanged interface so that
	// status change may be propagated
	private static OnLocationChanged locationListener;
	public interface OnLocationChanged extends GpsStatus.Listener {
		public abstract void onLocationGPSChanged(Location local);
//		public abstract void onLocationNetworkChanged(Location local);
	}
	public void setLocationListener(OnLocationChanged listener) {
		locationListener = listener;
	}
	public OnLocationChanged getLocationListener() {
		return locationListener;
	}
	
	//Singleton pattern - you always get the same LocationService, even on the Binder
	private static final Location_Service locationService = new Location_Service();
	public static Location_Service getService() {
		return locationService;
	}
	
	//Create service binder, so that we can bind activities to this service if needed
	private LocationBinder locationBinder = new LocationBinder();
	public class LocationBinder extends Binder {
		public Location_Service getService() {
			return Location_Service.getService();
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return locationBinder;
	}
	
	public void setHandler(Handler handler)	{
		
		this.handler = handler;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if(context == null)
			context = this.getApplicationContext();
		
		if(geocoder == null)
			geocoder = new Geocoder(context);
		
		startLocationListeners();
		
		Log.d("AWARE", "Location Service running!");
	}
	
	public void setApplicationContext(Context context) {
		Location_Service.context = context;
	}
	
	private void startLocationListeners() {
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		//Set up location criterias, this is recommended way to save battery!
		Criteria critGPS = new Criteria();
		critGPS.setAccuracy(Criteria.ACCURACY_FINE);
		
		Criteria critNetwork = new Criteria();
		critNetwork.setAccuracy(Criteria.ACCURACY_COARSE);
		
		//Try to get last known location, this should be unaccurate, but we will give the device a starting point
		gpsLocation = locationManager.getLastKnownLocation(
				locationManager.getBestProvider(critGPS, false)); //try to get last GPS
		
		//Make sure we have the location listeners created and running
		if(listenerGPS == null)
			createLocationListeners();
		
		// Make sure we have the correct Looper object so that messages are received in
		// the correct Thread. We don't want to be sending location messages to the main
		// looper as this will damage the responsiveness of the application
		Looper l = (handler == null) ? getMainLooper() : handler.getLooper();
		
		//we'll start by getting a network fix, to be fast and give a starting point for GPS
		locationManager.requestLocationUpdates(
				locationManager.getBestProvider(critGPS, true),
				updateIntervalGPS,
				minDistanceGPS, 
				listenerGPS,
				l);
		locationManager.addGpsStatusListener(locationListener);
	}
	
	private void createLocationListeners() {
//		listenerNetwork = new LocationListener() {
//			
//			@Override
//			public void onStatusChanged(String provider, int status, Bundle extras) {
//				
//				locationListener.onProviderStatusChange(provider, status, extras);
//			}
//			@Override
//			public void onProviderEnabled(String provider) {
//				
//				locationManager.removeUpdates(listenerNetwork);
//				locationManager.removeUpdates(listenerGPS);
//				startLocationListeners();
//			}
//			@Override
//			public void onProviderDisabled(String provider) {
//				
//				locationManager.removeUpdates(listenerNetwork);
//				locationManager.removeUpdates(listenerGPS);
//				startLocationListeners();
//			}
//			@Override
//			public void onLocationChanged(Location location) {
//				
//				netLocation = location;
//				if(locationListener!=null)
//					locationListener.onLocationNetworkChanged(location);
//			}
//		};
		
		listenerGPS = new LocationListener() {
			
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {	}
			@Override
			public void onProviderEnabled(String provider) {
				
//				locationManager.removeUpdates(listenerNetwork);
				locationManager.removeUpdates(listenerGPS);
				startLocationListeners();
			}
			@Override
			public void onProviderDisabled(String provider) {
				
//				locationManager.removeUpdates(listenerNetwork);
				locationManager.removeUpdates(listenerGPS);
				startLocationListeners();
			}
			@Override
			public void onLocationChanged(Location location) {
			
				gpsLocation = location;
				if(locationListener!=null)
					locationListener.onLocationGPSChanged(location);
			}
		};
	}
	
	public Location getCurrentBestLocation() {
		
		return gpsLocation;
//		if(gpsLocation.getAccuracy()>netLocation.getAccuracy() && gpsLocation.getTime()>netLocation.getTime())
//			return gpsLocation;
//		else return netLocation;
	}
	
	public Location getNetworkLocation() {
		
		return netLocation;
	}
	
	public Location getGPSLocation() {
		
		return gpsLocation;
	}
	
	public List<Address> getReadableLocation(Location currentLocation) {
		List<Address> addresses = null;
		try {
			if(currentLocation!=null)
				addresses = Location_Service.geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
		}catch(IOException e) {
			Log.e("AWARE", "Error @ Reversing Geocoding "+e.toString());
		}catch(NullPointerException e) {
			Log.e("AWARE", "Error @ Current location reverse geocoding"+e.toString());
		}
		
		return addresses;
	}

	public static long getUpdateIntervalGPS() {
		
		return updateIntervalGPS;
	}

	public static void setUpdateIntervalGPS(long updateIntervalGPS) {
		
		Location_Service.updateIntervalGPS = updateIntervalGPS;
	}

	public static int getUpdateIntervalNetwork() {
		return updateIntervalNetwork;
	}

	public void setUpdateIntervalNetwork(int updateIntervalNetwork) {
		Location_Service.updateIntervalNetwork = updateIntervalNetwork;
	}

	public static float getMinDistanceGPS() {
		return minDistanceGPS;
	}

	public void setMinDistanceGPS(float minDistanceGPS) {
		Location_Service.minDistanceGPS = minDistanceGPS;
	}

	public static float getMinDistanceNetwork() {
		return minDistanceNetwork;
	}

	public void setMinDistanceNetwork(float minDistanceNetwork) {
		Location_Service.minDistanceNetwork = minDistanceNetwork;
	}
	
	public GpsStatus getGPSStatus(GpsStatus gps_status)	{
		
		return locationManager.getGpsStatus(gps_status);
	}
	
	@Override
	public void onDestroy() {
		
//		locationManager.removeUpdates(listenerNetwork);
		locationManager.removeUpdates(listenerGPS);
		
		Log.d("AWARE","Location Service terminated...");
		
		super.onDestroy();
	}
}
