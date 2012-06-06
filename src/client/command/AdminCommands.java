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

import java.sql.ResultSet;
import tools.MapleLogger;
import constants.ParanoiaConstants;
import constants.ServerConstants;
import net.server.Channel;
import client.MapleCharacter;
import client.MapleClient;

/**
 * @author Aaron Weiss
 */
public class AdminCommands extends EnumeratedCommands {
	private static final int gmLevel = 5;
	private static final char heading = '!';
	
	@SuppressWarnings("unused")
	public static boolean execute(MapleClient c, String[] sub, char heading) {
		MapleCharacter chr = c.getPlayer();
		Channel cserv = c.getChannelServer();
		MapleCharacter victim; // For commands with targets.
		ResultSet rs; // For commands with MySQL results.
		try {
			Command command = Command.valueOf(sub[0]);
			switch (command) {
				default:
					// chr.yellowMessage("Command: " + heading + sub[0] + ": does not exist.");
					return false;
				case clearlogs:
					if (ParanoiaConstants.ALLOW_CLEARLOGS_COMMAND) {
						if (ParanoiaConstants.PARANOIA_CONSOLE_LOGGER) MapleLogger.clearLog(MapleLogger.PARANOIA_CONSOLE);
						if (ParanoiaConstants.PARANOIA_CHAT_LOGGER) MapleLogger.clearLog(MapleLogger.PARANOIA_CHAT);
						if (ParanoiaConstants.PARANOIA_COMMAND_LOGGER) MapleLogger.clearLog(MapleLogger.PARANOIA_COMMAND);
						chr.message("Done.");
					} else {
						chr.dropMessage("Paranoia Log Clearing is forbidden by the server.");
					}
					break;
				case help:
					if (sub.length > 1) {
						if (sub[1].equalsIgnoreCase("admin")) {
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
				case setgmlevel:
					victim = c.getChannelServer().getPlayerStorage().getCharacterByName(sub[1]);
					victim.saveToDB(true);
					victim.setGM(Integer.parseInt(sub[2]));
					chr.message("Done.");
					victim.getClient().disconnect();
					break;
			}
			if (ServerConstants.USE_PARANOIA && ParanoiaConstants.PARANOIA_COMMAND_LOGGER && ParanoiaConstants.LOG_ADMIN_COMMANDS) {
				MapleLogger.printFormatted(MapleLogger.PARANOIA_COMMAND, "[" + c.getPlayer().getName() + "] Used " + heading + sub[0] + ((sub.length > 1) ? " with parameters: " + joinStringFrom(sub, 1) : "."));
			}
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
	
	protected static void getHelp(MapleCharacter chr) {
		AdminCommands.getHelp(-1, chr);
	}

	protected static void getHelp(int page, MapleCharacter chr) {
        int pageNumber = (int) (Command.values().length / ServerConstants.ENTRIES_PER_PAGE);
        if (Command.values().length % ServerConstants.ENTRIES_PER_PAGE > 0) {
        	pageNumber++;
        }
		if (page <= 0 || pageNumber == 1) {
			chr.dropMessage(ServerConstants.SERVER_NAME + "'s AdminCommands Help");
			for (Command cmd : Command.values()) {
				chr.dropMessage(heading + cmd.name() + " - " + cmd.getDescription());
			}
		} else {
	        if (page > pageNumber) {
	        	page = pageNumber;
	        }
	        int lastPageEntry = (Command.values().length - Math.max(0, Command.values().length - (page * ServerConstants.ENTRIES_PER_PAGE)));
	        lastPageEntry -= 1;
			chr.dropMessage(ServerConstants.SERVER_NAME + "'s AdminCommands Help (Page " + page + " / " + pageNumber + ")");
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
		clearlogs("Clear Paranoia log files."),
		help("Displays this help message."),
		setgmlevel("Sets a victim's GM level.");

	    private final String description;
	    
	    private Command(String description){
	        this.description = description;
	    }
	    
	    public String getDescription() {
	    	return this.description;
	    }
	}
}
