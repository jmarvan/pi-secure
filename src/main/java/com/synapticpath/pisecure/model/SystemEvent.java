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
package com.synapticpath.pisecure.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.synapticpath.pisecure.Exportable;
import com.synapticpath.pisecure.SecuritySystem.SystemState;

/**
 * All that happens in the system is logged as an event.  The SecuritySystem may
 * elevate the event to {@link SecurityEvent}.
 *
 */
public class SystemEvent implements Exportable {	
	
	private static DateFormat dateFormat;
	
	private String source;
	
	private Date time;
	
	private SystemState state;
	
	private Long delay;
	
	private Type type;
	
	public static enum Type {
		STARTUP,
		SENSOR,
		LOGIN_LOCKED,
		LOGIN_BLOCKED,
		LOGIN_SUCCESS,
		LOGIN_INCORRECT,
		SETSTATE
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public SystemState getState() {
		return state;
	}

	public void setState(SystemState state) {
		this.state = state;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}	
	
	public void setTime(String time) {
		try {
			this.time = getDateFormatter().parse(time);
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
	}
	
	public Long getDelay() {
		return delay;
	}

	public void setDelay(Long delay) {
		this.delay = delay;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String toJson() {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n\"time\":\"").append(getDateFormatter().format(time)).append("\",\r\n");
		sb.append("\"source\":\"").append(source).append("\",\r\n");
		sb.append("\"type\":\"").append(type).append("\",\r\n");
		sb.append("\"delay\":\"").append(delay).append("\",\r\n");
		sb.append("\"state\":\"").append(state).append("\"\r\n}");
		return sb.toString();
	}
	
	public static SystemEvent create(Type type, String source) {
		return create(type, source, null);
	}
	
	public static SystemEvent create(Type type, String source, SystemState state) {
		SystemEvent event = new SystemEvent();
		event.type = type;
		event.source = source;
		event.state = state;
		event.time = new Date();
		return event;
	}
	
	public static DateFormat getDateFormatter() {
		if (dateFormat == null) {
			dateFormat = new SimpleDateFormat(getDateFormat());
		}
		return dateFormat;
	}
	
	public static String getDateFormat() {
		//TODO make this configurable?
		return "yyyy-MM-dd'T'HH:mm:ssZ";
	}

}
