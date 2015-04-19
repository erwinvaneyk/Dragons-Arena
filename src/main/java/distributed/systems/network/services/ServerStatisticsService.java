package distributed.systems.network.services;

import java.rmi.RemoteException;

import distributed.systems.core.LogMessage;
import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.network.ServerNode;
import distributed.systems.network.logging.InfluxLogger;
import distributed.systems.network.logging.Logger;

/**
 * Service responsible for sending statistics to the appropriate logger
 */
public class ServerStatisticsService implements SocketService {

	public static final String MESSAGE_TYPE = "STATISTICS";
	private static final long STATISTIC_INTERVAL = 1000;
	private final ServerNode me;
	private final InfluxLogger statisticsLogger;

	public ServerStatisticsService(ServerNode me) {
		this.me = me;
		this.statisticsLogger = InfluxLogger.getInstance();
	}

	@Override
	public String getMessageType() {
		return MESSAGE_TYPE;
	}

	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		return null;
	}

	@Override
	public void run() {
		try {
			while(!Thread.currentThread().isInterrupted()) {
				statisticsLogger.logServerStatistics(me.getServerState());
				Thread.sleep(STATISTIC_INTERVAL);
			}
		} catch (InterruptedException e) {
			me.safeLogMessage("ServerStatistics service has been stopped", LogType.INFO);
		}
	}
}
