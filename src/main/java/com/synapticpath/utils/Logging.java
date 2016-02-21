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
package com.synapticpath.utils;

import java.io.PrintStream;
import java.util.Date;

/**
 * This is a simple logger, it outputs to console. In the future as needed
 * turn this into facade for another logging service or add other PrintStreams.
 * 
 * @author jmarvan@synapticpath.com
 *
 */
public class Logging {
	
	private static enum LogLevel {ERROR, INFO, DEBUG, TRACE}
	
	private static LogLevel loglevel = LogLevel.INFO;
	
	private static PrintStream out = System.out;
	
	static {
		String ll = System.getProperty("logLevel");
		try {
			if (ll != null) {
				LogLevel.valueOf(ll);
			}
		} catch (Exception e) {
			error(Logging.class, "Could not determine the logLevel from %s", e, ll);
		}
	}
	
	private static void print(Class<?> source, LogLevel severity, String message) {
		print(source, severity, message, null);
	}
	
	private static void print(Class<?> source, LogLevel severity, String message, Exception exception) {
		out.println(severity +" "+source.getSimpleName()+": "+getDate()+" - "+ message);
		if (exception != null) {
			exception.printStackTrace(out);
		}
	}
	
	public static void info(Object source, String template, Object ... args) {
		print(source.getClass(), LogLevel.INFO, String.format(template, args));
	}
	
	public static void debug(Object source, String template, Object ... args) {
		if (LogLevel.DEBUG.equals(loglevel)) {
			print(source.getClass(), LogLevel.DEBUG, String.format(template, args));
		}
	}
	
	public static void trace(Object source, String template, Object ... args) {
		if (LogLevel.TRACE.equals(loglevel)) {
			print(source.getClass(), LogLevel.TRACE, String.format(template, args));
		}
	}
	
	public static void error(Object source, String template, Object ... args) {
		error(source, template, null, args);
	}
	
	public static void error(Object source, String template, Exception exception, Object ... args) {
		error(source.getClass(), template, exception, args);
	}
	
	public static void error(Class<?> source, String template, Exception exception, Object ... args) {
		print(source, LogLevel.ERROR, String.format(template, args));
	}
	
	private static String getDate() {
		return String.format("%1$te-%1$tm-%1$tY %1tT", new Date());
	}

}
