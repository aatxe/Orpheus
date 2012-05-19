package paranoia;

import constants.ParanoiaConstants;

public enum ParanoiaInformation {
	timestamps(ParanoiaConstants.USE_TIMESTAMPS, "Uses timestamps."),
	resetlogs(ParanoiaConstants.CLEAR_LOGS_ON_STARTUP, "Clears logs on startup."),
	blacklist(ParanoiaConstants.ENABLE_BLACKLISTING, "Allows account blacklisting."),
	console(ParanoiaConstants.PARANOIA_CONSOLE_LOGGER, "Logs server console output."),
	exactconsole(ParanoiaConstants.REPLICATE_CONSOLE_EXACTLY, "Logs console output exactly."),
	chat(ParanoiaConstants.PARANOIA_CHAT_LOGGER, "Logs user chat."),
	general(ParanoiaConstants.LOG_GENERAL_CHAT, "Logs general chat."),
	party(ParanoiaConstants.LOG_PARTY_CHAT, "Logs party chat."),
	buddy(ParanoiaConstants.LOG_BUDDY_CHAT, "Logs buddy chat."),
	guild(ParanoiaConstants.LOG_GUILD_CHAT, "Logs guild chat."),
	alliance(ParanoiaConstants.LOG_ALLIANCE_CHAT, "Logs alliance chat."),
	whispers(ParanoiaConstants.LOG_WHISPERS, "Logs whispers between players."),
	commands(ParanoiaConstants.PARANOIA_COMMAND_LOGGER, "Logs user commands."),
	player(ParanoiaConstants.LOG_PLAYER_COMMANDS, "Logs player commands."),
	donor(ParanoiaConstants.LOG_DONOR_COMMANDS, "Logs donor commands."),
	support(ParanoiaConstants.LOG_SUPPORT_COMMANDS, "Logs support commands."),
	gm(ParanoiaConstants.LOG_GM_COMMANDS, "Logs GM commands."),
	dev(ParanoiaConstants.LOG_DEVELOPER_COMMANDS, "Logs developer commands."),
	admin(ParanoiaConstants.LOG_ADMIN_COMMANDS, "Logs admin commands.");
	
	private boolean value = false;
	private String desc = "";
	
	private ParanoiaInformation(final boolean value, final String desc) {
		this.value = value;
		this.desc = desc;
	}
	
	public boolean get() {
		return this.value;
	}
	
	public String explain() {
		return this.desc;
	}
	
	public String toUsesString() {
		switch (this) {
			default:
				return "unknown";
			case timestamps:
				return "timestamping";
			case blacklist:
				return "blacklisting";
			case console:
				return "console logging";
			case chat:
				return "chat logging";
			case general:
				return "general chat logging";
			case party:
				return "party chat logging";
			case buddy:
				return "buddy chat logging";
			case guild:
				return "guild chat logging";
			case alliance:
				return "alliance chat logging";
			case whispers:
				return "whispers logging";
			case commands:
				return "command logging";
			case player:
				return "player command logging";
			case donor:
				return "donator command logging";
			case support:
				return "support command logging";
			case gm:
				return "GM command logging";
			case dev:
				return "developer command logging";
			case admin:
				return "admin command logging";
		}
	}
}
