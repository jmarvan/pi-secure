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

/**
 * Modules that can be disabled need to implement this interface through
 * which they will report their state.
 * 
 * @author jmarvan@synapticpath.com
 *
 */
public interface Disableable {

	/**
	 * Each module can be disabled and enabled. Disabled modules are responsible for handling
	 * their state.  If for example Module is an event listener, it should take no action
	 * when receiving an event while being in disabled state.
	 * 
	 * @param disabled
	 */
	void setDisabled(boolean disabled);
	
	/**
	 * Returns the current state of a Module.
	 * @return
	 */
	boolean isDisabled();
	
}
