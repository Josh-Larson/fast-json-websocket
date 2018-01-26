package me.joshlarson.json.websocket.server;

import me.joshlarson.json.JSONObject;

import java.nio.ByteBuffer;

public interface JSONWebSocketConnectionHandler {
	
	/**
	 * Called when the socket is officially connected
	 *
	 * @param socket the socket that was connected
	 */
	void onConnect(JSONWebSocketConnection socket);
	
	/**
	 * Called when the socket is officially disconnected
	 *
	 * @param socket the socket that was disconnected
	 */
	void onDisconnect(JSONWebSocketConnection socket);
	
	/**
	 * Called when a new message has arrived
	 *
	 * @param socket the socket that the message arrived from
	 * @param object the JSONObject received
	 */
	void onMessage(JSONWebSocketConnection socket, JSONObject object);
	
	/**
	 * Called when the client replies to a server ping
	 *
	 * @param socket the socket that was pinged
	 * @param data   the data from the pong
	 */
	void onPong(JSONWebSocketConnection socket, ByteBuffer data);
	
	/**
	 * Called when there is an internal error within the socket.  Could be an IOException from the network, or a JSONException when trying to decode a
	 * message
	 *
	 * @param socket the socket that the error occured on
	 * @param t      the throwable thrown
	 */
	void onError(JSONWebSocketConnection socket, Throwable t);
	
}
