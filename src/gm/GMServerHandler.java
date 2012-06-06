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

import gm.server.GMServer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericSeekableLittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;

public class GMServerHandler extends IoHandlerAdapter {

	GMPacketProcessor processor;
	IoSession session;

	public GMServerHandler() {
		this.processor = new GMPacketProcessor();
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		super.messageSent(session, message);
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		GMServer.getInstance().removeOutGame((String) session.getAttribute("NAME"));
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		this.session = session;
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		synchronized (session) {
			GMServer.getInstance().removeOutGame((String) session.getAttribute("NAME"));
		}
		super.sessionClosed(session);
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		byte[] content = (byte[]) message;
		SeekableLittleEndianAccessor slea = new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream(content));
		short opcode = slea.readShort();
		GMPacketHandler packetHandler = processor.getHandler(opcode);
		if (packetHandler != null) {
			try {
				packetHandler.handlePacket(slea, session);
			} catch (Throwable t) {
			}
		}
	}

	@Override
	public void sessionIdle(final IoSession session, final IdleStatus status) throws Exception {
		super.sessionIdle(session, status);
	}
}