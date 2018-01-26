package me.joshlarson.json.websocket.server;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.websockets.NanoWSD;
import org.nanohttpd.protocols.websockets.WebSocket;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

public class JSONWebSocketServer extends NanoWSD {
	
	private final AtomicReference<JSONWebSocketConnectionHandler> handler;
	
	public JSONWebSocketServer(int port) {
		super(port);
		this.handler = new AtomicReference<>(null);
	}
	
	public JSONWebSocketServer(String hostname, int port) {
		super(hostname, port);
		this.handler = new AtomicReference<>(null);
	}
	
	public JSONWebSocketServer(InetAddress address, int port) {
		this(address.getHostAddress(), port);
	}
	
	public JSONWebSocketServer(InetSocketAddress address) {
		this(address.getAddress(), address.getPort());
	}
	
	/**
	 * Sets the handler for event notifications
	 *
	 * @param handler the new event handler
	 */
	public void setHandler(JSONWebSocketConnectionHandler handler) {
		this.handler.set(handler);
	}
	
	@Override
	protected final WebSocket openWebSocket(IHTTPSession ihttpSession) {
		return new JSONWebSocketConnectionImpl(ihttpSession, handler);
	}
	
}
