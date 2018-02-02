package me.joshlarson.json.websocket.client;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

interface JSONWebSocketImplHandler {
	
	void onConnect();
	void onDisconnect();
	void onMessage(@Nonnull String message);
	void onPong(@Nonnull ByteBuffer data);
	void onPongTimed(long rttNano);
	void onError(@Nonnull Throwable t);
	
}
