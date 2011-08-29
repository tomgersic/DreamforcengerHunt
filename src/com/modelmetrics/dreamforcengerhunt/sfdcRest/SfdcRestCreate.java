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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.modelmetrics.dreamforcengerhunt.GlobalState;
import com.modelmetrics.dreamforcengerhunt.R;
import com.modelmetrics.dreamforcengerhunt.util.GeneralUtil;

public class SfdcRestCreate {
	public static String lastStatusLine;
	
	/**
	 * Create a new record in SFDC for the given SObject type
	 * @param sObjectType
	 * @param sObjectRecord
	 * @param accessTokens
	 * @param context
	 * @return
	 */
	public static Boolean create(String sObjectType, JSONObject sObjectRecord, OAuthTokens accessTokens, Context context)
	{
		DefaultHttpClient client = new DefaultHttpClient();
		JSONObject resultObject;
		Boolean successful;
		
		String url = accessTokens.get_instance_url() + 
					 "/services/data/"+context.getResources().getString(R.string.API_Version).toString()+
					 "/sobjects/"+sObjectType+"/";
		Log.d("Request URL: ",url);

		HttpPost request = new HttpPost(url);
		request.addHeader("Authorization", "OAuth " + accessTokens.get_access_token());
		request.addHeader("Content-Type", "application/json");
			
		try 
		{
			request.setEntity(new ByteArrayEntity(sObjectRecord.toString().getBytes("UTF8")));

			HttpResponse response = client.execute(request);
		
			lastStatusLine = response.getStatusLine().toString();
			
			Log.d("HTTP Response: ",lastStatusLine);

			//if it isn't in the 100 or 200 range, something went wrong
			Integer httpStatusCode = response.getStatusLine().getStatusCode();
			if(httpStatusCode < 300)
			{
				String result = EntityUtils.toString(response.getEntity()); 

				resultObject = (JSONObject) new JSONTokener(result).nextValue();
				successful = (Boolean)resultObject.get("success");
			
				if(!successful)
				{
					String[] errors = (String[])resultObject.get("errors");
					
					for(String error : errors)
					{
						GeneralUtil.toastIt(context, error);
					}
					return false;
				}
			}
			else if(httpStatusCode == 401) //Unauthroized
			{
				//need to refresh the session id
				Boolean refreshResult = OAuthUtil.RefreshToken(context, accessTokens.get_refresh_token());

				GlobalState globalState = (GlobalState) ((Activity)context).getApplication();

				accessTokens = globalState.getAccessTokens();
				
				if(refreshResult)
				{
					return create(sObjectType, sObjectRecord, accessTokens, context);
				}
				else
				{
					return null;
				}				
			}
			else
			{
				return false;
			}
		} 

		catch (Exception e)
		{
			GeneralUtil.handleGeneralException(context, e);
			return false;
		}	
		
		
		return successful;
	}
}
