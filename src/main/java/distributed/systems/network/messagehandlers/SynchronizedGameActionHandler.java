package distributed.systems.network.messagehandlers;

import static java.util.stream.Collectors.toList;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.das.units.Unit;
import distributed.systems.network.ConnectionException;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.ServerNode;

public class SynchronizedGameActionHandler implements MessageHandler {
	public static final String MESSAGE_TYPE = "SYNC_ACTION";

	public static final long TIMEOUT_MILLISECONDS = 1000;
	private static int FREE_ID = 0;
	private final ServerNode me;
	private Map<Claim, Integer> approvalMap = new ConcurrentHashMap<>();

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
		System.out.println(message);
		int tx = (Integer)message.get("x");
		int ty = (Integer)message.get("y");
		int ox = (Integer)message.get("ox");
		int oy = (Integer)message.get("oy");
		List<NodeAddress> nearByUnits = me.getServerState().getBattleField().getAdjacentUnits(tx, ty)
				.stream()
				.filter(unit -> !(unit.getX() == ox && unit.getY() == oy))
				.map(unit -> unit.getPlayerState().getServerAddress())
				.collect(toList());
		// If there are no units nearby, just move ahead.
		if(nearByUnits.size() == 0) {
			me.safeLogMessage("No need for synchronization, move ahead straight away!", LogType.DEBUG);
			return true;
		}
		Claim id = new Claim(FREE_ID, tx, ty, message.getTimestamp().getTime());
		approvalMap.put(id, 0);
		FREE_ID++;
		me.safeLogMessage("Synchronizing for move: " + id, LogType.DEBUG);

		Message request = me.getMessageFactory().createMessage(MESSAGE_TYPE)
				.put("id",id);
		long approvalCount = nearByUnits.stream().filter(address -> {
			try {
				me.getServerSocket().sendMessage(request, address);
				return true;
			} catch (ConnectionException e) {
				me.safeLogMessage("Could not send synchronize-message to " + address + ". Assuming that it has disconnected", LogType.WARN);
				return false;
			}
		}).count();

		// Wait for the reply
		long timestamp = request.getTimestamp().getTime();
		while(approvalMap.get(id) != null
				&& approvalMap.get(id) < approvalCount
				&& (System.currentTimeMillis() - timestamp < TIMEOUT_MILLISECONDS)) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {}
		}
		boolean isAllowed = (approvalMap.get(id) != null
				&& approvalMap.get(id) >= approvalCount
				&& (System.currentTimeMillis() - timestamp < TIMEOUT_MILLISECONDS));
		me.safeLogMessage("Synchronization for move to (" + id.tx + ", " + id.ty + ") was: " + isAllowed, LogType.DEBUG);
		approvalMap.remove(id);
		return isAllowed;
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
			synchronized (approvalMap) {
				if (isGoAhead) {
					Integer approvalCount = approvalMap.get(claim);
					me.safeLogMessage("Approved by " + message.getOrigin().getName(), LogType.DEBUG);
					if (approvalCount != null) {
						approvalMap.put(claim, approvalCount + 1);
					}
				}
				else {
					me.safeLogMessage("Received a no-go from " + message.getOrigin().getName(), LogType.WARN);
					approvalMap.remove(claim);
				}
			}
		} else {
			Message response = me.getMessageFactory().createMessage(MESSAGE_TYPE).put("id", claim);
			Optional<Claim> conflictingClaim = approvalMap.keySet()
					.stream()
					.filter(cl -> cl.id != claim.id && cl.tx == claim.tx && cl.ty == claim.ty)
					.findAny();
			//approve/disapprove message
			Long conflictingTimestamp = conflictingClaim.map(cc -> cc.timestamp).orElse(Long.MAX_VALUE);
			if(conflictingTimestamp > claim.timestamp) {
				response.put("goAhead", true);
				conflictingClaim.ifPresent(approvalMap::remove);
			} else {
				response.put("goAhead", false);
			}
			try {
				me.getServerSocket().sendMessage(response, message.getOrigin());
			}
			catch (ConnectionException e) {
				me.safeLogMessage("Could not reply to synchronize-message to "
						+ message.getOrigin() + ". Assuming that it has disconnected", LogType.WARN);
			}
		}
		return null;
	}
}
