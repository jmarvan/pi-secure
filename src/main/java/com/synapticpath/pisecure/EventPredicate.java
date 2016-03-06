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

import java.util.Set;
import java.util.function.BiPredicate;

import com.synapticpath.pisecure.model.SystemEvent;

/**
 * An enumeration of BiPredicate that are used for event filtering or event
 * detection. 
 * 
 * @author jmarvan@synapticpath.com
 *
 */
public enum EventPredicate implements BiPredicate<SystemEvent, Config> {

	UNARMED_SENSOR_EVENT((SystemEvent event, Config config) -> event.getType().isSensorEvent() && !config.getSystemModule().getState().isArmed()),
	DELAYED_ARM_SETSTATE_EVENT((SystemEvent event, Config config) -> event.getType().isSetstateEvent() && event.getState().isDelayedArm()),
	DISARMED_SETSTATE_EVENT((SystemEvent event, Config config) -> event.getType().isSetstateEvent() && event.getState().isDisarmed()),
	STARTUP_EVENT((SystemEvent event, Config config) -> event.getType().isStartupEvent()),
	SHUTDOWN_EVENT((SystemEvent event, Config config) -> event.getType().isShutdownEvent());

	private BiPredicate<SystemEvent, Config> pr;

	private EventPredicate(BiPredicate<SystemEvent, Config> pr) {
		this.pr = pr;
	}

	@Override
	public boolean test(SystemEvent event, Config config) {
		return this.pr.test(event, config);
	}
	
	public static boolean test(SystemEvent event, Config config, Set<EventPredicate> predicates) {
		for (EventPredicate predicate: predicates) {
			if (predicate.test(event, config)) {
				return true;
			}
		}
		return false;
	}

}
