package me.joshlarson.json.websocket.server;

import me.joshlarson.json.JSONException;
import me.joshlarson.json.JSONInputStream;
import me.joshlarson.json.JSONObject;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.websockets.CloseCode;
import org.nanohttpd.protocols.websockets.WebSocket;
import org.nanohttpd.protocols.websockets.WebSocketFrame;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

class JSONWebSocketConnectionImpl extends WebSocket {
	
	private final AtomicReference<JSONWebSocketConnectionHandler> handler;
	private final JSONWebSocketConnection socket;
	private final Random random;
	private final long pingId;
	private final AtomicBoolean connected;
	
	JSONWebSocketConnectionImpl(@Nonnull IHTTPSession handshakeRequest, @Nonnull AtomicReference<JSONWebSocketConnectionHandler> handler) {
		super(handshakeRequest);
		this.handler = handler;
		this.socket = new JSONWebSocketConnection(this);
		this.random = new Random();
		this.pingId = random.nextLong();
		this.connected = new AtomicBoolean(false);
	}
	
	public boolean isConnected() {
		return connected.get();
	}
	
	public void pingRandom() throws IOException {
		byte[] pingBytes = new byte[4];
		random.nextBytes(pingBytes);
		ping(pingBytes);
	}
	
	public void pingTimed() throws IOException {
		ByteBuffer data = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN);
		data.putLong(pingId);
		data.putLong(System.nanoTime());
		ping(data.array());
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
	protected void onMessage(@Nonnull WebSocketFrame webSocketFrame) {
		JSONWebSocketConnectionHandler handler = this.handler.get();
		if (handler == null)
			return;
		
		try (JSONInputStream in = new JSONInputStream(new ByteArrayInputStream(webSocketFrame.getBinaryPayload()))) {
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
	protected void onPong(@Nonnull WebSocketFrame webSocketFrame) {
		JSONWebSocketConnectionHandler handler = this.handler.get();
		if (handler != null) {
			byte [] payload = webSocketFrame.getBinaryPayload();
			try {
				handler.onPong(socket, ByteBuffer.wrap(payload));
			} catch (Throwable t) {
				System.err.println("Exception in handler's onPong() function");
				t.printStackTrace();
			}
			if (payload.length == 16) {
				try {
					ByteBuffer pong = ByteBuffer.wrap(payload).order(ByteOrder.BIG_ENDIAN);
					if (pong.getLong(0) == pingId) {
						handler.onPongTimed(socket, System.nanoTime() - pong.getLong(8));
					}
				} catch (Throwable t) {
					System.err.println("Exception in handler's onPongTimed() function");
					t.printStackTrace();
				}
			}
		}
	}
	
	@Override
	protected void onException(@Nonnull IOException e) {
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
