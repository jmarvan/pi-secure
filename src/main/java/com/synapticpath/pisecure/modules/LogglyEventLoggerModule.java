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
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.synapticpath.pisecure.Config;
import com.synapticpath.pisecure.Configurable;
import com.synapticpath.pisecure.Disableable;
import com.synapticpath.pisecure.EventLogger;
import com.synapticpath.pisecure.Module;
import com.synapticpath.pisecure.SecuritySystem.SystemState;
import com.synapticpath.pisecure.model.PaginatedList;
import com.synapticpath.pisecure.model.SystemEvent;
import com.synapticpath.pisecure.model.SystemEvent.Type;
import com.synapticpath.utils.Logging;

/**
 * This module is responsible for sending incoming events to Loggly logging service.
 * 
 * @author jmarvan@synapticpath.com
 *
 */
@Module
public class LogglyEventLoggerModule implements Disableable, EventLogger, Configurable {
	
	private String postRequestUrl;
	private String searchRequestUrl;
	private String eventsRequestUrl;
	
	
	private String tag;
	
	private String token;
	
	private String auth;
	
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
		postRequestUrl = config.getProperty("loggly.logger.post.url", true);
		searchRequestUrl = config.getProperty("loggly.logger.search.url", true);
		eventsRequestUrl = config.getProperty("loggly.logger.events.url", true);
		
		disabled = Boolean.valueOf(config.getProperty("loggly.logger.disabled", "true"));
		
		tag = config.getProperty("loggly.tag", true);
		token = config.getProperty("loggly.token", true);

		String username = config.getProperty("loggly.username", true);
		String password = config.getProperty("loggly.password", true);
		
		String userpass = username+":"+password;
		
		auth = new sun.misc.BASE64Encoder().encode(userpass.getBytes());
		
	}

	@Override
	public void onEvent(SystemEvent event) {		
		if (disabled) {
			return;
		}
		logEvent(event);
	}

	@Override
	public PaginatedList<SystemEvent> getEvents(int offset, int size) {

		try {
			
			int pageNumber = offset == 0 ? 0: offset / size;
			
			return search("*"/*"tag:"+tag*/, pageNumber, size);
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private void logEvent(SystemEvent event) {
		
		try {
			byte[] postData = event.toJson().getBytes(StandardCharsets.UTF_8);
	
			int postDataLength = postData.length;
			URL url = new URL(String.format(postRequestUrl, token, tag));
	
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("charset", "utf-8");
			conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
			conn.setUseCaches(false);
			try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
				wr.write(postData);
			}
			
			if (conn.getResponseCode() == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuilder response = new StringBuilder();
				String inputLine = null;
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private PaginatedList<SystemEvent> search(String query, int page, int pageSize) throws Exception {
		
		URL url = new URL(String.format(searchRequestUrl, query, "-1m","now", pageSize));
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestProperty("Authorization", "Basic "+auth);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuilder sb = new StringBuilder();
		String inputLine = null;
		while ((inputLine = in.readLine()) != null) {
			sb.append(inputLine);
		}
		in.close();
		
		JSONObject obj = new JSONObject(sb.toString());
		String rsid = obj.getJSONObject("rsid").getString("id");
		
		
		url = new URL(String.format(eventsRequestUrl, rsid, page));
		
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestProperty("Authorization", "Basic "+auth);
		
		in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		sb = new StringBuilder();
		inputLine = null;
		while ((inputLine = in.readLine()) != null) {
			sb.append(inputLine);
		}
		in.close();
		Logging.debug(this, sb.toString());
		
		obj = new JSONObject(sb.toString());
		int total = obj.getInt("total_events");
		
		List<SystemEvent> items = new ArrayList<>();
		JSONArray array = obj.getJSONArray("events");		
		array.forEach(item -> {
				JSONObject event = ((JSONObject)item).getJSONObject("event"); 
				if (event.has("json")) {
					items.add(deserialize(event.getJSONObject("json")));
				}
			});
		
		items.forEach(item -> Logging.trace(this, item.toJson()));
		
		PaginatedList<SystemEvent> pl = PaginatedList.create(total, page * pageSize, pageSize, items);
		return pl;
		
	}
	
	private SystemEvent deserialize(JSONObject json) {
		
		SystemEvent event = new SystemEvent(); 
		event.setTime(json.getString("time"));
		event.setSource(json.getString("source"));
		event.setState(SystemState.valueOf(json.getString("state")));
		event.setType(Type.valueOf(json.getString("type")));
		
		return event;
	}

}
