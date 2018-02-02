package me.joshlarson.json.websocket.server;

import me.joshlarson.json.JSONObject;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

public abstract class JSONWebSocketConnectionHandler {
	
	/**
	 * Called when the socket is officially connected
	 *
	 * @param socket the socket that was connected
	 */
	public void onConnect(@Nonnull JSONWebSocketConnection socket) {
		
	}
	
	/**
	 * Called when the socket is officially disconnected
	 *
	 * @param socket the socket that was disconnected
	 */
	public void onDisconnect(@Nonnull JSONWebSocketConnection socket) {
		
	}
	
	/**
	 * Called when a new message has arrived
	 *
	 * @param socket the socket that the message arrived from
	 * @param object the JSONObject received
	 */
	public void onMessage(@Nonnull JSONWebSocketConnection socket, @Nonnull JSONObject object) {
		
	}
	
	/**
	 * Called when the client replies to a server ping
	 *
	 * @param socket the socket that was pinged
	 * @param data   the data from the pong
	 */
	public void onPong(@Nonnull JSONWebSocketConnection socket, @Nonnull ByteBuffer data) {
		
	}
	
	/**
	 * Called when the client replies to a timed server ping.  This is called after onPong.
	 *
	 * @param socket  the socket that was pinged
	 * @param rttNano the round trip time in nanoseconds
	 */
	public void onPongTimed(@Nonnull JSONWebSocketConnection socket, long rttNano) {
		
	}
	
	/**
	 * Called when there is an internal error within the socket.  Could be an IOException from the network, or a JSONException when trying to decode a
	 * message
	 *
	 * @param socket the socket that the error occured on
	 * @param t      the throwable thrown
	 */
	public void onError(@Nonnull JSONWebSocketConnection socket, @Nonnull Throwable t) {
		
	}
	
}
