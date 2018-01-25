package me.joshlarson.json.websocket.client;

public interface JSONWebSocketImplHandler {
	
	void onConnect();
	void onDisconnect();
	void onMessage(String message);
	void onError(Throwable t);
	
}
