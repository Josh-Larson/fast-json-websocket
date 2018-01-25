package me.joshlarson.json.websocket.server;

import me.joshlarson.json.JSONObject;

public interface JSONWebSocketConnectionHandler {
	
	void onConnect(JSONWebSocketConnection socket);
	void onDisconnect(JSONWebSocketConnection socket);
	void onMessage(JSONWebSocketConnection socket, JSONObject object);
	void onPong(JSONWebSocketConnection socket);
	void onError(JSONWebSocketConnection socket, Throwable t);
	
}
