package client.command;

import java.sql.ResultSet;
import net.server.Channel;
import client.MapleCharacter;
import client.MapleClient;

public class AdminCommands extends Commands {
	public static boolean execute(MapleClient c, String[] sub, char heading) {
		MapleCharacter chr = c.getPlayer();
		Channel cserv = c.getChannelServer();
		MapleCharacter victim; // For commands with targets.
		ResultSet rs; // For commands with MySQL results.

		Command command = Command.valueOf(sub[0]);
		switch (command) {
			default:
				// chr.yellowMessage("Command: " + heading + sub[0] + ": does not exist.");
				return false;
			case setgmlevel:
				victim = c.getChannelServer().getPlayerStorage().getCharacterByName(sub[1]);
				victim.saveToDB(true);
				victim.setGM(Integer.parseInt(sub[2]));
				chr.message("Done.");
				victim.getClient().disconnect();
				break;
		}
		return true;
	}
	
	private static enum Command {
		setgmlevel
	}
}
