package distributed.systems.network;

public class ClusterException extends RuntimeException {

	public ClusterException(String s) {
		super(s);
	}

	public ClusterException(String errorMessage, Exception e) {
		super(errorMessage, e);
	}
}
