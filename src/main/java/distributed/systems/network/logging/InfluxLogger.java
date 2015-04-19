package distributed.systems.network.logging;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.concurrent.TimeUnit;

import distributed.systems.core.LogMessage;
import distributed.systems.core.Message;
import distributed.systems.das.units.Unit;
import distributed.systems.network.Address;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.ServerNode;
import distributed.systems.network.ServerState;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Database;
import org.influxdb.dto.Serie;

public class InfluxLogger implements Logger {

	public static boolean FLAG_USE_INFLUX = true;

	private static final String DATABASE_DATA = "data";
	private static final String DATABASE_GRAFANA = "grafana";
	private final InfluxDB influxDB;
	private final Address databaseLocation;
	private final String username;

	private static InfluxLogger performanceLogging;

	public static InfluxLogger getInstance() {
		if(performanceLogging == null) {
			performanceLogging = new InfluxLogger(new Address("localhost", 8086), "root", "root");
		}
		return performanceLogging;
	}

	public InfluxLogger() {
		this.influxDB = null;
		databaseLocation = null;
		username = null;
	}

	public InfluxLogger(Address databaseLocation, String username, String password) {
		this.databaseLocation = databaseLocation;
		this.username = username;

		// Connect to influx
		InfluxDB temptInfluxDB = InfluxDBFactory.connect(databaseLocation.toString(), username, password);

		// Create required databases if they are not present
		try {
			List<String> databases = temptInfluxDB.describeDatabases().stream().map(Database::getName).collect(toList());
			if (!databases.contains(DATABASE_DATA)) {
				temptInfluxDB.createDatabase(DATABASE_DATA);
			}
			if (!databases.contains(DATABASE_GRAFANA)) {
				temptInfluxDB.createDatabase(DATABASE_GRAFANA);
			}
		} catch(Exception e) {
			e.printStackTrace();
			temptInfluxDB = null;
		}
		influxDB = temptInfluxDB;
	}

	public boolean checkConnection() {
		return (influxDB != null && influxDB.ping().getStatus().equalsIgnoreCase("ok"));
	}

	public void logMessageDuration(Message message, NodeAddress messageHandler, long duration) {
		if(message.getMessageType().equals(LogMessage.MESSAGE_TYPE)) return;
		String origin = message.getOrigin() != null ? message.getOrigin().getName() : "";
		Serie serie = new Serie.Builder("messagePerformance")
				.columns("duration", "messagetype", "messagehandler", "origin", "time","receivedtime")
				.values(duration, message.getMessageType(), messageHandler.getName(), origin, message.getTimestamp().getTime(), message.getReceivedTimestamp().getTime())
				.build();
		writeToInflux(serie);
	}

	@Override
	public void log(LogMessage message) {
		String origin = message.getOrigin() != null ? message.getOrigin().getName() : "";
		Serie serie = new Serie.Builder("log")
				.columns("message","logtype","origin","time")
				.values(message.getLogMessage(), message.getLogType(), origin,
						message.getTimestamp().getTime())
				.build();
		writeToInflux(serie);
	}

	public void logUnitRoundDuration(Unit unit, NodeAddress server, long duration) {
		Serie serie = new Serie.Builder("unitRoundDuration")
				.columns("duration", "unit", "server", "time")
				.values(duration, unit.getUnitID(), server.getName(), System.currentTimeMillis())
				.build();
		writeToInflux(serie);
	}

	public void logServerStatistics(ServerState server) {
		Serie serie = new Serie.Builder("serverStatus")
				.columns("server", "clients")
				.values(server.getAddress().getName(), server.getClients().size())
				.build();
		writeToInflux(serie);
	}

	public void logTimeOut(NodeAddress me, NodeAddress timedOut) {
		Serie serie = new Serie.Builder("timeOuts")
				.columns("notifier","target")
				.values(me.getName(), timedOut.getName())
				.build();
		writeToInflux(serie);
	}

	private void writeToInflux(Serie serie) {
		if(FLAG_USE_INFLUX && influxDB != null) {
			influxDB.write(DATABASE_DATA, TimeUnit.MILLISECONDS, serie);
		}
	}
}
