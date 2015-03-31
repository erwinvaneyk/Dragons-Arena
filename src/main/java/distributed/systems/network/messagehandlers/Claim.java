package distributed.systems.network.messagehandlers;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Claim implements Serializable {
	public final int tx;
	public final int ty;
	public final int id;
	public final long timestamp;

	public Claim(int id, int tx, int ty, long timestamp) {
		this.tx = tx;
		this.ty = ty;
		this.id = id;
		this.timestamp = timestamp;
	}
}