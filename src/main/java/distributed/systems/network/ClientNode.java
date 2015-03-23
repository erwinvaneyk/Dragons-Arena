package distributed.systems.network;

import distributed.systems.core.Socket;

import java.io.Serializable;

public interface ClientNode {

	public NodeAddress getServerAddress();

	public NodeAddress getAddress();

	public Socket getSocket();
}
