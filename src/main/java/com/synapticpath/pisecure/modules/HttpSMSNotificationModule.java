/* Copyright (C) 2016 synapticpath.com - All Rights Reserved

 This file is part of Pi-Secure.

    Pi-Secure is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Pi-Secure is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Pi-Secure.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.synapticpath.pisecure.modules;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import com.synapticpath.pisecure.Config;
import com.synapticpath.pisecure.Configurable;
import com.synapticpath.pisecure.Disableable;
import com.synapticpath.pisecure.EventListener;
import com.synapticpath.pisecure.Module;
import com.synapticpath.pisecure.model.SecurityEvent;
import com.synapticpath.pisecure.model.SystemEvent;
import com.synapticpath.pisecure.model.SecurityEvent.Severity;
import com.synapticpath.utils.Logging;

/**
 * This module is responsible for sending a Text message through a Http sms gateway service when
 * it receives a SecurityEvent.
 * 
 * @author jmarvan@synapticpath.com
 *
 */
@Module
public class HttpSMSNotificationModule implements Disableable, EventListener, Configurable {
	
	private String strUrl;
	
	private String messageTemplate;
	
	private List<String> messageArgs;
	
	private List<String> numbers;
	
	private boolean disabled;
		

	@Override
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;	
	}

	@Override
	public boolean isDisabled() {
		return disabled;
	}

	@Override
	public void configure(Config config) throws Exception {				
		
		messageArgs = config.getPropertyList("smsGateway.argument.");
		numbers = config.getPropertyList("smsGateway.recipient.number.");
		strUrl = config.getProperty("smsGateway.url", true);
		disabled = Boolean.valueOf(config.getProperty("smsGateway.disabled", "true"));
		
		messageTemplate = config.getProperty("smsGateway.message.template", true);
		
	}

	/**
	 * Since sending sms event is paid service, log only severe SecurityEvents
	 */
	@Override
	public void onEvent(SystemEvent event) {
		
		if (!disabled && event instanceof SecurityEvent) {
			
			//TODO Implement security threshold logging, right now set at HIGH
			SecurityEvent secEvent = (SecurityEvent)event;
			if (Severity.HIGH.equals(secEvent.getSeverity())) {
				try {
					processEvent(secEvent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void processEvent(SecurityEvent secEvent) throws Exception {
		for(String number : numbers) {
			
			String text = String.format(messageTemplate, secEvent.getSeverity(), secEvent.getTime(), secEvent.getSource(), secEvent.getState());			
		
			Object [] args = new Object[messageArgs.size() + 2];
			args[0] = URLEncoder.encode(text, "UTF-8");
			args[1] = number;
			int index = 2;
			for (String arg : messageArgs) {
				args[index++] = arg;
			}
				
			send(args);
		}
	}
		
    public void send(Object ... args){
					
    	try {
			
			String message = String.format(strUrl, args);
			URL url = new URL(message);

	        HttpURLConnection con = (HttpURLConnection) url.openConnection();

	        // optional default is GET
	        con.setRequestMethod("GET");

	        //add request header
	        //con.setRequestProperty("User-Agent", "Mozilla/5.0");
	        int responseCode = con.getResponseCode();
	        Logging.info(this, "Sending 'GET' request to URL : %s", url);
	        Logging.info(this, "Response Code : %s", responseCode);

	        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	        String inputLine;
	        StringBuffer response = new StringBuffer();

	        while ((inputLine = in.readLine()) != null) {
	            response.append(inputLine);
	        }
	        in.close();

	        //TODO throw exception if response something other than 200
	        Logging.debug(this, response.toString());
    	} catch (Exception e) {
    		Logging.error(this, "Could not send SMS notification to recipient %s", args[1]);
    		e.printStackTrace();
    	}
		
	}
	
	
}
