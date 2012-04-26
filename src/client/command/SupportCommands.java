package client.command;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.ResultSet;
import server.MapleInventoryManipulator;
import constants.ItemConstants;
import net.server.Channel;
import net.server.Server;
import client.MapleCharacter;
import client.MapleClient;
import client.MaplePet;

public class SupportCommands extends Commands {
	public static void execute(MapleClient c, String[] sub, char heading) {
		MapleCharacter chr = c.getPlayer();
		Channel cserv = c.getChannelServer();
		Server serv = Server.getInstance();
		MapleCharacter victim; // For commands with targets.
		ResultSet rs; // For commands with MySQL results.

		Command command = Command.valueOf(sub[0]);
		switch (command) {
			default:
				chr.yellowMessage("Command: " + heading + sub[0] + ": does not exist.");
				break;
			case announce:
				String message = joinStringFrom(sub, 1);
				Server.getInstance().gmChat(chr.getName() + " (" + chr.getStaffRank() + "): " + message, null);
				break;
			case cleardrops:
				chr.getMap().clearDrops(chr);
				break;
			case item:
				int itemId = Integer.parseInt(sub[1]);
				short quantity = 1;
				try {
					quantity = Short.parseShort(sub[2]);
				} catch (Exception e) {}
				int petid = -1;
				if (ItemConstants.isPet(itemId)) {
					petid = MaplePet.createPet(itemId);
				}
				MapleInventoryManipulator.addById(c, itemId, quantity, chr.getName(), petid, -1);
				break;
			case mesos:
				chr.gainMeso(Integer.parseInt(sub[1]), true);
				break;
			case online:
				for (Channel ch : serv.getChannelsFromWorld(chr.getWorld())) {
					String s = "Characters Online (Channel " + ch.getId() + " Online: " + ch.getPlayerStorage().getAllCharacters().size() + ") : ";
					if (ch.getPlayerStorage().getAllCharacters().size() < 50) {
						for (MapleCharacter pc : ch.getPlayerStorage().getAllCharacters()) {
							s += MapleCharacter.makeMapleReadable(pc.getName()) + ", ";
						}
						chr.dropMessage(s.substring(0, s.length() - 2));
					}
				}
				break;
			case search:
				try {
					BufferedReader dis = new BufferedReader(new InputStreamReader(new URL("http://www.mapletip.com/search_java.php?search_value=" + sub[1] + "&check=true").openConnection().getInputStream()));
					String s;
					while ((s = dis.readLine()) != null) {
						chr.dropMessage(s);
					}
					dis.close();
				} catch (Exception e) {}
				break;
			case warp:
				try {
					victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
					chr.changeMap(victim.getMap());
					chr.setPosition(victim.getPosition());
				} catch (Exception e) {
					chr.message("Usage: !warp playerName");
				}
				break;
			case warphere:
				try {
					victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
					victim.changeMap(chr.getMap());
					victim.setPosition(chr.getPosition());
				} catch (Exception e) {
					chr.message("Usage: !warphere playerName");
				}
				break;
			case whereami:
				chr.dropMessage("You're at Map " + chr.getMapId());
				break;
		}
	}

	private static enum Command {
		announce,
		cleardrops,
		item,
		mesos,
		online,
		search,
		warp,
		warphere,
		whereami,
	}
}
