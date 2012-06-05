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
	public static short VERSION = 83; // MapleStory version number
	public static String ORPHEUS_VERSION = "6/3/12"; // date of version publishing
	public static final String SERVER_NAME = "OrpheusMS"; // Server's name
	public static final String TIP_NAME = "OrpheusTip"; // Tip Name for automsg
	public static String[] WORLD_NAMES = {"Scania", "Bera", "Broa", "Windia", "Khaini", "Bellocan", "Mardia", "Kradia", "Yellonde", "Demethos", "Galicia", "El Nido", "Zenith", "Arcenia", "Kastia", "Judis", "Plana", "Kalluna", "Stius", "Croa", "Medere"};
	
	// Start-Up Configuration
	public static final boolean CLEAR_ERROR_LOGS_ON_BOOT = true; // if true, error logs will be cleared on boot.
	public static final boolean LOAD_COMMANDS_ON_BOOT = true; // if true, commands will be loaded on boot.
	
	// Orpheus Source Configuration
	public static final boolean USE_EXTERNAL_COMMAND_LOADER = true; // if true, commands will be loaded externally. (Beginners should set to false)
	public static final String COMMAND_JAR_PATH = "dist/Commands.jar"; // path to Commands, also supports loading all jars in a directory.
	public static final boolean USE_PARANOIA = true; // if true, server uses Orpheus' everything logger, Paranoia.
	public static final boolean MAKE_NPCS_SCRIPTABLE = true; // if true, the server will force NPCs to be scriptable. (Setup in ScriptableNPCConstants)
	public static final int MAX_CHAT_MESSAGE_LENGTH = Byte.MAX_VALUE; // the number of characters allowed in chat messages. (fixes DC hax)
	public static final boolean PAGINATE_HELP = true; // if true, help messages will be displayed in pages.
	public static final short ENTRIES_PER_PAGE = 8; // the number of entries to display on each page.
	
	// Broadcasting Configuration
	public static final boolean GREET_PLAYERS_ON_LOGIN = true; // if true, server will announce when players have logged in.
	public static final boolean GREET_GMS_ON_LOGIN = true; // if true, server will announce when GMs have logged in.
	public static final boolean BROADCAST_GACHAPON_ITEMS = true; // if true, server will announce when a gachapon is used.
	
	// Rate Configuration
	public static final boolean BEGINNERS_USE_GMS_RATES = false; // if true, beginners will use GMS rates instead of server rates.
	public static final byte QUEST_EXP_RATE = 1; // experience rate from quests
	public static final byte QUEST_MESO_RATE = 1; // meso rate from quests
	
	// Login Configuration
	public static final boolean AUTO_UNSTUCK_ACCOUNTS = true; // if true, automatically attempt to unstuck stuck accounts.
	public static final boolean ENABLE_AUTOREGISTER = true; // if true, enables autoregistration upon login of nonexistant accounts.
	public static final boolean ENABLE_PIC = false; // if false, requires use of Maple Admin to delete characters.	
	public static final boolean HIDE_GMS_ON_LOGIN = true; // if true, GMs will appear hidden on login.
	public static final int CHANNEL_LOAD = 100; // Players per channel
	public static final long RANKING_INTERVAL = 3600000; // ranking interval, not used if the rankings event is used.
	
	// Event and World Configuration
	public static final boolean FREE_NX = true; // if true, @nx command is free.
	public static final int NX_COST = 1000000; // if false, @nx command costs this amount.
	public static final boolean UNLIMITED_PROJECTILES = false; // if true, players won't lose projectiles on use.
	public static final boolean DROP_UNTRADEABLE_ITEMS = false; // if true, players can drop normally untradeable items.
	public static final boolean USE_MTS_AS_FM_WARP = true; // if true, Trade button sends players to the FM.
	public static final boolean ALLOW_INFO_ON_GMS = true; // if false, non-GMs cannot get info on GMs.
	public static final boolean PERFECT_PITCH = false; // if true, Perfect Pitch can be earned by players.
	public static final boolean ENABLE_HARDCORE_MODE = true; // if true, players can enter hardcore mode for double exp and mesos.
	public static final String EVENTS = "automsg KerningPQ Boats Subway AirPlane elevator rankings"; // there is a rankings event to replace the default rankings tool.
	
	// MapleStocks Configuration [Unfinished]
	public static final boolean USE_MAPLE_STOCKS = true; // if true, MapleStocks will be enabled. (Requires maplestocks event)
	public static final boolean ALLOW_STOCKS_COMMAND = true; // if true, players can access stocks through @stocks.
	public static final boolean ALLOW_GM_MARKET_REGULATION = true; // if true, GMs can regulate the stock market (just a little).
	public static final boolean ALLOW_GM_STOCK_CREATION = true; // if true, GMs can create new stocks with !stocks.
	public static final boolean ALLOW_STOCK_CRASHES = true; // if true, stocks will be allowed to crash.
	public static final double STOCK_CRASH_THRESHOLD = 0.375; // percentage of stocks sold per interval to cause crash.
	public static final double STOCK_DECLINE_THRESHOLD = 0.275; // percentage of stocks sold per interval to cause decline.
	public static final int STOCK_VALUE_CAP = Integer.MAX_VALUE; // max value of any stock
	
	// Pet Configuration
	public static final long PET_FULLNESS_REPEAT_TIME = 180000; // time between fullness updates in ms.
	public static final long PET_FULLNESS_START_DELAY = 18000; // delay before initial fullness update in ms.
	public static final boolean PETS_NEVER_HUNGRY = false; // if true, pets will never grow hungry.
	public static final boolean GM_PETS_NEVER_HUNGRY = true; // if true, pets of GMs will never grow hungry.
	
	// IP Configuration
	public static final String HOST = "50.116.55.23"; // Server IP address
	
	// Database Configuration
	public static final boolean DB_USE_COMPILED_VALUES = false; // if true, then the values below will be used.
	public static final String DB_URL = "jdbc:mysql://localhost:3306/Orpheus?autoReconnect=true";
	public static final String DB_USER = "root";
	public static final String DB_PASS = "";
}