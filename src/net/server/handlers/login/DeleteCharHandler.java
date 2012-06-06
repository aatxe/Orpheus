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
package net.server.handlers.login;

import constants.ServerConstants;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class DeleteCharHandler extends AbstractMaplePacketHandler {
	@SuppressWarnings("unused")
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		String pic = slea.readMapleAsciiString();
		int cid = slea.readInt();
		if (c.checkPic(pic) || !ServerConstants.ENABLE_PIC) {
			c.announce(MaplePacketCreator.deleteCharResponse(cid, 0));
			c.deleteCharacter(cid);
		} else {
			c.announce(MaplePacketCreator.deleteCharResponse(cid, 0x14));
		}
	}
}
