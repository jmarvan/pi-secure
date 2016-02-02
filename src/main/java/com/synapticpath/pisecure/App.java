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

import com.synapticpath.pisecure.SystemEvent.Type;
import com.synapticpath.utils.Reflection;

/**
 * This is the main Class of Pi-Secure home security system. 
 * It has 2 primary functions:
 * 1) To read and build system configuration.
 * 2) To instantiate system modules.
 *
 * @author jmarvan@synapticpath.com
 */
public class App {
	
	private static boolean keepRunning = true;
	
	private Config config;
	
	private SecuritySystemImpl system;
		
	
	public void init(String configPath) throws Exception {		
		
		config = new Config();
		config.init(configPath);		
		
		system = new SecuritySystemImpl();
		system.configure(config);
		config.setSystemModule(system);
		
		loadModules();

		//Following event lets everyone know we are good to go.
		system.accept(SystemEvent.create(Type.STARTUP, "system"));
	}	
	
	private void loadModules() throws Exception {
		
		 for (Class<Object> moduleClass : getModules()) {		
			 
			 //TODO Implement module blacklist - blacklisted modules will not be loaded nor configured.
			 System.out.println("Loading Module : "+moduleClass.getName());
			 config.addModule(loadModule(moduleClass));
			 
		 }		
		
	}
	
	
	private Set<Class<Object>> getModules() {
		
		return Reflection.getTypesWithAnnotation("com.synapticpath.pisecure.modules", Module.class);
		 
	}
	
	
	private <T> T loadModule(Class<T> cls) throws Exception {
				
		T obj = cls.newInstance();		
		
		if (obj instanceof Configurable) {
			((Configurable) obj).configure(config);
		}
		
		return obj;
	}	
	
	
	public void shutdown() {
		//TODO broadcast shutdown event.		
	}		
	
	
    public static void main( String ... args ) throws Exception {
    	
        App app = new App();
        app.init(args == null || args.length == 0 ? null : args[0]);
        
        while (keepRunning) {
        	Thread.sleep(500);
        }                
        app.shutdown();
    }
}
