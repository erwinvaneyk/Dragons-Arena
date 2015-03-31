package distributed.systems.network;

import java.util.HashSet;

import distributed.systems.das.BattleField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * The state of **only** this server
 */
@Data
@ToString(callSuper = true, exclude = {"battleField"})
public class ServerState extends NodeState {

	private final HashSet<NodeAddress> clients;

	private final BattleField battleField;

	public ServerState(BattleField battleField, NodeAddress nodeAddress) {
		super(nodeAddress, NodeType.SERVER);
		this.clients = new HashSet<>();
		this.battleField = battleField;
	}

	public ServerState(BattleField battleField, NodeAddress nodeAddress, NodeType type) {
		super(nodeAddress, type);
		this.clients = new HashSet<>();
		this.battleField = battleField;
	}
}
