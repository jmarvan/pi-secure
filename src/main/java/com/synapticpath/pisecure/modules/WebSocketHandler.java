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

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONObject;

import com.synapticpath.pisecure.Config;
import com.synapticpath.pisecure.SecuritySystem.SystemState;

/**
 * Required by the Jetty websocket api to handle websockets.  This
 * class is responsible for notifying state changes to clients.
 * 
 * @author jmarvan@synapticpath.com
 *
 */
@WebSocket
public class WebSocketHandler  {
	
	protected static List<Session> sessions = new ArrayList<Session>();
	protected static Config config;
	
	@OnWebSocketConnect
	public void onConnect(Session user) throws Exception {
		System.out.println("Added websocket session");
		sessions.add(user);
		broadcastState(config.getSystemModule().getState());
	}

	@OnWebSocketClose
	public void onClose(Session user, int statusCode, String reason) {
		sessions.remove(user);
		System.out.println("UI websocket session ended.");
	}

	@OnWebSocketMessage
	public void onMessage(Session user, String message) {
		System.out.println("Incoming websocked message.");
	}
	
	public static void broadcastState(SystemState state) {
		sessions.stream().filter(Session::isOpen).forEach(session -> {
			try {
				session.getRemote()
						.sendString(String.valueOf(new JSONObject().put("state", state)
								.put("trans", "Translated state in other languages.")));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}	

}
