package distributed.systems.network.messagehandlers;

import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import distributed.systems.core.Message;
import distributed.systems.das.units.Unit;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.ServerNode;
import lombok.EqualsAndHashCode;

public class SynchronizedGameActionHandler implements MessageHandler {
	public static final String MESSAGE_TYPE = "SYNC_ACTION";

	public static final long TIMEOUT_MILLISECONDS = 1000;
	private static int FREE_ID = 0;
	private final ServerNode me;
	private Map<Claim, Integer> approvalMap = new ConcurrentHashMap<>();

	@EqualsAndHashCode
	private class Claim implements Serializable {
		public final int tx;
		public final int ty;
		public final int id;
		public final long timestamp;

		public Claim(int id, int tx, int ty, long timestamp) {
			this.tx = tx;
			this.ty = ty;
			this.id = id;
			this.timestamp = timestamp;
		}
	}

	public SynchronizedGameActionHandler(ServerNode me) {
		this.me = me;
	}
	
	@Override
	public String getMessageType() {
		return MESSAGE_TYPE;
	}

	/**
	 * Ask permission to move
	 */
	public boolean synchronizeAction(Message message) {
		int tx = (Integer)message.get("tx");
		int ty = (Integer)message.get("ty");
		int ox = (Integer)message.get("ox");
		int oy = (Integer)message.get("oy");
		List<NodeAddress> nearByUnits = me.getServerState().getBattleField().getAdjacentUnits(tx,ty)
				.stream()
				.filter(unit -> unit.getX() == ox && unit.getY() == oy)
				.map(Unit::getAddress)
				.collect(toList());
		// If there are no units nearby, just move ahead.
		if(nearByUnits.size() == 0) {
			return true;
		}
		Claim id = new Claim(FREE_ID, tx, ty, message.getTimestamp().getTime());
		approvalMap.put(id, 0);
		FREE_ID++;

		Message request = me.getMessageFactory().createMessage(MESSAGE_TYPE)
				.put("id",id);
		nearByUnits.forEach(address -> me.getServerSocket().sendMessage(request, address));

		// Wait for the reply
		long timestamp = request.getTimestamp().getTime();
		while(approvalMap.get(id) != null
				&& approvalMap.get(id) < nearByUnits.size()
				&& (System.currentTimeMillis() - timestamp < TIMEOUT_MILLISECONDS)) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {}

			if(approvalMap.get(id) < 0) {
				return false;
			}
		}
		return (approvalMap.get(id) != null
				&& approvalMap.get(id) < nearByUnits.size()
				&& (System.currentTimeMillis() - timestamp < TIMEOUT_MILLISECONDS));
	}

	/**
	 * Two types of messages:
	 * - approve/disapprove messages
	 * - receive messages approving/disaproving your action (goAhead)
	 */
	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		Boolean isGoAhead = (Boolean) message.get("goAhead");
		Claim claim = (Claim) message.get("id");
		if(isGoAhead != null) {
			if(isGoAhead) {
				approvalMap.put(claim, approvalMap.get(claim) + 1);
			} else {
				approvalMap.remove(claim);
			}
		} else {
			Message response = me.getMessageFactory().createMessage(MESSAGE_TYPE).put("id", claim);
			Optional<Claim> conflictingClaim = approvalMap.keySet()
					.stream()
					.filter(cl -> cl.tx == claim.tx && cl.ty == claim.ty)
					.findAny();
			//approve/disapprove message
			Long conflictingTimestamp = conflictingClaim.map(cc -> cc.timestamp).orElse(Long.MAX_VALUE);
			if(conflictingTimestamp > claim.timestamp) {
				response.put("goAhead", true);
				conflictingClaim.ifPresent(approvalMap::remove);
			} else {
				response.put("goAhead", false);
			}
			me.getServerSocket().sendMessage(response, message.getOrigin());
		}
		return null;
	}
}
