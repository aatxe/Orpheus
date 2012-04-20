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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import client.MapleCharacter;
import client.MapleClient;
import tools.DatabaseConnection;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class ViewCharHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT world, id FROM characters WHERE accountid = ?");
            ps.setInt(1, c.getAccID());
            short charsNum = 0;
            List<Byte> worlds = new ArrayList<Byte>();
            List<MapleCharacter> chars = new ArrayList<MapleCharacter>();
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                byte cworld = rs.getByte("world");
                boolean inside = false;
                for (int w : worlds) {
                    if (w == cworld) {
                        inside = true;
                    }
                }
                if (!inside) {
                    worlds.add(cworld);
                }
                MapleCharacter chr = MapleCharacter.loadCharFromDB(rs.getInt("id"), c, false);
                chars.add(chr);
                charsNum++;
            }
            rs.close();
            ps.close();
            int unk = charsNum + 3 - charsNum % 3;
            c.announce(MaplePacketCreator.showAllCharacter(charsNum, unk));
            for (byte w : worlds) {
                List<MapleCharacter> chrsinworld = new ArrayList<MapleCharacter>();
                for (MapleCharacter chr : chars) {
                    if (chr.getWorld() == w) {
                        chrsinworld.add(chr);
                    }
                }
                c.announce(MaplePacketCreator.showAllCharacterInfo(w, chrsinworld));
            }
        } catch (Exception e) {
        }
    }
}
