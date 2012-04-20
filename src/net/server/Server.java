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
package net.server;

import client.MapleCharacter;
import client.SkillFactory;
import constants.ServerConstants;
import gm.GMPacketCreator;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import net.MaplePacket;
import tools.DatabaseConnection;
import net.MapleServerHandler;
import net.PacketProcessor;
import net.mina.MapleCodecFactory;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildCharacter;
import gm.server.GMServer;
import server.TimerManager;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import server.CashShop.CashItemFactory;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.Pair;

public class Server implements Runnable {

    private IoAcceptor acceptor;
    private List<Map<Byte, String>> channels = new LinkedList<Map<Byte, String>>();
    private List<World> worlds = new ArrayList<World>();
    private Properties subnetInfo = new Properties();
    private static Server instance = null;
    private ArrayList<Map<Byte, AtomicInteger>> load = new ArrayList<Map<Byte, AtomicInteger>>();
    private List<Pair<Byte, String>> worldRecommendedList = new LinkedList<Pair<Byte, String>>();
    private Map<Integer, MapleGuild> guilds = new LinkedHashMap<Integer, MapleGuild>();
    private PlayerBuffStorage buffStorage = new PlayerBuffStorage();
    private Map<Integer, MapleAlliance> alliances = new LinkedHashMap<Integer, MapleAlliance>();
    private boolean online = false;

    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    public boolean isOnline() {
        return online;
    }

    public List<Pair<Byte, String>> worldRecommendedList() {
        return worldRecommendedList;
    }

    public void removeChannel(byte worldid, byte channel) {
        channels.remove(channel);
        if (load.contains(worldid)) {
            load.get(worldid).remove(channel);
        }
        World world = worlds.get(worldid);
        if (world != null) {
            world.removeChannel(channel);
        }
    }

    public Channel getChannel(byte world, byte channel) {
        return worlds.get(world).getChannel(channel);
    }

    public List<Channel> getChannelsFromWorld(byte world) {
        return worlds.get(world).getChannels();
    }

    public List<Channel> getAllChannels() {
        List<Channel> channelz = new ArrayList<Channel>();
        for (World world : worlds) {
            for (Channel ch : world.getChannels()) {
                channelz.add(ch);
            }
        }

        return channelz;
    }

    public String getIP(byte world, byte channel) {
        return channels.get(world).get(channel);
    }

