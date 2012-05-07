package client.command;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.ResultSet;
import server.MapleInventoryManipulator;
import constants.ItemConstants;
import constants.ServerConstants;
import net.server.Channel;
import net.server.Server;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import client.MaplePet;

public class SupportCommands extends Commands {
	public static boolean execute(MapleClient c, String[] sub, char heading) {
		MapleCharacter chr = c.getPlayer();
		Channel cserv = c.getChannelServer();
		Server serv = Server.getInstance();
		MapleCharacter victim; // For commands with targets.
		ResultSet rs; // For commands with MySQL results.

		try {
			Command command = Command.valueOf(sub[0]);
			switch (command) {
				default:
					// chr.yellowMessage("Command: " + heading + sub[0] + ": does not exist.");
					return false;
				case announce:
					String message = joinStringFrom(sub, 1);
					Server.getInstance().gmChat(chr.getName() + " (" + chr.getStaffRank() + "): " + message, null);
					break;
				case cleardrops:
					chr.getMap().clearDrops(chr);
					break;
				case help:
					if (sub.length > 1) {
						if (sub[1].equalsIgnoreCase("support")) {
							chr.dropMessage(ServerConstants.SERVER_NAME + "'s SupportCommands Help");
							for (Command cmd : Command.values()) {
								chr.dropMessage(heading + cmd.name() + " - " + cmd.getDescription());
							}
							break;
						} else {
							return false;
						}
					} else {
						return false;
					}
				case hide:
					chr.setHidden(true);
					chr.message("You're now invisible.");
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
				case job:
					if (sub.length >= 2) {
						chr.changeJob(MapleJob.getById(Integer.parseInt(sub[1])));
						chr.equipChanged();
					} else {
						chr.message("Usage: !job number");
					}
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
				case show:
					chr.setHidden(false);
					chr.message("You're now visible.");
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
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	private static enum Command {
		announce("Makes a server-wide announcement."),
		cleardrops("Clears the drops on the map."),
		help("Displays this help message."),
		hide("Hides you, making you invisible to non-GMs."),
		item("Gives an item to you or a victim."),
		job("Sets your job."),
		mesos("Gives you mesos."),
		online("Checks who's online."),
		search("Searches MapleTip for IDs"),
		show("Makes you visible to all players."),
		warp("Warps you to the victim."),
		warphere("Warps the victim to you."),
		whereami("Tells you what map you're on.");

	    private final String description;
	    
	    private Command(String description){
	        this.description = description;
	    }
	    
	    public String getDescription() {
	    	return this.description;
	    }
	}
}
