package client.command;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import server.MapleInventoryManipulator;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;
import net.server.Channel;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;

public class AdminCommands extends Commands {
	public static void execute(MapleClient c, String[] sub, char heading) {
		MapleCharacter chr = c.getPlayer();
		Channel cserv = c.getChannelServer();
		MapleCharacter victim; // For commands with targets.
		ResultSet rs; // For commands with MySQL results.

		Command command = Command.valueOf(sub[0]);
		switch (command) {
			default:
				chr.yellowMessage("Command: " + heading + sub[0] + ": does not exist.");
			case setgmlevel:
				victim = c.getChannelServer().getPlayerStorage().getCharacterByName(sub[1]);
				victim.setGM(Integer.parseInt(sub[2]));
				chr.message("Done.");
				victim.getClient().disconnect();
		}
	}

	private static enum Command {
		setgmlevel
	}
}
