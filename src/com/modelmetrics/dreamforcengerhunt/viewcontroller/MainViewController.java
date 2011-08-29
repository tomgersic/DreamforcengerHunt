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
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.modelmetrics.dreamforcengerhunt.R;

public class MainViewController extends Activity {
	ImageButton huntsButton;
	ImageButton logoutButton;
	ImageButton mmButton;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		huntsButton = (ImageButton)findViewById(R.id.button_hunts);
		huntsButton.setOnClickListener(new Button.OnClickListener()
		{
        	public void onClick(View buttonView) 
        	{
        		launchAvailableHunts(); 
        	}
        });
		
		logoutButton = (ImageButton)findViewById(R.id.button_logout);
		logoutButton.setOnClickListener(new Button.OnClickListener()
		{
        	public void onClick(View buttonView) 
        	{
        		launchLoginDialog(); 
        	}
        });
	
		mmButton = (ImageButton)findViewById(R.id.button_mm);
		mmButton.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View buttonView)
			{
				launchMM();
			}
		});
		
	}
	
	protected void launchAvailableHunts()
	{
		Intent i = new Intent(this, AvailableHuntsViewController.class);
		startActivity(i);
	}

	protected void launchLoginDialog()
	{
		Intent i = new Intent(this, LoginController.class);
		startActivity(i);
	}
	
	protected void launchMM()
	{
		Uri uri = Uri.parse("http://www.modelmetrics.com/");
		Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(launchBrowser);   
	}
}
