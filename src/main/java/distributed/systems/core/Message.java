package distributed.systems.core;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Message-object that will be passed between nodes.
 * Keys used internally by the existing game:
 * - id
 * - origin
 */
@ToString
public class Message implements Serializable {

	public enum Type {
		LOG, GENERIC, ROUND_FINISHED, HANDSHAKE, DISCOVER
	}

	@Getter @Setter
	private Type messageType = Type.GENERIC;

	@Getter
	private final HashMap<String, Serializable> content = new HashMap<>();

	@Getter
	private final Date timestamp;

	@Getter
	private Date receivedTimestamp;

	@Getter @Setter
	private String originId;

	public Message(Type messageType) {
		this.messageType = messageType;
		this.timestamp = new Date();
	}

	public Message() {
		this.timestamp = new Date();
	}

	public Message put(String key, Serializable value) {
		content.put(key, value);
		return this;
	}

	public Serializable get(String string) {
		return content.get(string);
	}

	public void setReceivedTimestamp() {
		if(receivedTimestamp == null) {
			receivedTimestamp = new Date();
		}
	}

	public Message(Message message) {
		this.content.putAll(message.content);
		this.timestamp = message.timestamp;
		this.receivedTimestamp = message.receivedTimestamp;
		this.messageType = message.messageType;
		this.originId = message.originId;
	}
}
