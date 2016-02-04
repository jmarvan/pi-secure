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

import java.util.LinkedList;
import java.util.UUID;

import com.synapticpath.pisecure.Config;
import com.synapticpath.pisecure.Configurable;
import com.synapticpath.pisecure.LoginService;
import com.synapticpath.pisecure.Module;
import com.synapticpath.pisecure.model.SecurityEvent;
import com.synapticpath.pisecure.model.SystemEvent;
import com.synapticpath.pisecure.model.SecurityEvent.Severity;
import com.synapticpath.pisecure.model.SystemEvent.Type;

/** 
 * The LoginModule provides means for users to authenticate themselves 
 * to the system in order to perform UI actions such as arming the system
 * or deactivating it while in alarm etc.
 * 
 * @author jmarvan@synapticpath.com
 *
 */
@Module
public class LoginModule implements LoginService, Configurable {
	
	private LinkedList<Long> badAttemptQueue = new LinkedList<Long>();

    private String pin;

    private int attempts;

    private long t1;

    private long t2;
    
    private String loginToken;
    
    private long loginAt;
    
    private long loginTokenTTL;
    
    private Config config;

	@Override
	public void configure(Config config) throws Exception {
		this.config = config;
		pin = config.getProperty("login.pin", true);
		attempts = Integer.valueOf(config.getProperty("login.attempts", true));
		t1 = Long.valueOf(config.getProperty("login.incorrect.time.millis", true));
		t2 = Long.valueOf(config.getProperty("login.lock.time.millis", true));		
		loginTokenTTL = Long.valueOf(config.getProperty("login.token.ttl.millis", true));
	}

	@Override
	public String login(String inputPin) {
		if (badAttemptQueue.size() >= attempts) {
            if (getTimeBetweenAttempts() <= t1 && getLockTime() > t2){
            	badAttemptQueue.clear();
            } else if (getTimeBetweenAttempts() > t1) {
            	badAttemptQueue.remove();
            } else {
            	logSecurityEvent(Type.LOGIN_BLOCKED);
                return null;
            }
        }

       if (pin.equals(inputPin)) {
              
    	   	  loginToken = UUID.randomUUID().toString();
    	   	  loginAt = System.currentTimeMillis();
    	   	  badAttemptQueue.clear();
              logEvent(Type.LOGIN_SUCCESS);
              return loginToken;
       } 
       
      
      logEvent(Type.LOGIN_INCORRECT);
      badAttemptQueue.add(System.currentTimeMillis());
      if (badAttemptQueue.size() >= attempts) {
    	  logSecurityEvent(Type.LOGIN_LOCKED);
      }
      return null;
       
	}
	
	private void logEvent(Type type) {
		SystemEvent event = SystemEvent.create(type, "login");
	    config.getSystemModule().accept(event);
	}
	
	private void logSecurityEvent(Type type) {
		SecurityEvent event = SecurityEvent.create(type, Severity.LOW, "login");
	    config.getSystemModule().accept(event);
	}

	@Override
	public boolean checkLoginToken(String token) {
		if (loginToken != null && (loginAt + loginTokenTTL) < System.currentTimeMillis()) {
			return false;
		}

		//Current login token still valid
		return loginToken != null && loginToken.equals(token);
	}
	
	private long getTimeBetweenAttempts () {
        Long first = badAttemptQueue.getFirst();
        Long last = badAttemptQueue.getLast();
        return last - first;
    }

	
    private long getLockTime () {

        Long last = badAttemptQueue.getLast();
        return System.currentTimeMillis() - last;
    }
	
}
