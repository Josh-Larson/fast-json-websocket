package me.joshlarson.json.websocket.client;

import me.joshlarson.json.JSONObject;

public interface JSONWebSocketHandler {
	
	void onConnect(JSONWebSocket socket);
	void onDisconnect(JSONWebSocket socket);
	void onMessage(JSONWebSocket socket, JSONObject object);
	void onError(JSONWebSocket socket, Throwable t);
	
}
