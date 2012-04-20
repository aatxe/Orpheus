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
package tools;

import constants.ServerConstants;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {//gay
    private static ThreadLocal<Connection> con = new ThreadLocalConnection();
    private static String url = ServerConstants.DB_URL;
    private static String user = ServerConstants.DB_USER;
    private static String pass = ServerConstants.DB_PASS;

    public static Connection getConnection() {
        return con.get();
    }

    public static void release() throws SQLException {
        con.get().close();
        con.remove();
    }

    private static class ThreadLocalConnection extends ThreadLocal<Connection> {
        static {
            try {
                Class.forName("com.mysql.jdbc.Driver"); // touch the mysql driver
            } catch (ClassNotFoundException e) {
                System.out.println("Could not locate the JDBC mysql driver.");
            }
        }

        @Override
        protected Connection initialValue() {
            return getConnection();
        }

        private Connection getConnection() {
            //Fk u *n word*
            try {
                return DriverManager.getConnection(url, user, pass);
            } catch (SQLException sql) {
                System.out.println("Could not create a SQL Connection object. Please make sure you've correctly configured the database properties inside constants/ServerConstants.java. MAKE SURE YOU COMPILED!");
                return null;
            }
        }

        @Override
        public Connection get() {
            Connection con = super.get();
            try {
                if (!con.isClosed()) {
                    return con;
                }
            } catch (SQLException sql) {
                // Munch munch, we'll get a new connection. :)
            }
            con = getConnection();
            super.set(con);
            return con;
        }
    }
}