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
package server.maps;

import java.awt.Point;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import client.MapleClient;
import java.sql.SQLException;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;

/**
 * 
 * @author XoticStory
 */
public class PlayerNPCs extends AbstractMapleMapObject {
	private Map<Byte, Integer> equips = new HashMap<Byte, Integer>();
	private int npcId, face, hair;
	private byte skin;
	private String name = "";
	private int FH, RX0, RX1, CY;

	public PlayerNPCs(ResultSet rs) {
		try {
			CY = rs.getInt("cy");
			name = rs.getString("name");
			hair = rs.getInt("hair");
			face = rs.getInt("face");
			skin = rs.getByte("skin");
			FH = rs.getInt("Foothold");
			RX0 = rs.getInt("rx0");
			RX1 = rs.getInt("rx1");
			npcId = rs.getInt("ScriptId");
			setPosition(new Point(rs.getInt("x"), CY));
			PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT equippos, equipid FROM playernpcs_equip WHERE NpcId = ?");
			ps.setInt(1, rs.getInt("id"));
			ResultSet rs2 = ps.executeQuery();
			while (rs2.next()) {
				equips.put(rs2.getByte("equippos"), rs2.getInt("equipid"));
			}
			rs2.close();
			ps.close();
		} catch (SQLException e) {
		}
	}

	public Map<Byte, Integer> getEquips() {
		return equips;
	}

	public int getId() {
		return npcId;
	}

	public int getFH() {
		return FH;
	}

	public int getRX0() {
		return RX0;
	}

	public int getRX1() {
		return RX1;
	}

	public int getCY() {
		return CY;
	}

	public byte getSkin() {
		return skin;
	}

	public String getName() {
		return name;
	}

	public int getFace() {
		return face;
	}

	public int getHair() {
		return hair;
	}

	@Override
	public void sendDestroyData(MapleClient client) {
		return;
	}

	@Override
	public MapleMapObjectType getType() {
		return MapleMapObjectType.PLAYER_NPC;
	}

	@Override
	public void sendSpawnData(MapleClient client) {
		client.getSession().write(MaplePacketCreator.spawnPlayerNPC(this));
		client.getSession().write(MaplePacketCreator.getPlayerNPC(this));
	}
}