package distributed.systems.network;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NodeStateTest {

	@Test
	public void testEquals() {
		NodeState node1 = new NodeState(new NodeAddress("127", 123));
		NodeState node2 = new NodeState(new NodeAddress("127", 123));
		assertTrue(node1.equals(node2));
	}
}
