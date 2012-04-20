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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import net.AbstractMaplePacketHandler;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/*
 * 
 * @author BubblesDev
 */
public final class ReportHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int type = slea.readByte(); //01 = Conversation claim 00 = illegal program
        String victim = slea.readMapleAsciiString();
        int reason = slea.readByte();
        String description = slea.readMapleAsciiString();
        if (type == 0) {
        if (c.getPlayer().getPossibleReports() > 0) {
            if (c.getPlayer().getMeso() > 299) {
                c.getPlayer().decreaseReports();
                c.getPlayer().gainMeso(-300, true);
            } else {
                c.announce(MaplePacketCreator.reportResponse((byte) 4));
                return;
            }
        } else {
                c.announce(MaplePacketCreator.reportResponse((byte) 2));
            return;
        }
        c.getChannelServer().broadcastGMPacket(MaplePacketCreator.serverNotice(6, victim + " was reported for: " + description));
        addReport(c.getPlayer().getId(), MapleCharacter.getIdByName(victim), 0, description, null);
    } else if (type == 1) {
        String chatlog = slea.readMapleAsciiString();
        if (chatlog == null) {
            return;
        }
        if (c.getPlayer().getPossibleReports() > 0) {
            if (c.getPlayer().getMeso() > 299) {
                c.getPlayer().decreaseReports();
                c.getPlayer().gainMeso(-300, true);
            } else {
                c.announce(MaplePacketCreator.reportResponse((byte) 4));
                return;
            }
        }
        c.getChannelServer().broadcastGMPacket(MaplePacketCreator.serverNotice(6, victim + " was reported for: " + description));
        addReport(c.getPlayer().getId() ,MapleCharacter.getIdByName(victim), reason, description, chatlog);
    } else {
        c.getChannelServer().broadcastGMPacket(MaplePacketCreator.serverNotice(6, c.getPlayer().getName() + " is probably packet editing. Got unknown report type, which is impossible."));
    }
  }

     public void addReport(int reporterid, int victimid, int reason, String description, String chatlog) {
      Connection con = DatabaseConnection.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO reports (`reporterid`, `victimid`, `reason`, `description`, `chatlog`) VALUES (?, ?, ?, ?, ?)");
                ps.setInt(1, reporterid);
                ps.setInt(2, victimid);
                ps.setInt(3, reason);
                ps.setString(4, description);
                ps.setString(5, chatlog);
                ps.addBatch();
                ps.executeBatch();
                ps.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
    }
}
