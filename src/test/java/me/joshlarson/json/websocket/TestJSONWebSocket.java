package me.joshlarson.json.websocket;

import me.joshlarson.json.JSONObject;
import me.joshlarson.json.websocket.server.JSONWebSocketConnection;
import me.joshlarson.json.websocket.server.JSONWebSocketConnectionHandler;
import me.joshlarson.json.websocket.server.JSONWebSocketServer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(JUnit4.class)
public class TestJSONWebSocket {
	
	private static final int PORT = 17289;
	
	@Test(expected=IOException.class)
	public void testNoConnection() throws IOException {
		JSONWebSocketClient socket = new JSONWebSocketClient();
		socket.connect(URI.create("ws://localhost:"+PORT));
	}
	
	@Test
	public void testBasicConnection() throws IOException {
		JSONWebSocketClient socket = new JSONWebSocketClient();
		JSONWebSocketServer server = new JSONWebSocketServer(PORT);
		server.start();
		socket.connect(URI.create("ws://localhost:"+PORT));
		socket.disconnect();
		server.closeAllConnections();
		server.stop();
	}
	
	@Test
	public void testMessageSend() throws IOException, InterruptedException {
		final AtomicBoolean connected = new AtomicBoolean(false);
		final AtomicBoolean disconnected = new AtomicBoolean(false);
		final AtomicBoolean pong = new AtomicBoolean(false);
		final AtomicBoolean received = new AtomicBoolean(false);
		JSONWebSocketClient socket = new JSONWebSocketClient();
		JSONWebSocketServer server = new JSONWebSocketServer(PORT);
		server.setHandler(new JSONWebSocketConnectionHandler() {
			public void onConnect(JSONWebSocketConnection socket) { connected.set(true); }
			public void onDisconnect(JSONWebSocketConnection socket) { disconnected.set(true); }
			public void onPong(JSONWebSocketConnection socket) {
				pong.set(true);
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
					Assert.fail(e.getMessage());
				}
			}
			public void onError(JSONWebSocketConnection socket, Throwable t) { Assert.fail(t.getMessage()); }
			public void onMessage(JSONWebSocketConnection socket, JSONObject object) {
				Assert.assertTrue(object.containsKey("key"));
				Assert.assertTrue(object.containsKey("num"));
				Assert.assertEquals("value", object.getString("key"));
				Assert.assertEquals(128, object.getInt("num"));
				received.set(true);
				try {
					socket.ping();
				} catch (IOException e) {
					e.printStackTrace();
					Assert.fail();
				}
			}
		});
		server.start();
		socket.connect(URI.create("ws://localhost:"+PORT));
		
		JSONObject object = new JSONObject();
		object.put("key", "value");
		object.put("num", 128);
		socket.sendMessage(object);
		
		long start = System.nanoTime();
		while (!disconnected.get() && (System.nanoTime() - start) < 1E9) {
			Thread.sleep(1);
		}
		
		Assert.assertTrue(connected.get());
		Assert.assertTrue(disconnected.get());
		Assert.assertTrue(received.get());
		Assert.assertTrue(pong.get());
		socket.disconnect();
		server.closeAllConnections();
		server.stop();
	}
	
	@Test
	public void testMessageSendNoHandler() throws IOException, InterruptedException {
		JSONWebSocketClient socket = new JSONWebSocketClient();
		JSONWebSocketServer server = new JSONWebSocketServer(PORT);
		server.start();
		socket.connect(URI.create("ws://localhost:"+PORT));
		
		JSONObject object = new JSONObject();
		object.put("key", "value");
		object.put("num", 128);
		socket.sendMessage(object);
		
		Thread.sleep(50);
		
		socket.disconnect();
		server.closeAllConnections();
		server.stop();
	}
	
}
