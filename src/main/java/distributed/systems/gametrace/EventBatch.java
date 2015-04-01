package distributed.systems.gametrace;

import java.util.Map;

import lombok.Data;

@Data
public class EventBatch {

	public enum EventType {
		PLAYER_JOIN, PLAYER_LEAVE
	}

	private long timestamp;

	private Map<Integer, EventType> events;

	public EventBatch addEvent(Integer id, EventType event) {
		events.put(id, event);
		return this;
	}

}
