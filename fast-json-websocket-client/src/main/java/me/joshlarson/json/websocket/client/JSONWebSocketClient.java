package me.joshlarson.json.websocket.client;

import me.joshlarson.json.JSONException;
import me.joshlarson.json.JSONInputStream;
import me.joshlarson.json.JSONObject;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

public class JSONWebSocketClient {
	
	private final JSONWebSocketImpl impl;
	private final AtomicReference<JSONWebSocketHandler> handler;
	
	public JSONWebSocketClient() {
		this.impl = new JSONWebSocketImpl(new JSONWebSocketImplHandler() {
			
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
			
			public void onPongTimed(long rttNano) {
				JSONWebSocketClient.this.onPongTimed(rttNano);
			}
			
			public void onError(Throwable t) {
				JSONWebSocketClient.this.onError(t);
			}
		});
		this.handler = new AtomicReference<>(null);
	}
	
	/**
	 * Sets the optional URI of a proxy server.  This sets each of the properties accordingly: secure, user, pass, host, and port. <p> Must be done
	 * before connecting </p>
	 *
	 * @param proxyUri the URI of the proxy server
	 */
	public void setProxy(URI proxyUri) {
		impl.setProxy(proxyUri);
	}
	
	/**
	 * Sets the optional URL of a proxy server.  This sets each of the properties accordingly: secure, user, pass, host, and port. <p> Must be done
	 * before connecting </p>
	 *
	 * @param proxyUrl the URL of the proxy server
	 */
	public void setProxy(URL proxyUrl) {
		impl.setProxy(proxyUrl);
	}
	
	/**
	 * Sets the optional URI of a proxy server.  This sets each of the properties accordingly: secure, user, pass, host, and port. <p> Must be done
	 * before connecting </p>
	 *
	 * @param uri the URI of the proxy server
	 */
	public void setProxy(String uri) {
		impl.setProxy(uri);
	}
	
	/**
	 * Sets the optional properties for a proxy server.  These are each individually set, as opposed to the URI/URL approach. <p> Must be done before
	 * connecting </p>
	 *
	 * @param secure whether or not to use a secure protocol (HTTP vs HTTPS)
	 * @param user   the user to authenticate as
	 * @param pass   the password to authenticate with
	 * @param host   the proxy server host
	 * @param port   the proxy server port
	 */
	public void setProxy(boolean secure, String user, String pass, String host, int port) {
		impl.setProxy(secure, user, pass, host, port);
	}
	
	/**
	 * Sets the socket factory to connect to the proxy server. <p> Must be done before connecting </p>
	 *
	 * @param factory the SocketFactory
	 */
	public void setProxySocketFactory(SocketFactory factory) {
		impl.setProxySocketFactory(factory);
	}
	
	/**
	 * Sets the ssl socket factory to connect to the proxy server. <p> Must be done before connecting </p>
	 *
	 * @param factory the SSLSocketFactory
	 */
	public void setProxySSLSocketFactory(SSLSocketFactory factory) {
		impl.setProxySSLSocketFactory(factory);
	}
	
	/**
	 * Sets the ssl context for connecting to the proxy server. <p> Must be done before connecting </p>
	 *
	 * @param context the SSLContext
	 */
	public void setProxySSLContext(SSLContext context) {
		impl.setProxySSLContext(context);
	}
	
	/**
	 * Sets the socket factory to connect to the server <p> Must be done before connecting </p>
	 *
	 * @param factory the SocketFactory
	 */
	public void setSocketFactory(SocketFactory factory) {
		impl.setSocketFactory(factory);
	}
	
	/**
	 * Sets the ssl socket factory to connect to the server <p> Must be done before connecting </p>
	 *
	 * @param factory the SSLSocketFactory
	 */
	public void setSSLSocketFactory(SSLSocketFactory factory) {
		impl.setSSLSocketFactory(factory);
	}
	
	/**
	 * Sets the ssl context for connecting to the server. <p> Must be done before connecting </p>
	 *
	 * @param context the SSLContext
	 */
	public void setSSLContext(SSLContext context) {
		impl.setSSLContext(context);
	}
	
	/**
	 * Sets the timeout for connecting to the server, with zero being interpreted as infinite. <p> Must be done before connecting </p>
	 *
	 * @param timeout the connection timeout
	 */
	public void setConnectionTimeout(int timeout) {
		impl.setConnectionTimeout(timeout);
	}
	
	/**
	 * Sets whether or not the local socket should verify the certificate's hostname during the SSL/TLS handshake. <p> Must be done before connecting
	 * </p>
	 *
	 * @param verifyHostname TRUE to verify the hostname (default), FALSE otherwise
	 */
	public void setVerifyHostname(boolean verifyHostname) {
		impl.setVerifyHostname(verifyHostname);
	}
	
	/**
	 * Remove all protocols from {@code Sec-WebSocket-Protocol}
	 * 
	 * @see com.neovisionaries.ws.client.WebSocket#clearProtocols
	 */
	public void clearProtocols() {
		impl.clearProtocols();
	}
	
    /**
     * Add a value for {@code Sec-WebSocket-Protocol}.
     *
     * @param protocol
     *         A protocol name.
     *
	 * @see com.neovisionaries.ws.client.WebSocket#addProtocol
     */
	public void addProtocol(String protocol) {
		impl.addProtocol(protocol);
	}
	
    /**
     * Remove a protocol from {@code Sec-WebSocket-Protocol}.
     *
     * @param protocol
     *         A protocol name. {@code null} is silently ignored.
     *
	 * @see com.neovisionaries.ws.client.WebSocket#removeProtocol
     */
	public void removeProtocol(String protocol) {
		impl.removeProtocol(protocol);
	}
	
