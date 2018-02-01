package me.joshlarson.json.websocket;

import me.joshlarson.json.JSONObject;
import me.joshlarson.json.websocket.client.JSONWebSocketClient;
import me.joshlarson.json.websocket.client.JSONWebSocketHandler;
import me.joshlarson.json.websocket.server.JSONWebSocketConnection;
import me.joshlarson.json.websocket.server.JSONWebSocketConnectionHandler;
import me.joshlarson.json.websocket.server.JSONWebSocketServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TestJSONWebSocket {
	
	protected final AtomicBoolean connected = new AtomicBoolean(false);
	protected final AtomicBoolean disconnected = new AtomicBoolean(false);
	protected final AtomicInteger serverPongCount = new AtomicInteger(0);
	protected final AtomicInteger clientPongCount = new AtomicInteger(0);
	protected final AtomicBoolean failed = new AtomicBoolean(false);
	protected final Set<Long> sessionIds = new HashSet<>();
	private final ServerMessageHandler defaultServerMessageHandler = new ServerMessageHandler() {
		
		public void onMessage(JSONWebSocketConnection socket, JSONObject object) { }
	};
	private final ClientMessageHandler defaultClientMessageHandler = new ClientMessageHandler() {
		
		public void onMessage(JSONWebSocketClient socket, JSONObject object) { }
	};
	private final ErrorHandler defaultErrorHandler = new ErrorHandler() {
		
		public void onError(Throwable t) {
			t.printStackTrace();
			failed.set(true);
		}
	};
	protected JSONWebSocketServer server;
	protected JSONWebSocketClient client;
	protected ServerMessageHandler serverMessageHandler;
	protected ErrorHandler serverErrorHandler;
	protected ClientMessageHandler clientMessageHandler;
	protected ErrorHandler clientErrorHandler;
	
	@Before
	public void initializeConnections() throws IOException {
		server = new JSONWebSocketServer(Constants.PORT);
		client = new JSONWebSocketClient();
		connected.set(false);
		disconnected.set(false);
		serverPongCount.set(0);
		clientPongCount.set(0);
		failed.set(false);
		setupHandler();
		
		server.start();
		client.connect(Constants.SERVER_URI);
		waitForBoolean(connected);
		Assert.assertTrue(client.isConnected());
	}
	
	@After
	public void terminateConnections() {
		try {
			server.closeAllConnections();
			waitForDisconnect();
			Assert.assertTrue(connected.get());
			Assert.assertTrue(disconnected.get());
			Assert.assertFalse(failed.get());
		} finally {
			server.stop();
		}
	}
	
	protected void setupHandler() {
		serverMessageHandler = defaultServerMessageHandler;
		clientMessageHandler = defaultClientMessageHandler;
		serverErrorHandler = defaultErrorHandler;
		clientErrorHandler = defaultErrorHandler;
		server.setHandler(new DefaultServerHandler());
		client.setHandler(new DefaultClientHandler());
	}
	
	protected void safePing(JSONWebSocketConnection socket) {
		try {
			socket.pingRandom();
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	protected void waitForDisconnect() {
		waitForBoolean(disconnected);
	}
	
	protected interface ServerMessageHandler {
		
		void onMessage(JSONWebSocketConnection socket, JSONObject object);
	}
	
	protected interface ClientMessageHandler {
		
		void onMessage(JSONWebSocketClient socket, JSONObject object);
	}
	
	protected interface ErrorHandler {
		
		void onError(Throwable t);
	}
	
	protected class DefaultServerHandler extends JSONWebSocketConnectionHandler {
		
		public void onConnect(JSONWebSocketConnection socket) {
			connected.set(true);
			if (!sessionIds.add(socket.getSocketId()))
				failed.set(true);
			if (!socket.getRemoteIpAddress().equals("127.0.0.1"))
				failed.set(true);
		}
		
		public void onDisconnect(JSONWebSocketConnection socket) { disconnected.set(true); }
		
		public void onMessage(JSONWebSocketConnection socket, JSONObject object) { serverMessageHandler.onMessage(socket, object); }
		
		public void onPong(JSONWebSocketConnection socket, ByteBuffer data) { serverPongCount.incrementAndGet(); }
		
		public void onError(JSONWebSocketConnection socket, Throwable t) { serverErrorHandler.onError(t); }
	}
	
	protected class DefaultClientHandler extends JSONWebSocketHandler {
		
		public void onConnect(JSONWebSocketClient socket) {
			
		}
		
		public void onDisconnect(JSONWebSocketClient socket) {
			
		}
		
		public void onMessage(JSONWebSocketClient socket, JSONObject object) {
			clientMessageHandler.onMessage(socket, object);
		}
		
		public void onPong(JSONWebSocketClient socket, ByteBuffer data) {
			clientPongCount.incrementAndGet();
		}
		
		public void onPongTimed(JSONWebSocketClient socket, long rttNano) {
			
		}
		
		public void onError(JSONWebSocketClient socket, Throwable t) {
			clientErrorHandler.onError(t);
		}
	}
	
	protected static void waitForNumber(AtomicInteger i, int value) {
		long start = System.nanoTime();
		while (i.get() != value && (System.nanoTime() - start) < 1E9) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				Assert.fail("Interrupted");
			}
		}
	}
	
	protected static void waitForBoolean(AtomicBoolean bool) {
		long start = System.nanoTime();
		while (!bool.get() && (System.nanoTime() - start) < 1E9) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				Assert.fail("Interrupted");
			}
		}
	}
}
