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

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.modelmetrics.dreamforcengerhunt.GlobalState;
import com.modelmetrics.dreamforcengerhunt.R;
import com.modelmetrics.dreamforcengerhunt.util.GeneralUtil;


public class OAuthUtil {
	private static final String PREFS_NAME = "oAuthTokens";

    /** 
     * Per the OAuth 2.0 Use Agent flow supported by Salesforce, the redirect URI will contain the access token (among other
     * other things) after the '#' sign. This method extracts those values.
     * 
     * @param url
     **/
	public static OAuthTokens parseToken(String url) {
		String temp = "";
		OAuthTokens myTokens = new OAuthTokens();
		
		try
		{
			temp = url.split("#")[1];
	
			String[] keypairs = temp.split("&");
			
			for (int i=0;i<keypairs.length;i++) 
			{
				String[] onepair = keypairs[i].split("=");
				if (onepair[0].equals("access_token")) 
				{
					myTokens.set_access_token(URLDecoder.decode(onepair[1],"UTF-8"));
				} 
				else if (onepair[0].equals("refresh_token")) 
				{
					myTokens.set_refresh_token(URLDecoder.decode(onepair[1],"UTF-8"));
				} 
				else if (onepair[0].equals("instance_url")) 
				{
					myTokens.set_instance_url(URLDecoder.decode(onepair[1],"UTF-8"));
				} 
				else if (onepair[0].equals("id")) 
				{
					String idString = URLDecoder.decode(onepair[1]);
					myTokens.set_id(idString);
					String[] idTokens = idString.split("/");
					myTokens.set_org_id(idTokens[idTokens.length-2]);
					myTokens.set_user_id(idTokens[idTokens.length-1]);
				} 
				else if (onepair[0].equals("issued_at")) 
				{
					myTokens.set_issued_at(Long.valueOf(onepair[1]));
				} 
				else if (onepair[0].equals("signature")) 
				{
					myTokens.set_signature(URLDecoder.decode(onepair[1],"UTF-8"));
				}
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return myTokens;
	}
	
	/**
	 * Save these tokens to disk
	 * @param tokens
	 * @param context
	 */
	public static void Save(OAuthTokens tokens, Context context)
	{
		//We need an Editor object to make preference changes.
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("access_token", tokens.get_access_token());
		editor.putString("refresh_token", tokens.get_refresh_token());
		editor.putString("instance_url", tokens.get_instance_url());
		editor.putLong("issued_at", tokens.get_issued_at().getTimeInMillis());
		editor.putString("signature", tokens.get_signature());
		editor.putString("id", tokens.get_id());
		editor.putString("user_id", tokens.get_user_id());
		editor.putString("org_id", tokens.get_org_id());
		editor.commit();
	}
	
	/**
	 * Load these tokens from disk
	 * 
	 * @param context
	 * @return
	 */
	public static OAuthTokens Load(Context context)
	{
		OAuthTokens myTokens = new OAuthTokens();
		
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		
		//if we have an access token, populate the tokens... otherwise, just return null. The user needs to log in
		if(settings.getString("access_token", "").length()>0)
		{
			myTokens.set_access_token(settings.getString("access_token", ""));
			myTokens.set_refresh_token(settings.getString("refresh_token", ""));
			myTokens.set_instance_url(settings.getString("instance_url", ""));
			myTokens.set_issued_at(settings.getLong("issued_at", 0));
			myTokens.set_signature(settings.getString("signature", ""));
			myTokens.set_id(settings.getString("id", ""));
			myTokens.set_user_id(settings.getString("user_id", ""));
			myTokens.set_org_id(settings.getString("org_id", ""));
			return myTokens;
		}
		else
		{
			return null;
		}
		
	}
	
	/**
	 * Access Token (session id) has Expired. Get another one using the refresh token
	 * @param context
	 * @param refreshToken
	 * @return
	 */
	public static Boolean RefreshToken(Context context,String refreshToken)
	{
		String url = context.getResources().getString(R.string.oAuthRefreshUrl);		
		
		//set up the refresh query parameters... sent as post 
		List<NameValuePair> postParams = new ArrayList<NameValuePair>();
		postParams.add(new BasicNameValuePair("grant_type", "refresh_token"));
		postParams.add(new BasicNameValuePair("client_id", context.getResources().getString(R.string.consumer_key)));
		postParams.add(new BasicNameValuePair("client_secret",context.getResources().getString(R.string.consumer_secret)));
		postParams.add(new BasicNameValuePair("refresh_token", refreshToken));
		postParams.add(new BasicNameValuePair("format", "json"));
		
		DefaultHttpClient client = new DefaultHttpClient();
		
		//HTTP POST Request
		Log.d("Refresh Request URL: ",url);
		HttpPost request = new HttpPost(url);
		
		try 
		{
			request.setEntity(new UrlEncodedFormEntity(postParams));
		
			HttpResponse response = client.execute(request);
			
			//Log the http status
			String statusLine = response.getStatusLine().toString();
			Log.d("HTTP Response: ",statusLine);
			
			//check the HTTP status code... 
			Integer httpStatusCode = response.getStatusLine().getStatusCode();
			
			if(httpStatusCode < 300)
			{
				//get the response values
				HttpEntity entity = response.getEntity();
				String responseText = EntityUtils.toString(entity);
				
				//response is JSON... make a JSONObject with it
				JSONObject responseObject = new JSONObject(responseText);
				
				//get the model
        		GlobalState globalState = (GlobalState) ((Activity)context).getApplication();
        		
        		//load the current access tokens from globalState
        		OAuthTokens accessTokens = globalState.getAccessTokens();
        		
        		//update the access token an issued at
        		accessTokens.set_access_token(responseObject.getString("access_token"));
        		accessTokens.set_issued_at(responseObject.getLong("issued_at"));
        		
        		//save the accessTokens back to the model
        		globalState.setAccessTokens(accessTokens);
        		
        		//save these new tokens to disk
        		OAuthUtil.Save(accessTokens,context);
        		
        		//Success!
        		return true;
			}
			else
			{
				GeneralUtil.toastIt(context, statusLine);
				return false;
			}
		}
		catch(Exception e)
		{
			GeneralUtil.handleGeneralException(context, e);
			return false;
		}
	}
	
}
