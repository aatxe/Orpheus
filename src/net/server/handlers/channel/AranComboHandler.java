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

import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleClient;

public class AranComboHandler extends AbstractMaplePacketHandler {

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		/*
		 * MapleCharacter player = c.getPlayer(); if
		 * (player.getJob().equals(MapleJob.LEGEND) ||
		 * player.getJob().isA(MapleJob.ARAN4)) { final long currentTime =
		 * System.currentTimeMillis(); short combo = player.getCombo(); if
		 * ((currentTime - player.getLastCombo()) > 3000 && combo > 0) { combo =
		 * 0; player.cancelEffectFromBuffStat(MapleBuffStat.ARAN_COMBO); }
		 * combo++; switch (combo) { case 10: case 20: case 30: case 40: case
		 * 50: case 60: case 70: case 80: case 90: case 100:
		 * SkillFactory.getSkill(21000000).getEffect(combo /
		 * 10).applyComboBuff(player, combo); break; }
		 * player.setLastCombo(currentTime); player.setCombo(combo); }
		 */
	}
}