    @Override
    public void run() {
        Properties p = new Properties();
        try {
            p.load(new FileInputStream("moople.ini"));
        } catch (Exception e) {
            System.out.println("Please start create_server.bat");
            System.exit(0);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(shutdown(false)));
        DatabaseConnection.getConnection();
        IoBuffer.setUseDirectBuffer(false);
        IoBuffer.setAllocator(new SimpleBufferAllocator());
        acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("codec", (IoFilter) new ProtocolCodecFilter(new MapleCodecFactory()));
        TimerManager tMan = TimerManager.getInstance();
        tMan.start();
        tMan.register(tMan.purge(), 300000);//Purging ftw...
        tMan.register(new RankingWorker(), ServerConstants.RANKING_INTERVAL);

        try {
            for (byte i = 0; i < Byte.parseByte(p.getProperty("worlds")); i++) {
                System.out.println("Starting world " + i);
                World world = new World(i,
                        Byte.parseByte(p.getProperty("flag" + i)),
                        p.getProperty("eventmessage" + i),
                        Byte.parseByte(p.getProperty("exprate" + i)),
                        Byte.parseByte(p.getProperty("droprate" + i)),
                        Byte.parseByte(p.getProperty("mesorate" + i)),
                        Byte.parseByte(p.getProperty("bossdroprate" + i)));//ohlol
                
                worldRecommendedList.add(new Pair<Byte, String>(i, p.getProperty("whyamirecommended" + i)));
                worlds.add(world);
                channels.add(new LinkedHashMap<Byte, String>());
                load.add(new LinkedHashMap<Byte, AtomicInteger>());
                for (byte j = 0; j < Byte.parseByte(p.getProperty("channels" + i)); j++) {
                    byte channelid = (byte) (j + 1);
                    Channel channel = new Channel(i, channelid);
                    world.addChannel(channel);
                    channels.get(i).put(channelid, channel.getIP());
                    load.get(i).put(channelid, new AtomicInteger());
                }
                world.setServerMessage(p.getProperty("servermessage" + i));
                System.out.println("Finished loading world " + i + "\r\n");
            }
        } catch (Exception e) {
            System.out.println("Error in moople.ini, start CreateINI.bat to re-make the file.");
            e.printStackTrace();//For those who get errors
            System.exit(0);
        }

        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);
        acceptor.setHandler(new MapleServerHandler(PacketProcessor.getProcessor()));
        try {
            acceptor.bind(new InetSocketAddress(8484));
        } catch (IOException ex) {
        }
        System.out.println("Listening on port 8484\r\n");
        System.out.print("Loading server");
        ScheduledFuture<?> loldot = null;//rofl
        loldot = tMan.register(new Runnable() {

            @Override
            public void run() {
                System.out.print(".");
            }
        }, 500, 500);
        SkillFactory.loadAllSkills();
        CashItemFactory.getSpecialCashItems();//just load who cares o.o
        MapleItemInformationProvider.getInstance().getAllItems();
        if (Boolean.parseBoolean(p.getProperty("gmserver"))) {
            GMServer.getInstance();
        }
        loldot.cancel(true);
        System.out.println("\r\nServer is now online.");
        online = true;
    }

    public void shutdown() {
        TimerManager.getInstance().stop();
        acceptor.unbind();
        System.out.println("Server offline.");
        System.exit(0);// BOEIEND :D
    }

    public static void main(String args[]) {
        Server.getInstance().run();
    }

    public Properties getSubnetInfo() {
        return subnetInfo;
    }

    public Map<Byte, AtomicInteger> getLoad(byte i) {
        return load.get(i);
    }

    public List<Map<Byte, AtomicInteger>> getLoad() {
        return load;
    }

    public MapleAlliance getAlliance(int id) {
        synchronized (alliances) {
            if (alliances.containsKey(id)) {
                return alliances.get(id);
            }
            return null;
        }
    }

    public void addAlliance(int id, MapleAlliance alliance) {
        synchronized (alliances) {
            if (!alliances.containsKey(id)) {
                alliances.put(id, alliance);
            }
        }
    }

    public void disbandAlliance(int id) {
        synchronized (alliances) {
            MapleAlliance alliance = alliances.get(id);
            if (alliance != null) {
                for (Integer gid : alliance.getGuilds()) {
                    guilds.get(gid).setAllianceId(0);
                }
                alliances.remove(id);
            }
        }
    }

    public void allianceMessage(int id, MaplePacket packet, int exception, int guildex) {
        MapleAlliance alliance = alliances.get(id);
        if (alliance != null) {
            for (Integer gid : alliance.getGuilds()) {
                if (guildex == gid) {
                    continue;
                }
                MapleGuild guild = guilds.get(gid);
                if (guild != null) {
                    guild.broadcast(packet, exception);
                }
            }
        }
    }

    public boolean addGuildtoAlliance(int aId, int guildId) {
        MapleAlliance alliance = alliances.get(aId);
        if (alliance != null) {
            alliance.addGuild(guildId);
            return true;
        }
        return false;
    }

    public boolean removeGuildFromAlliance(int aId, int guildId) {
        MapleAlliance alliance = alliances.get(aId);
        if (alliance != null) {
            alliance.removeGuild(guildId);
            return true;
        }
        return false;
    }

    public boolean setAllianceRanks(int aId, String[] ranks) {
        MapleAlliance alliance = alliances.get(aId);
        if (alliance != null) {
            alliance.setRankTitle(ranks);
            return true;
        }
        return false;
    }

    public boolean setAllianceNotice(int aId, String notice) {
        MapleAlliance alliance = alliances.get(aId);
        if (alliance != null) {
            alliance.setNotice(notice);
            return true;
        }
        return false;
    }

    public boolean increaseAllianceCapacity(int aId, int inc) {
        MapleAlliance alliance = alliances.get(aId);
        if (alliance != null) {
            alliance.increaseCapacity(inc);
            return true;
        }
        return false;
    }

    public Set<Byte> getChannelServer(byte world) {
        return new HashSet<Byte>(channels.get(world).keySet());
    }

    public byte getHighestChannelId() {
        byte highest = 0;
        for (Byte channel : channels.get(0).keySet()) {
            if (channel != null && channel.intValue() > highest) {
                highest = channel.byteValue();
            }
        }
        return highest;
    }

    public int createGuild(int leaderId, String name) {
        return MapleGuild.createGuild(leaderId, name);
    }

    public MapleGuild getGuild(int id, MapleGuildCharacter mgc) {
        synchronized (guilds) {
            if (guilds.get(id) != null) {
                return guilds.get(id);
            }
            if (mgc == null) {
                return null;
            }
            MapleGuild g = new MapleGuild(mgc);
            if (g.getId() == -1) {
                return null;
            }
            guilds.put(id, g);
            return g;
        }
    }

    public void clearGuilds() {//remake
        synchronized (guilds) {
            guilds.clear();
        }
        //for (List<Channel> world : worlds.values()) {
        //reloadGuildCharacters();

    }

    public void setGuildMemberOnline(MapleGuildCharacter mgc, boolean bOnline, byte channel) {
        MapleGuild g = getGuild(mgc.getGuildId(), mgc);
        g.setOnline(mgc.getId(), bOnline, channel);
    }

    public int addGuildMember(MapleGuildCharacter mgc) {
        MapleGuild g = guilds.get(mgc.getGuildId());
        if (g != null) {
            return g.addGuildMember(mgc);
        }
        return 0;
    }

    public boolean setGuildAllianceId(int gId, int aId) {
        MapleGuild guild = guilds.get(gId);
        if (guild != null) {
            guild.setAllianceId(aId);
            return true;
        }
        return false;
    }

    public void leaveGuild(MapleGuildCharacter mgc) {
        MapleGuild g = guilds.get(mgc.getGuildId());
        if (g != null) {
            g.leaveGuild(mgc);
        }
    }

    public void guildChat(int gid, String name, int cid, String msg) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            g.guildChat(name, cid, msg);
        }
    }

    public void changeRank(int gid, int cid, int newRank) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            g.changeRank(cid, newRank);
        }
    }

    public void expelMember(MapleGuildCharacter initiator, String name, int cid) {
        MapleGuild g = guilds.get(initiator.getGuildId());
        if (g != null) {
            g.expelMember(initiator, name, cid);
        }
    }

    public void setGuildNotice(int gid, String notice) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            g.setGuildNotice(notice);
        }
    }

    public void memberLevelJobUpdate(MapleGuildCharacter mgc) {
        MapleGuild g = guilds.get(mgc.getGuildId());
        if (g != null) {
            g.memberLevelJobUpdate(mgc);
        }
    }

    public void changeRankTitle(int gid, String[] ranks) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            g.changeRankTitle(ranks);
        }
    }

    public void setGuildEmblem(int gid, short bg, byte bgcolor, short logo, byte logocolor) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            g.setGuildEmblem(bg, bgcolor, logo, logocolor);
        }
    }

    public void disbandGuild(int gid) {
        synchronized (guilds) {
            MapleGuild g = guilds.get(gid);
            g.disbandGuild();
            guilds.remove(gid);
        }
    }

    public boolean increaseGuildCapacity(int gid) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            return g.increaseCapacity();
        }
        return false;
    }

    public void gainGP(int gid, int amount) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            g.gainGP(amount);
        }
    }

    public PlayerBuffStorage getPlayerBuffStorage() {
        return buffStorage;
    }

    public void deleteGuildCharacter(MapleGuildCharacter mgc) {
        setGuildMemberOnline(mgc, false, (byte) -1);
        if (mgc.getGuildRank() > 1) {
            leaveGuild(mgc);
        } else {
            disbandGuild(mgc.getGuildId());
        }
    }

    public void reloadGuildCharacters(byte world) {
        World worlda = getWorld(world);
        for (MapleCharacter mc : worlda.getPlayerStorage().getAllCharacters()) {
            if (mc.getGuildId() > 0) {
                setGuildMemberOnline(mc.getMGC(), true, worlda.getId());
                memberLevelJobUpdate(mc.getMGC());
            }
        }
        worlda.reloadGuildSummary();
    }

    public void broadcastMessage(byte world, MaplePacket packet) {
        for (Channel ch : getChannelsFromWorld(world)) {
            ch.broadcastPacket(packet);
        }
    }

    public World getWorld(int id) {
        return worlds.get(id);
    }

    public List<World> getWorlds() {
        return worlds;
    }

    public void gmChat(String message, String exclude) {
        GMServer server = GMServer.getInstance();
        server.broadcastInGame(MaplePacketCreator.serverNotice(6, message));
        server.broadcastOutGame(GMPacketCreator.chat(message), exclude);
    }

    public final Runnable shutdown(final boolean restart) {//only once :D
        return new Runnable() {

            @Override
            public void run() {
                System.out.println((restart ? "Restarting" : "Shutting down") + " the server!\r\n");
                for (World w : getWorlds()) {
                    w.shutdown();
                }
                for (World w : getWorlds()) {
                    while (w.getPlayerStorage().getAllCharacters().size() > 0) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            System.err.println("FUCK MY LIFE");
                        }
                    }
                }
                for (Channel ch : getAllChannels()) {
                    while (ch.getConnectedClients() > 0) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            System.err.println("FUCK MY LIFE");
                        }
                    }
                }

                TimerManager.getInstance().purge();
                TimerManager.getInstance().stop();

                for (Channel ch : getAllChannels()) {
                    while (!ch.finishedShutdown()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            System.err.println("FUCK MY LIFE");
                        }
                    }
                }
                worlds.clear();
                worlds = null;
                channels.clear();
                channels = null;
                worldRecommendedList.clear();
                worldRecommendedList = null;
                load.clear();
                load = null;
                System.out.println("Worlds + Channels are offline.");
                acceptor.unbind();
                acceptor = null;
                if (!restart) {
                    System.exit(0);
                } else {
                    System.out.println("\r\nRestarting the server....\r\n");
                    try {
                        instance.finalize();//FUU I CAN AND IT'S FREE
                    } catch (Throwable ex) {
                    }
                    instance = null;
                    System.gc();
                    getInstance().run();//DID I DO EVERYTHING?! D:
                }
            }
        };
    }
}