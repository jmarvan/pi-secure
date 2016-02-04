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

import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.synapticpath.pisecure.Config;
import com.synapticpath.pisecure.Configurable;
import com.synapticpath.pisecure.Disableable;
import com.synapticpath.pisecure.EventListener;
import com.synapticpath.pisecure.Module;
import com.synapticpath.pisecure.model.SecurityEvent;
import com.synapticpath.pisecure.model.SystemEvent;

/**
 * This module is responsible for turning SecurityEvents into texts in order
 * to notify the user of significant security issues.
 * 
 * @author jmarvan@synapticpath.com
 *
 */
@Module
public class SMTPNotificationModule implements Disableable, EventListener, Configurable {

	private List<String> recipients;

	private String from;

	private String user;

	private String pwd;

	private boolean disabled;
	
	private String messageTemplate;	

	@Override
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;		
	}

	@Override
	public boolean isDisabled() {
		return disabled;
	}

	@Override
	public void configure(Config config) throws Exception {

		recipients = config.getPropertyList("mail.smtp.recipient.");
		from = config.getProperty("mail.smtp.from", true);
		user = config.getProperty("mail.smtp.user", true);
		pwd = config.getProperty("mail.smtp.password", true);
		disabled = Boolean.valueOf(config.getProperty("mail.smtp.disabled"));

		// Get system properties
		Properties properties = System.getProperties();

		// Setup mail server
		properties.setProperty("mail.smtp.host", config.getProperty("mail.smtp.host", true));
		if (config.getProperty("mail.smtp.auth") != null) {
			properties.put("mail.smtp.auth", "true");
			properties.put("mail.smtp.socketFactory.port", config.getProperty("mail.smtp.port"));
		}
		
		messageTemplate = config.getProperty("mail.smtp.message.template", true);
	}

	@Override
	public void onEvent(SystemEvent event) {
		
		//We only want to email events that are worth sending, and those are all SecurityEvents.
		if (disabled || !(event instanceof SecurityEvent)) {
			return;
		}
		
		try {
			sendNotification(event);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

		
	protected void sendNotification(SystemEvent event) throws Exception {
		SecurityEvent secEvent = (SecurityEvent)event;
		// Get the default Session object.
		Session session = Session.getDefaultInstance(System.getProperties(), new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(user, pwd);
			}
		});

		// Create a default MimeMessage object.
		MimeMessage message = new MimeMessage(session);

		// Set From: header field of the header.
		message.setFrom(new InternetAddress(from));

		// Set To: header field of the header.
		for (String recipient : recipients) {
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
		}

		// Set Subject: header field
		message.setSubject("Security event");

		// Now set the actual message
		String text = String.format(messageTemplate, secEvent.getSeverity(), secEvent.getTime(), secEvent.getSource(), secEvent.getState());
		message.setText(text);

		// Send message
		Transport.send(message);
		System.out.println("Sent message successfully....");

	}

}
