package distributed.systems.gametrace;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DotAliciousConverter {

	List<Integer> players = new ArrayList<>();

	public void loadFromFile(File file) throws IOException {
		int lineNumber = 0;
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
			for(String line; (line = br.readLine()) != null; ) {


				line += 1;
			}
		}
	}

	private EventBatch getInstanceFromLine(String line) {
		String[] matchInfo = line.split(",");
		String[] players = matchInfo[3].replaceFirst("<(.*)>","$0").split(",");
		EventBatch eventBatch = new EventBatch();
		for(String player : players) {
			int id = Integer.valueOf(player.replace("([0-9]+)", "$0"));
			eventBatch.addEvent(id, EventBatch.EventType.PLAYER_JOIN);
		}
		return eventBatch;
	}

}
