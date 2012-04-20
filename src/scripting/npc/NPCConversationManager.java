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
package scripting.npc;

import client.Equip;
import client.IItem;
import client.ISkill;
import client.ItemFactory;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import constants.ExpTable;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import client.MapleJob;
import client.MaplePet;
import client.MapleSkinColor;
import client.MapleStat;
import client.SkillFactory;
import tools.Randomizer;
import java.io.File;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import net.server.Channel;
import tools.DatabaseConnection;
import net.server.MapleParty;
import net.server.MaplePartyCharacter;
import net.server.Server;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import provider.MapleData;
import provider.MapleDataProviderFactory;
import scripting.AbstractPlayerInteraction;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.events.gm.MapleEvent;
import server.expeditions.MapleExpedition;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.partyquest.Pyramid;
import server.partyquest.Pyramid.PyramidMode;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;

/**
 *
 * @author Matze
 */
public class NPCConversationManager extends AbstractPlayerInteraction {

    private int npc;
    private String getText;

    public NPCConversationManager(MapleClient c, int npc) {
        super(c);
        this.npc = npc;
    }

    public int getNpc() {
        return npc;
    }

    public void dispose() {
        NPCScriptManager.getInstance().dispose(this);
    }

    public void sendNext(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 01", (byte) 0));
    }

    public void sendPrev(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 00", (byte) 0));
    }

    public void sendNextPrev(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 01", (byte) 0));
    }

    public void sendOk(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 00", (byte) 0));
    }

