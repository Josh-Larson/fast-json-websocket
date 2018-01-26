package me.joshlarson.json.websocket.server;

import me.joshlarson.json.JSONObject;
import org.nanohttpd.protocols.websockets.CloseCode;

import java.io.IOException;
import java.util.Random;

public class JSONWebSocketConnection {
	
	private final JSONWebSocketConnectionImpl impl;
	private final Random random;
	
	JSONWebSocketConnection(JSONWebSocketConnectionImpl impl) {
		this.impl = impl;
		this.random = new Random();
	}
	
	public boolean isConnected() {
		return impl.isConnected();
	}
	
	public void close() throws IOException {
		close(CloseCode.NormalClosure, "");
	}
	
	public void close(CloseCode code, String reason) throws IOException {
		impl.close(code, reason, false);
	}
	
	public void ping() throws IOException {
		byte [] pingBytes = new byte[4];
		random.nextBytes(pingBytes);
		ping(pingBytes);
	}
	
	public void ping(byte[] payload) throws IOException {
		impl.ping(payload);
	}
	
	public void send(JSONObject object) throws IOException {
		impl.send(object.toString(true));
	}
	
}
