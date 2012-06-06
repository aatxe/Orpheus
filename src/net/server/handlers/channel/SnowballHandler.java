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
import server.events.gm.MapleSnowball;
import server.maps.MapleMap;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * 
 * @author kevintjuh93
 */
public final class SnowballHandler extends AbstractMaplePacketHandler {

	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		// D3 00 02 00 00 A5 01
		MapleCharacter chr = c.getPlayer();
		MapleMap map = chr.getMap();
		final MapleSnowball snowball = map.getSnowball(chr.getTeam());
		final MapleSnowball othersnowball = map.getSnowball(chr.getTeam() == 0 ? (byte) 1 : 0);
		int what = slea.readByte();
		// slea.skip(4);

		if (snowball == null || othersnowball == null || snowball.getSnowmanHP() == 0)
			return;
		if ((System.currentTimeMillis() - chr.getLastSnowballAttack()) < 500)
			return;
		if (chr.getTeam() != (what % 2))
			return;

		chr.setLastSnowballAttack(System.currentTimeMillis());
		int damage = 0;
		if (what < 2 && othersnowball.getSnowmanHP() > 0)
			damage = 10;
		else if (what == 2 || what == 3) {
			if (Math.random() < 0.03)
				damage = 45;
			else
				damage = 15;
		}

		if (what >= 0 && what <= 4)
			snowball.hit(what, damage);

	}
}
