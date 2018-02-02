package me.joshlarson.json.websocket;

import me.joshlarson.json.JSONObject;
import me.joshlarson.json.websocket.client.JSONWebSocketClient;
import me.joshlarson.json.websocket.client.JSONWebSocketHandler;
import me.joshlarson.json.websocket.server.JSONWebSocketConnection;
import me.joshlarson.json.websocket.server.JSONWebSocketConnectionHandler;
import me.joshlarson.json.websocket.server.JSONWebSocketServer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.annotation.Nonnull;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(JUnit4.class)
public class TestJSONWebSocketSecure {
	
	@Test
	public void testSecure() throws IOException, URISyntaxException {
		SSLContext sslContext = createSSLContext(getClass().getResourceAsStream("/keystore.jks"));
		JSONWebSocketServer server = new JSONWebSocketServer(0);
		server.makeSecure(sslContext.getServerSocketFactory(), null);
		server.start();
		JSONWebSocketClient client = new JSONWebSocketClient();
		client.setSSLContext(sslContext);
		
		final AtomicBoolean disconnected = new AtomicBoolean(false);
		final AtomicBoolean completed = new AtomicBoolean(false);
		final AtomicBoolean failed = new AtomicBoolean(false);
		server.setHandler(new JSONWebSocketConnectionHandler() {
			public void onDisconnect(@Nonnull JSONWebSocketConnection socket) {
				disconnected.set(true);
			}
			public void onMessage(@Nonnull JSONWebSocketConnection socket, @Nonnull JSONObject object) {
				try {
					socket.send(object);
				} catch (IOException e) {
					e.printStackTrace();
					failed.set(true);
					completed.set(true);
				}
			}
		});
		client.setHandler(new JSONWebSocketHandler() {
			public void onMessage(@Nonnull JSONWebSocketClient socket, @Nonnull JSONObject object) {
				completed.set(true);
			}
		});
		
		client.connect("wss://localhost:"+server.getListeningPort());
		client.send(new JSONObject());
		waitForBoolean(completed);
		client.disconnect();
		waitForBoolean(disconnected);
		server.stop();
		Assert.assertTrue(completed.get());
		Assert.assertTrue(disconnected.get());
		Assert.assertFalse(failed.get());
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
	
	private static SSLContext createSSLContext(InputStream keystoreStream) throws IOException {
		try {
			char[] passphrase = new char[]{'p','a','s','s','w','o','r','d'};
			KeyStore keystore = KeyStore.getInstance("PKCS12");
			
			keystore.load(keystoreStream, passphrase);
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keystore, passphrase);
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init(keystore);
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
			return ctx;
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}
	
}
