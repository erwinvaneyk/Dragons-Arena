package distributed.systems.network;

import java.util.HashSet;

import distributed.systems.das.BattleField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * The state of **only** this server
 */
@ToString(callSuper = true, exclude = {"battleField"})
public class ServerState extends NodeState {

	@Getter
	private final HashSet<NodeAddress> clients;

	@Getter
	private final BattleField battleField;

	public ServerState(BattleField battleField, NodeAddress nodeAddress) {
		super(nodeAddress);
		this.clients = new HashSet<>();
		this.battleField = battleField;
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}
}
