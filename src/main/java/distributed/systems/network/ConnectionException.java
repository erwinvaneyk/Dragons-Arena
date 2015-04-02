package distributed.systems.network;

import java.rmi.RemoteException;

public class ConnectionException extends Exception {
	public ConnectionException(String s, RemoteException e) {
		super(s,e);
	}

	public ConnectionException(String s) {
		super(s);
	}
}
