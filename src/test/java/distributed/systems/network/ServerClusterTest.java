package distributed.systems.network;

import org.junit.runner.RunWith;

@RunWith(CleanRunner.class)
public class ServerClusterTest {
/*
	@Test
	public void ServerNodeCreationCheckFieldsTest() throws RemoteException {
		ServerNode server1 = new ServerNode(CleanRunner.TEST_PORT_1);
		assertNull(server1.getBattlefield());
		assertNotNull(server1.getServerSocket());
		assertNotNull(server1.getServerAddress());
		assertNotNull(server1.getMessageFactory());
		assertNotNull(server1.getSocket());
		assertNotNull(server1.getOtherNodes());
		assertEquals(0, server1.getOtherNodes().size());
		System.out.println("Before: " + server1.getOwnRegistry().toString());
		server1.disconnect();
		System.out.println("after: " + server1.getOwnRegistry().toString());
	}

	@Test(expected = RuntimeException.class)
	public void ServerNodeCreationMessageTest() throws RemoteException {
		ServerNode server1 = new ServerNode(CleanRunner.TEST_PORT_2);

		// Stub message
		Message message = mock(Message.class);
		when(message.getMessageType()).thenReturn(PingPongHandler.MESSAGE_TYPE);
		Socket socket = LocalSocket.connectTo(server1.getAddress());
		Message response = socket.sendMessage(message, server1.getAddress());
		assertEquals(PingPongHandler.RESPONSE_TYPE, response.getMessageType());
		System.out.println("Before: " + server1.getOwnRegistry().toString());
		server1.disconnect();
		System.out.println("after: " + server1.getOwnRegistry().toString());
	}

	//@Test
	public void ClusterSetupCheckFieldTest() throws RemoteException {
		ServerNode server1 = new ServerNode(CleanRunner.TEST_PORT_3);
		server1.setupCluster();
		assertEquals(0, server1.getAddress().getId());
		assertNotNull(server1.getBattlefield());
		assertEquals(0, server1.getOtherNodes().size());
		System.out.println("Before: " + server1.getOwnRegistry().toString());
		server1.disconnect();
		//System.out.println("after: " + server1.getOwnRegistry().toString());
	}

	//@Test
	public void ClusterSetupMessageTest() throws RemoteException {
		ServerNode server1 = new ServerNode(CleanRunner.TEST_PORT_4);
		server1.setupCluster();

		// Stub message
		Message message = mock(Message.class);
		when(message.getMessageType()).thenReturn(PingPongHandler.MESSAGE_TYPE);
		Socket socket = LocalSocket.connectTo(server1.getAddress());
		Message response = socket.sendMessage(message, server1.getAddress());
		assertEquals(PingPongHandler.RESPONSE_TYPE, response.getMessageType());
		System.out.println("Before: " + server1.getOwnRegistry().toString());
		server1.disconnect();
		System.out.println("after: " + server1.getOwnRegistry().toString());
	}*/
}
