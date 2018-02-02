package me.joshlarson.json.websocket;

import me.joshlarson.json.JSONException;
import me.joshlarson.json.JSONObject;
import me.joshlarson.json.websocket.client.CloseCode;
import me.joshlarson.json.websocket.client.JSONWebSocketClient;
import me.joshlarson.json.websocket.server.JSONWebSocketConnection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@RunWith(JUnit4.class)
public class TestJSONWebSocketServer extends TestJSONWebSocket {
	
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
		
		client.send(new JSONObject());
		waitForNumber(serverPongCount, 1);
		
		Assert.assertTrue(received.get());
		Assert.assertEquals(1, serverPongCount.get());
	}
	
	@Test
	public void testServerNoHandler() throws InterruptedException {
		server.setHandler(null);
		client.send(new JSONObject());
		client.ping();
		Thread.sleep(100);
		setupHandler();
	}
	
	@Test
	public void testServerTimedPing() {
		final AtomicBoolean validPong = new AtomicBoolean(false);
		final AtomicBoolean receivedPong = new AtomicBoolean(false);
		final AtomicLong startTime = new AtomicLong(-1);
		server.setHandler(new DefaultServerHandler() {
			@Override
			public void onMessage(@Nonnull JSONWebSocketConnection socket, @Nonnull JSONObject object) {
				startTime.set(System.nanoTime());
				try {
					socket.pingTimed();
				} catch (IOException e) {
					e.printStackTrace();
					failed.set(true);
				}
			}
			@Override
			public void onPongTimed(@Nonnull JSONWebSocketConnection socket, long rttNano) {
				long rx = System.nanoTime();
				validPong.set(Math.abs((rx - startTime.get()) - rttNano) <= 1E6); // within 1ms of measured
				receivedPong.set(true);
			}
		});
		client.send(new JSONObject());
		client.flush();
		waitForBoolean(receivedPong);
		Assert.assertTrue(receivedPong.get());
		Assert.assertTrue(validPong.get());
	}
	
	@Test
	public void testServerEcho() {
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
		Assert.assertTrue(client.send(object));
		client.flush();
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
		client.send(new JSONObject());
		client.flush();
		waitForBoolean(success);
		Assert.assertTrue(success.get());
	}
	
	@Test
	public void testServerEmptyJSON() {
		final AtomicBoolean success = new AtomicBoolean(false);
		serverMessageHandler = new ServerMessageHandler() {
			public void onMessage(JSONWebSocketConnection socket, JSONObject object) {
				try {
					socket.send(new JSONObject() {
						public String toString(boolean compact) { // Massive hack to ensure the receive fails
							return "";
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
		client.send(new JSONObject());
		client.flush();
		waitForBoolean(success);
		Assert.assertTrue(success.get());
	}
	
	@Test
	public void testServerConnectionUserData() {
		final AtomicBoolean firstRun = new AtomicBoolean(true);
		final AtomicBoolean completed = new AtomicBoolean(false);
		final AtomicInteger userData = new AtomicInteger(18000);
		serverMessageHandler = new ServerMessageHandler() {
			public void onMessage(JSONWebSocketConnection socket, JSONObject object) {
				if (firstRun.getAndSet(false)) {
					if (socket.getUserData() != null) {
						firstRun.set(false);
						failed.set(true);
						completed.set(true);
						return;
					}
					socket.setUserData(userData);
					return;
				}
				AtomicInteger userData = (AtomicInteger) socket.getUserData();
				if (userData == null) {
					failed.set(true);
					System.err.println("User Data was not set!");
					return;
				}
				failed.set(userData.get() != 18000);
				completed.set(true);
			}
		};
		client.send(new JSONObject());
		client.send(new JSONObject());
		client.flush();
		waitForBoolean(completed);
		Assert.assertTrue(completed.get());
	}
	
	@Test
	public void testClientSpontaneousDisconnect() {
		final AtomicBoolean success = new AtomicBoolean(false);
		serverMessageHandler = new ServerMessageHandler() {
			public void onMessage(JSONWebSocketConnection socket, JSONObject object) {
				try {
					socket.send(object);
					Thread.sleep(100);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
					failed.set(true);
				}
			}
		};
		serverErrorHandler = new ErrorHandler() {
			public void onError(Throwable t) {
				success.set(true);
			}
		};
		clientMessageHandler = new ClientMessageHandler() {
			public void onMessage(JSONWebSocketClient socket, JSONObject object) {
				socket.disconnect(CloseCode.ABNORMAL_CLOSURE, "", 0);
			}
		};
		client.send(new JSONObject());
		client.flush();
		waitForBoolean(success);
		Assert.assertTrue(success.get());
	}
	
}
