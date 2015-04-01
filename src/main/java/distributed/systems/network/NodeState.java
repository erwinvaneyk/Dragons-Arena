package distributed.systems.network;

import java.io.Serializable;
import java.util.HashSet;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class NodeState implements Serializable {

	@Getter
	private final HashSet<NodeAddress> connectedNodes;

	@Getter @Setter
	private NodeAddress address;

	public NodeState(NodeAddress address) {
		this.connectedNodes = new HashSet<>();
		this.address = address;
	}

	@Override
	public int hashCode() {
		return 42 * address.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		if (o == this) {
			return true;
		}
		final NodeState other = (NodeState) o;
		return this.address.toString().equals(other.address.toString());
	}
}
