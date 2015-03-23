package distributed.systems.network;

import java.io.Serializable;


import distributed.systems.core.ExtendedSocket;
import distributed.systems.core.MessageFactory;
import distributed.systems.das.units.Unit;

public interface ClientNode extends Serializable {

	public NodeAddress getServerAddress();

	public NodeAddress getAddress();

	public ExtendedSocket getSocket();

	public MessageFactory getMessageFactory();

	public Unit getUnit();
}
