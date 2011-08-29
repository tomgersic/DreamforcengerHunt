/*
Copyright (c) 2011, Model Metrics 
All rights reserved. 
Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met: 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of Model Metrics nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package com.modelmetrics.dreamforcengerhunt.sfdcSoap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.modelmetrics.dreamforcengerhunt.GlobalState;
import com.modelmetrics.dreamforcengerhunt.R;
import com.modelmetrics.dreamforcengerhunt.sfdcRest.OAuthTokens;
import com.modelmetrics.dreamforcengerhunt.sfdcRest.OAuthUtil;
import com.modelmetrics.dreamforcengerhunt.util.GeneralUtil;

public class SfdcCustomCheckItemFound {
	public static String lastStatusLine;
	/**
	 * Calls SFDC Custom Webservice to determine if we have found a given item... note this is SOAP, not REST
	 * @param context
	 * @param accessTokens
	 * @param huntItemId
	 * @param Latitude
	 * @param Longitude
	 * @return the response from the SFDC webservice
	 */
	public static String checkItemFound(Context context, OAuthTokens accessTokens, String huntItemId, Float Latitude, Float Longitude)
	{
		String result = "";
		
		DefaultHttpClient client = new DefaultHttpClient();
		
		String url = "https://na1-api.salesforce.com/services/Soap/class/DH_Found_Item";//accessTokens.get_instance_url() + context.getResources().getString(R.string.CheckItemFoundURL);
		
		Log.d("Request URL: ",url);
		
		HttpPost request = new HttpPost(url);
		request.addHeader("Content-Type", "text/xml");
		request.addHeader("Soapaction", "\"\"");
		//request.addHeader("Host","na1-api.salesforce.com");
		//request.addHeader("User-Agent","Mac OS X; WebServicesCore (359)");

		String soapBody = context.getResources().getString(R.string.CheckItemFoundXML);
		soapBody = soapBody.replace("[SESSION_ID]",accessTokens.get_access_token());
		soapBody = soapBody.replace("[HUNT_ITEM_ID]",huntItemId);
		soapBody = soapBody.replace("[LATITUDE]",Latitude.toString());
		soapBody = soapBody.replace("[LONGITUDE]",Longitude.toString());
		
		try 
		{
			StringEntity se = new StringEntity(soapBody,HTTP.UTF_8);
			se.setContentType("text/xml");  
			
			request.setEntity(se);
			
			Log.d("REQUEST LINE:",request.getRequestLine().toString());

			HttpResponse response = client.execute(request);
		
			lastStatusLine = response.getStatusLine().toString();
			
			Log.d("HTTP Response: ",lastStatusLine);

			//if it isn't in the 100 or 200 range, something went wrong
			Integer httpStatusCode = response.getStatusLine().getStatusCode();
			String responseEntity = EntityUtils.toString(response.getEntity());
			if(httpStatusCode < 300)
			{
				result = responseEntity; 
			} 
			//      SOAP API gives this:
			//		08-16 07:54:03.682: DEBUG/HTTP Response:(302): HTTP/1.1 500 Internal Server Error
			//		08-16 07:54:03.732: DEBUG/API ERROR(302): <?xml version="1.0" encoding="UTF-8"?><soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:sf="http://soap.sforce.com/2006/08/apex"><soapenv:Body><soapenv:Fault><faultcode>sf:INVALID_SESSION_ID</faultcode><faultstring>INVALID_SESSION_ID: Invalid Session ID found in SessionHeader: Illegal Session. Session not found, missing session key: 00D30000001FyDg!ARUAQJnTiDbdgKQlPp5XDBvNtZCVP18aSj_42C053O.ITiFyf0D_Z_nJBOvq2i1qYzghraFqzSP7priFJqVkE_SrPALurw5X</faultstring></soapenv:Fault></soapenv:Body></soapenv:Envelope>
			else if(httpStatusCode == 401 || (httpStatusCode == 500 && responseEntity.contains("sf:INVALID_SESSION_ID"))) //Unauthroized or internal server error
			{	
				Log.d("API ERROR",EntityUtils.toString(response.getEntity()));
					
				//need to refresh the session id
				Boolean refreshResult = OAuthUtil.RefreshToken(context, accessTokens.get_refresh_token());

				GlobalState globalState = (GlobalState) ((Activity)context).getApplication();

				accessTokens = globalState.getAccessTokens();
				
				if(refreshResult)
				{
					result = checkItemFound(context, accessTokens, huntItemId, Latitude, Longitude);
				}
				else
				{
					result = null;
				}				
			}
			else
			{
				result = null; 
				Log.d("API ERROR",EntityUtils.toString(response.getEntity()));
			}
		} 

		catch (Exception e)
		{
			GeneralUtil.handleGeneralException(context, e);
			result =null;
		}	
		
		
		
		String response = "";
		//quick and dirty response parsing...
		Pattern pattern = Pattern.compile(".*<result>(.*)</result>.*");
		Matcher matcher = pattern.matcher(result);
		while (matcher.find()) {
		     response = matcher.group(1).toString();
		}
		Log.d("RESPONSE",response);
		return response;
	}
}
