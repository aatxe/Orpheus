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
package gm.server.handler;

import client.MapleCharacter;
import gm.GMPacketCreator;
import gm.GMPacketHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.server.Channel;
import net.server.Server;
import org.apache.mina.core.session.IoSession;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * 
 * @author kevintjuh93
 */
public class PlayerListHandler implements GMPacketHandler {

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, IoSession session) {
		List<String> playerList = new ArrayList<String>();
		for (Channel ch : Server.getInstance().getAllChannels()) {
			Collection<MapleCharacter> list = ch.getPlayerStorage().getAllCharacters();
			synchronized (list) {
				for (MapleCharacter chr : list) {
					if (!chr.isGM()) {
						playerList.add(chr.getName());
					}
				}
			}
		}
		session.write(GMPacketCreator.sendPlayerList(playerList));
	}
}
