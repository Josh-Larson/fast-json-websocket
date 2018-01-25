package me.joshlarson.json.websocket.client;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

@ClientEndpoint
public class JSONWebSocketImpl {
	
	private final AtomicReference<JSONWebSocketImplHandler> messageHandler;
	private final AtomicReference<Session> session;
	private final Object connectionMutex;
	
	public JSONWebSocketImpl() {
		this.messageHandler = new AtomicReference<>(null);
		this.session = new AtomicReference<>(null);
		this.connectionMutex = new Object();
	}
	
	public void connect(URI endpoint) throws IOException {
		try {
			synchronized (connectionMutex) {
				ContainerProvider.getWebSocketContainer().connectToServer(this, endpoint);
			}
		} catch (DeploymentException e) {
			throw new IOException("WebSocket error: " + e.getMessage());
		}
	}
	
	@OnOpen
	public void onOpen(Session userSession) {
		this.session.set(userSession);
		getHandler().onConnect();
	}
	
	@OnClose
	public void onClose(Session userSession, CloseReason reason) {
		this.session.set(null);
		getHandler().onDisconnect();
	}
	
	@OnMessage
	public void onMessage(String message) {
		getHandler().onMessage(message);
	}
	
	@OnError
	public void onError(Throwable t) {
		getHandler().onError(t);
	}
	
	public void setMessageHandler(JSONWebSocketImplHandler handler) {
		this.messageHandler.set(handler);
	}
	
	public void sendMessage(String message) {
		Session session = this.session.get();
		if (session != null)
			session.getAsyncRemote().sendText(message);
	}
	
	private JSONWebSocketImplHandler getHandler() {
		JSONWebSocketImplHandler handler = messageHandler.get();
		assert handler != null;
		return handler;
	}
	
}
