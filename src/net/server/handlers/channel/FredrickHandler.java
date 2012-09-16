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
import client.ItemFactory;
import client.ItemInventoryEntry;
import client.MapleCharacter;
import client.MapleClient;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * 
 * @author kevintjuh93
 */
public class FredrickHandler extends AbstractMaplePacketHandler {
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		MapleCharacter chr = c.getPlayer();
		byte operation = slea.readByte();

		switch (operation) {
			case 0x19: // Will never come...
				// c.announce(MaplePacketCreator.getFredrick((byte) 0x24));
				break;
			case 0x1A:
				List<ItemInventoryEntry> items;
				try {
					items = ItemFactory.MERCHANT.loadItems(chr.getId(), false);
					if (!check(chr, items)) {
						c.announce(MaplePacketCreator.fredrickMessage((byte) 0x21));
						return;
					}

					chr.gainMeso(chr.getMerchantMeso(), false);
					chr.setMerchantMeso(0);
					if (deleteItems(chr)) {
						for (int i = 0; i < items.size(); i++) {
							MapleInventoryManipulator.addFromDrop(c, items.get(i).item, false);
						}
						c.announce(MaplePacketCreator.fredrickMessage((byte) 0x1E));
					} else {
						chr.message("An unknown error has occured.");
						return;
					}
					break;
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
				break;
			case 0x1C: // Exit
				break;
			default:

		}
	}

	private static boolean check(MapleCharacter chr, List<ItemInventoryEntry> entries) {
		if (chr.getMeso() + chr.getMerchantMeso() < 0) {
			return false;
		}
		for (ItemInventoryEntry entry : entries) {
			final IItem item = entry.item;
			if (!MapleInventoryManipulator.checkSpace(chr.getClient(), item.getItemId(), item.getQuantity(), item.getOwner()))
				return false;
		}

		return true;
	}

	private static boolean deleteItems(MapleCharacter chr) {
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM `inventoryitems` WHERE `type` = ? AND `characterid` = ?");

			ps.setInt(1, ItemFactory.MERCHANT.getValue());
			ps.setInt(2, chr.getId());
			ps.execute();
			ps.close();
			return true;
		} catch (SQLException e) {
			return false;
		}

	}
}
