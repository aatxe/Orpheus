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

import client.MapleClient;
import constants.ServerConstants;
import net.AbstractMaplePacketHandler;
import net.server.Server;
import net.server.World;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class ServerlistRequestHandler extends AbstractMaplePacketHandler {
	private static final String[] names = ServerConstants.WORLD_NAMES;

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		Server server = Server.getInstance();
		World world;
		for (byte i = 0; i < Math.min(server.getLoad().size(), names.length); i++) {
			world = server.getWorld(i);
			c.announce(MaplePacketCreator.getServerList(i, names[i], world.getFlag(), world.getEventMessage(), server.getLoad(i)));
		}
		c.announce(MaplePacketCreator.getEndOfServerList());
		c.announce(MaplePacketCreator.selectWorld(0));// too lazy to make a
														// check lol
		c.announce(MaplePacketCreator.sendRecommended(server.worldRecommendedList()));
	}
}