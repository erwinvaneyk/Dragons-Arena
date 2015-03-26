package distributed.systems.core;

import java.util.Queue;
import java.util.concurrent.SynchronousQueue;

/**
 *
 * Only have 1 message of a player at any time
 * If it receives a message from a player, who already has a message in the queue, it will throw out the oldest message (based on the timestamp)
 */
public class SynchronizedQueue {

	private final SynchronousQueue<Message> queue;

	public SynchronizedQueue() {
		queue = new SynchronousQueue<>();
	}

	public void add(Message message) {
		removeMessages(message.getOrigin().getName());
		queue.add(message);
	}

	public Message poll() {
		return queue.poll();
	}

	private void removeMessages(String id) {
		queue.stream().filter(el -> {
			return el.getOrigin().getName().equals(id);
		}).forEach(queue::remove);
	}

}
