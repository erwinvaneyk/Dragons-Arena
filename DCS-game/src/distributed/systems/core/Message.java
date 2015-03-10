package distributed.systems.core;

import java.io.Serializable;
import java.util.HashMap;

import distributed.systems.das.MessageRequest;

/**
 * Created by mashenjun on 3-3-15.
 */
public class Message implements Serializable{
	
	private HashMap<String, Serializable> content;

	public void put(String string, Serializable serializable) {
		// TODO Auto-generated method stub
		
	}

	public void put(String string, int x) {
		// TODO Auto-generated method stub
		
	}

	public Serializable get(String string) {
		// TODO Auto-generated method stub
		return this.content.get(string);
	}
}
