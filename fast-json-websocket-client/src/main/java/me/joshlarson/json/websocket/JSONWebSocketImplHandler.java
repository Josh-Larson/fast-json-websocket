package me.joshlarson.json.websocket;

import java.nio.ByteBuffer;

public interface JSONWebSocketImplHandler {
	
	void onConnect();
	void onDisconnect();
	void onMessage(String message);
	void onPong(ByteBuffer data);
	void onError(Throwable t);
	
}
