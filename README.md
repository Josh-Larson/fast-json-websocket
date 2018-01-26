## How to get it:

#### Client:
<a href='https://bintray.com/josh-larson/fast-json-websocket/fast-json-websocket-client?source=watch' alt='Get automatic notifications about new "fast-json-websocket-client" versions'><img src='https://www.bintray.com/docs/images/bintray_badge_color.png'></a>
 [ ![Download](https://api.bintray.com/packages/josh-larson/fast-json-websocket/fast-json-websocket-client/images/download.svg) ](https://bintray.com/josh-larson/fast-json-websocket/fast-json-websocket-client/_latestVersion) 

#### Server:
<a href='https://bintray.com/josh-larson/fast-json-websocket/fast-json-websocket-server?source=watch' alt='Get automatic notifications about new "fast-json-websocket-server" versions'><img src='https://www.bintray.com/docs/images/bintray_badge_color.png'></a>
[ ![Download](https://api.bintray.com/packages/josh-larson/fast-json-websocket/fast-json-websocket-server/images/download.svg) ](https://bintray.com/josh-larson/fast-json-websocket/fast-json-websocket-server/_latestVersion)

## How to use it:

#### Client:

```java
JSONWebSocketClient client = new JSONWebSocketClient();
client.connect("ws://localhost:80");
client.setHandler(new JSONWebSocketHandler() {
    public void onConnect(JSONWebSocketClient socket) { }
    public void onDisconnect(JSONWebSocketClient socket) { }
    public void onMessage(JSONWebSocketClient socket, JSONObject object) { }
    public void onPong(JSONWebSocketClient socket, ByteBuffer data) { }
    public void onError(JSONWebSocketClient socket, Throwable t) { }
});

JSONObject example = new JSONObject();
example.put("key", "value");
client.send(example);

client.disconnect();
```

#### Server:

```java
JSONWebSocketServer server = new JSONWebSocketServer(80);
server.setHandler(new JSONWebSocketConnectionHandler() {
    public void onConnect(JSONWebSocketConnection socket) { }
    public void onDisconnect(JSONWebSocketConnection socket) { }
    public void onMessage(JSONWebSocketConnection socket, JSONObject object) { }
    public void onPong(JSONWebSocketConnection socket, ByteBuffer data) { }
    public void onError(JSONWebSocketConnection socket, Throwable t) { }
});
server.start();
// ...
server.stop();
```
