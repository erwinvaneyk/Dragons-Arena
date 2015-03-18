package distributed.systems.core;

import java.io.Serializable;
import java.util.HashMap;


public class Message implements Serializable {
	
	private HashMap<String, Serializable> content = new HashMap<>();

	public void put(String key, Serializable value) {
		content.put(key, value);
	}

	public Serializable get(String string) {
		return content.get(string);
	}

	@Override
	public String toString() {
		return "Message{ content=" + content + '}';
	}
}
