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

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.synapticpath.pisecure.Config;
import com.synapticpath.pisecure.Configurable;
import com.synapticpath.pisecure.Disableable;
import com.synapticpath.pisecure.EventListener;
import com.synapticpath.pisecure.Module;
import com.synapticpath.pisecure.SecuritySystem.SystemState;
import com.synapticpath.pisecure.model.SecurityEvent;
import com.synapticpath.pisecure.model.SystemEvent;

/**
 * This module configures Raspberry GPIO pin for output to drive a
 * siren device.
 * 
 * @author jmarvan@synapticpath.com
 *
 */
@Module
public class SirenModule implements Disableable, EventListener, Configurable {

	private String name;
	private Boolean disabled;

	private GpioPinDigitalOutput sirenPin;	
		

	@Override
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;		
	}

	@Override
	public boolean isDisabled() {
		return disabled;
	}

	public void configure(Config config) {
		this.name = config.getProperty("siren.name", true);
		this.disabled = Boolean.valueOf(config.getProperty("siren.disabled", "false"));

		Pin pin = RaspiPin.getPinByName(config.getProperty("siren.gpioid", true));
		
		try {
			final GpioController gpio = GpioFactory.getInstance();
	
			sirenPin = gpio.provisionDigitalOutputPin(pin, this.name, PinState.LOW);
			sirenPin.setShutdownOptions(true, PinState.LOW);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public void onEvent(SystemEvent event) {
		if (!disabled) {

			if (SystemEvent.Type.SETSTATE.equals(event.getType())) {

				if (event instanceof SecurityEvent && SystemState.ALARM.equals(event.getState())) {
					// Turn on siren
					sirenPin.setState(PinState.HIGH);

					// TODO enable siren auto-shutoff after some time.
				} else {
					sirenPin.setState(PinState.LOW);
					// TODO reset auto-shutoff timer if present
				}

			}
		}
	}
}
