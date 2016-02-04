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
import java.text.SimpleDateFormat;
import java.util.Date;

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

			String token = req.queryParams("token");
			String value = req.queryParams("value");

			if (config.getModule(LoginService.class).checkLoginToken(token)) {
				SystemState state = SystemState.valueOf(value.toUpperCase());
				SystemEvent event = SystemEvent.create(SystemEvent.Type.SETSTATE, "ui", state);
				config.getSystemModule().accept(event);

				return "";
			}
			res.status(401);
			return "";
		});
		get("/event", (req, res) -> {
			//TODO check token, this is a locked feature.
			String strOffset = req.queryParams("offset");
			int offset = strOffset == null ? 0 : Integer.parseInt(strOffset);
			return config.getModule(SimpleEventLoggerModule.class).getEvents(offset, 10).toJson();
		});
	}
	
	private void sendResource(Request req, Response res, String resourceName) throws Exception {
		sendResource(req, res, resourceName, true);
	}

	private void sendResource(Request req, Response res, String resourceName, boolean checkModified) throws Exception {

		System.out.println("UI Module serving : "+resourceName);
		if (checkModified) {
			File file = new File(getClass().getResource(resourceName).getFile());
			String value = req.headers("if-modified-since");
			
			if (value != null) {
				
				Date d = format.parse(value);
				
				if (file.lastModified() <= d.getTime()) {
					res.status(304);
					return;
				}
												
			}
			res.header("Last-Modified", format.format(new Date(file.lastModified())));
			res.header("Expires", "-1");
			res.header("Cache-Control", "must-revalidate, private");
			

		}
 
		res.status(200);

		final ServletOutputStream os = res.raw().getOutputStream();
		IOUtils.copy(getClass().getResourceAsStream(resourceName), os);
		os.close();
	}

	

}
