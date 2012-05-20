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
package net.server.handlers.channel;

import paranoia.BlacklistHandler;
import constants.ParanoiaConstants;
import constants.ServerConstants;
import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import net.server.Server;
import net.server.World;
import tools.MapleLogger;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class PartyChatHandler extends AbstractMaplePacketHandler {
	
	@SuppressWarnings("unused")
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		MapleCharacter player = c.getPlayer();
		int type = slea.readByte(); // 0 for buddys, 1 for partys
		int numRecipients = slea.readByte();
		int recipients[] = new int[numRecipients];
		for (int i = 0; i < numRecipients; i++) {
			recipients[i] = slea.readInt();
		}
		String chattext = slea.readMapleAsciiString();
		World world = c.getWorldServer();
		if (chattext.length() > ServerConstants.MAX_CHAT_MESSAGE_LENGTH) {
			player.dropMessage("Your message is too long.");
			return; // packet editing, fucker.
		}
		if (type == 0) {
			if (ServerConstants.USE_PARANOIA && ParanoiaConstants.PARANOIA_CHAT_LOGGER && ParanoiaConstants.LOG_BUDDY_CHAT) {
				MapleLogger.printFormatted(MapleLogger.PARANOIA_CHAT, "[Buddy] [" + c.getPlayer().getName() + "] " + chattext);
			}
			if (ServerConstants.USE_PARANOIA && ParanoiaConstants.ENABLE_BLACKLISTING && ParanoiaConstants.LOG_BLACKLIST_CHAT) {
				if (BlacklistHandler.isBlacklisted(c.getAccID())) {
					BlacklistHandler.printBlacklistLog("[Buddy] [" + c.getPlayer().getName() + "] " + chattext, c.getAccID());
				}
			}
			world.buddyChat(recipients, player.getId(), player.getName(), chattext);
		} else if (type == 1 && player.getParty() != null) {
			if (ServerConstants.USE_PARANOIA && ParanoiaConstants.PARANOIA_CHAT_LOGGER && ParanoiaConstants.LOG_PARTY_CHAT) {
				MapleLogger.printFormatted(MapleLogger.PARANOIA_CHAT, "[Party] [" + c.getPlayer().getName() + "] " + chattext);
			}
			if (ServerConstants.USE_PARANOIA && ParanoiaConstants.ENABLE_BLACKLISTING && ParanoiaConstants.LOG_BLACKLIST_CHAT) {
				if (BlacklistHandler.isBlacklisted(c.getAccID())) {
					BlacklistHandler.printBlacklistLog("[Party] [" + c.getPlayer().getName() + "] " + chattext, c.getAccID());
				}
			}
			world.partyChat(player.getParty(), chattext, player.getName());
		} else if (type == 2 && player.getGuildId() > 0) {
			if (ServerConstants.USE_PARANOIA && ParanoiaConstants.PARANOIA_CHAT_LOGGER && ParanoiaConstants.LOG_GUILD_CHAT) {
				MapleLogger.printFormatted(MapleLogger.PARANOIA_CHAT, "[Guild] [" + c.getPlayer().getName() + "] " + chattext);
			}
			if (ServerConstants.USE_PARANOIA && ParanoiaConstants.ENABLE_BLACKLISTING && ParanoiaConstants.LOG_BLACKLIST_CHAT) {
				if (BlacklistHandler.isBlacklisted(c.getAccID())) {
					BlacklistHandler.printBlacklistLog("[Guild] [" + c.getPlayer().getName() + "] " + chattext, c.getAccID());
				}
			}
			Server.getInstance().guildChat(player.getGuildId(), player.getName(), player.getId(), chattext);
		} else if (type == 3 && player.getGuild() != null) {
			int allianceId = player.getGuild().getAllianceId();
			if (allianceId > 0) {
				if (ServerConstants.USE_PARANOIA && ParanoiaConstants.PARANOIA_CHAT_LOGGER && ParanoiaConstants.LOG_ALLIANCE_CHAT) {
					MapleLogger.printFormatted(MapleLogger.PARANOIA_CHAT, "[Alliance] [" + c.getPlayer().getName() + "] " + chattext);
				}
				if (ServerConstants.USE_PARANOIA && ParanoiaConstants.ENABLE_BLACKLISTING && ParanoiaConstants.LOG_BLACKLIST_CHAT) {
					if (BlacklistHandler.isBlacklisted(c.getAccID())) {
						BlacklistHandler.printBlacklistLog("[Alliance] [" + c.getPlayer().getName() + "] " + chattext, c.getAccID());
					}
				}
				Server.getInstance().allianceMessage(allianceId, MaplePacketCreator.multiChat(player.getName(), chattext, 3), player.getId(), -1);
			}
		}
	}
}
