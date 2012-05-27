package client.command.external;

import tools.MapleLogger;
import constants.ParanoiaConstants;
import constants.ServerConstants;
import client.MapleClient;

public abstract class Commands {
	private static final int gmLevel = 0;
	private static final char heading = '@';

	@SuppressWarnings("unused")
	public static boolean execute(MapleClient c, String[] sub, char heading) {
		if (ServerConstants.USE_PARANOIA && ParanoiaConstants.PARANOIA_COMMAND_LOGGER && ParanoiaConstants.LOG_INVALID_COMMANDS) {
			MapleLogger.printFormatted(MapleLogger.PARANOIA_COMMAND, "[" + c.getPlayer().getName() + "] Attempted " + heading + sub[0] + ((sub.length > 1) ? " with parameters: " + joinStringFrom(sub, 1) : "."));
		}
		c.getPlayer().yellowMessage("Command: " + heading + sub[0] + ": does not exist.");
		return false;
	}
	
	public static int getRequiredStaffRank() {
		return gmLevel;
	}
	
	public static char getHeading() {
		return heading;
	}
	
	public static boolean isAnnotated() {
		return false;
	}
	
	protected static String joinStringFrom(String arr[], int start) {
		StringBuilder builder = new StringBuilder();
		for (int i = start; i < arr.length; i++) {
			builder.append(arr[i]);
			if (i != arr.length - 1) {
				builder.append(" ");
			}
		}
		return builder.toString();
	}
}
