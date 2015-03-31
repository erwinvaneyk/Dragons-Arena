package distributed.systems.network;

import java.io.Serializable;
import java.util.HashSet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(exclude = {"connectedNodes"})
@ToString
public class NodeState implements Serializable {

	private final HashSet<NodeAddress> connectedNodes;

	private final NodeAddress address;

	private final NodeType nodeType;

	public NodeState(NodeAddress address, NodeType nodeType) {
		this.nodeType = nodeType;
		this.connectedNodes = new HashSet<>();
		this.address = address;
	}

}
