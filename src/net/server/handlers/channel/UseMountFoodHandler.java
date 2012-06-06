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
package net.server.handlers.channel;

import client.MapleClient;
import client.MapleInventoryType;
import constants.ExpTable;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * @author PurpleMadness
 */
public final class UseMountFoodHandler extends AbstractMaplePacketHandler {
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		slea.skip(6);
		int itemid = slea.readInt();
		if (c.getPlayer().getInventory(MapleInventoryType.USE).findById(itemid) != null) {
			if (c.getPlayer().getMount() != null && c.getPlayer().getMount().getTiredness() > 0) {
				c.getPlayer().getMount().setTiredness(Math.max(c.getPlayer().getMount().getTiredness() - 30, 0));
				c.getPlayer().getMount().setExp(2 * c.getPlayer().getMount().getLevel() + 6 + c.getPlayer().getMount().getExp());
				int level = c.getPlayer().getMount().getLevel();
				boolean levelup = c.getPlayer().getMount().getExp() >= ExpTable.getMountExpNeededForLevel(level) && level < 31;
				if (levelup) {
					c.getPlayer().getMount().setLevel(level + 1);
				}
				c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.updateMount(c.getPlayer().getId(), c.getPlayer().getMount(), levelup));
				MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemid, 1, true, false);
			}
		}
	}
}