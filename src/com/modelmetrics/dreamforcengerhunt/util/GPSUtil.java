/*
Copyright (c) 2011, Model Metrics 
All rights reserved. 
Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met: 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of Model Metrics nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package com.modelmetrics.dreamforcengerhunt.util;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GPSUtil {
	//singleton instance
	private static GPSUtil gpsUtil;
	private static final int GPS_UPDATE_INTERVAL = 1000 * 60 * 2;
	private Context context;
	private Location currentLocation;
	private LocationListener locationListener;
	
	private LocationManager locationManager;

	private Boolean gpsEnabled=false;
	
	public GPSUtil(Context context)
	{
		this.context = context;
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		locationListener = new LocationListener() 
		{
			public void onLocationChanged(Location location) 
			{
				// Called when a new location is found by the network location provider.
				if(isBetterLocation(location, currentLocation()))
				{
		    		  makeUseOfNewLocation(location);
				}
		    }

		    public void onStatusChanged(String provider, int status, Bundle extras) {}

		    public void onProviderEnabled(String provider) 
		    {
		    	if(provider==LocationManager.GPS_PROVIDER)
		    		gpsEnabled=true;
		    }

		    public void onProviderDisabled(String provider)
		    {
		    	if(provider==LocationManager.GPS_PROVIDER)
		    		gpsEnabled=false;
		    }	
		};
				
	}
	
	/**
	 * Return the current location... probably don't use this
	 * @return
	 */
	public Location getCurrentLocation()
	{
		return currentLocation;
	}
	
	/**
	 * GPSUtil is a singleton... get the instance
	 * @param context
	 * @return GPSUtil singleton instance
	 */
	public static GPSUtil getGpsUtil(Context context)
	{
		
		if(gpsUtil == null)
		{
			gpsUtil = new GPSUtil(context);

			gpsUtil.startLocationListener();
		}
		return gpsUtil;
	}

	/**
	 *  Register the listener with the Location Manager to receive location updates
	 */
	public void startLocationListener()
	{
		if(!gpsEnabled)
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	}
	
	/**
	 * Disable the location listener
	 */
	public void stopLocationListener()
	{
		if(gpsEnabled)
			locationManager.removeUpdates(locationListener);
	}

	/**
	 * get the current location
	 * @return last known location
	 */
	public Location currentLocation()
	{ 
		startLocationListener(); //just make sure this is running
		LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	}
	
	/**
	 * Keep track locally of the currentLocation... probably not massively useful since locationManager is doing the same thing
	 * @param location
	 */
	public void makeUseOfNewLocation(Location location)
	{
		currentLocation = location;
	}
	

	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > GPS_UPDATE_INTERVAL;
	    boolean isSignificantlyOlder = timeDelta < -GPS_UPDATE_INTERVAL;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
	
}
