package distributed.systems.core.exception;

import java.rmi.AlreadyBoundException;

public class AlreadyAssignedIDException extends RuntimeException {

	public AlreadyAssignedIDException(String s, AlreadyBoundException e) {
		super(s,e);
	}
}
