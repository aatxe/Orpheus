package client.command;

import client.MapleClient;

public abstract class Commands {
	public static boolean execute(MapleClient c, String[] sub, char heading) {
		c.getPlayer().yellowMessage("Command: " + heading + sub[0] + ": does not exist.");
		return false;
	}
	
	public static boolean isCommand(String command) {
		for (int i = 0; i < Command.values().length; i++) {
			if (Command.valueOf(command) == Command.values()[i]) {
				return true;
			}
		}
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
	
	private static enum Command {};
}
