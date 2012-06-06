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
import tools.MaplePacketCreator;
import tools.Output;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * 
 * @author Jay Estrella
 */
public final class FamilyAddHandler extends AbstractMaplePacketHandler {
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		Output.print(slea.toString());
		String toAdd = slea.readMapleAsciiString();
		MapleCharacter addChr = c.getChannelServer().getPlayerStorage().getCharacterByName(toAdd);
		if (addChr != null) {
			addChr.getClient().announce(MaplePacketCreator.sendFamilyInvite(c.getPlayer().getId(), toAdd));
			c.getPlayer().dropMessage("The invite has been sent.");
		} else {
			c.getPlayer().dropMessage("The player cannot be found!");
		}
		c.announce(MaplePacketCreator.enableActions());
	}
}
