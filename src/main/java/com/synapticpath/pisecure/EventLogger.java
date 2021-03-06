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

import com.synapticpath.pisecure.model.PaginatedList;
import com.synapticpath.pisecure.model.SystemEvent;

/**
 * Event logger is responsible for writing system logs as well as subsequent retrieval and filtering.
 * 
 * @author jmarvan@synapticpath.com
 *
 */
public interface EventLogger extends EventListener {		
		
	public PaginatedList<SystemEvent> getEvents(int offset, int size);

}
