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

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.modelmetrics.dreamforcengerhunt.GlobalState;
import com.modelmetrics.dreamforcengerhunt.R;
import com.modelmetrics.dreamforcengerhunt.sfdcRest.SfdcRestQuery;

public class HuntListViewController extends Activity {
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		//find the model
		GlobalState globalState = (GlobalState) getApplication();
		
		setContentView(R.layout.hunt_list);

		//get a reference to the hunts table from the view
		ListView huntsListView = (ListView)findViewById(R.id.hunt_list_view);
		
		//set click listener for huntsListView
		huntsListView.setOnItemClickListener(new OnItemClickListener() 
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				GlobalState globalState = (GlobalState) getApplication();
				try {
					globalState.setSelectedHuntItem(globalState.getHuntItems().getJSONObject(position));
				}
				catch(Exception e)
				{
					e.printStackTrace();
					Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
				}
				launchHuntItemDetail();
			}
		});
		
		//Query SFDC with SOQL
		JSONArray huntItems = new JSONArray();
		try
		{
			 huntItems = SfdcRestQuery.query("SELECT Id, Name FROM Hunt_Item__c WHERE Hunt__c = '"+globalState.getSelectedHunt().getString("Id")+"'", globalState.getAccessTokens(), this);
		}
		catch(JSONException e)
		{
			e.printStackTrace();
			Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
		}
		
		globalState.setHuntItems(huntItems);
		
		ArrayList<String> huntNames= new ArrayList<String>();
		
		//loop through the hunts JSONArray, and create a string array of the names
		for (int i=0;i<huntItems.length();i++) 
		{
			try
			{
				JSONObject record = (JSONObject) huntItems.get(i);
				huntNames.add(record.getString("Name"));
			}
			catch(JSONException e)
			{
				e.printStackTrace();
				Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
			}
			
		}
		
		//save the hunt names to the model
		globalState.setHuntNames(huntNames.toArray(new String[0]));
		
		//set the adapter for the hunts listview...shows each row as a string
		huntsListView.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, globalState.getHuntNames()));
	}
	
	protected void launchHuntItemDetail() {
        Intent i = new Intent(this, HuntItemViewController.class);
        startActivity(i);
    }

}
