/*
Copyright (c) 2011, Model Metrics 
All rights reserved. 
Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met: 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of Model Metrics nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package com.modelmetrics.dreamforcengerhunt.sfdcRest;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.modelmetrics.dreamforcengerhunt.GlobalState;
import com.modelmetrics.dreamforcengerhunt.R;
import com.modelmetrics.dreamforcengerhunt.util.GeneralUtil;

public class SfdcRestListObjects {

	public static JSONArray listObjects(OAuthTokens accessTokens, Context context)
	{
		DefaultHttpClient client = new DefaultHttpClient();
		
		//Build the query string
		String url = accessTokens.get_instance_url() + "/services/data/"+
					 context.getResources().getString(R.string.API_Version).toString()+
					 "/sobjects/";

		//HTTP GET Request for Queries
		Log.d("Request URL: ",url);
		HttpGet getRequest = new HttpGet(url);
		
		//Add the Auth Header with the access token (session id)
		getRequest.addHeader("Authorization", "OAuth " + accessTokens.get_access_token());
		//getRequest.addHeader("X-PrettyPrint","1");
		JSONArray sobjects = new JSONArray();
		try 
		{
			//execute the request, and get a response back
			HttpResponse response = client.execute(getRequest);
			
			String statusLine = response.getStatusLine().toString();
			Log.d("HTTP Response: ",statusLine);
			
			//if it isn't in the 100 or 200 range, something went wrong
			Integer httpStatusCode = response.getStatusLine().getStatusCode();
			if(httpStatusCode < 300)
			{
				//get the result set, and return a JSONArray of records	
				String result = EntityUtils.toString(response.getEntity()); 
				JSONObject object = (JSONObject) new JSONTokener(result).nextValue();
				sobjects = object.getJSONArray("sobjects");
			}
			else if(httpStatusCode == 401) //Unauthorized
			{
				//need to refresh the session id -- start Refresh Token flow
				Boolean refreshResult = OAuthUtil.RefreshToken(context, accessTokens.get_refresh_token());

				GlobalState globalState = (GlobalState) ((Activity)context).getApplication();
				
				//get the new access tokens
				accessTokens = globalState.getAccessTokens();
				
				if(refreshResult)
				{
					//recurse -- run the user's query again. 
					//Note, this will try this forever, so it might be a good idea to limit this at some point. 
					return listObjects(accessTokens,context);
				}
				else
				{
					return null;
				}
			}
			else
			{
				return null;
			}
			
		}
		catch(Exception e)
		{
			GeneralUtil.handleGeneralException(context, e);
		}
		//Log.d("SOBJECTS",sobjects.toString());
		return sobjects;
	}
}
