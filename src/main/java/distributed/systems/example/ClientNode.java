package distributed.systems.example;

import distributed.systems.core.Socket;

public interface ClientNode {

	public NodeAddress getServerAddress();

	public NodeAddress getAddress();

	public Socket getSocket();
}
