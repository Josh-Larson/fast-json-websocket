package me.joshlarson.json.websocket;

import me.joshlarson.json.JSONException;
import me.joshlarson.json.JSONInputStream;
import me.joshlarson.json.JSONObject;

import javax.websocket.CloseReason.CloseCode;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

public class JSONWebSocketClient {
	
	private final JSONWebSocketImpl impl;
	private final AtomicReference<JSONWebSocketHandler> handler;
	
	public JSONWebSocketClient() {
		this.impl = new JSONWebSocketImpl();
		this.handler = new AtomicReference<>(null);
		this.impl.setMessageHandler(new JSONWebSocketImplHandler() {
			
			public void onConnect() {
				JSONWebSocketClient.this.onConnect();
			}
			
			public void onDisconnect() {
				JSONWebSocketClient.this.onDisconnect();
			}
			
			public void onMessage(String message) {
				JSONWebSocketClient.this.onMessage(message);
			}
			
			public void onPong(ByteBuffer data) {
				JSONWebSocketClient.this.onPong(data);
			}
			
			public void onError(Throwable t) {
				JSONWebSocketClient.this.onError(t);
			}
		});
	}
	
	/**
	 * Determines if the current session is connected
	 *
	 * @return TRUE if the session is connected, FALSE otherwise
	 */
	public boolean isConnected() {
		return impl.isConnected();
	}
	
	/**
	 * Determines if the current connection is secure
	 *
	 * @return TRUE if the connection is secure, FALSE otherwise
	 */
	public boolean isSecure() {
		return impl.isSecure();
	}
	
	/**
	 * Blocks until a connection is made to the remote endpoint specified by the URI
	 *
	 * @param uri the URI to connect to
	 * @throws IOException if an I/O error occurs
	 */
	public void connect(String uri) throws URISyntaxException, IOException {
		connect(new URI(uri));
	}
	
	/**
	 * Blocks until a connection is made to the remote endpoint specified by the URI
	 *
	 * @param uri the URI to connect to
	 * @throws IOException if an I/O error occurs
	 */
	public void connect(URI uri) throws IOException {
		impl.connect(uri);
	}
	
	/**
	 * Stops the session with the remote endpoint with the normal close code and an empty reason string
	 *
	 * @throws IOException if an I/O error occurs
	 */
	public void disconnect() throws IOException {
		impl.disconnect();
	}
	
	/**
	 * Stops the session with the remote endpoint with the specified close code and reason string
	 *
	 * @param code   the close code to end the session with
	 * @param reason the reason the session is being ended
	 * @throws IOException if an I/O error occurs
	 */
	public void disconnect(CloseCode code, String reason) throws IOException {
		impl.disconnect(code, reason);
	}
	
	/**
	 * Sends a synchronous message to the remote endpoint of the JSONObject encoded as a compact string.  A compact string has no whitespace between
	 * any tokens in JSON.  As an example:
	 * <pre>{    "key": "value"    }</pre>
	 * turns into:
	 * <pre>{"key":"value"}</pre>
	 *
	 * @param object the JSONObject to send
	 */
	public void send(JSONObject object) {
		impl.send(object.toString(true));
	}
	
	/**
	 * Sends a ping with random data to the remote endpoint
	 *
	 * @throws IOException if an I/O error occurs
	 */
	public void ping() throws IOException {
		impl.ping();
	}
	
	/**
	 * Sends a ping with the specified data to the remote endpoint
	 *
	 * @param data the data to ping with
	 * @throws IOException if an I/O error occurs
	 */
	public void ping(byte[] data) throws IOException {
		impl.ping(data);
	}
	
	/**
	 * Sends a ping with the specified data to the remote endpoint
	 *
	 * @param data the data to ping with
	 * @throws IOException if an I/O error occurs
	 */
	public void ping(ByteBuffer data) throws IOException {
		impl.ping(data);
	}
	
	/**
	 * Sets the handler for event notifications
	 *
	 * @param handler the new event handler
	 */
	public void setHandler(JSONWebSocketHandler handler) {
		this.handler.set(handler);
	}
	
	private void onConnect() {
		JSONWebSocketHandler handler = this.handler.get();
		if (handler != null)
			handler.onConnect(this);
	}
	
	private void onDisconnect() {
		JSONWebSocketHandler handler = this.handler.get();
		if (handler != null)
			handler.onDisconnect(this);
	}
	
	private void onMessage(String message) {
		try (JSONInputStream in = new JSONInputStream(message)) {
			JSONObject object = in.readObject();
			JSONWebSocketHandler handler = this.handler.get();
			if (handler != null)
				handler.onMessage(this, object);
		} catch (JSONException | IOException e) {
			onError(e);
		}
	}
	
	private void onPong(ByteBuffer data) {
		JSONWebSocketHandler handler = this.handler.get();
		if (handler != null)
			handler.onPong(this, data);
	}
	
	private void onError(Throwable t) {
		JSONWebSocketHandler handler = this.handler.get();
		if (handler != null)
			handler.onError(this, t);
	}
	
}
