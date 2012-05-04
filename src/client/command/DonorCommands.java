package client.command;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import server.MapleInventoryManipulator;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;
import net.server.Channel;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;

public class DonorCommands extends Commands {
	public static boolean execute(MapleClient c, String[] sub, char heading) {
		MapleCharacter chr = c.getPlayer();
		Channel cserv = c.getChannelServer();
		MapleCharacter victim; // For commands with targets.
		ResultSet rs; // For commands with MySQL results.

		Command command = Command.valueOf(sub[0]);
		switch (command) {
			default:
				// chr.yellowMessage("Command: " + heading + sub[0] + ": does not exist.");
				return false;
			case donor:
				chr.setHp(30000);
				chr.setMaxHp(30000);
				chr.setMp(30000);
				chr.setMaxMp(30000);
				chr.updateSingleStat(MapleStat.HP, 30000);
				chr.updateSingleStat(MapleStat.MAXHP, 30000);
				chr.updateSingleStat(MapleStat.MP, 30000);
				chr.updateSingleStat(MapleStat.MAXMP, 30000);
				chr.message("Who's awesome? You're awesome!");
				break;
			case heal:
				chr.setHp(chr.getMaxHp());
				chr.setMp(chr.getMaxMp());
				chr.updateSingleStat(MapleStat.HP, chr.getMaxHp());
				chr.updateSingleStat(MapleStat.MP, chr.getMaxMp());
				chr.message("Healed for free. Thanks for your donation!");
				break;
			case itemvac:
				List<MapleMapObject> items = chr.getMap().getMapObjectsInRange(chr.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM));
				for (MapleMapObject item : items) {
					MapleMapItem mapitem = (MapleMapItem) item;
					if (!MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true)) {
						continue;
					}
					mapitem.setPickedUp(true);
					chr.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
					chr.getMap().removeMapObject(item);
					chr.getMap().nullifyObject(item);
				}
				break;
		}
		return true;
	}

	private static enum Command {
		donor, 
		heal,
		itemvac,
	}
}
