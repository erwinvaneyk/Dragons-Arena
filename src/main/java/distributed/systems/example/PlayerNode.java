package distributed.systems.example;

import distributed.systems.das.units.Player;

public class PlayerNode {

	private Player player;

	public static void main(String[] args) {
		new PlayerNode(Integer.valueOf(args[0]), Integer.valueOf(args[1]));
	}

	public PlayerNode(int x, int y) {
		this.player = new Player(x,y);
	}
}
