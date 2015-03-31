package distributed.systems.network;

import lombok.Getter;
import lombok.Setter;

public class PlayerState extends NodeState {

	@Getter @Setter
	private NodeAddress serverAddress;

	public PlayerState(NodeAddress myAddress, NodeAddress serverAddress) {
		super(myAddress);
		this.serverAddress = serverAddress;
	}

}