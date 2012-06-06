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
import client.autoban.AutobanFactory;
import client.autoban.AutobanManager;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public final class HealOvertimeHandler extends AbstractMaplePacketHandler {

	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		MapleCharacter chr = c.getPlayer();
		AutobanManager abm = chr.getAutobanManager();
		abm.setTimestamp(0, slea.readInt());
		slea.skip(4);
		short healHP = slea.readShort();
		if (healHP != 0) {
			if ((abm.getLastSpam(0) + 1500) > System.currentTimeMillis())
				AutobanFactory.FAST_HP_HEALING.addPoint(abm, "Fast hp healing");
			if (healHP > 140) {
				AutobanFactory.HIGH_HP_HEALING.autoban(chr, "Healing: " + healHP + "; Max is 140.");
				return;
			}
			chr.addHP(healHP);
			// chr.getMap().broadcastMessage(chr,
			// MaplePacketCreator.showHpHealed(chr.getId(), healHP), false);
			chr.checkBerserk();
			abm.spam(0);
		}
		short healMP = slea.readShort();
		if (healMP != 0 && healMP < 1000) {
			if ((abm.getLastSpam(1) + 1500) > System.currentTimeMillis())
				AutobanFactory.FAST_MP_HEALING.addPoint(abm, "Fast mp healing");
			chr.addMP(healMP);
			abm.spam(1);
		}
	}
}
