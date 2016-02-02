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
package com.synapticpath.pisecure;

import java.util.Date;

import com.synapticpath.pisecure.SecuritySystem.SystemState;

/**
 * Instance of this object is being sent to indicate that system is experiencing
 * a security issue, each event carries severity information to indicate the seriousness
 * the situation.
 * 
 *    The HIGH severity events indicate a serious event, LOW severity events indicate a security warning.
 * 
 * @author jmarvan@synapticpath.com
 *
 */
public class SecurityEvent extends SystemEvent {
	
	public enum Severity {
		HIGH, LOW;
	};
	
	public SecurityEvent() {
		
	}

	private Severity severity;

	public Severity getSeverity() {
		return severity;
	}

	public void setSeverity(Severity severity) {
		this.severity = severity;
	}
	
	public static SecurityEvent create(Type type, Severity severity, String source) {
		return create(type, severity, source, null);
	}
	
	public static SecurityEvent create(Type type, Severity severity, String source, SystemState state) {
		SecurityEvent event = new SecurityEvent();
		event.setType(type);
		event.setSource(source);
		event.setState(state);
		event.setTime(new Date());
		event.setSeverity(severity);
		return event;
	}
	

}
