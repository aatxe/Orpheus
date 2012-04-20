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
package net.server.handlers.channel;

import client.MapleCharacter;
import client.MapleClient;
import java.awt.Point;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public final class MesoDropHandler extends AbstractMaplePacketHandler {//FIX
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if (!chr.isAlive()) {
            c.announce(MaplePacketCreator.enableActions());
            return;
        }
        slea.skip(4);
        int meso = slea.readInt();
        if (meso <= c.getPlayer().getMeso() && meso > 9 && meso < 50001) {
            c.getPlayer().gainMeso(-meso, false, true, false);
            c.getPlayer().getMap().spawnMesoDrop(meso, chr.getPosition(), chr, chr, true, (byte) 2);
        }
    }
}