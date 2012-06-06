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
import constants.skills.DarkKnight;
import java.util.Collection;
import net.AbstractMaplePacketHandler;
import server.maps.MapleSummon;
import tools.Output;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * 
 * @author BubblesDev
 */
public final class BeholderHandler extends AbstractMaplePacketHandler {// broken
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		Output.print("[BH] " + slea.toString());
		Collection<MapleSummon> summons = c.getPlayer().getSummons().values();
		int oid = slea.readInt();
		MapleSummon summon = null;
		for (MapleSummon sum : summons) {
			if (sum.getObjectId() == oid) {
				summon = sum;
			}
		}
		if (summon != null) {
			int skillId = slea.readInt();
			if (skillId == DarkKnight.AURA_OF_BEHOLDER) {
				slea.readShort(); // Not sure.
			} else if (skillId == DarkKnight.HEX_OF_BEHOLDER) {
				slea.readByte(); // Not sure.
			} // show to others here
		} else {
			c.getPlayer().getSummons().clear();
		}
	}
}
