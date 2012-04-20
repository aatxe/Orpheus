/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package net.server.handlers.login;

import java.net.InetAddress;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import net.server.Server;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class CharSelectedHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int charId = slea.readInt();
        String macs = slea.readMapleAsciiString();
        c.updateMacs(macs);
        if (c.hasBannedMac()) {
            c.getSession().close(true);
            return;
        }
        try {
            if (c.getIdleTask() != null) {
                c.getIdleTask().cancel(true);
            }
            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
            String channelServerIP = MapleClient.getChannelServerIPFromSubnet(c.getSession().getRemoteAddress().toString().replace("/", "").split(":")[0], c.getChannel());
            if (channelServerIP.equals("0.0.0.0")) {
                String[] socket = Server.getInstance().getIP(c.getWorld(), c.getChannel()).split(":");
                c.announce(MaplePacketCreator.getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
            } else {
                String[] socket = Server.getInstance().getIP(c.getWorld(), c.getChannel()).split(":");
                c.announce(MaplePacketCreator.getServerIP(InetAddress.getByName(channelServerIP), Integer.parseInt(socket[1]), charId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}