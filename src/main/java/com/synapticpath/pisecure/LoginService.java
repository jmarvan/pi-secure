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
 * The LoginService implementations provide validation and subsequent validation
 * of login credentials.
 * 
 *  When the login method is called, will generate a login token if provided credentials are correct.
 *  Subsequent interaction with the system will trigger check of provided login token.
 * 
 * @author jmarvan@synapticpath.com
 *
 */
public interface LoginService {
	
	/**
	 * If login pin successful, generate token.
	 * @param pin
	 * @return generated login token
	 */
	String login(String pin);
	
	
	/**
	 * Checks whether supplied login token is valid.
	 * @param token
	 * @return true if token is OK, false otherwise
	 */
	boolean checkLoginToken(String token);
		

}
