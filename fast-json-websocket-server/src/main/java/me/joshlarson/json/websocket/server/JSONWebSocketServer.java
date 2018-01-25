package me.joshlarson.json.websocket.server;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.websockets.NanoWSD;
import org.nanohttpd.protocols.websockets.WebSocket;

import java.util.concurrent.atomic.AtomicReference;

public class JSONWebSocketServer extends NanoWSD {
	
	private final AtomicReference<JSONWebSocketConnectionHandler> handler;
	
	public JSONWebSocketServer(int port) {
		super(port);
		this.handler = new AtomicReference<>(null);
	}
	
	public void setHandler(JSONWebSocketConnectionHandler handler) {
		this.handler.set(handler);
	}
	
	@Override
	protected final WebSocket openWebSocket(IHTTPSession ihttpSession) {
		return new JSONWebSocketConnectionImpl(ihttpSession, handler);
	}
	
}
