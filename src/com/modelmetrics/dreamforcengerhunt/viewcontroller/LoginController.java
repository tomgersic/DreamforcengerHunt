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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.modelmetrics.dreamforcengerhunt.GlobalState;
import com.modelmetrics.dreamforcengerhunt.R;
import com.modelmetrics.dreamforcengerhunt.sfdcRest.OAuthTokens;
import com.modelmetrics.dreamforcengerhunt.sfdcRest.OAuthUtil;
import com.modelmetrics.dreamforcengerhunt.util.GeneralUtil;

public class LoginController extends Activity
{
	WebView webview;
	String callbackUrl;	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		//Load the login view
		setContentView(R.layout.login);
	        
        /* As per the OAuth 2.0 User-Agent Flow supported by Salesforce, we pass along the Client Id (aka Consumer Key) as a GET 
         * parameter. We also pass along a special String as the redirect URI so that we can verify it when Salesforce redirects
         * the user back to the mobile device
         */
        String consumerKey = this.getResources().getString(R.string.consumer_key).toString();
        String url = this.getResources().getString(R.string.oAuthUrl).toString();
        callbackUrl = this.getResources().getString(R.string.callbackUrl).toString();
        
        //the url to load into the web view -- this will bring up the SFDC login page, formatted for a mobile device
        String reqUrl = url + consumerKey + "&redirect_uri=" + callbackUrl;
        
        //find the web view 
        webview = (WebView) findViewById(R.id.webview);
        
        webview.setWebViewClient(new LoginWebViewClient(this));
        
        webview.getSettings().setJavaScriptEnabled(true);
        
        Log.d("Login URL",reqUrl);
        
        webview.loadUrl(reqUrl);
    }
    
    /**
     * Extend WebViewClient so we can monitor page loads and redirect when we get the callback url back from SFDC   
     * @author tomgersic
     *
     */
    private class LoginWebViewClient extends WebViewClient {
        
    	Activity act;
    	public LoginWebViewClient(Activity myAct) {
    		act = myAct;
    	}
    	
        @Override
        public void onPageFinished(WebView view, String url) {
            
	        Log.d("TG:", "Redirect URL: " + url);

	        //check if the redirect URL starts with the callbackUrl
	        //if it does, we're done with the web view, and need to parse the tokens we got back from SFDC
	        if (url.startsWith(callbackUrl)) 
	        {
	        	if(!url.contains("error"))
	        	{
		        	
		        	Log.d("TG","Redirecting to Main View");
		        	
		        	//parse the access tokens from the callbackUrl redirect
	        		OAuthTokens accessTokens = OAuthUtil.parseToken(url);
	        		
	        		//save the access tokens to disk for next time
	        		OAuthUtil.Save(accessTokens, getApplicationContext());
	        		
	        		//keep track of the access tokens in the model
	        		GlobalState gs = (GlobalState) getApplication();
	        		gs.setAccessTokens(accessTokens);
	        		
	        		//redirect to the main view controller
	        		Intent i = new Intent(act, MainViewController.class);
	        		startActivity(i);
	        	}
	        	else
	        	{
	        		GeneralUtil.toastIt(act, "Error, could not log in. Access denied");
	        	}
	        } 
        }
    }
    	
	

}
