package distributed.systems.core;

import java.io.Serializable;
import java.util.HashMap;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Message-object that will be passed between nodes.
 * Keys used internally by the existing game:
 * - id
 * - origin
 */
@ToString
@NoArgsConstructor
public class Message implements Serializable {

	public enum Type {
		LOG, GENERIC, ROUND_FINISHED
	}

	@Getter @Setter
	private Type messageType = Type.GENERIC;

	public Message(Type messageType) {
		this.messageType = messageType;
	}

	private final HashMap<String, Serializable> content = new HashMap<>();

	public void put(String key, Serializable value) {
		content.put(key, value);
	}

	public Serializable get(String string) {
		return content.get(string);
	}
}
