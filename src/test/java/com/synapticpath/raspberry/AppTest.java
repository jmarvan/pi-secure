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
package com.synapticpath.raspberry;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.synapticpath.pisecure.Config;
import com.synapticpath.pisecure.LoginService;
import com.synapticpath.pisecure.SecuritySystem.SystemState;
import com.synapticpath.pisecure.SecuritySystemImpl;
import com.synapticpath.pisecure.model.SystemEvent;
import com.synapticpath.pisecure.model.SystemEvent.Type;
import com.synapticpath.pisecure.modules.LoginModule;
import com.synapticpath.pisecure.modules.SMTPNotificationModule;
import com.synapticpath.pisecure.modules.SimpleEventLoggerModule;
import com.synapticpath.pisecure.modules.HttpSMSNotificationModule;

/**
 * Unit test for simple App.
 */
public class AppTest {
	
	private Config config;
	
	@Before
	public void init() throws Exception {
		config = new Config();
		config.init(null);
		
		SecuritySystemImpl system = new SecuritySystemImpl();
		system.configure(config);
		config.setSystemModule(system);
		
		SimpleEventLoggerModule logger = new SimpleEventLoggerModule();
		logger.configure(config);
				
		config.addModule(logger);
	}
		
	
	@Ignore
	@Test
	public void testMail() throws Exception {

		 SMTPNotificationModule mailer = new SMTPNotificationModule();
		 mailer.configure(config);
		 //mailer.sendMessage("Security event");
	}

	@Ignore
	@Test
	public void testSms() throws Exception {

		HttpSMSNotificationModule sender = new HttpSMSNotificationModule();
		sender.configure(config);
		//sender.sendMessage("Security event");
	}
	
	@Test
	public void testLoginService() throws Exception {
				
		LoginService li = new LoginModule();
		((LoginModule)li).configure(config);
		
		loginFail(li, "1");
		loginFail(li,"2");
		loginFail(li,"3");
		loginFail(li,"1234");  //Even though correct pin supplied, login still fails as lock occurs
		Thread.sleep(1050);
		
        
		loginFail(li,"5");
		loginFail(li,"6");
		loginFail(li,"7");
		loginFail(li, "1234");
        
        Thread.sleep(1050);
        
        loginFail(li,"9");
        loginFail(li,"10");
        loginSuccess(li, "1234");  

        loginFail(li,"11");
        loginFail(li,"12");
        loginFail(li,"13");
        loginFail(li, "1234");
        
	}
	
	private void loginFail(LoginService li, String text) {
		
		String token = li.login(text);
		assertNull(token);
		
	}
	
	private String loginSuccess(LoginService li, String text) {
		
		String token = li.login(text);
		assertNotNull(token);
		
		return token;
		
	}
	
	@Test
	public void testDelayedArm() throws Exception {
		//Attempting to arm system, 

		config.getSystemModule().accept(SystemEvent.create(Type.SETSTATE, "test", SystemState.ARMED));
		assertEquals(SystemState.DELAYED_ARM, config.getSystemModule().getState());
		
		//During delayed arm, there nothing should cause alarm until state becomes ARMED, configuration is 500ms.
		config.getSystemModule().accept(SystemEvent.create(Type.SENSOR, "test"));
		//Verify that system still in delayed arm
		assertEquals(SystemState.DELAYED_ARM, config.getSystemModule().getState());
		Thread.sleep(600);
		
		//At this point the system should be in armed mode.
		assertEquals(SystemState.ARMED, config.getSystemModule().getState());		
		
		config.getSystemModule().accept(SystemEvent.create(Type.SETSTATE, "test", SystemState.DISARMED));
		config.getSystemModule().accept(SystemEvent.create(Type.SETSTATE, "test", SystemState.ARMED));
		assertEquals(SystemState.DELAYED_ARM, config.getSystemModule().getState());
		
		//Wait about 1/2 the time, then disarm.
		Thread.sleep(300);
		config.getSystemModule().accept(SystemEvent.create(Type.SETSTATE, "test", SystemState.DISARMED));
		assertEquals(SystemState.DISARMED, config.getSystemModule().getState());
		
		Thread.sleep(400);
		//Make sure that system still disarmed after initial arm timeout.
		assertEquals(SystemState.DISARMED, config.getSystemModule().getState());
		
	}
	
	@Test
	public void testAlarm() throws Exception {
		config.getSystemModule().accept(SystemEvent.create(Type.SETSTATE, "test", SystemState.ARMED));
		assertEquals(SystemState.DELAYED_ARM, config.getSystemModule().getState());
		Thread.sleep(600);
		
		//At this point the system should be in armed mode.
		assertEquals(SystemState.ARMED, config.getSystemModule().getState());
		SystemEvent event = SystemEvent.create(Type.SENSOR, "test");
		event.setDelay(200L);  //Alarm after 200ms
		config.getSystemModule().accept(event);
		
		assertEquals(SystemState.DELAYED_ALARM, config.getSystemModule().getState());
		Thread.sleep(250);
		assertEquals(SystemState.ALARM, config.getSystemModule().getState());
		
		
		//Following tests user DISARM action causes DELAYED ALARM to be aborted.
		config.getSystemModule().accept(SystemEvent.create(Type.SETSTATE, "test", SystemState.ARMED));		
		
		Thread.sleep(600);
		
		//At this point the system should be in armed mode.
		assertEquals(SystemState.ARMED, config.getSystemModule().getState());
		SystemEvent.create(Type.SENSOR, "test");
		event.setDelay(200L);  //Alarm after 200ms
		config.getSystemModule().accept(event);
		
		assertEquals(SystemState.DELAYED_ALARM, config.getSystemModule().getState());
		//User Disarms
		config.getSystemModule().accept(SystemEvent.create(Type.SETSTATE, "test", SystemState.DISARMED));
		assertEquals(SystemState.DISARMED, config.getSystemModule().getState());
		
		//Make sure that ALARM condition does not occur after original timeout
		Thread.sleep(250);
		assertEquals(SystemState.DISARMED, config.getSystemModule().getState());
		
		
		//Following tests immediate alarm condidion after delayed ALARM triggered
		config.getSystemModule().accept(SystemEvent.create(Type.SETSTATE, "test", SystemState.ARMED));		
		
		Thread.sleep(600);
		
		//At this point the system should be in armed mode.
		assertEquals(SystemState.ARMED, config.getSystemModule().getState());
		SystemEvent.create(Type.SENSOR, "test");
		event.setDelay(200L);  //Alarm after 200ms
		config.getSystemModule().accept(event);
		
		assertEquals(SystemState.DELAYED_ALARM, config.getSystemModule().getState());
		//Another event, this time without delay.
		event.setDelay(0L);
		config.getSystemModule().accept(event);
		assertEquals(SystemState.ALARM, config.getSystemModule().getState());
	}

}
