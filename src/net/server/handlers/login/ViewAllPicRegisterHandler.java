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

import java.net.InetAddress;
import java.net.UnknownHostException;
import net.server.Server;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Randomizer;

public final class ViewAllPicRegisterHandler extends AbstractMaplePacketHandler { // Gey
																					// class
																					// name
																					// lol
	private static Logger log = LoggerFactory.getLogger(ViewAllPicRegisterHandler.class);

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		slea.readByte();
		int charId = slea.readInt();
		byte world = (byte) slea.readInt(); // world
		byte channel = (byte) Randomizer.rand(0, Server.getInstance().getWorld(world).getChannels().size());
		String mac = slea.readMapleAsciiString();
		c.updateMacs(mac);
		if (c.hasBannedMac()) {
			c.getSession().close(true);
			return;
		}
		slea.readMapleAsciiString();
		String pic = slea.readMapleAsciiString();
		c.setPic(pic);
		try {
			if (c.getIdleTask() != null) {
				c.getIdleTask().cancel(true);
			}
			c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
			String channelServerIP = MapleClient.getChannelServerIPFromSubnet(c.getSession().getRemoteAddress().toString().replace("/", "").split(":")[0], channel);

			if (channelServerIP.equals("0.0.0.0")) {
				String[] socket = Server.getInstance().getIP(world, channel).split(":");
				c.announce(MaplePacketCreator.getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
			} else {
				String[] socket = Server.getInstance().getIP(world, channel).split(":");
				c.announce(MaplePacketCreator.getServerIP(InetAddress.getByName(channelServerIP), Integer.parseInt(socket[1]), charId));
			}
		} catch (UnknownHostException e) {
			log.error("Host not found", e);
		}
	}
}