    /**
     * Clear all extra HTTP headers.
     *
	 * @see com.neovisionaries.ws.client.WebSocket#clearHeaders
     */
	public void clearHeaders() {
		impl.clearHeaders();
	}
	
    /**
     * Add a pair of extra HTTP header.
     *
     * @param name
     *         An HTTP header name. When {@code null} or an empty
     *         string is given, no header is added.
     *
     * @param value
     *         The value of the HTTP header.
     *
	 * @see com.neovisionaries.ws.client.WebSocket#addHeader
     */
	public void addHeader(String name, String value) {
		impl.addHeader(name, value);
	}
	
    /**
     * Remove pairs of extra HTTP headers.
     *
     * @param name
     *         An HTTP header name. {@code null} is silently ignored.
     *
	 * @see com.neovisionaries.ws.client.WebSocket#removeHeaders
     */
	public void removeHeader(String name) {
		impl.removeHeader(name);
	}
	
    /**
     * Clear the credentials to connect to the WebSocket endpoint.
     *
	 * @see com.neovisionaries.ws.client.WebSocket#clearUserInfo
     */
	public void clearUserInfo() {
		impl.clearUserInfo();
	}
	
    /**
     * Set the credentials to connect to the WebSocket endpoint.
     *
     * @param username
     *         The username.
     *
     * @param password
     *         The password.
     *
	 * @see com.neovisionaries.ws.client.WebSocket#setUserInfo
     */
	public void setUserInfo(String username, String password) {
		impl.setUserInfo(username, password);
	}
	
    /**
     * Set the size of the frame queue. The default value is 0 and it means
     * there is no limit on the queue size.
     *
     * <p>
     * <code>send<i>Xxx</i></code> methods queue a {@link com.neovisionaries.ws.client.WebSocketFrame}
     * instance to the internal queue. If the number of frames in the queue
     * has reached the upper limit (which has been set by this method) when
     * a <code>send<i>Xxx</i></code> method is called, the method blocks
     * until the queue gets spaces.
     * </p>
     *
     * <p>
     * Under some conditions, even if the queue is full, <code>send<i>Xxx</i></code>
     * methods do not block. For example, in the case where the thread to send
     * frames ({@code WritingThread}) is going to stop or has already stopped.
     * In addition, method calls to send a <a href=
     * "https://tools.ietf.org/html/rfc6455#section-5.5">control frame</a> (e.g.
     * {@link com.neovisionaries.ws.client.WebSocket#sendClose()} and {@link com.neovisionaries.ws.client.WebSocket#sendPing()}) do not block.
     * </p>
     *
     * @param size
     *         The queue size. 0 means no limit. Negative numbers are not allowed.
     *
	 * @see com.neovisionaries.ws.client.WebSocket#setFrameQueueSize
     */
	public void setFrameQueueSize(int size) {
		impl.setFrameQueueSize(size);
	}
	
    /**
     * Set the maximum payload size.
     *
     * <p>
     * Text, binary and continuation frames whose payload size is bigger than
     * the maximum payload size will be split into multiple frames. Note that
     * control frames (close, ping and pong frames) are not split as per the
     * specification even if their payload size exceeds the maximum payload size.
     * </p>
     *
     * @param size
     *         The maximum payload size. 0 to unset the maximum payload size.
     *
	 * @see com.neovisionaries.ws.client.WebSocket#setFrameQueueSize
     */
	public void setMaxPayloadSize(int size) {
		impl.setMaxPayloadSize(size);
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
	 */
	public void disconnect() {
		impl.disconnect();
	}
	
	/**
	 * Stops the session with the remote endpoint with the specified close code and reason string
	 *
	 * @param code   the close code to end the session with
	 * @param reason the reason the session is being ended
	 */
	public void disconnect(CloseCode code, String reason) {
		impl.disconnect(code, reason);
	}
	
	/**
	 * Stops the session with the remote endpoint with the specified close code and reason string
	 *
	 * @param code         the close code to end the session with
	 * @param reason       the reason the session is being ended
	 * @param closeTimeout the time until the Socket is closed forcibly. This is meant to give time for the server to respond to the close request
	 */
	public void disconnect(CloseCode code, String reason, long closeTimeout) {
		impl.disconnect(code, reason, closeTimeout);
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
	public boolean send(JSONObject object) {
		return impl.send(object.toString(true));
	}
	
	/**
	 * Flushes the buffered data into the network
	 */
	public void flush() {
		impl.flush();
	}
	
	/**
	 * Sends a ping with random data to the remote endpoint
	 */
	public void ping() {
		impl.ping();
	}
	
	/**
	 * Sends a ping with the specified data to the remote endpoint
	 *
	 * @param data the data to ping with
	 */
	public void ping(byte[] data) {
		impl.ping(data);
	}
	
	/**
	 * Sends a ping with the specified data to the remote endpoint
	 *
	 * @param data the data to ping with
	 */
	public void ping(ByteBuffer data) {
		impl.ping(data);
	}
	
	/**
	 * Sends a ping with data that allows the client to determine the round-trip time
	 */
	public void pingTimed() {
		impl.pingTimed();
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
			if (object == null)
				throw new JSONException("Invalid JSON: empty string");
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
	
	private void onPongTimed(long rttNano) {
		JSONWebSocketHandler handler = this.handler.get();
		if (handler != null)
			handler.onPongTimed(this, rttNano);
	}
	
	private void onError(Throwable t) {
		JSONWebSocketHandler handler = this.handler.get();
		if (handler != null)
			handler.onError(this, t);
	}
	
}
