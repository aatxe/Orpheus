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

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import client.MapleInventoryType;
import client.MapleInventory;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class ItemSortHandler extends AbstractMaplePacketHandler {
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		MapleCharacter chr = c.getPlayer();
		chr.getAutobanManager().setTimestamp(2, slea.readInt());
		byte inv = slea.readByte();
		boolean sorted = false;
		MapleInventoryType pInvType = MapleInventoryType.getByType(inv);
		MapleInventory pInv = chr.getInventory(pInvType);
		while (!sorted) {
			byte freeSlot = pInv.getNextFreeSlot();
			if (freeSlot != -1) {
				byte itemSlot = -1;
				for (int i = freeSlot + 1; i <= 100; i++) {
					if (pInv.getItem((byte) i) != null) {
						itemSlot = (byte) i;
						break;
					}
				}
				if (itemSlot <= 100 && itemSlot > 0) {
					MapleInventoryManipulator.move(c, pInvType, itemSlot, freeSlot);
				} else {
					sorted = true;
				}
			}
		}
		c.announce(MaplePacketCreator.finishedSort(inv));
		c.announce(MaplePacketCreator.enableActions());
	}
}
