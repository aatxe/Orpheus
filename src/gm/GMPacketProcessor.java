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
package gm;

import tools.Output;
import gm.server.handler.*;

/**
 * 
 * @author kevintjuh93
 */
public final class GMPacketProcessor {
	private GMPacketHandler[] handlers;

	public GMPacketProcessor() {
		int maxRecvOp = 0;
		for (GMRecvOpcode op : GMRecvOpcode.values()) {
			if (op.getValue() > maxRecvOp) {
				maxRecvOp = op.getValue();
			}
		}
		handlers = new GMPacketHandler[maxRecvOp + 1];
		reset();
	}

	public GMPacketHandler getHandler(short packetId) {
		if (packetId > handlers.length) {
			return null;
		}
		GMPacketHandler handler = handlers[packetId];
		if (handler != null) {
			return handler;
		}
		return null;
	}

	public void registerHandler(GMRecvOpcode code, GMPacketHandler handler) {
		try {
			handlers[code.getValue()] = handler;
		} catch (ArrayIndexOutOfBoundsException e) {
			Output.print("Error registering handler - " + code.name());
		}
	}

	public void reset() {
		handlers = new GMPacketHandler[handlers.length];
		registerHandler(GMRecvOpcode.LOGIN, new LoginHandler());
		registerHandler(GMRecvOpcode.GM_CHAT, new ChatHandler());
		registerHandler(GMRecvOpcode.PLAYER_LIST, new PlayerListHandler());
		registerHandler(GMRecvOpcode.COMMAND, new CommandHandler());
	}
}
