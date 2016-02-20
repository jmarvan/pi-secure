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

import java.io.DataInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.synapticpath.pisecure.Config;
import com.synapticpath.pisecure.Configurable;
import com.synapticpath.pisecure.Module;
import com.synapticpath.pisecure.model.SecurityEvent;
import com.synapticpath.pisecure.model.SecurityEvent.Severity;
import com.synapticpath.pisecure.model.SystemEvent.Type;

/**
 * This module polls apcupsd socket interface, detecting and informing system about 
 * interesting changes to system runtime.
 * 
 * 
 * @author jmarvan@synapticpath.com
 *
 */
@Module
public class ApcUpsModule implements Configurable {
	
	private static final Pattern kvPattern = Pattern.compile("\\ *:\\ *");
	
	private Map<Long, Integer> batteryPercent;
	
	private int criticalLevel;
	
	private int pollInterval;
	
	private String socketAddress;
	
	private int port;
	
	private boolean batteryOperation;
	
	private Config config;
		

	@Override
	public void configure(Config config) throws Exception {
		batteryPercent = new LinkedHashMap<>();
		this.config = config;
		criticalLevel = Integer.parseInt(config.getProperty("acpups.module.critical.level", true));
		pollInterval = Integer.parseInt(config.getProperty("acpups.module.socket.poll.interval", "60000"));
		socketAddress = config.getProperty("acpups.module.socket.address", "localhost");
		port = Integer.parseInt(config.getProperty("acpups.module.socket.port", "3551"));
		//TODO, make this into EventListener, react to some events?
		
		Runnable runnable = () -> {
			while (true) {
				try {
					evaluateStatus();
					Thread.sleep(pollInterval);
				} catch (Exception e) {
					e.printStackTrace();		
					break;
				}
			}
		};
		new Thread(runnable).start();
		
	}
	
	private void evaluateStatus() throws Exception {
		Map<String,String> statusMap = getStatus();

		String bChargeValue = statusMap.get("BCHARGE"); 
		String statusValue = statusMap.get("STATUS");
		
		Number chargeValue = bChargeValue == null ? 100 : DecimalFormat.getInstance().parse(bChargeValue);
		boolean onBattery = statusValue.equalsIgnoreCase("ONBATT");
		
		if (batteryOperation) {
			batteryPercent.put(System.currentTimeMillis(), chargeValue.intValue());

			if (batteryCriticalReached()) {
				config.getSystemModule().accept(SecurityEvent.create(Type.BATTERY_CRITICAL, Severity.HIGH, "acpupsd"));
			}
		}		
		
		if (!batteryOperation && onBattery) {
			//Just detected transition to battery operation
			batteryOperation = true;
			config.getSystemModule().accept(SecurityEvent.create(Type.BATTERY_ON, Severity.LOW, "acpupsd"));
		}
		
		if (batteryOperation && !onBattery) {
			batteryPercent.clear();			
			config.getSystemModule().accept(SecurityEvent.create(Type.BATTERY_OFF, Severity.LOW, "acpupsd"));
		}
		
		batteryOperation = onBattery;
				
	}
	
	private boolean batteryCriticalReached() {
		
		boolean result = false;
		
		for (int value : batteryPercent.values()) {
			if (value < criticalLevel) {
				if (result) {
					return false;
				}
				result = true;
			}
		}
		
		return result;
	}
	
	
	private Map<String, String> getStatus() throws Exception {
		
		String msg = "status";		
		Socket socket = new Socket(socketAddress, port);
		
		DataInputStream stream = new DataInputStream(socket.getInputStream()); 
		
		byte bytes [] = {0,6};
		OutputStream os = socket.getOutputStream();
		os.write(bytes);
		os.write(msg.getBytes());
		os.flush();
		
		
		byte [] byteLen = new byte[2];
		int len = Integer.MAX_VALUE;
		
		Map<String, String> valueMap = new HashMap<>();
		
		while (len > 0) {
			stream.read(byteLen);
			len = byteToInt(byteLen);
			
			if (len > 0) {
				byte [] message = new byte [len]; 
				int size = stream.read(message);
				
				String[] parts = kvPattern.split(new String(message));
				if (parts.length == 2) {
					valueMap.put(parts[0], parts[1]);
				}
			}
		}
		
		socket.close();
		
		return valueMap;
	}
	
	public int byteToInt(byte[] bytes) {
        int val = 0;        
        for (int i = 0; i < bytes.length; i++) {
            val=val<<8;
            val=val|(bytes[i] & 0xFF);
        }
        return val;
    }
	 
	
}
