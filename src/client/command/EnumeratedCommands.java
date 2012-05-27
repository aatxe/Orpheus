package client.command;

import client.MapleCharacter;
import client.command.external.Commands;

public abstract class EnumeratedCommands extends Commands {
	protected static void getHelp(MapleCharacter chr) {
		EnumeratedCommands.getHelp(-1, chr);
	}
	
	protected static void getHelp(int page, MapleCharacter chr) {
		chr.dropMessage("Command.getHelp() was not overridden.");
	}
	
	public static enum Command {};
}
