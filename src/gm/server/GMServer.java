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
package gm.server;

import gm.GMPacketCreator;
import gm.GMServerHandler;
import gm.mina.GMCodecFactory;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.MaplePacket;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import tools.Output;

/**
 * 
 * @author kevintjuh93
 */
public class GMServer {

	private IoAcceptor acceptor;
	private Map<String, IoSession> outGame;// LOL
	private Map<String, IoSession> inGame;
	private static GMServer instance;
	public final static String KEYWORD = "MOOPLEDEV";

	public static GMServer getInstance() {
		if (instance == null) {
			instance = new GMServer();
		}
		return instance;
	}

	public GMServer() {
		IoBuffer.setUseDirectBuffer(false);
		IoBuffer.setAllocator(new SimpleBufferAllocator());
		acceptor = new NioSocketAcceptor();
		acceptor.setHandler(new GMServerHandler());
		acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);
		acceptor.getFilterChain().addLast("codec", (IoFilter) new ProtocolCodecFilter(new GMCodecFactory()));
		((SocketSessionConfig) acceptor.getSessionConfig()).setTcpNoDelay(true);
		try {
			acceptor.bind(new InetSocketAddress(5252));
			Output.print("GM Server: Listening on port 5252.");
		} catch (Exception e) {
			Output.print("Failed to bind the GM server to port 5252.");
		}
		outGame = new HashMap<String, IoSession>();
		inGame = new HashMap<String, IoSession>();
	}

	public void broadcastOutGame(MaplePacket packet, String exclude) {
		for (IoSession ss : outGame.values()) {
			if (!ss.getAttribute("NAME").equals(exclude)) {
				ss.write(packet);
			}
		}
	}

	public void broadcastInGame(MaplePacket packet) {
		for (IoSession ss : inGame.values()) {
			ss.write(packet);
		}
	}

	public void addInGame(String name, IoSession session) {
		if (!inGame.containsKey(name)) {
			broadcastOutGame(GMPacketCreator.chat(name + " has logged in."), null);
			broadcastOutGame(GMPacketCreator.addUser(name), null);
		}
		inGame.put(name, session);// replace old one (:
	}

	public void addOutGame(String name, IoSession session) {
		outGame.put(name, session);
	}

	public void removeInGame(String name) {
		if (inGame.remove(name) != null) {
			broadcastOutGame(GMPacketCreator.removeUser(name), null);
			broadcastOutGame(GMPacketCreator.chat(name + " has logged out."), null);
		}
	}

	public void removeOutGame(String name) {
		IoSession ss = outGame.remove(name);
		if (ss != null) {
			if (!ss.isClosing()) {
				broadcastOutGame(GMPacketCreator.removeUser(name), null);
				broadcastOutGame(GMPacketCreator.chat(name + " has logged out."), null);
			}
		}
	}

	public boolean contains(String name) {
		return inGame.containsKey(name) || outGame.containsKey(name);
	}

	public final void closeAllSessions() {
		try {// I CAN AND IT'S FREE BITCHES
			Collection<IoSession> sss = Collections.synchronizedCollection(outGame.values());
			synchronized (sss) {
				final Iterator<IoSession> outIt = sss.iterator();
				while (outIt.hasNext()) {
					outIt.next().close(true);
					outIt.remove();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<String> getUserList(String exclude) {
		List<String> returnList = new ArrayList<String>(outGame.keySet());
		returnList.remove(exclude);// Already sent in LoginHandler (So you are
									// first on the list (:
		returnList.addAll(inGame.keySet());
		return returnList;
	}

	public final void shutdown() {// nothing to save o.o
		try {
			closeAllSessions();
			acceptor.unbind();
			Output.print("GM Server is now offline.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}