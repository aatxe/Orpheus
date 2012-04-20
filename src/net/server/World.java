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

import client.BuddyList;
import client.BuddyList.BuddyAddResult;
import client.BuddyList.BuddyOperation;
import client.BuddylistEntry;
import client.MapleCharacter;
import client.MapleFamily;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.MaplePacket;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildCharacter;
import net.server.guild.MapleGuildSummary;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;

/**
 *
 * @author kevintjuh93
 */
public class World {

    private byte id, flag, exprate, droprate, mesorate, bossdroprate;
    private String eventmsg;
    private List<Channel> channels = new ArrayList<Channel>();
    private Map<Integer, MapleParty> parties = new HashMap<Integer, MapleParty>();
    private AtomicInteger runningPartyId = new AtomicInteger();
    private Map<Integer, MapleMessenger> messengers = new HashMap<Integer, MapleMessenger>();
    private AtomicInteger runningMessengerId = new AtomicInteger();
    private Map<Integer, MapleFamily> families = new LinkedHashMap<Integer, MapleFamily>();
    private Map<Integer, MapleGuildSummary> gsStore = new HashMap<Integer, MapleGuildSummary>();
    private PlayerStorage players = new PlayerStorage();

    public World(byte world, byte flag, String eventmsg, byte exprate, byte droprate, byte mesorate, byte bossdroprate) {
        this.id = world;
        this.flag = flag;
        this.eventmsg = eventmsg;
        this.exprate = exprate;
        this.droprate = droprate;
        this.mesorate = mesorate;
        this.bossdroprate = bossdroprate;
        runningPartyId.set(1);
        runningMessengerId.set(1);
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public Channel getChannel(byte channel) {
        return channels.get(channel - 1);
    }

    public void addChannel(Channel channel) {
        channels.add(channel);
    }

    public void removeChannel(byte channel) {
        channels.remove(channel);
    }

    public void setFlag(byte b) {
        this.flag = b;
    }

    public byte getFlag() {
        return flag;
    }

    public String getEventMessage() {
        return eventmsg;
    }

    public byte getExpRate() {
        return exprate;
    }

    public void setExpRate(byte exp) {
        this.exprate = exp;
    }

    public byte getDropRate() {
        return droprate;
    }

    public void setDropRate(byte drop) {
        this.droprate = drop;
    }

    public byte getMesoRate() {
        return mesorate;
    }

    public void setMesoRate(byte meso) {
        this.mesorate = meso;
    }

    public byte getBossDropRate() {
        return bossdroprate;
    }

    public PlayerStorage getPlayerStorage() {
        return players;
    }

    public void removePlayer(MapleCharacter chr) {
        channels.get(chr.getClient().getChannel() - 1).removePlayer(chr);
        players.removePlayer(chr.getId());
    }

    public byte getId() {
        return id;
    }

    public void addFamily(int id, MapleFamily f) {
        synchronized (families) {
            if (!families.containsKey(id)) {
                families.put(id, f);
            }
        }
    }

    public MapleFamily getFamily(int id) {
        synchronized (families) {
            if (families.containsKey(id)) {
                return families.get(id);
            }
            return null;
        }
    }

    public MapleGuild getGuild(MapleGuildCharacter mgc) {
        int gid = mgc.getGuildId();
        MapleGuild g = null;
        g = Server.getInstance().getGuild(gid, mgc);
        if (gsStore.get(gid) == null) {
            gsStore.put(gid, new MapleGuildSummary(g));
        }
        return g;
    }

    public MapleGuildSummary getGuildSummary(int gid) {
        if (gsStore.containsKey(gid)) {
            return gsStore.get(gid);
        } else {
            MapleGuild g = Server.getInstance().getGuild(gid, null);
            if (g != null) {
                gsStore.put(gid, new MapleGuildSummary(g));
            }
            return gsStore.get(gid);
        }
    }

    public void updateGuildSummary(int gid, MapleGuildSummary mgs) {
        gsStore.put(gid, mgs);
    }

    public void reloadGuildSummary() {
        MapleGuild g;
        Server server = Server.getInstance();
        for (int i : gsStore.keySet()) {
            g = server.getGuild(i, null);
            if (g != null) {
                gsStore.put(i, new MapleGuildSummary(g));
            } else {
                gsStore.remove(i);
            }
        }
    }

    public void setGuildAndRank(List<Integer> cids, int guildid, int rank, int exception) {
        for (int cid : cids) {
            if (cid != exception) {
                setGuildAndRank(cid, guildid, rank);
            }
        }
    }

    public void setOfflineGuildStatus(int guildid, byte guildrank, int cid) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET guildid = ?, guildrank = ? WHERE id = ?");
            ps.setInt(1, guildid);
            ps.setInt(2, guildrank);
            ps.setInt(3, cid);
            ps.execute();
            ps.close();
            ps = null;
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void setGuildAndRank(int cid, int guildid, int rank) {
        MapleCharacter mc = getPlayerStorage().getCharacterById(cid);
        if (mc == null) {
            return;
        }
        boolean bDifferentGuild;
        if (guildid == -1 && rank == -1) {
            bDifferentGuild = true;
        } else {
            bDifferentGuild = guildid != mc.getGuildId();
            mc.setGuildId(guildid);
            mc.setGuildRank(rank);
            mc.saveGuildStatus();
        }
        if (bDifferentGuild) {
            mc.getMap().broadcastMessage(mc, MaplePacketCreator.removePlayerFromMap(cid), false);
            mc.getMap().broadcastMessage(mc, MaplePacketCreator.spawnPlayerMapobject(mc), false);
        }
    }

    public void changeEmblem(int gid, List<Integer> affectedPlayers, MapleGuildSummary mgs) {
        updateGuildSummary(gid, mgs);
        sendPacket(affectedPlayers, MaplePacketCreator.guildEmblemChange(gid, mgs.getLogoBG(), mgs.getLogoBGColor(), mgs.getLogo(), mgs.getLogoColor()), -1);
        setGuildAndRank(affectedPlayers, -1, -1, -1);	//respawn player
    }

    public void sendPacket(List<Integer> targetIds, MaplePacket packet, int exception) {
        MapleCharacter c;
        for (int i : targetIds) {
            if (i == exception) {
                continue;
            }
            c = getPlayerStorage().getCharacterById(i);
            if (c != null) {
                c.getClient().getSession().write(packet);
            }
        }
    }

    public MapleParty createParty(MaplePartyCharacter chrfor) {
        int partyid = runningPartyId.getAndIncrement();
        MapleParty party = new MapleParty(partyid, chrfor);
        parties.put(party.getId(), party);
        return party;
    }

    public MapleParty getParty(int partyid) {
        return parties.get(partyid);
    }

    public MapleParty disbandParty(int partyid) {
        return parties.remove(partyid);
    }

    public void updateParty(MapleParty party, PartyOperation operation, MaplePartyCharacter target) {
        for (MaplePartyCharacter partychar : party.getMembers()) {
            MapleCharacter chr = getPlayerStorage().getCharacterByName(partychar.getName());
            if (chr != null) {
                if (operation == PartyOperation.DISBAND) {
                    chr.setParty(null);
                    chr.setMPC(null);
                } else {
                    chr.setParty(party);
                    chr.setMPC(partychar);
                }
                chr.getClient().getSession().write(MaplePacketCreator.updateParty(chr.getClient().getChannel(), party, operation, target));
            }
        }
        switch (operation) {
            case LEAVE:
            case EXPEL:
                MapleCharacter chr = getPlayerStorage().getCharacterByName(target.getName());
                if (chr != null) {
                    chr.getClient().getSession().write(MaplePacketCreator.updateParty(chr.getClient().getChannel(), party, operation, target));
                    chr.setParty(null);
                    chr.setMPC(null);
                }
        }
    }

    public void updateParty(int partyid, PartyOperation operation, MaplePartyCharacter target) {
        MapleParty party = getParty(partyid);
        if (party == null) {
            throw new IllegalArgumentException("no party with the specified partyid exists");
        }
        switch (operation) {
            case JOIN:
                party.addMember(target);
                break;
            case EXPEL:
            case LEAVE:
                party.removeMember(target);
                break;
            case DISBAND:
                disbandParty(partyid);
                break;
            case SILENT_UPDATE:
            case LOG_ONOFF:
                party.updateMember(target);
                break;
            case CHANGE_LEADER:
                party.setLeader(target);
                break;
            default:
                System.out.println("Unhandeled updateParty operation " + operation.name());
        }
        updateParty(party, operation, target);
    }

    public byte find(String name) {
        byte channel = -1;
        MapleCharacter chr = getPlayerStorage().getCharacterByName(name);
        if (chr != null) {
            channel = chr.getClient().getChannel();
        }
        return channel;
    }

    public byte find(int id) {
        byte channel = -1;
        MapleCharacter chr = getPlayerStorage().getCharacterById(id);
        if (chr != null) {
            channel = chr.getClient().getChannel();
        }
        return channel;
    }

    public void partyChat(MapleParty party, String chattext, String namefrom) {
        for (MaplePartyCharacter partychar : party.getMembers()) {
            if (!(partychar.getName().equals(namefrom))) {
                MapleCharacter chr = getPlayerStorage().getCharacterByName(partychar.getName());
                if (chr != null) {
                    chr.getClient().getSession().write(MaplePacketCreator.multiChat(namefrom, chattext, 1));
                }
            }
        }
    }

    public void buddyChat(int[] recipientCharacterIds, int cidFrom, String nameFrom, String chattext) {
        PlayerStorage playerStorage = getPlayerStorage();
        for (int characterId : recipientCharacterIds) {
            MapleCharacter chr = playerStorage.getCharacterById(characterId);
            if (chr != null) {
                if (chr.getBuddylist().containsVisible(cidFrom)) {
                    chr.getClient().getSession().write(MaplePacketCreator.multiChat(nameFrom, chattext, 0));
                }
            }
        }
    }

    public CharacterIdChannelPair[] multiBuddyFind(int charIdFrom, int[] characterIds) {
        List<CharacterIdChannelPair> foundsChars = new ArrayList<CharacterIdChannelPair>(characterIds.length);
        for (Channel ch : getChannels()) {
            for (int charid : ch.multiBuddyFind(charIdFrom, characterIds)) {
                foundsChars.add(new CharacterIdChannelPair(charid, ch.getId()));
            }
        }
        return foundsChars.toArray(new CharacterIdChannelPair[foundsChars.size()]);
    }

    public MapleMessenger getMessenger(int messengerid) {
        return messengers.get(messengerid);
    }

    public void leaveMessenger(int messengerid, MapleMessengerCharacter target) {
        MapleMessenger messenger = getMessenger(messengerid);
        if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
        int position = messenger.getPositionByName(target.getName());
        messenger.removeMember(target);
        removeMessengerPlayer(messenger, position);
    }

    public void messengerInvite(String sender, int messengerid, String target, byte fromchannel) {
        if (isConnected(target)) {
            MapleMessenger messenger = getPlayerStorage().getCharacterByName(target).getMessenger();
            if (messenger == null) {
                getPlayerStorage().getCharacterByName(target).getClient().getSession().write(MaplePacketCreator.messengerInvite(sender, messengerid));
                MapleCharacter from = getChannel(fromchannel).getPlayerStorage().getCharacterByName(sender);
                from.getClient().getSession().write(MaplePacketCreator.messengerNote(target, 4, 1));
            } else {
                MapleCharacter from = getChannel(fromchannel).getPlayerStorage().getCharacterByName(sender);
                from.getClient().getSession().write(MaplePacketCreator.messengerChat(sender + " : " + target + " is already using Maple Messenger"));
            }
        }
    }

    public void addMessengerPlayer(MapleMessenger messenger, String namefrom, byte fromchannel, int position) {
        for (MapleMessengerCharacter messengerchar : messenger.getMembers()) {
            if (!(messengerchar.getName().equals(namefrom))) {
                MapleCharacter chr = getPlayerStorage().getCharacterByName(messengerchar.getName());
                if (chr != null) {
                    MapleCharacter from = getChannel(fromchannel).getPlayerStorage().getCharacterByName(namefrom);
                    chr.getClient().getSession().write(MaplePacketCreator.addMessengerPlayer(namefrom, from, position, (byte) (fromchannel - 1)));
                    from.getClient().getSession().write(MaplePacketCreator.addMessengerPlayer(chr.getName(), chr, messengerchar.getPosition(), (byte) (messengerchar.getChannel() - 1)));
                }
            } else if ((messengerchar.getName().equals(namefrom))) {
                MapleCharacter chr = getPlayerStorage().getCharacterByName(messengerchar.getName());
                if (chr != null) {
                    chr.getClient().getSession().write(MaplePacketCreator.joinMessenger(messengerchar.getPosition()));
                }
            }
        }
    }

    public void removeMessengerPlayer(MapleMessenger messenger, int position) {
        for (MapleMessengerCharacter messengerchar : messenger.getMembers()) {
            MapleCharacter chr = getPlayerStorage().getCharacterByName(messengerchar.getName());
            if (chr != null) {
                chr.getClient().getSession().write(MaplePacketCreator.removeMessengerPlayer(position));
            }
        }
    }

    public void messengerChat(MapleMessenger messenger, String chattext, String namefrom) {
        for (MapleMessengerCharacter messengerchar : messenger.getMembers()) {
            if (!(messengerchar.getName().equals(namefrom))) {
                MapleCharacter chr = getPlayerStorage().getCharacterByName(messengerchar.getName());
                if (chr != null) {
                    chr.getClient().getSession().write(MaplePacketCreator.messengerChat(chattext));
                }
            }
        }
    }

    public void declineChat(String target, String namefrom) {
        if (isConnected(target)) {
            MapleCharacter chr = getPlayerStorage().getCharacterByName(target);
            if (chr != null && chr.getMessenger() != null) {
                chr.getClient().getSession().write(MaplePacketCreator.messengerNote(namefrom, 5, 0));
            }
        }
    }

    public void updateMessenger(int messengerid, String namefrom, byte fromchannel) {
        MapleMessenger messenger = getMessenger(messengerid);
        int position = messenger.getPositionByName(namefrom);
        updateMessenger(messenger, namefrom, position, fromchannel);
    }

    public void updateMessenger(MapleMessenger messenger, String namefrom, int position, byte fromchannel) {
        for (MapleMessengerCharacter messengerchar : messenger.getMembers()) {
            Channel ch = getChannel(fromchannel);
            if (!(messengerchar.getName().equals(namefrom))) {
                MapleCharacter chr = ch.getPlayerStorage().getCharacterByName(messengerchar.getName());
                if (chr != null) {
                    chr.getClient().getSession().write(MaplePacketCreator.updateMessengerPlayer(namefrom, getChannel(fromchannel).getPlayerStorage().getCharacterByName(namefrom), position, (byte) (fromchannel - 1)));
                }
            }
        }
    }

    public void silentLeaveMessenger(int messengerid, MapleMessengerCharacter target) {
        MapleMessenger messenger = getMessenger(messengerid);
        if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
        messenger.silentRemoveMember(target);
    }

    public void joinMessenger(int messengerid, MapleMessengerCharacter target, String from, byte fromchannel) {
        MapleMessenger messenger = getMessenger(messengerid);
        if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
        messenger.addMember(target);
        addMessengerPlayer(messenger, from, fromchannel, target.getPosition());
    }

    public void silentJoinMessenger(int messengerid, MapleMessengerCharacter target, int position) {
        MapleMessenger messenger = getMessenger(messengerid);
        if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
        messenger.silentAddMember(target, position);
    }

    public MapleMessenger createMessenger(MapleMessengerCharacter chrfor) {
        int messengerid = runningMessengerId.getAndIncrement();
        MapleMessenger messenger = new MapleMessenger(messengerid, chrfor);
        messengers.put(messenger.getId(), messenger);
        return messenger;
    }

    public boolean isConnected(String charName) {
        return getPlayerStorage().getCharacterByName(charName) != null;
    }

    public void whisper(String sender, String target, byte channel, String message) {
        if (isConnected(target)) {
            getPlayerStorage().getCharacterByName(target).getClient().getSession().write(MaplePacketCreator.getWhisper(sender, channel, message));
        }
    }

    public BuddyAddResult requestBuddyAdd(String addName, byte channelFrom, int cidFrom, String nameFrom) {
        MapleCharacter addChar = getPlayerStorage().getCharacterByName(addName);
        if (addChar != null) {
            BuddyList buddylist = addChar.getBuddylist();
            if (buddylist.isFull()) {
                return BuddyAddResult.BUDDYLIST_FULL;
            }
            if (!buddylist.contains(cidFrom)) {
                buddylist.addBuddyRequest(addChar.getClient(), cidFrom, nameFrom, channelFrom);
            } else if (buddylist.containsVisible(cidFrom)) {
                return BuddyAddResult.ALREADY_ON_LIST;
            }
        }
        return BuddyAddResult.OK;
    }

    public void buddyChanged(int cid, int cidFrom, String name, byte channel, BuddyOperation operation) {
        MapleCharacter addChar = getPlayerStorage().getCharacterById(cid);
        if (addChar != null) {
            BuddyList buddylist = addChar.getBuddylist();
            switch (operation) {
                case ADDED:
                    if (buddylist.contains(cidFrom)) {
                        buddylist.put(new BuddylistEntry(name, "Default Group", cidFrom, channel, true));
                        addChar.getClient().getSession().write(MaplePacketCreator.updateBuddyChannel(cidFrom, (byte) (channel - 1)));
                    }
                    break;
                case DELETED:
                    if (buddylist.contains(cidFrom)) {
                        buddylist.put(new BuddylistEntry(name, "Default Group", cidFrom, (byte) -1, buddylist.get(cidFrom).isVisible()));
                        addChar.getClient().getSession().write(MaplePacketCreator.updateBuddyChannel(cidFrom, (byte) -1));
                    }
                    break;
            }
        }
    }

    public void loggedOff(String name, int characterId, byte channel, int[] buddies) {
        updateBuddies(characterId, channel, buddies, true);
    }

    public void loggedOn(String name, int characterId, byte channel, int buddies[]) {
        updateBuddies(characterId, channel, buddies, false);
    }

    private void updateBuddies(int characterId, byte channel, int[] buddies, boolean offline) {
        PlayerStorage playerStorage = getPlayerStorage();
        for (int buddy : buddies) {
            MapleCharacter chr = playerStorage.getCharacterById(buddy);
            if (chr != null) {
                BuddylistEntry ble = chr.getBuddylist().get(characterId);
                if (ble != null && ble.isVisible()) {
                    byte mcChannel;
                    if (offline) {
                        ble.setChannel((byte) -1);
                        mcChannel = -1;
                    } else {
                        ble.setChannel(channel);
                        mcChannel = (byte) (channel - 1);
                    }
                    chr.getBuddylist().put(ble);
                    chr.getClient().getSession().write(MaplePacketCreator.updateBuddyChannel(ble.getCharacterId(), mcChannel));
                }
            }
        }
    }

    public void setServerMessage(String msg) {
        for (Channel ch : channels) {
            ch.setServerMessage(msg);
        }
    }

    public void broadcastPacket(MaplePacket data) {
        for (MapleCharacter chr : players.getAllCharacters()) {
            chr.announce(data);
        }
    }

    public final void shutdown() {
        for (Channel ch : getChannels()) {
            ch.shutdown();
        }
        players.disconnectAll();
    }
}
