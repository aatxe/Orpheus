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
package net.server;

import java.io.File;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import client.MapleCharacter;
import constants.ServerConstants;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import tools.DatabaseConnection;
import tools.Output;
import net.MaplePacket;
import net.MapleServerHandler;
import net.PacketProcessor;
import net.mina.MapleCodecFactory;
import provider.MapleDataProviderFactory;
import scripting.event.EventScriptManager;
import server.TimerManager;
import server.maps.MapleMapFactory;
import tools.MaplePacketCreator;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import server.events.gm.MapleEvent;
import server.expeditions.MapleExpedition;
import server.expeditions.MapleExpeditionType;
import server.maps.HiredMerchant;
import server.maps.MapleMap;

public final class Channel {

	private int port = 7575;
	private PlayerStorage players = new PlayerStorage();
	private byte world, channel;
	private IoAcceptor acceptor;
	private String ip, serverMessage;
	private MapleMapFactory mapFactory;
	private EventScriptManager eventSM;
	private Map<Integer, HiredMerchant> hiredMerchants = new HashMap<Integer, HiredMerchant>();
	private ReentrantReadWriteLock merchant_lock = new ReentrantReadWriteLock(true);
	private EnumMap<MapleExpeditionType, MapleExpedition> expeditions = new EnumMap<MapleExpeditionType, MapleExpedition>(MapleExpeditionType.class);
	private MapleEvent event;
	private boolean finishedShutdown = false;

