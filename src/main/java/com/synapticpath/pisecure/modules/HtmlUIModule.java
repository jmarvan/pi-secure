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

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.webSocket;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Supplier;

import javax.servlet.ServletOutputStream;

import com.synapticpath.pisecure.Config;
import com.synapticpath.pisecure.Configurable;
import com.synapticpath.pisecure.EventListener;
import com.synapticpath.pisecure.LoginService;
import com.synapticpath.pisecure.Module;
import com.synapticpath.pisecure.SecuritySystem.SystemState;
import com.synapticpath.pisecure.model.SystemEvent;
import com.synapticpath.pisecure.model.SystemEvent.Type;

import spark.Request;
import spark.Response;
import spark.utils.IOUtils;

/**
 * This module uses spark to provide HTML ui for controlling the SecuritySystem.
 * 
 * @author jmarvan@synapticpath.com
 *
 */
@Module
public class HtmlUIModule implements Configurable, EventListener {

	private Config config;
	
	private static final SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

	public void configure(Config config) {

		this.config = config;
		setupWeb();
	}

	@Override
	public void onEvent(SystemEvent event) {
		if (event.getType().equals(Type.SETSTATE)) {
			System.out.println("HtmlUIModule intercepted setstate to : "+event.getState());
			WebSocketHandler.broadcastState(event.getState());
		}
	}

	protected void setupWeb() {

		WebSocketHandler.config = config;
		webSocket("/state", WebSocketHandler.class);
		get("/", (req, res) -> {
			sendResource(req, res, "/index.html");
			return "";
		});
		get("/code", (req, res) -> {
			sendResource(req, res, "/code.js");
			return "";
		});
		get("/style", (req, res) -> {
			sendResource(req, res, "/style.css");
			return "";
		});
		get("/manifest", (req, res) -> {
			sendResource(req, res, "/manifest.json");
			return "";
		});
		get("/icon-hi.png", (req, res) -> {
			sendResource(req, res, "/icon-hi.png");
			return "";
		});
		get("/icon-lo.png", (req, res) -> {
			sendResource(req, res, "/icon-lo.png");
			return "";
		});
		post("/login", (req, res) -> {
			String pin = req.body();
			String token = config.getModule(LoginService.class).login(pin);
			if (token == null) {
				res.status(401);
				return "";
			}
			return token;
		});
		post("/state", (req, res) -> {
			
			return withLoginToken(req, res, () -> {
				String value = req.queryParams("value");
				SystemState state = SystemState.valueOf(value.toUpperCase());
				SystemEvent event = SystemEvent.create(SystemEvent.Type.SETSTATE, "ui", state);
				config.getSystemModule().accept(event);

				return "";
			});			
		});
		get("/event", (req, res) -> {			
			return withLoginToken(req, res, () -> {
				String strOffset = req.queryParams("offset");
				int offset = strOffset == null ? 0 : Integer.parseInt(strOffset);
				return config.getModule(SimpleEventLoggerModule.class).getEvents(offset, 10).toJson();
			}); 
		});
	}
	
	private String withLoginToken(Request req, Response res, Supplier<String> supplier) {
		
		String token = req.queryParams("token");
		
		if (config.getModule(LoginService.class).checkLoginToken(token)) {			
			return supplier.get();			
		}
		res.status(401);
		return "";		
	}
	
	private void sendResource(Request req, Response res, String resourceName) throws Exception {
		sendResource(req, res, resourceName, true);
	}

	private void sendResource(Request req, Response res, String resourceName, boolean checkModified) throws Exception {

		if (checkModified) {
			URL url = getClass().getResource(resourceName);
			Date lastModified = new Date(url.openConnection().getLastModified());
					
			String value = req.headers("If-Modified-Since");
			
			if (value != null && !value.isEmpty()) {
				
				Date d = format.parse(value);
				
				if (lastModified.getTime() <= d.getTime()) {
					res.status(304);
					return;
				}
												
			}
			res.header("Last-Modified", format.format(lastModified));
			res.header("Expires", "-1");
			res.header("Cache-Control", "must-revalidate, private");
			

		}
 
		res.status(200);

		final ServletOutputStream os = res.raw().getOutputStream();
		IOUtils.copy(getClass().getResourceAsStream(resourceName), os);
		os.close();
	}

	

}
