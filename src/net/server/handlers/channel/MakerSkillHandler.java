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
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MakerItemFactory;
import server.MakerItemFactory.MakerItemCreateEntry;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * 
 * @author Jay Estrella
 */
public final class MakerSkillHandler extends AbstractMaplePacketHandler {
	private MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		slea.readInt();
		int toCreate = slea.readInt();
		MakerItemCreateEntry recipe = MakerItemFactory.getItemCreateEntry(toCreate);
		if (canCreate(c, recipe) && !c.getPlayer().getInventory(ii.getInventoryType(toCreate)).isFull()) {
			for (Pair<Integer, Integer> p : recipe.getReqItems()) {
				int toRemove = p.getLeft();
				MapleInventoryManipulator.removeById(c, ii.getInventoryType(toRemove), toRemove, p.getRight(), false, false);
			}
			MapleInventoryManipulator.addById(c, toCreate, (short) recipe.getRewardAmount());
		}
	}

	private boolean canCreate(MapleClient c, MakerItemCreateEntry recipe) {
		return hasItems(c, recipe) && c.getPlayer().getMeso() >= recipe.getCost() && c.getPlayer().getLevel() >= recipe.getReqLevel() && c.getPlayer().getSkillLevel(c.getPlayer().getJob().getId() / 1000 * 1000 + 1007) >= recipe.getReqSkillLevel();
	}

	private boolean hasItems(MapleClient c, MakerItemCreateEntry recipe) {
		for (Pair<Integer, Integer> p : recipe.getReqItems()) {
			int itemId = p.getLeft();
			if (c.getPlayer().getInventory(ii.getInventoryType(itemId)).countById(itemId) < p.getRight()) {
				return false;
			}
		}
		return true;
	}
}
