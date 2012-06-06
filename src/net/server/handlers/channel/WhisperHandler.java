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
package net.server.handlers.channel;

import client.MapleCharacter;
import client.MapleClient;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import paranoia.BlacklistHandler;
import constants.ParanoiaConstants;
import constants.ServerConstants;
import net.AbstractMaplePacketHandler;
import net.server.World;
import tools.DatabaseConnection;
import tools.MapleLogger;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * 
 * @author Matze
 */
public final class WhisperHandler extends AbstractMaplePacketHandler {
	@SuppressWarnings("unused")
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		byte mode = slea.readByte();
		if (mode == 6) { // whisper
			String recipient = slea.readMapleAsciiString();
			String text = slea.readMapleAsciiString();
			MapleCharacter player = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient);
			if (player != null) {
				if (ServerConstants.USE_PARANOIA && ParanoiaConstants.ENABLE_BLACKLISTING && ParanoiaConstants.LOG_BLACKLIST_CHAT) {
					if (BlacklistHandler.isBlacklisted(c.getAccID())) {
						BlacklistHandler.printBlacklistLog("[Whisper] [" + c.getPlayer().getName() + " > " + recipient + "] " + text, c.getAccID());
					}
				}
				if (ServerConstants.USE_PARANOIA && ParanoiaConstants.PARANOIA_CHAT_LOGGER && ParanoiaConstants.LOG_WHISPERS) {
					MapleLogger.printFormatted(MapleLogger.PARANOIA_CHAT, "[Whisper] [" + c.getPlayer().getName() + " > " + recipient + "] " + text);
				}
				if (text.length() <= ServerConstants.MAX_CHAT_MESSAGE_LENGTH) {
					player.getClient().announce(MaplePacketCreator.getWhisper(c.getPlayer().getName(), c.getChannel(), text));
					c.announce(MaplePacketCreator.getWhisperReply(recipient, (byte) 1));
				} else {
					player.dropMessage("Your message was too long.");
				}
			} else {// not found
				World world = c.getWorldServer();
				if (world.isConnected(recipient)) {
					world.whisper(c.getPlayer().getName(), recipient, c.getChannel(), text);
					c.announce(MaplePacketCreator.getWhisperReply(recipient, (byte) 1));
				} else {
					c.announce(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
				}
			}
		} else if (mode == 5) { // - /find
			String recipient = slea.readMapleAsciiString();
			MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient);
			if (victim != null && c.getPlayer().gmLevel() >= victim.gmLevel()) {
				if (victim.getCashShop().isOpened()) {
					c.announce(MaplePacketCreator.getFindReply(victim.getName(), -1, 2));
					// } else if (victim.inMTS()) {
					// c.announce(MaplePacketCreator.getFindReply(victim.getName(),
					// -1, 0));
				} else {
					c.announce(MaplePacketCreator.getFindReply(victim.getName(), victim.getMap().getId(), 1));
				}
			} else { // not found
				try {
					PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT gm FROM characters WHERE name = ?");
					ps.setString(1, recipient);
					ResultSet rs = ps.executeQuery();
					if (rs.next()) {
						if (rs.getInt("gm") > c.getPlayer().gmLevel()) {
							c.announce(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
							return;
						}
					}
					rs.close();
					ps.close();
					byte channel = (byte) (c.getWorldServer().find(recipient) - 1);
					if (channel > -1) {
						c.announce(MaplePacketCreator.getFindReply(recipient, channel, 3));
					} else {
						c.announce(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} else if (mode == 0x44) {
			// Buddy find?
		}
	}
}
