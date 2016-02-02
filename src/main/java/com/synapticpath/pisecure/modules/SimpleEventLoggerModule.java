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

import java.util.ArrayList;
import java.util.List;

import com.synapticpath.pisecure.Config;
import com.synapticpath.pisecure.Configurable;
import com.synapticpath.pisecure.EventLogger;
import com.synapticpath.pisecure.Module;
import com.synapticpath.pisecure.SystemEvent;

/**
 * This is the default event logger... it stores incoming events in a "queue" of
 * size that can be configured by the user.
 * 
 * @author jmarvan@synapticpath.com
 *
 */
@Module
public class SimpleEventLoggerModule implements EventLogger, Configurable {
	
	private List<SystemEvent> events = new ArrayList<>();
	
	private int limit;			

	@Override
	public void configure(Config config) throws Exception {
		limit = Integer.parseInt(config.getProperty("simple.event.logger.size", "100"));
	}	

    @Override
    public synchronized void onEvent(SystemEvent event) {    	
        boolean added = events.add(event);
        while (added && events.size() > limit) {
           events.remove(0);
        }
    }
    
    
    public synchronized List<SystemEvent> getEvents(int offset, int size) {
    	
    	List<SystemEvent> result = new ArrayList<>();
    	int index = events.size() - offset - 1;
    	while (index >= (events.size() - offset - size) && index > -1) {
    		SystemEvent event = events.get(index--);
    		result.add(event);
    	}
    	
    	return result;
    }
	
	public List<SystemEvent> getAllEvents() {
		return events;
	}

}
