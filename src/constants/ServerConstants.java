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
package constants;

public class ServerConstants {
    public static short VERSION = 83;
    public static String[] WORLD_NAMES = {"Scania", "Bera", "Broa", "Windia", "Khaini", "Bellocan", "Mardia", "Kradia", "Yellonde", "Demethos", "Galicia", "El Nido", "Zenith", "Arcenia", "Kastia", "Judis", "Plana", "Kalluna", "Stius", "Croa", "Medere"};;
    // Rate Configuration
    public static final byte QUEST_EXP_RATE = 4;
    public static final byte QUEST_MESO_RATE = 3;
    // Login Configuration
    public static final int CHANNEL_LOAD = 150;//Players per channel
    public static final long RANKING_INTERVAL = 3600000;
    public static final boolean ENABLE_PIC = true;
    //Event Configuration
    public static final boolean PERFECT_PITCH = false;
    public static final String EVENTS = "automsg KerningPQ Boats Subway AirPlane elevator";
    // IP Configuration
    public static final String HOST = "localhost";
    //Database Configuration
    public static final String DB_URL = "jdbc:mysql://localhost:3306/MoopleDEV?autoReconnect=true";
    public static final String DB_USER = "root";
    public static final String DB_PASS = "";
}