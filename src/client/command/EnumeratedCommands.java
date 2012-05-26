package client.command;

import tools.MapleLogger;
import constants.ParanoiaConstants;
import constants.ServerConstants;
import client.MapleClient;

public abstract class EnumeratedCommands extends Commands {
	@SuppressWarnings("unused")
	public static boolean execute(MapleClient c, String[] sub, char heading) {
		if (ServerConstants.USE_PARANOIA && ParanoiaConstants.PARANOIA_COMMAND_LOGGER && ParanoiaConstants.LOG_INVALID_COMMANDS) {
			MapleLogger.printFormatted(MapleLogger.PARANOIA_COMMAND, "[" + c.getPlayer().getName() + "] Attempted " + heading + sub[0] + ((sub.length > 1) ? " with parameters: " + joinStringFrom(sub, 1) : "."));
		}
		c.getPlayer().yellowMessage("Command: " + heading + sub[0] + ": does not exist.");
		return false;
	}
	
	protected static String getHelp() {
		return EnumeratedCommands.getHelp(-1);
	}
	
	protected static String getHelp(int page) {
		return "Command.getHelp() was not overridden.";
	}
	
	public static enum Command {};
}
