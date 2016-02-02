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
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.synapticpath.pisecure.SecurityEvent.Severity;
import com.synapticpath.pisecure.SystemEvent.Type;

/**
 * This is the brain of the Pi-Secure security system.  It evaluates events sent
 * to it and acts on them to update its internal state, and notifies EventListeners of notable
 * changes.
 * 
 * @author jmarvan@synapticpath.com
 *
 */
public class SecuritySystemImpl implements SecuritySystem, Configurable {
	
	private SystemState state;
	
	private Queue<SystemEvent> queue = new ConcurrentLinkedQueue<SystemEvent>();
	
	private Config config;	

	public SecuritySystemImpl() {	
		state = SystemState.DISARMED;	
	}
	
	public void configure(Config config) {
		this.config = config;		
	}

	public SystemState getState() {
		return state;
	}
	
	public void accept(SystemEvent event) {
		
		if (event.getState() == null) {
			event.setState(state);
		}
				
		queue.add(event);
		process();
		
		//Now handle the event.
		onEvent(event);
	}

	protected void process() {

		SystemEvent event = null;
		while ((event = queue.poll()) != null) {

			for (EventListener listener : config.getModules(EventListener.class)) {
				listener.onEvent(event);
			}
		}
				
	}
		
	
	protected void onEvent(SystemEvent event) {	
	
		switch (event.getType()) {
			case SENSOR:
				if (state.isArmed()) {
					alarm(event);
				}
				break;
			case SETSTATE :
				this.state = event.getState();
				break;
				
			default: break;
		}
		
	}


	public void alarm(SystemEvent event) {
		System.out.println("ALARM!");		
		
		SecurityEvent secEvent = new SecurityEvent();
		secEvent.setSeverity(Severity.HIGH);
		secEvent.setSource(event.getSource());
		secEvent.setTime(new Date());
		secEvent.setType(Type.SETSTATE);	
		secEvent.setState(SystemState.ALARM);
		accept(secEvent);
	}
	

}
