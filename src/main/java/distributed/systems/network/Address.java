package distributed.systems.network;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Address implements Serializable {

	private final String ip;

	private final int port;

	public static Address getMyAddress(int port) {
		return new Address("localhost", port);
	}
}
