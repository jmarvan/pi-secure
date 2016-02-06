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

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.synapticpath.pisecure.Config;
import com.synapticpath.pisecure.Configurable;
import com.synapticpath.pisecure.EventListener;
import com.synapticpath.pisecure.Module;
import com.synapticpath.pisecure.model.SystemEvent;
import com.synapticpath.pisecure.model.SystemEvent.Type;

/**
 * This module configures Raspberry GPIO pins for output to drive a anything
 * that needs to be switched on and off.
 * 
 * In this project, one instance will be configured to turn on/off 12V power
 * source for PIR sensors. The idea here is to only turn them off when needed.
 * 
 * The GPSWitchModule is responsible for reading switch configurations
 * instantiate the inner GPSwitch class and to pass on incoming events.
 * 
 * @author jmarvan@synapticpath.com
 *
 */
@Module
public class GPSwitchModule implements EventListener, Configurable {

	private List<GPSwitch> configuredSwitches;

	public void configure(Config config) {
		configuredSwitches = new ArrayList<GPSwitch>();
		
		String switchId = null;
		int index = 1;
		do {
			switchId = config.getProperty("gpswitch."+(index++), index==1);
			if (switchId != null) {
				GPSwitch cs = new GPSwitch(switchId);
				cs.configure(config);
				configuredSwitches.add(cs);
			}
		} while (switchId != null);
	}

	@Override
	public void onEvent(SystemEvent event) {
		configuredSwitches.forEach(configuredPin -> configuredPin.onEvent(event));
	}

	public class GPSwitch implements EventListener, Configurable {

		private GpioPinDigitalOutput configuredPin;

		private String id;
		
		private SystemEvent.Type switchOnAt;
		private SystemEvent.Type switchOffAt;
		

		public GPSwitch(String id) {
			this.id = id;
		}

		public void configure(Config config) {
			
			switchOnAt = Type.valueOf(config.getProperty("gpswitch." + id + ".on.event.type", false));
			switchOffAt = Type.valueOf(config.getProperty("gpswitch." + id + ".off.event.type", false));
			
			String pinId = config.getProperty("gpswitch." + id + ".gpioid", true);
			Pin pin = RaspiPin.getPinByName(pinId);
			System.out.println(String.format("Commissioning general purpose switch %s on raspberry pin %s.", id, pinId));

			try {
				final GpioController gpio = GpioFactory.getInstance();

				configuredPin = gpio.provisionDigitalOutputPin(pin, this.id, PinState.LOW);
				configuredPin.setShutdownOptions(true, PinState.LOW);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

		@Override
		public void onEvent(SystemEvent event) {

			if (event.getType().equals(switchOnAt)) {
				
				// Turn the switch on
				configuredPin.setState(PinState.HIGH);
				
			} else if (event.getType().equals(switchOffAt)) {
				
				configuredPin.setState(PinState.LOW);
			}

		}

	}
}
