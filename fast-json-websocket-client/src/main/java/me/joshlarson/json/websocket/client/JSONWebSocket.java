package me.joshlarson.json.websocket.client;

import me.joshlarson.json.JSONException;
import me.joshlarson.json.JSONInputStream;
import me.joshlarson.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicReference;

public class JSONWebSocket {
	
	private final JSONWebSocketImpl impl;
	private final AtomicReference<JSONWebSocketHandler> handler;
	
	public JSONWebSocket() {
		this.impl = new JSONWebSocketImpl();
		this.handler = new AtomicReference<>(null);
		this.impl.setMessageHandler(new JSONWebSocketImplHandler() {
			public void onConnect() {
				JSONWebSocket.this.onConnect();
			}
			public void onDisconnect() {
				JSONWebSocket.this.onDisconnect();
			}
			public void onMessage(String message) {
				JSONWebSocket.this.onMessage(message);
			}
			public void onError(Throwable t) {
				JSONWebSocket.this.onError(t);
			}
		});
	}
	
	public void connect(String uri) throws URISyntaxException, IOException {
		connect(new URI(uri));
	}
	
	public void connect(URI uri) throws IOException {
		impl.connect(uri);
	}
	
	public void sendMessage(JSONObject object) {
		impl.sendMessage(object.toString(true));
	}
	
	public void setHandler(JSONWebSocketHandler handler) {
		this.handler.set(handler);
	}
	
	private void onConnect() {
		JSONWebSocketHandler handler = this.handler.get();
		if (handler != null)
			handler.onConnect(this);
	}
	
	private void onDisconnect() {
		JSONWebSocketHandler handler = this.handler.get();
		if (handler != null)
			handler.onDisconnect(this);
	}
	
	private void onMessage(String message) {
		try (JSONInputStream in = new JSONInputStream(message)) {
			JSONObject object = in.readObject();
			JSONWebSocketHandler handler = this.handler.get();
			if (handler != null)
				handler.onMessage(this, object);
		} catch (JSONException | IOException e) {
			onError(e);
		}
	}
	
	private void onError(Throwable t) {
		JSONWebSocketHandler handler = this.handler.get();
		if (handler != null)
			handler.onError(this, t);
	}
	
}
