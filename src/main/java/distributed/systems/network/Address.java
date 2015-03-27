package distributed.systems.network;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Address implements Serializable {

	public static final String DEFAULT_PROTOCOL = "http";

	private final String ip;

	private final int port;

	private final String protocol;

	public Address(String ip,int port) {
		this.ip = ip;
		this.port = port;
		this.protocol = DEFAULT_PROTOCOL;
	}

	public static Address getMyAddress(int port) {
		return new Address("localhost", port);
	}

	public String toString() {
		return protocol + "://" + ip + ":" + port;
	}

}
