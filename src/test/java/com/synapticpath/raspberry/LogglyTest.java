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
package com.synapticpath.raspberry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;

import com.synapticpath.pisecure.Config;
import com.synapticpath.pisecure.SecuritySystem.SystemState;
import com.synapticpath.pisecure.SystemEvent;
import com.synapticpath.pisecure.SystemEvent.Type;
import com.synapticpath.pisecure.modules.LogglyEventLoggerModule;

/**
 * Tests loggly logging and retrieval
 * @author jmarvan@synapticpath.com
 *
 */
public class LogglyTest {
	
	@Ignore
	@Test
	public void test() throws Exception {
		Config config = new Config();
		config.init("../config.properties");
		
		LogglyEventLoggerModule logger = new LogglyEventLoggerModule();
		logger.configure(config);
		
		SystemEvent event = createEvent();
		logger.onEvent(event);
		
		//Loggly has some time delay before being able to retrieve posted event, therefore
		//This is not a good way to test the api ......
		List<SystemEvent> events = logger.getEvents(0, 0);
		assertNotNull(events);
		assertTrue(events.size() > 0);
		
		SystemEvent retrievedEvent = events.iterator().next();
		assertEquals(retrievedEvent.getTime(), event.getTime());
		assertEquals(retrievedEvent.getSource(), event.getSource());
				
		
	}
	
	private SystemEvent createEvent() {
		SystemEvent event = SystemEvent.create(Type.SENSOR, UUID.randomUUID().toString(), SystemState.DISARMED);
		return event;
	}

}
