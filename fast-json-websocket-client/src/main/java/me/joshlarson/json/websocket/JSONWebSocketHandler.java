package me.joshlarson.json.websocket;

import me.joshlarson.json.JSONObject;

public interface JSONWebSocketHandler {
	
	void onConnect(JSONWebSocketClient socket);
	void onDisconnect(JSONWebSocketClient socket);
	void onMessage(JSONWebSocketClient socket, JSONObject object);
	void onError(JSONWebSocketClient socket, Throwable t);
	
}
