package me.joshlarson.json.websocket.client;

@SuppressWarnings("unused")
public enum CloseCode {
	NORMAL_CLOSURE				(1000),
	GOING_AWAY					(1001),
	PROTOCOL_ERROR				(1002),
	UNSUPPORTED_DATA			(1003),
	NO_STATUS_RECEIVED			(1005),
	ABNORMAL_CLOSURE			(1006),
	INVALID_FRAME_PAYLOAD_DATA	(1007),
	POLICY_VIOLATION			(1008),
	MESSAGE_TOO_BIG				(1009),
	MANDATORY_EXIT				(1010),
	INTERNAL_SERVER_ERROR		(1011),
	TLS_HANDSHAKE				(1015);
	
	private final int code;
	
	CloseCode(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
	
}
