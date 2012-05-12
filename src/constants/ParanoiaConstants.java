package constants;

public class ParanoiaConstants {
	public static short PARANOIA_VERSION = 3;
	public static final boolean USE_TIMESTAMPS = true;
	public static final boolean CLEAR_LOGS_ON_STARTUP = false;
	
	// Paranoia Console Logger settings
	public static final boolean PARANOIA_CONSOLE_LOGGER = false;
	public static final boolean REPLICATE_CONSOLE_EXACTLY = false; // if true, new lines added for formatting will be included in the log.
	
	// Paranoia Chat Logger settings
	public static final boolean PARANOIA_CHAT_LOGGER = false;
	public static final boolean LOG_GENERAL_CHAT = true;
	public static final boolean LOG_PARTY_CHAT = false;
	public static final boolean LOG_BUDDY_CHAT = false;
	public static final boolean LOG_GUILD_CHAT = false;
	public static final boolean LOG_ALLIANCE_CHAT = false;
	public static final boolean LOG_WHISPERS = true;
	
	// Paranoia Command Logger settings
	public static final boolean PARANOIA_COMMAND_LOGGER = true;
	public static final boolean LOG_INVALID_COMMANDS = false;
	public static final boolean LOG_PLAYER_COMMANDS = false;
	public static final boolean LOG_DONOR_COMMANDS = false;
	public static final boolean LOG_SUPPORT_COMMANDS = true;
	public static final boolean LOG_GM_COMMANDS = true;
	public static final boolean LOG_DEVELOPER_COMMANDS = true;
	public static final boolean LOG_ADMIN_COMMANDS = true;
}
