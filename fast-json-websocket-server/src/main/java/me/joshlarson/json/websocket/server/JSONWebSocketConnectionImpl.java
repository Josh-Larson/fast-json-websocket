package me.joshlarson.json.websocket.server;

import me.joshlarson.json.JSONException;
import me.joshlarson.json.JSONInputStream;
import me.joshlarson.json.JSONObject;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.websockets.CloseCode;
import org.nanohttpd.protocols.websockets.WebSocket;
import org.nanohttpd.protocols.websockets.WebSocketFrame;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

class JSONWebSocketConnectionImpl extends WebSocket {
	
	private final AtomicReference<JSONWebSocketConnectionHandler> handler;
	private final JSONWebSocketConnection socket;
	
	public JSONWebSocketConnectionImpl(IHTTPSession handshakeRequest, AtomicReference<JSONWebSocketConnectionHandler> handler) {
		super(handshakeRequest);
		this.handler = handler;
		this.socket = new JSONWebSocketConnection(this);
	}
	
	@Override
	protected void onOpen() {
		JSONWebSocketConnectionHandler handler = this.handler.get();
		if (handler != null)
			handler.onConnect(socket);
	}
	
	@Override
	protected void onClose(CloseCode closeCode, String s, boolean b) {
		JSONWebSocketConnectionHandler handler = this.handler.get();
		if (handler != null)
			handler.onDisconnect(socket);
	}
	
	@Override
	protected void onMessage(WebSocketFrame webSocketFrame) {
		JSONWebSocketConnectionHandler handler = this.handler.get();
		if (handler == null)
			return;
		try (JSONInputStream in = new JSONInputStream(webSocketFrame.getTextPayload())) {
			JSONObject object = in.readObject();
			handler.onMessage(socket, object);
		} catch (JSONException | IOException e) {
			handler.onError(socket, e);
		}
	}
	
	@Override
	protected void onPong(WebSocketFrame webSocketFrame) {
		JSONWebSocketConnectionHandler handler = this.handler.get();
		if (handler != null)
			handler.onPong(socket);
	}
	
	@Override
	protected void onException(IOException e) {
		JSONWebSocketConnectionHandler handler = this.handler.get();
		if (handler != null)
			handler.onError(socket, e);
	}
	
}
