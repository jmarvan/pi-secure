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

import java.util.function.Consumer;

import com.synapticpath.pisecure.model.SystemEvent;

/**
 * SecuritySystem interface defines public methods to which various system
 * Modules report events.
 * SecuritySystem implementation is responsible for evaluating the events
 * and will propagate them to the remainder of the system.  
 * 
 * @author jmarvan@synapticpath.com
 *
 */
public interface SecuritySystem extends Consumer<SystemEvent> {
	
	public static enum SystemState {
		DELAYED_ARM, ARMED, DELAYED_ALARM, ALARM, DISARMED;

		public boolean isArmed() {
			return this.equals(ARMED);
		}
		
		public boolean isDelayedArm() {
			return this.equals(DELAYED_ARM);
		}
		
		public boolean isDelayedAlarm() {
			return this.equals(DELAYED_ALARM);
		}
		
		public boolean isAlarm() {
			return this.equals(ALARM);
		}

	}
	
	public SystemState getState(); 

}
