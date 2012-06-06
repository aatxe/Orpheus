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
package constants;

public class ParanoiaConstants {
	public static short PARANOIA_VERSION = 7;
	
	// Paranoia General settings -- basic config stuffs
	public static final boolean USE_TIMESTAMPS = true;
	public static final boolean CLEAR_LOGS_ON_STARTUP = false;
	
	// Paranoia Interface settings -- support for user interfacing with Paranonia
	public static final boolean ALLOW_CLEARLOGS_COMMAND = true; // clears logs with Admin !clearlogs
	public static final boolean ALLOW_QUERY_COMMAND = true; // check Paranoia settings with Dev !paranoia
	public static final boolean ALLOW_BLACKLIST_COMMAND = false; // blacklist users with GM !blacklist
	public static final boolean ALLOW_RELOADBLACKLIST_COMMAND = false; // reload blacklist with Dev !reloadblacklist
	public static final boolean CLEARLOG_CLEARS_BLACKLIST = false; // allow !clearlogs to clear blacklists.
	
	// Paranoia Blacklisting settings -- support for account logging with everything
	public static final boolean ENABLE_BLACKLISTING = false;
	public static final boolean LOAD_BLACKLIST_ON_STARTUP = true;
	public static final boolean LOG_BLACKLIST_CHAT = true;
	public static final boolean LOG_BLACKLIST_COMMAND = true;
	
	// Paranoia Console Logger settings -- server console logging support
	public static final boolean PARANOIA_CONSOLE_LOGGER = true;
	public static final boolean REPLICATE_CONSOLE_EXACTLY = true; // if true, new lines added for formatting will be included in the log.
	
	// Paranoia Chat Logger settings -- chat logging support
	public static final boolean PARANOIA_CHAT_LOGGER = false;
	public static final boolean LOG_GENERAL_CHAT = true;
	public static final boolean LOG_PARTY_CHAT = false;
	public static final boolean LOG_BUDDY_CHAT = false;
	public static final boolean LOG_GUILD_CHAT = false;
	public static final boolean LOG_ALLIANCE_CHAT = false;
	public static final boolean LOG_WHISPERS = true;
	
	// Paranoia Command Logger settings -- command logging support
	public static final boolean PARANOIA_COMMAND_LOGGER = true;
	public static final boolean LOG_INVALID_COMMANDS = false;
	public static final boolean LOG_PLAYER_COMMANDS = false;
	public static final boolean LOG_DONOR_COMMANDS = false;
	public static final boolean LOG_SUPPORT_COMMANDS = true;
	public static final boolean LOG_GM_COMMANDS = true;
	public static final boolean LOG_DEVELOPER_COMMANDS = true;
	public static final boolean LOG_ADMIN_COMMANDS = true;
}
