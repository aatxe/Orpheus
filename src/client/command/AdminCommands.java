package client.command;

import java.sql.ResultSet;
import tools.MapleLogger;
import constants.ParanoiaConstants;
import constants.ServerConstants;
import net.server.Channel;
import client.MapleCharacter;
import client.MapleClient;

public class AdminCommands extends Commands {
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
				case help:
					if (sub.length > 1) {
						if (sub[1].equalsIgnoreCase("admin")) {
							chr.dropMessage(ServerConstants.SERVER_NAME + "'s AdminCommands Help");
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
	
	private static enum Command {
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
