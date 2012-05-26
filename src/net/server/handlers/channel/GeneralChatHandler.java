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
import tools.MapleLogger;
import tools.MaplePacketCreator;
import tools.Output;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleClient;
import client.command.AdminCommands;
import client.command.EnumeratedCommands;
import client.command.DeveloperCommands;
import client.command.DonorCommands;
import client.command.GMCommands;
import client.command.PlayerCommands;
import client.command.SupportCommands;

public final class GeneralChatHandler extends net.AbstractMaplePacketHandler {

	@SuppressWarnings("unused")
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		String s = slea.readMapleAsciiString();
		MapleCharacter chr = c.getPlayer();
		char heading = s.charAt(0);
		/*
		 * WARNING: Daemons below!
		 * The following code is messy, and hard to follow.
		 * This is in an attempt to make the client.command classes
		 * easier to understand and work with. I apologize for the mess!
		 * 
		 * As of May 6th, the daemons should be a little cleaner.
		 */
		if (heading == '/' || heading == '!' || heading == '@') {
			String[] sp = s.split(" ");
			sp[0] = sp[0].toLowerCase().substring(1);
			if (heading == '@' || heading == '/') {
				boolean commandExecuted = true;
				switch (chr.gmLevel()) {
					case 5:
					case 4:
					case 3: 
					case 2:
					case 1:
						commandExecuted = DonorCommands.execute(c, sp, heading);
						if (commandExecuted) break;
					case 0:
						commandExecuted = PlayerCommands.execute(c, sp, heading);
						if (commandExecuted) break;
					default:
						EnumeratedCommands.execute(c, sp, heading);
						break;
				}
			} else {
				boolean commandExecuted = false;
				switch (chr.gmLevel()) {
					case 5:
						commandExecuted = AdminCommands.execute(c, sp, heading);
						if (commandExecuted) break;
					case 4:
						DeveloperCommands.setSLEA(slea);
						commandExecuted = DeveloperCommands.execute(c, sp, heading);
						if (commandExecuted) break;
					case 3:
						commandExecuted = GMCommands.execute(c, sp, heading);
						if (commandExecuted) break;
					case 2:
						commandExecuted = SupportCommands.execute(c, sp, heading);
						if (commandExecuted) break;
					default:
						EnumeratedCommands.execute(c, sp, heading);
						break;
				}
			}
			if (ServerConstants.USE_PARANOIA && ParanoiaConstants.ENABLE_BLACKLISTING && ParanoiaConstants.LOG_BLACKLIST_COMMAND) {
				if (BlacklistHandler.isBlacklisted(c.getAccID())) {
					BlacklistHandler.printBlacklistLog("[" + c.getPlayer().getName() + "] Used " + heading + sp[0] + ((sp.length > 1) ? " with parameters: " + Output.joinStringFrom(sp, 1) : "."), c.getAccID());
				}
			}
		} else {
			if (ServerConstants.USE_PARANOIA && ParanoiaConstants.ENABLE_BLACKLISTING && ParanoiaConstants.LOG_BLACKLIST_CHAT) {
				if (BlacklistHandler.isBlacklisted(c.getAccID())) {
					BlacklistHandler.printBlacklistLog("[General] [" + c.getPlayer().getName() + "] " + s, c.getAccID());
				}
			}
			if (ServerConstants.USE_PARANOIA && ParanoiaConstants.PARANOIA_CHAT_LOGGER && ParanoiaConstants.LOG_GENERAL_CHAT) {
				MapleLogger.printFormatted(MapleLogger.PARANOIA_CHAT, "[General] [" + c.getPlayer().getName() + "] " + s);
			}
			if (!chr.isHidden()) {
				if (s.length() <= ServerConstants.MAX_CHAT_MESSAGE_LENGTH) {
					chr.getMap().broadcastMessage(MaplePacketCreator.getChatText(chr.getId(), s, (chr.isGM() && chr.getGMText()), slea.readByte()));
				} else {
					chr.dropMessage("Your message was too long.");
				}
			} else {
				chr.getMap().broadcastGMMessage(MaplePacketCreator.getChatText(chr.getId(), s, (chr.isGM() && chr.getGMText()), slea.readByte()));
			}
		}
	}
}