    public void sendYesNo(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 1, text, "", (byte) 0));
    }

    public void sendAcceptDecline(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0x0C, text, "", (byte) 0));
    }

    public void sendSimple(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 4, text, "", (byte) 0));
    }

    public void sendNext(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 01", speaker));
    }

    public void sendPrev(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 00", speaker));
    }

    public void sendNextPrev(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 01", speaker));
    }

    public void sendOk(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 00", speaker));
    }

    public void sendYesNo(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 1, text, "", speaker));
    }

    public void sendAcceptDecline(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0x0C, text, "", speaker));
    }

    public void sendSimple(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 4, text, "", speaker));
    }

    public void sendStyle(String text, int styles[]) {
        getClient().announce(MaplePacketCreator.getNPCTalkStyle(npc, text, styles));
    }

    public void sendGetNumber(String text, int def, int min, int max) {
        getClient().announce(MaplePacketCreator.getNPCTalkNum(npc, text, def, min, max));
    }

    public void sendGetText(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalkText(npc, text, ""));
    }

    /*
     * 0 = ariant colliseum
     * 1 = Dojo
     * 2 = Carnival 1
     * 3 = Carnival 2
     * 4 = Ghost Ship PQ?
     * 5 = Pyramid PQ
     * 6 = Kerning Subway
     */
    public void sendDimensionalMirror(String text) {
        getClient().announce(MaplePacketCreator.getDimensionalMirror(text));
    }

    public void setGetText(String text) {
        this.getText = text;
    }

    public String getText() {
        return this.getText;
    }

    public int getJobId() {
        return getPlayer().getJob().getId();
    }

    public void startQuest(short id) {
        try {
            MapleQuest.getInstance(id).forceStart(getPlayer(), npc);
        } catch (NullPointerException ex) {
        }
    }

    public void completeQuest(short id) {
        try {
            MapleQuest.getInstance(id).forceComplete(getPlayer(), npc);
        } catch (NullPointerException ex) {
        }
    }

    public int getMeso() {
        return getPlayer().getMeso();
    }

    public void gainMeso(int gain) {
        getPlayer().gainMeso(gain, true, false, true);
    }

    public void gainExp(int gain) {
        getPlayer().gainExp(gain, true, true);
    }

    public int getLevel() {
        return getPlayer().getLevel();
    }

    public void showEffect(String effect) {
        getPlayer().getMap().broadcastMessage(MaplePacketCreator.environmentChange(effect, 3));
    }

    public void playSound(String sound) {
        getPlayer().getMap().broadcastMessage(MaplePacketCreator.environmentChange(sound, 4));
    }

    public void setHair(int hair) {
        getPlayer().setHair(hair);
        getPlayer().updateSingleStat(MapleStat.HAIR, hair);
        getPlayer().equipChanged();
    }

    public void setFace(int face) {
        getPlayer().setFace(face);
        getPlayer().updateSingleStat(MapleStat.FACE, face);
        getPlayer().equipChanged();
    }

    public void setSkin(int color) {
        getPlayer().setSkinColor(MapleSkinColor.getById(color));
        getPlayer().updateSingleStat(MapleStat.SKIN, color);
        getPlayer().equipChanged();
    }

    public int itemQuantity(int itemid) {
        return getPlayer().getInventory(MapleItemInformationProvider.getInstance().getInventoryType(itemid)).countById(itemid);
    }

    public void displayGuildRanks() {
        MapleGuild.displayGuildRanks(getClient(), npc);
    }

    @Override
    public MapleParty getParty() {
        return getPlayer().getParty();
    }

    @Override
    public void resetMap(int mapid) {
        getClient().getChannelServer().getMapFactory().getMap(mapid).resetReactors();
    }

    public void gainCloseness(int closeness) {
        for (MaplePet pet : getPlayer().getPets()) {
            if (pet.getCloseness() > 30000) {
                pet.setCloseness(30000);
                return;
            }
            pet.gainCloseness(closeness);
            while (pet.getCloseness() > ExpTable.getClosenessNeededForLevel(pet.getLevel())) {
                pet.setLevel((byte) (pet.getLevel() + 1));
                byte index = getPlayer().getPetIndex(pet);
                getClient().announce(MaplePacketCreator.showOwnPetLevelUp(index));
                getPlayer().getMap().broadcastMessage(getPlayer(), MaplePacketCreator.showPetLevelUp(getPlayer(), index));
            }
            IItem petz = getPlayer().getInventory(MapleInventoryType.CASH).getItem(pet.getPosition());
            getPlayer().getClient().announce(MaplePacketCreator.updateSlot(petz));
        }
    }

    public String getName() {
        return getPlayer().getName();
    }

    public int getGender() {
        return getPlayer().getGender();
    }

    public void changeJobById(int a) {
        getPlayer().changeJob(MapleJob.getById(a));
    }

    public void addRandomItem(int id) {
        MapleItemInformationProvider i = MapleItemInformationProvider.getInstance();
        MapleInventoryManipulator.addFromDrop(getClient(), i.randomizeStats((Equip) i.getEquipById(id)), true);
    }

    public MapleJob getJobName(int id) {
        return MapleJob.getById(id);
    }

    public MapleStatEffect getItemEffect(int itemId) {
        return MapleItemInformationProvider.getInstance().getItemEffect(itemId);
    }

    public void resetStats() {
        getPlayer().resetStats();
    }

    public void maxMastery() {
        for (MapleData skill_ : MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz")).getData("Skill.img").getChildren()) {
            try {
                ISkill skill = SkillFactory.getSkill(Integer.parseInt(skill_.getName()));
                getPlayer().changeSkillLevel(skill, (byte) 0, skill.getMaxLevel(), -1);
            } catch (NumberFormatException nfe) {
                break;
            } catch (NullPointerException npe) {
                continue;
            }
        }
    }

    public void processGachapon(int[] id, boolean remote) {
        int[] gacMap = {100000000, 101000000, 102000000, 103000000, 105040300, 800000000, 809000101, 809000201, 600000000, 120000000};
        int itemid = id[Randomizer.nextInt(id.length)];
        addRandomItem(itemid);
        if (!remote) {
            gainItem(5220000, (short) -1);
        }
        sendNext("You have obtained a #b#t" + itemid + "##k.");
        getClient().getChannelServer().broadcastPacket(MaplePacketCreator.gachaponMessage(getPlayer().getInventory(MapleInventoryType.getByType((byte) (itemid / 1000000))).findById(itemid), c.getChannelServer().getMapFactory().getMap(gacMap[(getNpc() != 9100117 && getNpc() != 9100109) ? (getNpc() - 9100100) : getNpc() == 9100109 ? 8 : 9]).getMapName(), getPlayer()));
    }

    public void disbandAlliance(MapleClient c, int allianceId) {
        PreparedStatement ps = null;
        try {
            ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM `alliance` WHERE id = ?");
            ps.setInt(1, allianceId);
            ps.executeUpdate();
            ps.close();
            Server.getInstance().allianceMessage(c.getPlayer().getGuild().getAllianceId(), MaplePacketCreator.disbandAlliance(allianceId), -1, -1);
            Server.getInstance().disbandAlliance(allianceId);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
            } catch (SQLException ex) {
            }
        }
    }

    public boolean canBeUsedAllianceName(String name) {
        if (name.contains(" ") || name.length() > 12) {
            return false;
        }
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT name FROM alliance WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ps.close();
                rs.close();
                return false;
            }
            ps.close();
            rs.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static MapleAlliance createAlliance(MapleCharacter chr1, MapleCharacter chr2, String name) {
        int id = 0;
        int guild1 = chr1.getGuildId();
        int guild2 = chr2.getGuildId();
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO `alliance` (`name`, `guild1`, `guild2`) VALUES (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setInt(2, guild1);
            ps.setInt(3, guild2);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            id = rs.getInt(1);
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        MapleAlliance alliance = new MapleAlliance(name, id, guild1, guild2);
        try {
            Server.getInstance().setGuildAllianceId(guild1, id);
            Server.getInstance().setGuildAllianceId(guild2, id);
            chr1.setAllianceRank(1);
            chr1.saveGuildStatus();
            chr2.setAllianceRank(2);
            chr2.saveGuildStatus();
            Server.getInstance().addAlliance(id, alliance);
            Server.getInstance().allianceMessage(id, MaplePacketCreator.makeNewAlliance(alliance, chr1.getClient()), -1, -1);
        } catch (Exception e) {
            return null;
        }
        return alliance;
    }

    public List<MapleCharacter> getPartyMembers() {
        if (getPlayer().getParty() == null) {
            return null;
        }
        List<MapleCharacter> chars = new LinkedList<MapleCharacter>();
        for (Channel channel : Server.getInstance().getChannelsFromWorld(getPlayer().getWorld())) {
            for (MapleCharacter chr : channel.getPartyMembers(getPlayer().getParty())) {
                if (chr != null) {
                    chars.add(chr);
                }
            }
        }
        return chars;
    }

    public void warpParty(int id) {
        for (MapleCharacter mc : getPartyMembers()) {
            if (id == 925020100) {
                mc.setDojoParty(true);
            }
            mc.changeMap(getWarpMap(id));
        }
    }

    public boolean hasMerchant() {
        return getPlayer().hasMerchant();
    }

    public boolean hasMerchantItems() {
        try {
            if (!ItemFactory.MERCHANT.loadItems(getPlayer().getId(), false).isEmpty()) {
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
        if (getPlayer().getMerchantMeso() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public void showFredrick() {
        c.announce(MaplePacketCreator.getFredrick(getPlayer()));
    }

    public int partyMembersInMap() {
        int inMap = 0;
        for (MapleCharacter char2 : getPlayer().getMap().getCharacters()) {
            if (char2.getParty() == getPlayer().getParty()) {
                inMap++;
            }
        }
        return inMap;
    }

    public MapleEvent getEvent() {
        return c.getChannelServer().getEvent();
    }

    public void divideTeams() {
        if (getEvent() != null) {
            getPlayer().setTeam(getEvent().getLimit() % 2); //muhaha :D
        }
    }
    
    public MapleExpedition createExpedition(String type, byte min) {
        MapleParty party = getPlayer().getParty();
        if (party == null || party.getMembers().size() < min) return null;
        return new MapleExpedition(getPlayer());        
    }
    
    public boolean createPyramid(String mode, boolean party) {//lol
        PyramidMode mod = PyramidMode.valueOf(mode);

        MapleParty partyz = getPlayer().getParty();
        MapleMapFactory mf = c.getChannelServer().getMapFactory();

        MapleMap map = null;
        int mapid = 926010100;
        if (party) {
            mapid += 10000;
        }
        mapid += (mod.getMode() * 1000);

        for (byte b = 0; b < 5; b++) {//They cannot warp to the next map before the timer ends (:
            map = mf.getMap(mapid + b);
            if (map.getCharacters().size() > 0) {
                map = null;
                continue;
            } else {
                break;
            }
        }

        if (map == null) {
            return false;
        }

        if (!party) {
            partyz = new MapleParty(-1, new MaplePartyCharacter(getPlayer()));
        }
        Pyramid py = new Pyramid(partyz, mod, map.getId());
        getPlayer().setPartyQuest(py);
        py.warp(mapid);
        dispose();
        return true;
    }
}
