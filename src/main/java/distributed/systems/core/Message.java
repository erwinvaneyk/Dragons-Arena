package distributed.systems.core;

import distributed.systems.network.NodeAddress;
import lombok.Getter;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

/**
 * Message-object that will be passed between nodes.
 * Keys used internally by the existing game:
 * - id
 * - origin
 */
public class Message implements Serializable {

	@Getter
	private String messageType = "DEFAULT";


	@Getter
	private final HashMap<String, Serializable> content = new HashMap<>();

	@Getter
	private final Date timestamp;

	@Getter
	private Date receivedTimestamp;

	@Getter
	private NodeAddress origin;

	Message(NodeAddress origin) {
		this.timestamp = new Date();
		this.origin = origin;
	}


	Message(String type) {
		this.timestamp = new Date();
		this.messageType = type;
	}

	Message(Message message) {
		this.content.putAll(message.content);
		this.timestamp = message.timestamp;
		this.messageType = message.messageType;
		this.origin = message.origin;
	}

	public Message put(String key, Serializable value) {
		content.put(key, value);
		return this;
	}

	public Serializable get(String string) {
		return content.get(string);
	}

	public Message setMessageType(String any) {
		messageType = any;
		return this;
	}

	public Message setOrigin(NodeAddress originId) {
		this.origin = originId;
		return this;
	}

	public void setReceivedTimestamp() {
		if(receivedTimestamp == null) {
			receivedTimestamp = new Date();
		}
	}

	public String toString() {
		return "[" + this.origin + "] Message(messageType=" + this.messageType + ", content=" + this.content
				+ ", timestamp=" + this.timestamp + ")";
	}
}
