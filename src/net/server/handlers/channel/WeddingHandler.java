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

import client.IItem;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.Output;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * 
 * @author Kevin
 */
public class WeddingHandler extends AbstractMaplePacketHandler {

	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		Output.print("Wedding Packet: " + slea);
		MapleCharacter chr = c.getPlayer();
		byte operation = slea.readByte();
		switch (operation) {
			case 0x06:// Add an item to the Wedding Registry
				byte slot = (byte) slea.readShort();
				int itemid = slea.readInt();
				short quantity = slea.readShort();
				MapleInventoryType type = MapleItemInformationProvider.getInstance().getInventoryType(itemid);
				IItem item = chr.getInventory(type).getItem(slot);
				if (itemid == item.getItemId() && quantity <= item.getQuantity()) {
					c.getSession().write(MaplePacketCreator.addItemToWeddingRegistry(chr, item));
				}
		}
	}
}