	public Channel(final byte world, final byte channel) {
		this.world = world;
		this.channel = channel;
		this.mapFactory = new MapleMapFactory(MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Map.wz")), MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")), world, channel);

		try {
			eventSM = new EventScriptManager(this, ServerConstants.EVENTS.split(" "));
			Connection c = DatabaseConnection.getConnection();
			PreparedStatement ps = c.prepareStatement("UPDATE accounts SET loggedin = 0");
			ps.executeUpdate();
			ps.close();
			ps = c.prepareStatement("UPDATE characters SET HasMerchant = 0");
			ps.executeUpdate();
			ps.close();
			port = 7575 + this.channel - 1;
			port += (world * 100);
			ip = ServerConstants.HOST + ":" + port;
			IoBuffer.setUseDirectBuffer(false);
			IoBuffer.setAllocator(new SimpleBufferAllocator());
			acceptor = new NioSocketAcceptor();
			TimerManager.getInstance().register(new respawnMaps(), 10000);
			acceptor.setHandler(new MapleServerHandler(PacketProcessor.getProcessor(), channel, world));
			acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);
			acceptor.getFilterChain().addLast("codec", (IoFilter) new ProtocolCodecFilter(new MapleCodecFactory()));
			acceptor.bind(new InetSocketAddress(port));
			((SocketSessionConfig) acceptor.getSessionConfig()).setTcpNoDelay(true);

			eventSM.init();
			Output.print("World " + world + ": Channel " + getId() + ": Listening on port " + port + ".");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public final void shutdown() {
		try {
			Output.print("Server is now shutting down channel " + channel + " on world" + world + ".");

			closeAllMerchants();
			players.disconnectAll();
			acceptor.unbind();

			finishedShutdown = true;
			Output.print("Channel " + channel + " on world" + world + " has been shut down.");
		} catch (Exception e) {
			Output.print("Channel " + channel + " on world" + world + " has failed to shut down.\r\n" + e);
		}
	}

	public void closeAllMerchants() {
		WriteLock wlock = merchant_lock.writeLock();
		wlock.lock();
		try {
			final Iterator<HiredMerchant> hmit = hiredMerchants.values().iterator();
			while (hmit.hasNext()) {
				hmit.next().forceClose();
				hmit.remove();
			}
		} catch (Exception e) {
		} finally {
			wlock.unlock();
		}
	}

	public MapleMapFactory getMapFactory() {
		return mapFactory;
	}

	public int getWorld() {
		return world;
	}

	public void addPlayer(MapleCharacter chr) {
		players.addPlayer(chr);
		chr.announce(MaplePacketCreator.serverMessage(serverMessage));
	}

	public PlayerStorage getPlayerStorage() {
		return players;
	}

	public void removePlayer(MapleCharacter chr) {
		players.removePlayer(chr.getId());
	}

	public int getConnectedClients() {
		return players.getAllCharacters().size();
	}

	public void broadcastPacket(MaplePacket data) {
		for (MapleCharacter chr : players.getAllCharacters()) {
			chr.announce(data);
		}
	}

	public final byte getId() {
		return channel;
	}

	public String getIP() {
		return ip;
	}

	public MapleEvent getEvent() {
		return event;
	}

	public void setEvent(MapleEvent event) {
		this.event = event;
	}

	public EventScriptManager getEventSM() {
		return eventSM;
	}

	public void broadcastGMPacket(MaplePacket data) {
		for (MapleCharacter chr : players.getAllCharacters()) {
			if (chr.isGM()) {
				chr.announce(data);
			}
		}
	}

	public void broadcastGMPacket(MaplePacket data, String exclude) {
		for (MapleCharacter chr : players.getAllCharacters()) {
			if (chr.isGM() && !chr.getName().equals(exclude)) {
				chr.announce(data);
			}
		}
	}

	public void yellowWorldMessage(String msg) {
		for (MapleCharacter mc : getPlayerStorage().getAllCharacters()) {
			mc.announce(MaplePacketCreator.sendYellowTip(msg));
		}
	}

	public void worldMessage(String msg) {
		for (MapleCharacter mc : getPlayerStorage().getAllCharacters()) {
			mc.dropMessage(msg);
		}
	}

	public List<MapleCharacter> getPartyMembers(MapleParty party) {
		List<MapleCharacter> partym = new ArrayList<MapleCharacter>(8);
		for (MaplePartyCharacter partychar : party.getMembers()) {
			if (partychar.getChannel() == getId()) {
				MapleCharacter chr = getPlayerStorage().getCharacterByName(partychar.getName());
				if (chr != null) {
					partym.add(chr);
				}
			}
		}
		return partym;

	}

	public class respawnMaps implements Runnable {

		@Override
		public void run() {
			for (Entry<Integer, MapleMap> map : mapFactory.getMaps().entrySet()) {
				map.getValue().respawn();
			}
		}
	}

	public Map<Integer, HiredMerchant> getHiredMerchants() {
		return hiredMerchants;
	}

	public void addHiredMerchant(int chrid, HiredMerchant hm) {
		WriteLock wlock = merchant_lock.writeLock();
		wlock.lock();
		try {
			hiredMerchants.put(chrid, hm);
		} finally {
			wlock.unlock();
		}
	}

	public void removeHiredMerchant(int chrid) {
		WriteLock wlock = merchant_lock.writeLock();
		wlock.lock();
		try {
			hiredMerchants.remove(chrid);
		} finally {
			wlock.unlock();
		}
	}

	public int[] multiBuddyFind(int charIdFrom, int[] characterIds) {
		List<Integer> ret = new ArrayList<Integer>(characterIds.length);
		PlayerStorage playerStorage = getPlayerStorage();
		for (int characterId : characterIds) {
			MapleCharacter chr = playerStorage.getCharacterById(characterId);
			if (chr != null) {
				if (chr.getBuddylist().containsVisible(charIdFrom)) {
					ret.add(characterId);
				}
			}
		}
		int[] retArr = new int[ret.size()];
		int pos = 0;
		for (Integer i : ret) {
			retArr[pos++] = i.intValue();
		}
		return retArr;
	}

	public boolean hasExpedition(MapleExpeditionType type) {
		return expeditions.containsKey(type);
	}

	public void addExpedition(MapleExpeditionType type, MapleExpedition exped) {
		expeditions.put(type, exped);
	}

	public MapleExpedition getExpedition(MapleExpeditionType type) {
		return expeditions.get(type);
	}

	public boolean isConnected(String name) {
		return getPlayerStorage().getCharacterByName(name) != null;
	}

	public boolean finishedShutdown() {
		return finishedShutdown;
	}

	public void setServerMessage(String message) {
		this.serverMessage = message;
		broadcastPacket(MaplePacketCreator.serverMessage(message));
	}
}