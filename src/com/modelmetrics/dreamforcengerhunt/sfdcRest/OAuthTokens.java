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

import java.util.Calendar;

public class OAuthTokens {
	    	private String _access_token;
	    	private String _refresh_token;
	    	private String _instance_url;
	    	private String _id;
	    	private String _org_id;
	    	private String _user_id;
	    	private Calendar _issued_at;
	    	private String _signature;
	    	
	    	
	    	public String get_user_id() { return _user_id; }
	    	public void set_user_id(String _user_id) { this._user_id = _user_id; }

	    	public String get_org_id() { return _org_id; }
	    	public void set_org_id(String _org_id) { this._org_id = _org_id; }	    	
	    	
			public String get_access_token() {
				return _access_token;
			}
			public void set_access_token(String _access_token) {
				this._access_token = _access_token;
			}
			public String get_refresh_token() {
				return _refresh_token;
			}
			public void set_refresh_token(String _refresh_token) {
				this._refresh_token = _refresh_token;
			}
			public String get_instance_url() {
				return _instance_url;
			}
			public void set_instance_url(String _instance_url) {
				this._instance_url = _instance_url;
			}
			public String get_id() {
				return _id;
			}
			public void set_id(String _id) {
				this._id = _id;
			}
			public Calendar get_issued_at() {
				return _issued_at;
			}
			public void set_issued_at(Long issued_at) {
				this._issued_at = Calendar.getInstance();
				this._issued_at.setTimeInMillis(issued_at);
			}
			public String get_signature() {
				return _signature;
			}
			public void set_signature(String _signature) {
				this._signature = _signature;
			}
	      
}