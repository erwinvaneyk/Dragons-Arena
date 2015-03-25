package distributed.systems.network;

import java.io.Serializable;

import distributed.systems.core.ExtendedSocket;

public interface ClientNode extends Serializable {

	public NodeAddress getServerAddress();

	public NodeAddress getAddress();

	public ExtendedSocket getSocket();
}
