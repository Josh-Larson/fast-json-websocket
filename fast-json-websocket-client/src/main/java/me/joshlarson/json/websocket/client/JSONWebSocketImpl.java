package me.joshlarson.json.websocket.client;

import com.neovisionaries.ws.client.*;

import javax.annotation.Nonnull;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

class JSONWebSocketImpl extends WebSocketAdapter {
	
	private final AtomicReference<WebSocket> socket;
	private final JSONWebSocketImplHandler messageHandler;
	private final WebSocketSettings socketSettings;
	private final WebSocketFactory webSocketFactory;
	private final Random random;
	private final long pingId;
	
	JSONWebSocketImpl(@Nonnull JSONWebSocketImplHandler handler) {
		this.socket = new AtomicReference<>(null);
		this.messageHandler = handler;
		this.socketSettings = new WebSocketSettings();
		this.webSocketFactory = new WebSocketFactory();
		this.random = new Random();
		this.pingId = random.nextLong();
	}
	
	public boolean isConnected() {
		WebSocket socket = this.socket.get();
		return socket != null && socket.isOpen();
	}
	
	public void setProxy(@Nonnull URI proxyUri) {
		Objects.requireNonNull(proxyUri, "proxyUri");
		webSocketFactory.getProxySettings().setServer(proxyUri);
	}
	
	public void setProxy(@Nonnull URL proxyUrl) {
		Objects.requireNonNull(proxyUrl, "proxyUrl");
		webSocketFactory.getProxySettings().setServer(proxyUrl);
	}
	
	public void setProxy(@Nonnull String uri) {
		Objects.requireNonNull(uri, "uri");
		webSocketFactory.getProxySettings().setServer(uri);
	}
	
	public void setProxy(boolean secure, @Nonnull String user, @Nonnull String pass, @Nonnull String host, int port) {
		Objects.requireNonNull(user, "user");
		Objects.requireNonNull(pass, "pass");
		Objects.requireNonNull(host, "host");
		webSocketFactory.getProxySettings().setSecure(secure).setId(user).setPassword(pass).setHost(host).setPort(port);
	}
	
	public void setProxySocketFactory(@Nonnull SocketFactory factory) {
		Objects.requireNonNull(factory, "factory");
		webSocketFactory.getProxySettings().setSocketFactory(factory);
	}
	
	public void setProxySSLSocketFactory(@Nonnull SSLSocketFactory factory) {
		Objects.requireNonNull(factory, "factory");
		webSocketFactory.getProxySettings().setSSLSocketFactory(factory);
	}
	
	public void setProxySSLContext(@Nonnull SSLContext context) {
		Objects.requireNonNull(context, "context");
		webSocketFactory.getProxySettings().setSSLContext(context);
	}
	
	public void setSocketFactory(@Nonnull SocketFactory factory) {
		Objects.requireNonNull(factory, "factory");
		webSocketFactory.setSocketFactory(factory);
	}
	
	public void setSSLSocketFactory(@Nonnull SSLSocketFactory factory) {
		Objects.requireNonNull(factory, "factory");
		webSocketFactory.setSSLSocketFactory(factory);
	}
	
	public void setSSLContext(@Nonnull SSLContext context) {
		Objects.requireNonNull(context, "context");
		webSocketFactory.setSSLContext(context);
	}
	
	public void setConnectionTimeout(int timeout) {
		webSocketFactory.setConnectionTimeout(timeout);
	}
	
	public void setVerifyHostname(boolean verifyHostname) {
		webSocketFactory.setVerifyHostname(verifyHostname);
	}
	
	public void clearProtocols() {
		socketSettings.clearProtocols();
	}
	
	public void addProtocol(@Nonnull String protocol) {
		socketSettings.addProtocol(protocol);
	}
	
	public void removeProtocol(@Nonnull String protocol) {
		socketSettings.removeProtocol(protocol);
	}
	
	public void clearHeaders() {
		socketSettings.clearHeaders();
	}
	
	public void addHeader(@Nonnull String key, @Nonnull String value) {
		socketSettings.addHeader(key, value);
	}
	
	public void removeHeader(@Nonnull String key) {
		socketSettings.removeHeader(key);
	}
	
