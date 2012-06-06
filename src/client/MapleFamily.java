/*
 	OrpheusMS: MapleStory Private Server based on OdinMS
    Copyright (C) 2012 Aaron Weiss

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
package client;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import tools.DatabaseConnection;

/**
 * 
 * @author Jay Estrella :3
 */
public class MapleFamily {
	private static int id;
	private static Map<Integer, MapleFamilyEntry> members = new HashMap<Integer, MapleFamilyEntry>();

	public MapleFamily(int cid) {
		try {
			PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT familyid FROM family_character WHERE cid = ?");
			ps.setInt(1, cid);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				MapleFamily.id = rs.getInt("familyid");
			}
			ps.close();
			rs.close();
			getMapleFamily();
		} catch (SQLException ex) {
		}
	}

	public static void getMapleFamily() {
		try {
			PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM family_character WHERE familyid = ?");
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				MapleFamilyEntry ret = new MapleFamilyEntry();
				ret.setFamilyId(id);
				ret.setRank(rs.getInt("rank"));
				ret.setReputation(rs.getInt("reputation"));
				ret.setTotalJuniors(rs.getInt("totaljuniors"));
				ret.setFamilyName(rs.getString("name"));
				ret.setJuniors(rs.getInt("juniorsadded"));
				ret.setTodaysRep(rs.getInt("todaysrep"));
				int cid = rs.getInt("cid");
				ret.setChrId(cid);
				members.put(cid, ret);
			}
			rs.close();
			ps.close();
		} catch (SQLException sqle) {
		}
	}

	public MapleFamilyEntry getMember(int cid) {
		if (members.containsKey(cid))
			return members.get(cid);

		return null;
	}

	public Map<Integer, MapleFamilyEntry> getMembers() {
		return members;
	}
}
