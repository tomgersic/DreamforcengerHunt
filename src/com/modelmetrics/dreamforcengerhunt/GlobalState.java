/*
Copyright (c) 2011, Model Metrics 
All rights reserved. 
Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met: 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of Model Metrics nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package com.modelmetrics.dreamforcengerhunt;

import org.json.JSONArray;
import org.json.JSONObject;

import com.modelmetrics.dreamforcengerhunt.sfdcRest.OAuthTokens;

import android.app.Application;

public class GlobalState extends Application {
	private OAuthTokens accessTokens;
	private String huntNames[];
	private JSONArray hunts;
	private JSONObject selectedHunt;
	private JSONArray huntItems;
	private JSONObject selectedHuntItem;

	/** Access Tokens**/
	public OAuthTokens getAccessTokens() { return accessTokens; }
	public void setAccessTokens(OAuthTokens accessTokens) { this.accessTokens = accessTokens; }
	
	/**Hunt Names**/
	public String[] getHuntNames() { return huntNames; }
	public void setHuntNames(String[] huntNames) { this.huntNames = huntNames; }
	
	/** Hunts**/
	public JSONArray getHunts() { return hunts; }
	public void setHunts(JSONArray hunts) { this.hunts = hunts; }
	
	/**Selected Hunt**/
	public JSONObject getSelectedHunt(){ return selectedHunt; }
	public void setSelectedHunt(JSONObject selectedHunt) { this.selectedHunt = selectedHunt; }
	
	/** Hunt Items **/
	public JSONArray getHuntItems() { return huntItems; }
	public void setHuntItems( JSONArray huntItems ) { this.huntItems = huntItems; }
	
	/** Selected Hunt Item**/
	public JSONObject getSelectedHuntItem() { return selectedHuntItem; }
	public void setSelectedHuntItem( JSONObject selectedHuntItem ) { this.selectedHuntItem = selectedHuntItem; }
}
