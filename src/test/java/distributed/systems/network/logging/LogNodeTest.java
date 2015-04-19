package distributed.systems.network.logging;

import static org.mockito.Mockito.*;

import java.rmi.RemoteException;
import java.util.Date;

import distributed.systems.core.LogMessage;
import distributed.systems.network.CleanRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CleanRunner.class)
public class LogNodeTest {

	/**
	 * Logs should be ordered before being written to a file
	 */
	@Test
	public void timestampQueueTest() throws RemoteException{
		// Stub logger
		Logger mockLogger = mock(Logger.class);

		// Stub messages
		LogMessage message1 = mock(LogMessage.class); // older message
		LogMessage message2 = mock(LogMessage.class); // newer message
		LogMessage message3 = mock(LogMessage.class); // newest message
		when(message1.getTimestamp()).thenReturn(new Date(10));
		when(message2.getTimestamp()).thenReturn(new Date(20));
		when(message3.getTimestamp()).thenReturn(new Date(30));
		when(message1.getMessageType()).thenReturn(LogMessage.MESSAGE_TYPE);
		when(message2.getMessageType()).thenReturn(LogMessage.MESSAGE_TYPE);
		when(message3.getMessageType()).thenReturn(LogMessage.MESSAGE_TYPE);

		// Setup lognode, prevent immediate flushing
		LogNode lognode = new LogNode(CleanRunner.TEST_PORT_1, mockLogger);
		lognode.setFlushThreshold(Long.MAX_VALUE);

		// Send messages to lognode
		lognode.onMessageReceived(message2);
		lognode.onMessageReceived(message1);
		lognode.onMessageReceived(message3);

		// Flush messages
		lognode.flushMessagesOlderThan(25);

		// First the oldest message should be logged, followed by the second oldest.
		verify(mockLogger).log(message1);
		verify(mockLogger).log(message2);
	}
}
