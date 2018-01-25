package me.joshlarson.json.websocket.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.net.URI;

@RunWith(JUnit4.class)
public class TestJSONWebSocket {
	
	@Test(expected=IOException.class)
	public void testNoConnection() throws IOException {
		JSONWebSocket socket = new JSONWebSocket();
		socket.connect(URI.create("wss://localhost:17296"));
	}
	
}
