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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.security.NoSuchAlgorithmException;
import tools.DatabaseConnection;
import tools.HashCreator;
import tools.MapleLogger;

public class AutoRegister {
	private static final int ACCOUNTS_PER_IP = 4;
	private static boolean success;

	public static boolean wasSuccessful() {
		return success;
	}

	public static boolean getAccountExists(String login) {
		boolean accountExists = false;
		Connection con = DatabaseConnection.getConnection();
		try {
			PreparedStatement ps = con.prepareStatement("SELECT name FROM accounts WHERE name = ?");
			ps.setString(1, login);
			ResultSet rs = ps.executeQuery();
			if (rs.first()) {
				accountExists = true;
			}
		} catch (Exception e) {
		}
		return accountExists;
	}

	public static void createAccount(String login, String pwd, String eip) {
		String sockAddr = eip;
		Connection con;
		try {
			con = DatabaseConnection.getConnection();
		} catch (Exception e) {
			MapleLogger.print(MapleLogger.EXCEPTION_CAUGHT, "There's a problem with automatic registration.\r\n" + e);
			return;
		}
		try {
			PreparedStatement ipc = con.prepareStatement("SELECT lastknownip FROM accounts WHERE lastknownip = ?");
			ipc.setString(1, sockAddr.substring(1, sockAddr.lastIndexOf(':')));
			ResultSet rs = ipc.executeQuery();
			if (rs.first() == false || rs.last() == true && rs.getRow() < ACCOUNTS_PER_IP) {
				try {
					PreparedStatement ps = con.prepareStatement("INSERT INTO accounts (name, password, email, birthday, macs, lastknownip) VALUES (?, ?, ?, ?, ?, ?)");
					ps.setString(1, login);
					ps.setString(2, HashCreator.getHash(pwd));
					ps.setString(3, "no@email.provided");
					ps.setString(4, "0000-00-00");
					ps.setString(5, "00-00-00-00-00-00");
					ps.setString(6, sockAddr.substring(1, sockAddr.lastIndexOf(':')));
					ps.executeUpdate();
					ps.close();
					success = true;
				} catch (NoSuchAlgorithmException e) {
					MapleLogger.print(MapleLogger.EXCEPTION_CAUGHT, "There's a problem with automatic registration.\r\n" + e);
					return;
				} catch (SQLException ex) {
					MapleLogger.print(MapleLogger.EXCEPTION_CAUGHT, "There's a problem with automatic registration.\r\n" + ex);
					return;
				}
			}
			ipc.close();
			rs.close();
		} catch (SQLException e) {
			MapleLogger.print(MapleLogger.EXCEPTION_CAUGHT, "There's a problem with automatic registration.\r\n" + e);
		}
	}
}