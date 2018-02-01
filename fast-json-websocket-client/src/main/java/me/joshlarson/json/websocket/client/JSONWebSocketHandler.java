package me.joshlarson.json.websocket.client;

import me.joshlarson.json.JSONObject;

import java.nio.ByteBuffer;

public abstract class JSONWebSocketHandler {
	
	/**
	 * Called when the session is officially connected
	 *
	 * @param socket the socket that was connected
	 */
	public void onConnect(JSONWebSocketClient socket) {
		
	}
	
	/**
	 * Called when the session is officially disconnected
	 *
	 * @param socket the socket that was disconnected
	 */
	public void onDisconnect(JSONWebSocketClient socket) {
		
	}
	
	/**
	 * Called when a new message has arrived
	 *
	 * @param socket the socket that the message arrived from
	 * @param object the JSONObject received
	 */
	public abstract void onMessage(JSONWebSocketClient socket, JSONObject object);
	
	/**
	 * Called when the server replies to a client ping
	 *
	 * @param socket the socket that pinged
	 * @param data   the data from the pong
	 */
	public void onPong(JSONWebSocketClient socket, ByteBuffer data) {
		
	}
	
	/**
	 * Called when the server replies to a timed client ping.  This is called after onPong.
	 *
	 * @param socket  the socket that was pinged
	 * @param rttNano the round trip time in nanoseconds
	 */
	public void onPongTimed(JSONWebSocketClient socket, long rttNano) {
		
	}
	
	/**
	 * Called when there is an internal error within the socket.  Could be an IOException from the network, or a JSONException when trying to decode a
	 * message
	 *
	 * @param socket the socket that the error occured on
	 * @param t      the throwable thrown
	 */
	public void onError(JSONWebSocketClient socket, Throwable t) {
		t.printStackTrace();
	}
	
}
