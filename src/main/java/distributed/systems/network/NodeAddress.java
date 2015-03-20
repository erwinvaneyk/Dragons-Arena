package distributed.systems.network;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NodeAddress {

	public static final String SEPERATOR = "-";

	public enum NodeType {
		SERVER, PLAYER, DRAGON
	}

	private final NodeType type;

	private final int id;

	@Override
	public String toString() {
		return type.toString() + SEPERATOR + id;
	}

	public static NodeAddress fromAddress(String address) {
		String[] parts = address.split(SEPERATOR, 2);
		NodeType nodeType = NodeType.valueOf(parts[0]);
		return new NodeAddress(nodeType, Integer.valueOf(parts[1]));
	}

	public boolean isServer() {
		return type.equals(NodeType.SERVER);
	}
}
