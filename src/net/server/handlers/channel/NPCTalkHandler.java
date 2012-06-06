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
import scripting.npc.NPCScriptManager;
import server.life.MapleNPC;
import server.maps.MapleMapObject;
import server.maps.PlayerNPCs;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class NPCTalkHandler extends AbstractMaplePacketHandler {
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		if (!c.getPlayer().isAlive()) {
			c.announce(MaplePacketCreator.enableActions());
			return;
		}
		int oid = slea.readInt();
		MapleMapObject obj = c.getPlayer().getMap().getMapObject(oid);
		if (obj instanceof MapleNPC) {
			MapleNPC npc = (MapleNPC) obj;
			if (npc.getId() == 9010009) {
				c.announce(MaplePacketCreator.sendDuey((byte) 8, DueyHandler.loadItems(c.getPlayer())));
			} else if (npc.hasShop()) {
				if (c.getPlayer().getShop() != null) {
					return;
				}
				npc.sendShop(c);
			} else {
				if (c.getCM() != null || c.getQM() != null) {
					c.announce(MaplePacketCreator.enableActions());
					return;
				}
				NPCScriptManager.getInstance().start(c, npc.getId(), null, null);
			}
		} else if (obj instanceof PlayerNPCs) {
			NPCScriptManager.getInstance().start(c, ((PlayerNPCs) obj).getId(), null, null);
		}
	}
}