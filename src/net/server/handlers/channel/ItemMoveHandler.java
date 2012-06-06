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
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * 
 * @author Matze
 */
public final class ItemMoveHandler extends AbstractMaplePacketHandler {
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		slea.skip(4);
		MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
		byte src = (byte) slea.readShort();
		byte action = (byte) slea.readShort();
		short quantity = slea.readShort();
		if (src < 0 && action > 0) {
			MapleInventoryManipulator.unequip(c, src, action);
		} else if (action < 0) {
			MapleInventoryManipulator.equip(c, src, action);
		} else if (action == 0) {
			MapleInventoryManipulator.drop(c, type, src, quantity);
		} else {
			MapleInventoryManipulator.move(c, type, src, action);
		}
	}
}