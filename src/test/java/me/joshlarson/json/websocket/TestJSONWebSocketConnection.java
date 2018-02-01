package me.joshlarson.json.websocket;

import me.joshlarson.json.websocket.client.JSONWebSocketClient;
import me.joshlarson.json.websocket.server.JSONWebSocketServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
		testGeneric(new JSONWebSocketServer(Constants.PORT));
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
	
	@Test
	public void testServerBind1() throws IOException {
		testGeneric(new JSONWebSocketServer("localhost", Constants.PORT));
	}
	
	@Test
	public void testServerBind2() throws IOException {
		testGeneric(new JSONWebSocketServer(InetAddress.getLoopbackAddress(), Constants.PORT));
	}
	
	@Test
	public void testServerBind3() throws IOException {
		testGeneric(new JSONWebSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), Constants.PORT)));
	}
	
	private void testGeneric(JSONWebSocketServer server) throws IOException {
		JSONWebSocketClient socket = new JSONWebSocketClient();
		server.start();
		socket.connect(Constants.SERVER_URI);
		socket.disconnect();
		server.closeAllConnections();
		server.stop();
	}
	
}