	public void clearUserInfo() {
		socketSettings.clearUserInfo();
	}
	
	public void setUserInfo(@Nonnull String username, @Nonnull String password) {
		socketSettings.setUserInfo(username, password);
	}
	
	public void setFrameQueueSize(int size) {
		socketSettings.setFrameQueueSize(size);
	}
	
	public void setMaxPayloadSize(int size) {
		socketSettings.setMaxPayloadSize(size);
	}
	
	public void connect(@Nonnull URI endpoint) throws IOException {
		Objects.requireNonNull(endpoint, "endpoint");
		synchronized (webSocketFactory) {
			try {
				WebSocket socket = webSocketFactory.createSocket(endpoint);
				socket.addListener(this);
				socketSettings.apply(socket);
				socket.connect();
				this.socket.set(socket);
			} catch (WebSocketException e) {
				throw new IOException("WebSocket error: " + e.getMessage());
			}
		}
	}
	
	public void disconnect() {
		getSocket().disconnect();
		this.socket.set(null);
	}
	
	public void disconnect(@Nonnull CloseCode code, @Nonnull String reason) {
		Objects.requireNonNull(code, "code");
		Objects.requireNonNull(reason, "reason");
		getSocket().disconnect(code.getCode(), reason);
		this.socket.set(null);
	}
	
	public void disconnect(@Nonnull CloseCode code, @Nonnull String reason, long closeTimeout) {
		Objects.requireNonNull(code, "code");
		Objects.requireNonNull(reason, "reason");
		getSocket().disconnect(code.getCode(), reason, closeTimeout);
		this.socket.set(null);
	}
	
