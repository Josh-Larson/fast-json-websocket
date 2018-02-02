package me.joshlarson.json.websocket.server;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.websockets.NanoWSD;
import org.nanohttpd.protocols.websockets.WebSocket;

import javax.annotation.Nonnull;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

public class JSONWebSocketServer extends NanoWSD {
	
	private final AtomicReference<JSONWebSocketConnectionHandler> handler;
	
	public JSONWebSocketServer(int port) {
		super(port);
		this.handler = new AtomicReference<>(null);
	}
	
	public JSONWebSocketServer(@Nonnull String hostname, int port) {
		super(hostname, port);
		this.handler = new AtomicReference<>(null);
	}
	
	public JSONWebSocketServer(@Nonnull InetAddress address, int port) {
		this(address.getHostAddress(), port);
	}
	
	public JSONWebSocketServer(@Nonnull InetSocketAddress address) {
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
	protected final WebSocket openWebSocket(@Nonnull IHTTPSession ihttpSession) {
		return new JSONWebSocketConnectionImpl(ihttpSession, handler);
	}
	
}
