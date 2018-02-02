package me.joshlarson.json.websocket;

import me.joshlarson.json.JSONException;
import me.joshlarson.json.JSONObject;
import me.joshlarson.json.websocket.client.JSONWebSocketClient;
import me.joshlarson.json.websocket.server.JSONWebSocketConnection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@RunWith(JUnit4.class)
public class TestJSONWebSocketClient extends TestJSONWebSocket {
	
	@Test
	public void testClientInvalidJSON() {
		final AtomicBoolean success = new AtomicBoolean(false);
		serverErrorHandler = new ErrorHandler() {
			public void onError(Throwable t) {
				success.set(t instanceof JSONException);
			}
		};
		client.send(new JSONObject() {
			public String toString(boolean compact) { // Massive hack to ensure the receive fails
				return "{invalid{";
			}
		});
		client.flush();
		waitForBoolean(success);
		Assert.assertTrue(success.get());
	}
	
	@Test
	public void testClientEmptyJSON() {
		final AtomicBoolean success = new AtomicBoolean(false);
		serverErrorHandler = new ErrorHandler() {
			public void onError(Throwable t) {
				success.set(t instanceof JSONException);
			}
		};
		client.send(new JSONObject() {
			public String toString(boolean compact) { // Massive hack to ensure the receive fails
				return "";
			}
		});
		client.flush();
		waitForBoolean(success);
		Assert.assertTrue(success.get());
	}
	
	@Test
	public void testClientPings() {
		waitForNumber(clientPongCount, 0);
		Assert.assertEquals(0, clientPongCount.get());
		client.ping();
		waitForNumber(clientPongCount, 1);
		Assert.assertEquals(1, clientPongCount.get());
		client.ping(new byte[]{1, 2, 3, 4});
		waitForNumber(clientPongCount, 2);
		Assert.assertEquals(2, clientPongCount.get());
		client.ping(ByteBuffer.wrap(new byte[]{ 5, 6, 7, 8}));
		waitForNumber(clientPongCount, 3);
		Assert.assertEquals(3, clientPongCount.get());
	}
	
	@Test
	public void testClientTimedPing() {
		final AtomicBoolean validPong = new AtomicBoolean(false);
		final AtomicBoolean receivedPong = new AtomicBoolean(false);
		final AtomicLong startTime = new AtomicLong(-1);
		client.setHandler(new DefaultClientHandler() {
			@Override
			public void onPongTimed(@Nonnull JSONWebSocketClient socket, long rttNano) {
				long rx = System.nanoTime();
				validPong.set(Math.abs((rx - startTime.get()) - rttNano) <= 1E6); // within 1ms of measured
				receivedPong.set(true);
			}
		});
		startTime.set(System.nanoTime());
		client.pingTimed();
		waitForBoolean(receivedPong);
		Assert.assertTrue(receivedPong.get());
		Assert.assertTrue(validPong.get());
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
				t.printStackTrace();
				success.set(true);
			}
		};
		for (int i = 0; i < 512; i++) {
			JSONObject obj = new JSONObject();
			obj.put("id", i);
			client.send(obj);
			client.flush();
		}
		waitForBoolean(success);
		Assert.assertTrue(success.get());
	}
	
}
