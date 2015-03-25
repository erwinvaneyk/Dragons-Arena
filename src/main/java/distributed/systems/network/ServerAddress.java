package distributed.systems.network;

import java.util.ArrayList;
import java.util.List;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true, exclude = {"clients"})
public class ServerAddress extends NodeAddress {

	private List<NodeAddress> clients = new ArrayList<>();

	public ServerAddress(NodeType type, int id, Address physicalAddress) {
		super(type, id, physicalAddress);
	}

	public ServerAddress(String ip, int port) {
		super(ip, port);
	}

	public ServerAddress(int port, NodeType type) {
		super(port, type);
	}

	public ServerAddress(NodeAddress nodeAddress) {
		super(nodeAddress.getType(), nodeAddress.getId(), nodeAddress.getPhysicalAddress());
	}

	public List<NodeAddress> getClients() {
		if(!this.isServer()) {
			throw new RuntimeException("Trying to access clients of a non-server `" + this + "`!");
		}
		return clients;
	}
}
