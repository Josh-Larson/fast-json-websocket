package me.joshlarson.json.websocket;

import me.joshlarson.json.JSONException;
import me.joshlarson.json.JSONObject;
import me.joshlarson.json.websocket.server.JSONWebSocketConnection;
import me.joshlarson.json.websocket.server.JSONWebSocketConnectionHandler;
import me.joshlarson.json.websocket.server.JSONWebSocketServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(JUnit4.class)
public class TestJSONWebSocketLifecycle {
	
	private final ServerMessageHandler defaultServerMessageHandler = new ServerMessageHandler() {
		public void onMessage(JSONWebSocketConnection socket, JSONObject object) { }
	};
	private final ClientMessageHandler defaultClientMessageHandler = new ClientMessageHandler() {
		public void onMessage(JSONWebSocketClient socket, JSONObject object) { }
	};
	private final ErrorHandler defaultErrorHandler = new ErrorHandler() {
		public void onError(Throwable t) { t.printStackTrace(); failed.set(true); }
	};
	
	private final AtomicBoolean connected = new AtomicBoolean(false);
	private final AtomicBoolean disconnected = new AtomicBoolean(false);
	private final AtomicInteger pongCount = new AtomicInteger(0);
	private final AtomicBoolean failed = new AtomicBoolean(false);
	
	private JSONWebSocketServer server;
	private JSONWebSocketClient client;
	private ServerMessageHandler serverMessageHandler;
	private ErrorHandler serverErrorHandler;
	private ClientMessageHandler clientMessageHandler;
	private ErrorHandler clientErrorHandler;
	
	@Before
	public void initializeConnections() throws IOException {
		server = new JSONWebSocketServer(Constants.PORT);
		client = new JSONWebSocketClient();
		connected.set(false);
		disconnected.set(false);
		pongCount.set(0);
		failed.set(false);
		setupHandler();
		
		server.start();
		client.connect(Constants.SERVER_URI);
		waitForBoolean(connected);
		Assert.assertTrue(client.isConnected());
		Assert.assertFalse(client.isSecure());
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
	
	@Test
	public void testServerHandler()  {
		final AtomicBoolean received = new AtomicBoolean(false);
		serverMessageHandler = new ServerMessageHandler() {
			@Override
			public void onMessage(JSONWebSocketConnection socket, JSONObject object) {
				received.set(true);
				safePing(socket);
				if (!socket.isConnected()) {
					System.err.println("Socket is not connected when receiving a message");
					failed.set(true);
				}
			}
		};
		
		client.sendMessage(new JSONObject());
		waitForNumber(pongCount, 1);
		
		Assert.assertTrue(received.get());
		Assert.assertEquals(1, pongCount.get());
	}
	
	@Test
	public void testServerNoHandler() throws IOException, InterruptedException {
		server.setHandler(null);
		client.sendMessage(new JSONObject());
		client.ping();
		Thread.sleep(100);
		setupHandler();
	}
	
	@Test
	public void testClientInvalidJSON() {
		final AtomicBoolean success = new AtomicBoolean(false);
		serverErrorHandler = new ErrorHandler() {
			public void onError(Throwable t) {
				success.set(t instanceof JSONException);
			}
		};
		client.sendMessage(new JSONObject() {
			public String toString(boolean compact) { // Massive hack to ensure the receive fails
				return "{invalid{";
			}
		});
		waitForBoolean(success);
		Assert.assertTrue(success.get());
	}
	
	@Test
	public void testEchoServer() {
		final AtomicBoolean receivedEcho = new AtomicBoolean(false);
		final AtomicBoolean validEcho = new AtomicBoolean(false);
		serverMessageHandler = new ServerMessageHandler() {
			public void onMessage(JSONWebSocketConnection socket, JSONObject object) {
				try {
					socket.send(object);
				} catch (IOException e) {
					e.printStackTrace();
					failed.set(true);
				}
			}
		};
		clientMessageHandler = new ClientMessageHandler() {
			public void onMessage(JSONWebSocketClient socket, JSONObject object) {
				boolean valid = object.containsKey("key");
				valid = valid && object.containsKey("num");
				valid = valid && object.getString("key").equals("val");
				valid = valid && object.getInt("num") == 256;
				receivedEcho.set(true);
				validEcho.set(valid);
			}
		};
		JSONObject object = new JSONObject();
		object.put("key", "val");
		object.put("num", 256);
		client.sendMessage(object);
		waitForBoolean(receivedEcho);
		Assert.assertTrue(receivedEcho.get());
		Assert.assertTrue(validEcho.get());
	}
	
	@Test
	public void testServerInvalidJSON() {
		final AtomicBoolean success = new AtomicBoolean(false);
		serverMessageHandler = new ServerMessageHandler() {
			public void onMessage(JSONWebSocketConnection socket, JSONObject object) {
				try {
					socket.send(new JSONObject() {
						public String toString(boolean compact) { // Massive hack to ensure the receive fails
							return "{invalid{";
						}
					});
				} catch (IOException e) {
					e.printStackTrace();
					failed.set(true);
				}
			}
		};
		clientErrorHandler = new ErrorHandler() {
			public void onError(Throwable t) {
				success.set(t instanceof JSONException);
			}
		};
		client.sendMessage(new JSONObject());
		waitForBoolean(success);
		Assert.assertTrue(success.get());
	}
	
	@Test
	public void testClientPings() throws IOException {
		client.ping();
		client.ping(new byte[]{1, 2, 3, 4});
		client.ping(ByteBuffer.wrap(new byte[]{5, 6, 7, 8}));
	}
	
	@Test
	public void testServerSpontaneousDisconnect() {
		final AtomicBoolean success = new AtomicBoolean(false);
		final AtomicInteger count = new AtomicInteger(0);
		serverMessageHandler = new ServerMessageHandler() {
			public void onMessage(JSONWebSocketConnection socket, JSONObject object) {
				if (count.getAndIncrement() < 256)
					return;
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
					failed.set(true);
				}
			}
		};
		clientErrorHandler = new ErrorHandler() {
			public void onError(Throwable t) {
				success.set(t instanceof IOException);
			}
		};
		for (int i = 0; i < 512; i++) {
			client.sendMessage(new JSONObject());
		}
		waitForBoolean(success);
		Assert.assertTrue(success.get());
	}
	
