package me.joshlarson.json.websocket.server;

import me.joshlarson.json.JSONObject;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.websockets.CloseCode;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class JSONWebSocketConnection {
	
	private static final AtomicLong GLOBAL_SOCKET_ID = new AtomicLong(0);
	
	private final JSONWebSocketConnectionImpl impl;
	private final AtomicReference<Object> userData;
	private final long socketId;
	
	JSONWebSocketConnection(JSONWebSocketConnectionImpl impl) {
		this.impl = impl;
		this.userData = new AtomicReference<>(null);
		this.socketId = GLOBAL_SOCKET_ID.incrementAndGet();
	}
	
	/**
	 * Gets the remote IP address
	 * 
	 * @return the remote IP address
	 */
	public String getRemoteIpAddress() {
		return impl.getHandshakeRequest().getRemoteIpAddress();
	}
	
	/**
	 * Gets the handshake request that started the connection
	 * 
	 * @return the handshake request
	 */
	public IHTTPSession getHandshakeRequest() {
		return impl.getHandshakeRequest();
	}
	
	/**
	 * Sets data that can be used during events to further uniquely identify this connection
	 *
	 * @param userData the data to be associated with this connection
	 */
	public void setUserData(Object userData) {
		this.userData.set(userData);
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
	 * Returns a unique socket id for this particular web socket connection.  No other JSONWebSocketConnection will have the same id
	 *
	 * @return a unique id for this socket
	 */
	public long getSocketId() {
		return socketId;
	}
	
	/**
	 * Gets the user data supplied during a previous call to <code>setUserData(Object)</code>
	 *
	 * @return the data associated with this connection
	 */
	public Object getUserData() {
		return userData.get();
	}
	
	/**
	 * Closes the session with the normal close code and an empty reason string
	 *
	 * @throws IOException if an I/O error occurs
	 */
	public void close() throws IOException {
		close(CloseCode.NormalClosure, "");
	}
	
	/**
	 * Closes the session with the specified close code and reason string
	 *
	 * @param code   the close code to end the session with
	 * @param reason the reason the session is being ended
	 * @throws IOException if an I/O error occurs
	 */
	public void close(CloseCode code, String reason) throws IOException {
		impl.close(code, reason, false);
	}
	
	/**
	 * Sends a ping with random data to the client
	 *
	 * @throws IOException if an I/O error occurs
	 */
	public void pingRandom() throws IOException {
		impl.pingRandom();
	}
	
	/**
	 * Sends a ping with data that allows the server to determine the round-trip time
	 * 
	 * @throws IOException if an I/O error occurs
	 */
	public void pingTimed() throws IOException {
		impl.pingTimed();
	}
	
	/**
	 * Sends a ping with the specified data to the client
	 *
	 * @param data the data to ping with
	 * @throws IOException if an I/O error occurs
	 */
	public void ping(byte[] data) throws IOException {
		impl.ping(data);
	}
	
	/**
	 * Sends a synchronous message to the client of the JSONObject encoded as a compact string.  A compact string has no whitespace between
	 * any tokens in JSON.  As an example:
	 * <pre>{    "key": "value"    }</pre>
	 * turns into:
	 * <pre>{"key":"value"}</pre>
	 *
	 * @param object the JSONObject to send
	 * @throws IOException if an I/O error occurs   
	 */
	public void send(JSONObject object) throws IOException {
		impl.send(object.toString(true));
	}
	
}
