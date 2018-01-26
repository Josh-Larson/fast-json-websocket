package me.joshlarson.json.websocket.server;

import me.joshlarson.json.JSONException;
import me.joshlarson.json.JSONInputStream;
import me.joshlarson.json.JSONObject;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.websockets.CloseCode;
import org.nanohttpd.protocols.websockets.WebSocket;
import org.nanohttpd.protocols.websockets.WebSocketFrame;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

class JSONWebSocketConnectionImpl extends WebSocket {
	
	private final AtomicReference<JSONWebSocketConnectionHandler> handler;
	private final JSONWebSocketConnection socket;
	private final AtomicBoolean connected;
	
	JSONWebSocketConnectionImpl(IHTTPSession handshakeRequest, AtomicReference<JSONWebSocketConnectionHandler> handler) {
		super(handshakeRequest);
		this.handler = handler;
		this.socket = new JSONWebSocketConnection(this);
		this.connected = new AtomicBoolean(false);
	}
	
	public boolean isConnected() {
		return connected.get();
	}
	
	@Override
	protected void onOpen() {
		connected.set(true);
		JSONWebSocketConnectionHandler handler = this.handler.get();
		if (handler != null) {
			try {
				handler.onConnect(socket);
			} catch (Throwable t) {
				System.err.println("Exception in handler's onConnect() function");
				t.printStackTrace();
			}
		}
	}
	
	@Override
	protected void onClose(CloseCode closeCode, String s, boolean b) {
		connected.set(false);
		JSONWebSocketConnectionHandler handler = this.handler.get();
		if (handler != null) {
			try {
				handler.onDisconnect(socket);
			} catch (Throwable t) {
				System.err.println("Exception in handler's onDisconnect() function");
				t.printStackTrace();
			}
		}
	}
	
	@Override
	protected void onMessage(WebSocketFrame webSocketFrame) {
		JSONWebSocketConnectionHandler handler = this.handler.get();
		if (handler == null)
			return;
		
		try (JSONInputStream in = new JSONInputStream(webSocketFrame.getTextPayload())) {
			JSONObject object = in.readObject();
			if (object == null)
				throw new JSONException("Invalid JSON: empty string");
			try {
				handler.onMessage(socket, object);
			} catch (Throwable t) {
				System.err.println("Exception in handler's onMessage() function");
				t.printStackTrace();
			}
		} catch (JSONException | IOException e) {
			try {
				handler.onError(socket, e);
			} catch (Throwable t) {
				System.err.println("Exception in handler's onError() function");
				t.printStackTrace();
			}
		}
	}
	
	@Override
	protected void onPong(WebSocketFrame webSocketFrame) {
		JSONWebSocketConnectionHandler handler = this.handler.get();
		if (handler != null) {
			try {
				handler.onPong(socket, ByteBuffer.wrap(webSocketFrame.getBinaryPayload()));
			} catch (Throwable t) {
				System.err.println("Exception in handler's onPong() function");
				t.printStackTrace();
			}
		}
	}
	
	@Override
	protected void onException(IOException e) {
		if (e instanceof SocketException && e.getMessage() != null && e.getMessage().toLowerCase(Locale.US).contains("socket closed"))
			return;
		JSONWebSocketConnectionHandler handler = this.handler.get();
		if (handler != null) {
			try {
				handler.onError(socket, e);
			} catch (Throwable t) {
				System.err.println("Exception in handler's onError() function");
				t.printStackTrace();
			}
		}
	}
	
}
