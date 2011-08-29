/*
Copyright (c) 2011, Model Metrics 
All rights reserved. 
Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met: 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of Model Metrics nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package com.modelmetrics.dreamforcengerhunt.viewcontroller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.modelmetrics.dreamforcengerhunt.GlobalState;
import com.modelmetrics.dreamforcengerhunt.R;
import com.modelmetrics.dreamforcengerhunt.sfdcRest.SfdcRestCreate;
import com.modelmetrics.dreamforcengerhunt.sfdcRest.SfdcRestQuery;
import com.modelmetrics.dreamforcengerhunt.sfdcSoap.SfdcCustomCheckItemFound;
import com.modelmetrics.dreamforcengerhunt.util.GPSUtil;
import com.modelmetrics.dreamforcengerhunt.util.GeneralUtil;

public class HuntItemViewController extends Activity{
	Button geotagButton;
	JSONObject selectedHuntItem;
	Location currentLocation;
	final Float ACCEPTABLE_ACCURACY = 100.0f; // in meters
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		//find the model
		GlobalState globalState = (GlobalState) getApplication();
		
		//get hunt item details
		selectedHuntItem = globalState.getSelectedHuntItem();
		
		setContentView(R.layout.hunt_item);
		
		//get a reference to the text label that tells the user what hunt item they've selected
		TextView huntItemText = (TextView)findViewById(R.id.text_hunt_item);
		
		try
		{
			huntItemText.setText(selectedHuntItem.getString("Name"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
		}
		
		
		geotagButton = (Button)findViewById(R.id.button_geotag_it);
		geotagButton.setEnabled(true);
		geotagButton.setOnClickListener(new Button.OnClickListener()
		{
        	public void onClick(View buttonView) 
        	{
        		String isFound = sfdcCheckFound();
        		
        		//ACCEPTABLE_ACCURACY meters
        		if(isFound.equals("unacceptable"))
        		{
        			notifyAccuracyFail();
        		}
        		else if(isFound.equals("true"))
        		{
        			found();
        		}
        		else if(isFound.equals("false"))
        		{
        			notifyNotFound();
        		}
        		else if(isFound.equals("exception"))
        		{
        			notifyApiException();
        		}
        		
        	}
        });

		//if hunt item already found (in SFDC), display that in the view
		if(checkHuntFoundStatus())
		{
			notifyFound();
		}
	}
	
	/**
	 * Check whether this item has already been found
	 * @return true/false
	 */
	private Boolean checkHuntFoundStatus()
	{
		//find the model
		GlobalState globalState = (GlobalState) getApplication();
		
		//Query SFDC with SOQL
		JSONArray foundItem = new JSONArray();
		try
		{
			 foundItem = SfdcRestQuery.query("SELECT Id, Found__c FROM Found_Item__c WHERE Hunt_Item__c = '"+selectedHuntItem.getString("Id")+"' AND OwnerId='"+globalState.getAccessTokens().get_user_id()+"'", globalState.getAccessTokens(), this);
		}
		catch(JSONException e)
		{
			e.printStackTrace();
			Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
		}
		
		if(foundItem != null && foundItem.length() > 0)
			return true;
		else
			return false;
	}
	
	/**
	 * Check current location against SFDC Custom Webservice to see if we've found it
	 * @return
	 */
	private String sfdcCheckFound()
	{
		//get GPS singleton
		GPSUtil gpsUtil = GPSUtil.getGpsUtil(this);
		currentLocation = gpsUtil.getCurrentLocation();
		
		if(currentLocation == null)
			currentLocation = gpsUtil.currentLocation();	
		
		if(currentLocation==null)
				return "unacceptable";
		
		GlobalState globalState = (GlobalState) getApplication();
		
		Log.d("GPS Location",currentLocation.getLatitude()+", "+currentLocation.getLongitude());
		
		String itemFoundResult = "";
		
		//if the accuracy isn't good enough to determine if we're nearby
		if(currentLocation.getAccuracy() > ACCEPTABLE_ACCURACY)
		{
			GeneralUtil.toastIt(this, "GPS Accuracy Level is "+currentLocation.getAccuracy());
			return "unacceptable";
		}
		
		try
		{
			itemFoundResult = SfdcCustomCheckItemFound.checkItemFound(this, globalState.getAccessTokens(), globalState.getSelectedHuntItem().getString("Id"), (float)currentLocation.getLatitude(), (float)currentLocation.getLongitude());
		}
		catch(Exception e)
		{
			GeneralUtil.handleGeneralException(this, e);
		}
		return itemFoundResult;
	}
	
	/**
	 * @return distance between current location and expected location. Returns negative accuracy in meters if the accuracy is greater than ACCEPTABLE_ACCURACY meters
	 * 
	 * Note: Unused at this point... removed for added security
	 */
	/*private float checkGpsDistance()
	{
		//get GPS singleton
		GPSUtil gpsUtil = GPSUtil.getGpsUtil(this);
		currentLocation = gpsUtil.currentLocation();	
		
		GlobalState globalState = (GlobalState) getApplication();
		
		//the location we're trying to find
		Location destinationLocation = new Location(LocationManager.GPS_PROVIDER);
		try
		{
			destinationLocation.setLatitude(globalState.getSelectedHuntItem().getDouble("Latitude__c"));
			destinationLocation.setLongitude(globalState.getSelectedHuntItem().getDouble("Longitude__c"));
		}
		catch(Exception e)
		{
			GeneralUtil.handleGeneralException(this, e);
		}
		Log.d("GPS Location",6.getLatitude()+", "+currentLocation.getLongitude());
		
		float distanceTo = currentLocation.distanceTo(destinationLocation);
		
		Log.d("Distance to target (meters)",String.valueOf(distanceTo));
		
		//give a negative distance if the accuracy isn't good enough to determine if we're nearby
		if(currentLocation.getAccuracy() > ACCEPTABLE_ACCURACY)
			distanceTo = currentLocation.getAccuracy() * -1;
		
		return distanceTo;
	}*/
	
	/**
	 * Item found
	 */
	private void found()
	{
		notifyFound();
		/*if(!saveFound())
		{
			GeneralUtil.toastIt(this, this.getResources().getString(R.string.API_ERROR));
			GeneralUtil.toastIt(this, SfdcRestCreate.lastStatusLine);
		}*/
	}
	
	/**
	 * Save Found to SFDC
	 */
	private Boolean saveFound()
	{
		//find the model
		GlobalState globalState = (GlobalState) getApplication();

		try
		{
			JSONObject foundItemRecord = new JSONObject();
			foundItemRecord.put("Hunt_Item__c",selectedHuntItem.get("Id"));
			foundItemRecord.put("Found__c","true");
			foundItemRecord.put("Latitude__c",currentLocation.getLatitude());
			foundItemRecord.put("Longitude__c",currentLocation.getLongitude());
			
			return SfdcRestCreate.create("Found_Item__c", foundItemRecord, globalState.getAccessTokens(), this);
		}
		catch(Exception e)
		{
			GeneralUtil.handleGeneralException(this,e);
			return false;
		}
	}
	
	/**
	 * Notify Found
	 **/
	private void notifyFound()
	{
		notify((TextView)findViewById(R.id.hunt_text_found),this.getResources().getString(R.string.Item_Found));
		geotagButton = (Button)findViewById(R.id.button_geotag_it);
		geotagButton.setEnabled(false);
	}
	
	/**
	 * Notify not found
	 */
	private void notifyNotFound()
	{
		notify((TextView)findViewById(R.id.hunt_text_found),this.getResources().getString(R.string.Nope_Keep_Looking)); 
	}
	
	/**
	 * Notify accuracy bad
	 */
	private void notifyAccuracyFail()
	{
		notify((TextView)findViewById(R.id.hunt_text_found),this.getResources().getString(R.string.Accuracy_Inadequate));
		
	}

	/**
	 * Notify Api Exception
	 */
	private void notifyApiException()
	{
		notify((TextView)findViewById(R.id.hunt_text_found),this.getResources().getString(R.string.API_Exception_Message)); 
	}
	
	/**
	 * notify
	 * @param notifyView: text view to show a message in
	 * @param notification: the text to show
	 */
	private void notify(TextView notifyView, String notification)
	{
		//get a reference to the text label that tells the user what hunt item they've selected
		TextView huntItemText = notifyView;
		huntItemText.setText(notification);
		
		GeneralUtil.toastIt(this, notification);			
	}
}
