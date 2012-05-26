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
