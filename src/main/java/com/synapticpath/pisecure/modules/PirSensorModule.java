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

import java.util.Date;
import java.util.function.Consumer;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.synapticpath.pisecure.Config;
import com.synapticpath.pisecure.Configurable;
import com.synapticpath.pisecure.Module;
import com.synapticpath.pisecure.SystemEvent;

/**
 * This module interfaces with Raspberry PI GPIO pins and configures them for
 * input. It is expected that the pins will be in a "pull-up" configuration.
 * 
 * TODO devise generic ability to arm/disarm each loop. Perhaps by registering
 * each Senor loop as module?
 * 
 * @author jmarvan@synapticpath.com
 *
 */
@Module
public class PirSensorModule implements Configurable {	
	

	public void configure(Config config) {
		
		String loopId = null;
		int index = 1;
		do {
			loopId = config.getProperty("pir.sensor.loop."+(index++), index==1);
			if (loopId != null) {
				PirSensor sensor = new PirSensor(loopId, config.getSystemModule());
				sensor.configure(config);
			}
		} while (loopId != null);
	}
	
	private class PirSensor implements Configurable {
	
		private String id;
		private String name;
		private Long delay;	
		private Consumer<SystemEvent> consumer;
		
		private PinState activeState = PinState.HIGH;
		
		public PirSensor(String id, Consumer<SystemEvent> consumer) {
			this.id = id;
			this.consumer = consumer;
			System.out.println(String.format("Setting up sensor loop with id %s", id));
		}
		
		public void configure(Config config) {
			
			this.name = config.getProperty("pir.sensor."+id+".name", true);
			this.delay = Long.parseLong(config.getProperty("pir.sensor."+id+".delay", "0"));
			String pinId = config.getProperty("pir.sensor."+id+".gpioid", true);

			System.out.println(String.format("Commissioning pir loop %s on raspberry pin %s.", name, pinId));
			Pin pin = RaspiPin.getPinByName(pinId);
			
			try {
				final GpioController gpio = GpioFactory.getInstance();
		
		        final GpioPinDigitalInput gpioInput = gpio.provisionDigitalInputPin(pin, PinPullResistance.PULL_UP);
		        
		        gpioInput.addListener(new GpioPinListenerDigital() {
								
		            @Override
		            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {		                
		                
		                if (activeState.equals(event.getState())) {
		                	SystemEvent secEvent = new SystemEvent();
		                	secEvent.setSource(name);
		                	secEvent.setTime(new Date(System.currentTimeMillis()));
		                	secEvent.setType(SystemEvent.Type.SENSOR);
		                	secEvent.setDelay(delay);
		                	consumer.accept(secEvent);
		                }
		            }
		        });
			} catch (Throwable t) {
				t.printStackTrace();
			}
			
		}
	}

}