	@Test
	public void testClientSpontaneousDisconnect() {
		final AtomicBoolean success = new AtomicBoolean(false);
		final AtomicInteger count = new AtomicInteger(0);
		serverMessageHandler = new ServerMessageHandler() {
			public void onMessage(JSONWebSocketConnection socket, JSONObject object) {
				try {
					for (int i = 0; i < 512; i++) {
						socket.send(object);
					}
				} catch (IOException e) {
					e.printStackTrace();
					failed.set(true);
				}
			}
		};
		serverErrorHandler = new ErrorHandler() {
			public void onError(Throwable t) {
				success.set(t instanceof IOException);
			}
		};
		clientMessageHandler = new ClientMessageHandler() {
			public void onMessage(JSONWebSocketClient socket, JSONObject object) {
				if (count.getAndIncrement() < 256)
					return;
				try {
					socket.disconnect();
				} catch (IOException e) {
					e.printStackTrace();
					failed.set(true);
				}
			}
		};
		client.sendMessage(new JSONObject());
		waitForBoolean(success);
		Assert.assertTrue(success.get());
	}
	
	private void setupHandler() {
		serverMessageHandler = defaultServerMessageHandler;
		clientMessageHandler = defaultClientMessageHandler;
		serverErrorHandler = defaultErrorHandler;
		clientErrorHandler = defaultErrorHandler;
		server.setHandler(new JSONWebSocketConnectionHandler() {
			public void onConnect(JSONWebSocketConnection socket) { connected.set(true); }
			public void onDisconnect(JSONWebSocketConnection socket) { disconnected.set(true); }
			public void onMessage(JSONWebSocketConnection socket, JSONObject object) { serverMessageHandler.onMessage(socket, object); }
			public void onPong(JSONWebSocketConnection socket) { pongCount.incrementAndGet(); }
			public void onError(JSONWebSocketConnection socket, Throwable t) { serverErrorHandler.onError(t); }
		});
		client.setHandler(new JSONWebSocketHandler() {
			public void onConnect(JSONWebSocketClient socket) { }
			public void onDisconnect(JSONWebSocketClient socket) { }
			public void onMessage(JSONWebSocketClient socket, JSONObject object) { clientMessageHandler.onMessage(socket, object); }
			public void onError(JSONWebSocketClient socket, Throwable t) { clientErrorHandler.onError(t); }
		});
	}
	
	private void safePing(JSONWebSocketConnection socket) {
		try {
			socket.ping();
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	private static void waitForNumber(AtomicInteger i, int value) {
		long start = System.nanoTime();
		while (i.get() != value && (System.nanoTime() - start) < 1E9) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				Assert.fail("Interrupted");
			}
		}
	}
	
	private static void waitForBoolean(AtomicBoolean bool) {
		long start = System.nanoTime();
		while (!bool.get() && (System.nanoTime() - start) < 1E9) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				Assert.fail("Interrupted");
			}
		}
	}
	
	private void waitForDisconnect() {
		waitForBoolean(disconnected);
	}
	
	private interface ServerMessageHandler {
		void onMessage(JSONWebSocketConnection socket, JSONObject object);
	}
	
	private interface ClientMessageHandler {
		void onMessage(JSONWebSocketClient socket, JSONObject object);
	}
	
	private interface ErrorHandler {
		void onError(Throwable t);
	}
	
}