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

import com.synapticpath.pisecure.model.SecurityEvent;
import com.synapticpath.pisecure.model.SystemEvent;
import com.synapticpath.pisecure.model.SecurityEvent.Severity;
import com.synapticpath.pisecure.model.SystemEvent.Type;

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
	
	private int armDelay;
	
	private Runner armDelayRunner;
	
	private Runner delayedAlarmRunner;

	public SecuritySystemImpl() {	
		state = SystemState.DISARMED;	
	}
	
	public void configure(Config config) {
		this.config = config;
		this.armDelay = Integer.parseInt(config.getProperty("pisecure.arm.delay.millis", "0"));
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
				if (state.isArmed() || state.isDelayedAlarm()) {
					alarm(event);
				}
				break;
			case SETSTATE :
				setStateFromEvent(event);
				break;
				
			default: break;
		}
		
	}


	public void alarm(SystemEvent event) {
		
		SecurityEvent secEvent = new SecurityEvent();
		secEvent.setSource(event.getSource());
		secEvent.setTime(new Date());
		secEvent.setType(Type.SETSTATE);
		
		//Start delayed alarm
		if (state.isArmed() && event.getDelay() > 0) {
			System.out.println("DELAYED ALARM!");
			secEvent.setSeverity(Severity.LOW);
			secEvent.setState(SystemState.DELAYED_ALARM);
			secEvent.setDelay(event.getDelay());
			
		} else if (event.getDelay() == 0) {
			
			System.out.println("ALARM!");
			secEvent.setSeverity(Severity.HIGH);	
			secEvent.setState(SystemState.ALARM);
			
		} else {
			//We must be in DELAYED_ALARM and delayed sensor is triggered
			//This changes nothing 
			return;
		}
		
		accept(secEvent);
	}
	
	private void setStateFromEvent(SystemEvent event) {
		
		if (SystemState.ARMED.equals(event.getState()) && armDelay > 0 && !getState().equals(SystemState.DELAYED_ARM)) {
			SystemEvent delayedEvent = SystemEvent.create(Type.SETSTATE, "system");
			delayedEvent.setState(SystemState.DELAYED_ARM);			
			accept(delayedEvent);					
			
		} else {		
			this.state = event.getState();
			checkDelayedArm(event);
			checkDelayedAlarm(event);
		}
		
						
	}
	
	private void checkDelayedAlarm(SystemEvent event) {
		if (delayedAlarmRunner != null) {
			delayedAlarmRunner.cancel();
		}
		
		if (SystemState.DELAYED_ALARM.equals(event.getState())) {
			SystemEvent delayedEvent = SystemEvent.create(Type.SETSTATE, "system");
			delayedEvent.setState(SystemState.ALARM);
			delayedAlarmRunner = new Runner(delayedEvent, event.getDelay());
			new Thread(delayedAlarmRunner).start();
		}
	}
	
	private void checkDelayedArm(SystemEvent event) {
		if (armDelayRunner != null) {
			//First cancel pending 
			armDelayRunner.cancel();
		}	
		
		if (SystemState.DELAYED_ARM.equals(state)) {
			SystemEvent delayedEvent = SystemEvent.create(Type.SETSTATE, "system");
			delayedEvent.setState(SystemState.ARMED);
			armDelayRunner = new Runner(delayedEvent, armDelay);
			new Thread(armDelayRunner).start();
		}		
	}
	
	
	private class Runner implements Runnable {
		
		private boolean cancelled;
		private long delay;
		private SystemEvent eventToSend;
		
		public Runner(SystemEvent eventToSend, long delay) {
			this.delay = delay;
			this.eventToSend = eventToSend;
		}
		
		public void cancel() {
			cancelled = true;
		}				

		@Override
		public void run() {
			try {
				Thread.sleep(delay);
				if (!cancelled) {				
					System.out.println("Firnig delayed event "+eventToSend.toJson());
					eventToSend.setTime(new Date());
					accept(eventToSend);
				}
			}catch (InterruptedException ie) {
				
			}
		}
		
	}

}
