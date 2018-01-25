package me.joshlarson.json.websocket;

public interface JSONWebSocketImplHandler {
	
	void onConnect();
	void onDisconnect();
	void onMessage(String message);
	void onError(Throwable t);
	
}
