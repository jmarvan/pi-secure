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

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * This class holds configuration of the Pi-Secure system. It gets passed
 * to any module that declares the need using Configurable interface.
 * @author jmarvan@synapticpath.com
 *
 */
public class Config {	
	
	private Properties props;
	
	private List<Object> modules;
	
	private ResourceBundle resources;
	
	private SecuritySystem systemModule;	
	
	public void init(String configPath) throws Exception {		
		
		props = new Properties();
        props.load(getClass().getResourceAsStream("/config.properties"));

        if (configPath != null) {
        	File configFile = new File(configPath);
        	props.load(new FileInputStream(configFile));
        }
        
        String configLocale = getProperty("locale");
        Locale locale = configLocale == null ? Locale.getDefault() : new Locale(configLocale);
        resources = ResourceBundle.getBundle("resources", locale);
        
        modules = new ArrayList<>();
		
	}	
	
	public void setSystemModule(SecuritySystem system) {
		this.systemModule = system;
	}
	
	public SecuritySystem getSystemModule() {
		return systemModule;
	}
	
	public void addModule(Object module) {
		
		modules.add(module);
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> getModules(Class<T> moduleType) {
		
		List<T> result = new ArrayList<>();
		for (Object module : modules) {
			
			if (moduleType.isAssignableFrom(module.getClass())) {
				result.add((T)module);
			}
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getModule(Class<T> moduleClass) {
		
		for (Object module : modules) {
			if (moduleClass.isAssignableFrom(module.getClass())) {
				return (T)module;
			}
		}
		
		return null;
	}
	
	
	/**
	 * Calls resourceBundle.get
	 * @param key
	 */
	public String getMessage(String key) {
		return resources.getString(key);
	}
	
	
	/**
	 * Attempts to read from configuration a list of properties whose key starts with
	 * given prefix, ending with index number that is iterated from value 1.
	 * 
	 * Therefore a list of 2 properties can be formed by adding following into config file.
	 * 
	 * myproperty.1 = value1
	 * myproperty.2 = value2
	 * 
	 * To obtain a list with both values, call this method with prefix "myproperty."
	 * 
	 * @param prefix
	 * @return
	 */
	public List<String> getPropertyList(String prefix) {
        List<String> list = new ArrayList<String>();
        int index = 1;
        String value = null;
        do {
            value = props.getProperty(prefix+index++);
            if (value != null) {
                list.add(value);
            }

        } while (value != null);

        return list;
    }
	
	public String getProperty(String name) {
		return props.getProperty(name);
	}
	
	public String getProperty(String name, String defaultValue) {
		String value = props.getProperty(name); 
		return value == null ? defaultValue : value;
	}
	
	public String getProperty(String name, boolean required) {
		String value = props.getProperty(name);
		if (required && value == null) {
			throw new IllegalArgumentException("Propery named "+name+" is required, but not present in configuration file(s).");
		}
		return value; 
	}
	
	

}
