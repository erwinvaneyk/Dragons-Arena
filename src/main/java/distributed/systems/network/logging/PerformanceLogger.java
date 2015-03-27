package distributed.systems.network.logging;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.concurrent.TimeUnit;

import distributed.systems.core.Message;
import distributed.systems.network.NodeAddress;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Database;
import org.influxdb.dto.Serie;

public class PerformanceLogger {

	private static final String DATABASE_DATA = "data";
	private static final String DATABASE_GRAFANA = "grafana";
	private final InfluxDB influxDB;

	private static PerformanceLogger performanceLogging;

	public static PerformanceLogger getInstance() {
		if(performanceLogging == null) {
			performanceLogging = new PerformanceLogger();
		}
		return performanceLogging;
	}

	public PerformanceLogger() {
		influxDB = InfluxDBFactory.connect("http://localhost:8086", "root", "root");

		List<String> databases = influxDB.describeDatabases().stream().map(Database::getName).collect(toList());
		if(!databases.contains(DATABASE_DATA)) {
			influxDB.createDatabase(DATABASE_DATA);
		}
		if(!databases.contains(DATABASE_GRAFANA)) {
			influxDB.createDatabase(DATABASE_GRAFANA);
		}
	}



	public void logMessageDuration(Message message, NodeAddress messageHandler, long duration) {
		String origin = message.getOrigin() != null ? message.getOrigin().getName() : "";
		Serie serie = new Serie.Builder("messagePerformance")
				.columns("duration", "messagetype", "messagehandler", "origin")
				.values(duration, message.getMessageType(), messageHandler.getName(), origin)
				.build();
		influxDB.write(DATABASE_DATA, TimeUnit.MILLISECONDS, serie);
	}

	public void logNumberOfServers(int servers) {
		Serie serie = new Serie.Builder("NodeCount")
				.columns("numberOfServers")
				.values(servers)
				.build();
		influxDB.write(DATABASE_DATA, TimeUnit.MILLISECONDS, serie);
	}

	public void logLine(String line) {
		Serie serie = new Serie.Builder("log")
				.columns("message")
				.values(line)
				.build();
		influxDB.write(DATABASE_DATA, TimeUnit.MILLISECONDS, serie);
	}
}
