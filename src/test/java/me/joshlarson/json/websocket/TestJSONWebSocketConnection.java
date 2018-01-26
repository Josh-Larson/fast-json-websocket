package me.joshlarson.json.websocket;

import me.joshlarson.json.websocket.server.JSONWebSocketServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.net.URISyntaxException;

@RunWith(JUnit4.class)
public class TestJSONWebSocketConnection {
	
	@Test(expected=IOException.class)
	public void testNoConnection() throws IOException {
		JSONWebSocketClient socket = new JSONWebSocketClient();
		socket.connect(Constants.SERVER_URI);
	}
	
	@Test
	public void testBasicConnection() throws IOException {
		JSONWebSocketClient socket = new JSONWebSocketClient();
		JSONWebSocketServer server = new JSONWebSocketServer(Constants.PORT);
		server.start();
		socket.connect(Constants.SERVER_URI);
		socket.disconnect();
		server.closeAllConnections();
		server.stop();
	}
	
	@Test
	public void testBasicConnectionUriString() throws IOException, URISyntaxException {
		JSONWebSocketClient socket = new JSONWebSocketClient();
		JSONWebSocketServer server = new JSONWebSocketServer(Constants.PORT);
		server.start();
		socket.connect(Constants.SERVER_URI.toString());
		socket.disconnect();
		server.closeAllConnections();
		server.stop();
	}
	
}
