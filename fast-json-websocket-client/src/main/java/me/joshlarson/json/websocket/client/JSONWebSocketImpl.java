package me.joshlarson.json.websocket.client;

import javax.websocket.*;
import javax.websocket.CloseReason.CloseCode;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

@ClientEndpoint
public class JSONWebSocketImpl {
	
	private final AtomicReference<JSONWebSocketImplHandler> messageHandler;
	private final AtomicReference<Session> session;
	private final Random random;
	private final Object connectionMutex;
	
	JSONWebSocketImpl() {
		this.messageHandler = new AtomicReference<>(null);
		this.session = new AtomicReference<>(null);
		this.random = new Random();
		this.connectionMutex = new Object();
	}
	
	public boolean isConnected() {
		Session session = this.session.get();
		return session != null && session.isOpen();
	}
	
	public boolean isSecure() {
		Session session = this.session.get();
		return session != null && session.isSecure();
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
	
	public void disconnect() throws IOException {
		Session session = this.session.get();
		if (session != null)
			session.close();
	}
	
	public void disconnect(CloseCode code, String reason) throws IOException {
		Session session = this.session.get();
		if (session != null)
			session.close(new CloseReason(code, reason));
	}
	
	@OnOpen
	public void onOpen(Session userSession) {
		this.session.set(userSession);
		userSession.addMessageHandler(new MessageHandler.Whole<PongMessage>() {
			public void onMessage(PongMessage message) {
				getHandler().onPong(message.getApplicationData());
			}
		});
		try {
			getHandler().onConnect();
		} catch (Throwable user) {
			System.err.println("Exception in handler's onConnect() function");
			user.printStackTrace();
		}
	}
	
	@OnClose
	public void onClose(Session userSession, CloseReason reason) {
		this.session.set(null);
		try {
			getHandler().onDisconnect();
		} catch (Throwable user) {
			System.err.println("Exception in handler's onDisconnect() function");
			user.printStackTrace();
		}
	}
	
	@OnMessage
	public void onMessage(String message) {
		try {
			getHandler().onMessage(message);
		} catch (Throwable user) {
			System.err.println("Exception in handler's onMessage() function");
			user.printStackTrace();
		}
	}
	
	@OnError
	public void onError(Throwable t) {
		try {
			getHandler().onError(t);
		} catch (Throwable user) {
			System.err.println("Exception in handler's onError() function");
			user.printStackTrace();
		}
	}
	
	public void setMessageHandler(JSONWebSocketImplHandler handler) {
		this.messageHandler.set(handler);
	}
	
	public boolean send(String message) {
		Session session = this.session.get();
		if (session == null)
			return false;
		try {
			session.getBasicRemote().sendText(message);
			return true;
		} catch (Throwable t) {
			onError(t);
		}
		return false;
	}
	
	public void ping() throws IOException {
		byte [] pingData = new byte[4];
		random.nextBytes(pingData);
		ping(pingData);
	}
	
	public void ping(byte [] data) throws IOException {
		ping(ByteBuffer.wrap(data));
	}
	
	public void ping(ByteBuffer data) throws IOException {
		Session session = this.session.get();
		if (session != null)
			session.getAsyncRemote().sendPing(data);
	}
	
	private JSONWebSocketImplHandler getHandler() {
		JSONWebSocketImplHandler handler = messageHandler.get();
		assert handler != null;
		return handler;
	}
	
}
