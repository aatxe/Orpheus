/*
 	OrpheusMS: MapleStory Private Server based on OdinMS
    Copyright (C) 2012 Aaron Weiss <aaron@deviant-core.net>
    				Patrick Huy <patrick.huy@frz.cc>
					Matthias Butz <matze@odinms.de>
					Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package client.command;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.ResultSet;
import server.MapleInventoryManipulator;
import tools.MapleLogger;
import tools.MaplePacketCreator;
import constants.ItemConstants;
import constants.ParanoiaConstants;
import constants.ServerConstants;
import net.server.Channel;
import net.server.Server;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import client.MaplePet;
import client.MapleRank;

/**
 * @author Aaron Weiss
 */
public class SupportCommands extends EnumeratedCommands {
	private static final int gmLevel = 2;
	private static final char heading = '!';
	
	@SuppressWarnings("unused")
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
					Server.getInstance().broadcastMessage(chr.getWorld(), MaplePacketCreator.serverNotice(6, chr.getName() + " (" + MapleRank.getById(chr.gmLevel()).toString() + "): " + joinStringFrom(sub, 1)));
					break;
				case cleardrops:
					chr.getMap().clearDrops(chr);
					break;
				case help:
					if (sub.length > 1) {
						if (sub[1].equalsIgnoreCase("support")) {
							if (sub.length > 2 && ServerConstants.PAGINATE_HELP) {
								getHelp(Integer.parseInt(sub[2]), chr);
							} else {
								getHelp(chr);
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
						BufferedReader dis = new BufferedReader(new InputStreamReader(new URL("http://www.mapletip.com/search_java.php?search_value=" + joinStringFrom(sub, 2).replace(" ", "%20") + "&check=true").openConnection().getInputStream()));
						String s;
						while ((s = dis.readLine()) != null) {
							if (s.startsWith(" "))
								s = s.substring(1);
							if (!s.startsWith("_") && !s.endsWith("_") && s.startsWith(getMapleTipPrefix(sub[1])) && getMapleTipPrefix(sub[1]) != "") {
								chr.dropMessage(s.substring(getMapleTipPrefix(sub[1]).length() + 2));
							} else if (!s.startsWith("_") && !s.endsWith("_") && s.startsWith(getMapleTipPrefix(sub[1]))) {
								chr.dropMessage(s);
							}
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
			if (ServerConstants.USE_PARANOIA && ParanoiaConstants.PARANOIA_COMMAND_LOGGER && ParanoiaConstants.LOG_SUPPORT_COMMANDS) {
				MapleLogger.printFormatted(MapleLogger.PARANOIA_COMMAND, "[" + c.getPlayer().getName() + "] Used " + heading + sub[0] + ((sub.length > 1) ? " with parameters: " + joinStringFrom(sub, 1) : "."));
			}
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	private static String getMapleTipPrefix(String entry) {
		if (entry.equalsIgnoreCase("npc")) {
			return "NPC";
		} else if (entry.equalsIgnoreCase("skill") || entry.equalsIgnoreCase("skills")) {
			return "Skill";
		} else if (entry.equalsIgnoreCase("equip") || entry.equalsIgnoreCase("equips")) {
			return "Equip";
		} else if (entry.equalsIgnoreCase("mob") || entry.equalsIgnoreCase("mobs")) {
			return "Monster";
		} else if (entry.equalsIgnoreCase("map") || entry.equalsIgnoreCase("maps")) {
			return "Map";
		} else if (entry.equalsIgnoreCase("use")) {
			return "Usable";
		} else if (entry.equalsIgnoreCase("etc")) {
			return "ETC";
		} else if (entry.equalsIgnoreCase("cash")) {
			return "Cash";
		} else {
			return "";
		}
	}
	
	protected static void getHelp(MapleCharacter chr) {
		SupportCommands.getHelp(-1, chr);
	}

	protected static void getHelp(int page, MapleCharacter chr) {
        int pageNumber = (int) (Command.values().length / ServerConstants.ENTRIES_PER_PAGE);
        if (Command.values().length % ServerConstants.ENTRIES_PER_PAGE > 0) {
        	pageNumber++;
        }
		if (page <= 0 || pageNumber == 1) {
			chr.dropMessage(ServerConstants.SERVER_NAME + "'s SupportCommands Help");
			for (Command cmd : Command.values()) {
				chr.dropMessage(heading + cmd.name() + " - " + cmd.getDescription());
			}
		} else {
	        if (page > pageNumber) {
	        	page = pageNumber;
	        }
	        int lastPageEntry = (Command.values().length - Math.max(0, Command.values().length - (page * ServerConstants.ENTRIES_PER_PAGE)));
	        lastPageEntry -= 1;
			chr.dropMessage(ServerConstants.SERVER_NAME + "'s SupportCommands Help (Page " + page + " / " + pageNumber + ")");
	        for (int i = lastPageEntry; i < lastPageEntry + ServerConstants.ENTRIES_PER_PAGE; i++) {
				chr.dropMessage(heading + Command.values()[i].name() + " - " + Command.values()[i].getDescription());
	        }
		}
	}
	
	public static int getRequiredStaffRank() {
		return gmLevel;
	}
	
	public static char getHeading() {
		return heading;
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
