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
import tools.data.input.SeekableLittleEndianAccessor;

public final class CancelDebuffHandler extends AbstractMaplePacketHandler {// TIP: BAD STUFF LOL
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		/*
		 * List<MapleDisease> diseases = c.getPlayer().getDiseases();
		 * List<MapleDisease> diseases_ = new ArrayList<MapleDisease>(); for
		 * (MapleDisease disease : diseases) { List<MapleDisease> disease_ = new
		 * ArrayList<MapleDisease>(); disease_.add(disease);
		 * diseases_.add(disease);
		 * c.announce(MaplePacketCreator.cancelDebuff(disease_));
		 * c.getPlayer().getMap().broadcastMessage(c.getPlayer(),
		 * MaplePacketCreator.cancelForeignDebuff(c.getPlayer().getId(),
		 * disease_), false); } for (MapleDisease disease : diseases_) {
		 * c.getPlayer().removeDisease(disease); }
		 */
	}
}