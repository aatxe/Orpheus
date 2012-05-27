package client.command.external;

import client.MapleClient;

public abstract class AbstractCommandProcessor {
	public abstract void execute(MapleClient c, String[] sub, char heading);
}
