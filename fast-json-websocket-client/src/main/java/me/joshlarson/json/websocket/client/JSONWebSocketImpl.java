package me.joshlarson.json.websocket.client;

import com.neovisionaries.ws.client.*;
import com.sun.istack.internal.NotNull;

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
	private final long pingId;
	
	JSONWebSocketImpl(JSONWebSocketImplHandler handler) {
		if (handler == null)
			throw new NullPointerException("Handler cannot be null");
		this.socket = new AtomicReference<>(null);
		this.messageHandler = handler;
		this.socketSettings = new WebSocketSettings();
		this.webSocketFactory = new WebSocketFactory();
		this.pingId = new Random().nextLong();
	}
	
	public boolean isConnected() {
		WebSocket socket = this.socket.get();
		return socket != null && socket.isOpen();
	}
	
	public void setProxy(URI proxyUri) {
		webSocketFactory.getProxySettings().setServer(proxyUri);
	}
	
	public void setProxy(URL proxyUrl) {
		webSocketFactory.getProxySettings().setServer(proxyUrl);
	}
	
	public void setProxy(String uri) {
		webSocketFactory.getProxySettings().setServer(uri);
	}
	
	public void setProxy(boolean secure, String user, String pass, String host, int port) {
		webSocketFactory.getProxySettings().setSecure(secure).setId(user).setPassword(pass).setHost(host).setPort(port);
	}
	
	public void setProxySocketFactory(SocketFactory factory) {
		webSocketFactory.getProxySettings().setSocketFactory(factory);
	}
	
	public void setProxySSLSocketFactory(SSLSocketFactory factory) {
		webSocketFactory.getProxySettings().setSSLSocketFactory(factory);
	}
	
	public void setProxySSLContext(SSLContext context) {
		webSocketFactory.getProxySettings().setSSLContext(context);
	}
	
	public void setSocketFactory(SocketFactory factory) {
		webSocketFactory.setSocketFactory(factory);
	}
	
	public void setSSLSocketFactory(SSLSocketFactory factory) {
		webSocketFactory.setSSLSocketFactory(factory);
	}
	
	public void setSSLContext(SSLContext context) {
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
	
	public void addProtocol(String protocol) {
		socketSettings.addProtocol(protocol);
	}
	
	public void removeProtocol(String protocol) {
		socketSettings.removeProtocol(protocol);
	}
	
	public void clearHeaders() {
		socketSettings.clearHeaders();
	}
	
	public void addHeader(String key, String value) {
		socketSettings.addHeader(key, value);
	}
	
	public void removeHeader(String key) {
		socketSettings.removeHeader(key);
	}
	
	public void clearUserInfo() {
		socketSettings.clearUserInfo();
	}
	
	public void setUserInfo(String username, String password) {
		socketSettings.setUserInfo(username, password);
	}
	
	public void setFrameQueueSize(int size) {
		socketSettings.setFrameQueueSize(size);
	}
	
	public void setMaxPayloadSize(int size) {
		socketSettings.setMaxPayloadSize(size);
	}
	
	public void connect(URI endpoint) throws IOException {
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
	
	public void disconnect(CloseCode code, String reason) {
		getSocket().disconnect(code.getCode(), reason);
		this.socket.set(null);
	}
	
	public void disconnect(CloseCode code, String reason, long closeTimeout) {
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
	
	public boolean send(String message) {
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
		new Random().nextBytes(pingData);
		ping(pingData);
	}
	
	public void ping(byte[] data) {
		getSocket().sendPing(data);
	}
	
	public void ping(ByteBuffer data) {
		getSocket().sendPing(data.array());
	}
	
	public void pingTimed() {
		ByteBuffer data = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN);
		data.putLong(pingId);
		data.putLong(System.nanoTime());
		ping(data.array());
	}
	
	private void onError(Throwable t) {
		try {
			getHandler().onError(t);
		} catch (Throwable user) {
			System.err.println("Exception in handler's onError() function");
			user.printStackTrace();
		}
	}
	
	private JSONWebSocketImplHandler getHandler() {
		return messageHandler;
	}
	
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
		
		public void addProtocol(String protocol) {
			this.protocols.add(protocol);
		}
		
		public void removeProtocol(String protocol) {
			this.protocols.remove(protocol);
		}
		
		public void clearHeaders() {
			headers.clear();
		}
		
		public void addHeader(String key, String value) {
			headers.put(key, value);
		}
		
		public void removeHeader(String key) {
			headers.remove(key);
		}
		
		public void clearUserInfo() {
			userInfo.setKey(null);
			userInfo.setValue(null);
		}
		
		public void setUserInfo(String username, String password) {
			userInfo.setKey(username);
			userInfo.setValue(password);
		}
		
		public void setFrameQueueSize(int size) {
			frameQueueSize.set(size);
		}
		
		public void setMaxPayloadSize(int size) {
			maxPayloadSize.set(size);
		}
		
		public void apply(@NotNull WebSocket socket) {
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