	@Override
	public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
		try {
			getHandler().onConnect();
		} catch (Throwable user) {
			System.err.println("Exception in handler's onConnect() function");
			user.printStackTrace();
		}
	}
	
	@Override
	public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) {
		try {
			getHandler().onDisconnect();
		} catch (Throwable user) {
			System.err.println("Exception in handler's onDisconnect() function");
			user.printStackTrace();
		}
	}
	
	@Override
	public void onPongFrame(WebSocket websocket, WebSocketFrame frame) {
		try {
			getHandler().onPong(ByteBuffer.wrap(frame.getPayload()));
		} catch (Throwable user) {
			System.err.println("Exception in handler's onPong() function");
			user.printStackTrace();
		}
		if (frame.getPayloadLength() == 16) {
			try {
				ByteBuffer pong = ByteBuffer.wrap(frame.getPayload()).order(ByteOrder.BIG_ENDIAN);
				if (pong.getLong(0) == pingId) {
					getHandler().onPongTimed(System.nanoTime() - pong.getLong(8));
				}
			} catch (Throwable user) {
				System.err.println("Exception in handler's onPongTimed() function");
				user.printStackTrace();
			}
		}
	}
	
	@Override
	public void onTextMessage(WebSocket websocket, String text) {
		try {
			if (text == null)
				text = "";
			getHandler().onMessage(text);
		} catch (Throwable user) {
			System.err.println("Exception in handler's onMessage() function");
			user.printStackTrace();
		}
	}
	
	@Override
	public void onError(WebSocket websocket, WebSocketException cause) {
		onError(cause);
	}
	
	public boolean send(@Nonnull String message) {
		Objects.requireNonNull(message, "message");
		WebSocket socket = getSocket();
		try {
			socket.sendText(message);
			return socket.getState() == WebSocketState.OPEN;
		} catch (Throwable t) {
			onError(t);
			return false;
		}
	}
	
	public void flush() {
		WebSocket socket = getSocket();
		try {
			socket.getSocket().getOutputStream().flush();
		} catch (Throwable t) {
			onError(t);
		}
	}
	
	public void ping() {
		byte[] pingData = new byte[4];
		random.nextBytes(pingData);
		ping(pingData);
	}
	
	public void ping(@Nonnull byte[] data) {
		Objects.requireNonNull(data, "data");
		getSocket().sendPing(data);
	}
	
	public void ping(@Nonnull ByteBuffer data) {
		Objects.requireNonNull(data, "data");
		getSocket().sendPing(data.array());
	}
	
	public void pingTimed() {
		ByteBuffer data = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN);
		data.putLong(pingId);
		data.putLong(System.nanoTime());
		ping(data.array());
	}
	
	private void onError(@Nonnull Throwable t) {
		try {
			getHandler().onError(t);
		} catch (Throwable user) {
			System.err.println("Exception in handler's onError() function");
			user.printStackTrace();
		}
	}
	
	@Nonnull
	private JSONWebSocketImplHandler getHandler() {
		return messageHandler;
	}
	
	@Nonnull
	private WebSocket getSocket() {
		WebSocket socket = this.socket.get();
		if (socket != null)
			return socket;
		throw new JSONWebSocketException("WebSocket hasn't been connected yet!");
	}
	
	/**
	 * Exception for when something goes unexpectedly wrong due to programmer error
	 */
	public static class JSONWebSocketException extends RuntimeException {
		
		public JSONWebSocketException(String message) {
			super(message);
		}
		
	}
	
	private static class WebSocketSettings {
		
		private final List<String> protocols;
		private final LinkedHashMap<String, String> headers;
		private final Pair<String, String> userInfo;
		private final AtomicInteger frameQueueSize;
		private final AtomicInteger maxPayloadSize;
		
		public WebSocketSettings() {
			this.protocols = new ArrayList<>();
			this.headers = new LinkedHashMap<>();
			this.userInfo = new Pair<>(null, null);
			this.frameQueueSize = new AtomicInteger(-1);
			this.maxPayloadSize = new AtomicInteger(-1);
		}
		
		public void clearProtocols() {
			protocols.clear();
		}
		
		public void addProtocol(@Nonnull String protocol) {
			Objects.requireNonNull(protocol, "protocol");
			this.protocols.add(protocol);
		}
		
		public void removeProtocol(@Nonnull String protocol) {
			Objects.requireNonNull(protocol, "protocol");
			this.protocols.remove(protocol);
		}
		
		public void clearHeaders() {
			headers.clear();
		}
		
		public void addHeader(@Nonnull String key, @Nonnull String value) {
			Objects.requireNonNull(key, "key");
			Objects.requireNonNull(value, "value");
			headers.put(key, value);
		}
		
		public void removeHeader(@Nonnull String key) {
			Objects.requireNonNull(key, "key");
			headers.remove(key);
		}
		
		public void clearUserInfo() {
			userInfo.setKey(null);
			userInfo.setValue(null);
		}
		
		public void setUserInfo(@Nonnull String username, @Nonnull String password) {
			Objects.requireNonNull(username, "username");
			Objects.requireNonNull(password, "password");
			userInfo.setKey(username);
			userInfo.setValue(password);
		}
		
		public void setFrameQueueSize(int size) {
			frameQueueSize.set(size);
		}
		
		public void setMaxPayloadSize(int size) {
			maxPayloadSize.set(size);
		}
		
		public void apply(@Nonnull WebSocket socket) {
			Objects.requireNonNull(socket, "socket");
			for (String str : protocols)
				socket.addProtocol(str);
			for (Entry<String, String> e : headers.entrySet())
				socket.addHeader(e.getKey(), e.getValue());
			{
				String username = userInfo.getKey();
				String password = userInfo.getValue();
				if (username != null && password != null)
					socket.setUserInfo(username, password);
			}
			{
				int frameQueueSize = this.frameQueueSize.get();
				if (frameQueueSize != -1)
					socket.setFrameQueueSize(frameQueueSize);
			}
			{
				int maxPayloadSize = this.maxPayloadSize.get();
				if (maxPayloadSize != -1)
					socket.setMaxPayloadSize(maxPayloadSize);
			}
		}
		
	}
	
	private static class Pair<T, U> {
		
		private T t;
		private U u;
		
		public Pair(T t, U u) {
			this.t = t;
			this.u = u;
		}
		
		public T getKey() {
			return t;
		}
		
		public U getValue() {
			return u;
		}
		
		public void setKey(T t) {
			this.t = t;
		}
		
		public void setValue(U u) {
			this.u = u;
		}
		
	}
	
}
