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
package scripting;

import java.util.Arrays;
import java.util.List;
import client.Equip;
import client.IItem;
import client.ISkill;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventory;
import client.MapleInventoryType;
import client.MaplePet;
import client.MapleQuestStatus;
import client.SkillFactory;
import constants.ItemConstants;
import java.awt.Point;
import net.server.MapleParty;
import net.server.Server;
import net.server.guild.MapleGuild;
import scripting.event.EventManager;
import scripting.npc.NPCScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.partyquest.Pyramid;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;

public class AbstractPlayerInteraction {
    public MapleClient c;

    public AbstractPlayerInteraction(MapleClient c) {
        this.c = c;
    }

    public MapleClient getClient() {
        return c;
    }

    public MapleCharacter getPlayer() {
        return c.getPlayer();
    }

    public void warp(int map) {
        getPlayer().changeMap(getWarpMap(map), getWarpMap(map).getPortal(0));
    }

    public void warp(int map, int portal) {
        getPlayer().changeMap(getWarpMap(map), getWarpMap(map).getPortal(portal));
    }

    public void warp(int map, String portal) {
        getPlayer().changeMap(getWarpMap(map), getWarpMap(map).getPortal(portal));
    }

    public void warpMap(int map) {
        for (MapleCharacter mc : getPlayer().getMap().getCharacters()) {
            mc.changeMap(getWarpMap(map), getWarpMap(map).getPortal(0));
        }
    }

    protected MapleMap getWarpMap(int map) {
        MapleMap target;
        if (getPlayer().getEventInstance() == null) {
            target = c.getChannelServer().getMapFactory().getMap(map);
        } else {
            target = getPlayer().getEventInstance().getMapInstance(map);
        }
        return target;
    }

    public MapleMap getMap(int map) {
        return getWarpMap(map);
    }

    public EventManager getEventManager(String event) {
        return getClient().getChannelServer().getEventSM().getEventManager(event);
    }

    public boolean haveItem(int itemid) {
        return haveItem(itemid, 1);
    }

    public boolean haveItem(int itemid, int quantity) {
        return getPlayer().getItemQuantity(itemid, false) >= quantity;
    }

    public boolean canHold(int itemid) {
        return getPlayer().getInventory(MapleItemInformationProvider.getInstance().getInventoryType(itemid)).getNextFreeSlot() > -1;
    }

    public void openNpc(int npcid) {
        NPCScriptManager.getInstance().dispose(c);
        NPCScriptManager.getInstance().start(c, npcid, null, null);
    }

    public void updateQuest(int questid, String status) {
            c.announce(MaplePacketCreator.updateQuest((short) questid, status));
    }
    
    public MapleQuestStatus.Status getQuestStatus(int id) {
        return c.getPlayer().getQuest(MapleQuest.getInstance(id)).getStatus();
    }

    public boolean isQuestCompleted(int quest) {
        try {
            return getQuestStatus(quest) == MapleQuestStatus.Status.COMPLETED;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean isQuestStarted(int quest) {
        try {
            return getQuestStatus(quest) == MapleQuestStatus.Status.STARTED;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public int getQuestProgress(int qid) {
        return Integer.parseInt(getPlayer().getQuest(MapleQuest.getInstance(29932)).getProgress().get(0));
    }

    public void gainItem(int id, short quantity) {
        gainItem(id, quantity, false);
    }

    public void gainItem(int id) {
        gainItem(id, (short) 1, false);
    }

    public void gainItem(int id, short quantity, boolean randomStats) {
        if (id >= 5000000 && id <= 5000100) {
            MapleInventoryManipulator.addById(c, id, (short) 1, null, MaplePet.createPet(id), -1);
        }
        if (quantity >= 0) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            IItem item = ii.getEquipById(id);
            if (!MapleInventoryManipulator.checkSpace(c, id, quantity, "")) {
                c.getPlayer().dropMessage(1, "Your inventory is full. Please remove an item from your " + ii.getInventoryType(id).name() + " inventory.");
                return;
            }
            if (ii.getInventoryType(id).equals(MapleInventoryType.EQUIP) && !ItemConstants.isRechargable(item.getItemId())) {
                if (randomStats) {
                    MapleInventoryManipulator.addFromDrop(c, ii.randomizeStats((Equip) item), false);
                } else {
                    MapleInventoryManipulator.addFromDrop(c, (Equip) item, false);
                }
            } else {
                MapleInventoryManipulator.addById(c, id, quantity);
            }
        } else {
            MapleInventoryManipulator.removeById(c, MapleItemInformationProvider.getInstance().getInventoryType(id), id, -quantity, true, false);
        }
        c.announce(MaplePacketCreator.getShowItemGain(id, quantity, true));
    }

    public void changeMusic(String songName) {
        getPlayer().getMap().broadcastMessage(MaplePacketCreator.musicChange(songName));
    }

    public void playerMessage(int type, String message) {
        c.announce(MaplePacketCreator.serverNotice(type, message));
    }

    public void message(String message) {
        getPlayer().message(message);
    }

    public void mapMessage(int type, String message) {
        getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(type, message));
    }

    public void mapEffect(String path) {
       c.announce(MaplePacketCreator.mapEffect(path));
    }

    public void mapSound(String path) {
       c.announce(MaplePacketCreator.mapSound(path));
    }

    public void showIntro(String path) {
       c.announce(MaplePacketCreator.showIntro(path));
    }

    public void showInfo(String path) {
       c.announce(MaplePacketCreator.showInfo(path));
       c.announce(MaplePacketCreator.enableActions());
    }

    public void guildMessage(int type, String message) {
        if (getGuild() != null) {
            getGuild().guildMessage(MaplePacketCreator.serverNotice(type, message));
        }
    }

    public MapleGuild getGuild() {
        try {
            return Server.getInstance().getGuild(getPlayer().getGuildId(), null);
        } catch (Exception e) {
        }
        return null;
    }

    public MapleParty getParty() {
        return getPlayer().getParty();
    }

    public boolean isLeader() {
        return getParty().getLeader().equals(getPlayer().getMPC());
    }

    public void givePartyItems(int id, short quantity, List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            MapleClient cl = chr.getClient();
            if (quantity >= 0) {
                MapleInventoryManipulator.addById(cl, id, quantity);
            } else {
                MapleInventoryManipulator.removeById(cl, MapleItemInformationProvider.getInstance().getInventoryType(id), id, -quantity, true, false);
            }
            cl.announce(MaplePacketCreator.getShowItemGain(id, quantity, true));
        }
    }

    public void givePartyExp(int amount, List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            chr.gainExp((amount * chr.getExpRate()), true, true);
        }
    }

    public void removeFromParty(int id, List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            MapleClient cl = chr.getClient();
            MapleInventoryType type = MapleItemInformationProvider.getInstance().getInventoryType(id);
            MapleInventory iv = cl.getPlayer().getInventory(type);
            int possesed = iv.countById(id);
            if (possesed > 0) {
                MapleInventoryManipulator.removeById(c, MapleItemInformationProvider.getInstance().getInventoryType(id), id, possesed, true, false);
                cl.announce(MaplePacketCreator.getShowItemGain(id, (short) -possesed, true));
            }
        }
    }

    public void removeAll(int id) {
        removeAll(id, c);
    }

    public void removeAll(int id, MapleClient cl) {
        int possessed = cl.getPlayer().getInventory(MapleItemInformationProvider.getInstance().getInventoryType(id)).countById(id);
        if (possessed > 0) {
            MapleInventoryManipulator.removeById(cl, MapleItemInformationProvider.getInstance().getInventoryType(id), id, possessed, true, false);
            cl.announce(MaplePacketCreator.getShowItemGain(id, (short) -possessed, true));
        }
    }

    public int getMapId() {
        return c.getPlayer().getMap().getId();
    }

    public int getPlayerCount(int mapid) {
        return c.getChannelServer().getMapFactory().getMap(mapid).getCharacters().size();
    }

    public void showInstruction(String msg, int width, int height) {
        c.announce(MaplePacketCreator.sendHint(msg, width, height));
        c.announce(MaplePacketCreator.enableActions());
    }

    public void disableMinimap() {
        c.announce(MaplePacketCreator.disableMinimap());
    }

    public void resetMap(int mapid) {
        getMap(mapid).resetReactors();
        getMap(mapid).killAllMonsters();
        for (MapleMapObject i : getMap(mapid).getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM))) {
            getMap(mapid).removeMapObject(i);
            getMap(mapid).broadcastMessage(MaplePacketCreator.removeItemFromMap(i.getObjectId(), 0, c.getPlayer().getId()));
        }
    }

    public void sendClock(MapleClient d, int time) {
        d.announce(MaplePacketCreator.getClock((int) (time - System.currentTimeMillis()) / 1000));
    }

    public void useItem(int id) {
        MapleItemInformationProvider.getInstance().getItemEffect(id).applyTo(c.getPlayer());
        c.announce(MaplePacketCreator.getItemMessage(id));//Useful shet :3
    }

    public void giveTutorialSkills() {
        if (getPlayer().getMapId() == 914000100) {
        ISkill skill = SkillFactory.getSkill(20000018);
        ISkill skill0 = SkillFactory.getSkill(20000017);
        getPlayer().changeSkillLevel(skill, (byte) 1, 1, -1);
        getPlayer().changeSkillLevel(skill0, (byte) 1, 1, -1);
        } else if (getPlayer().getMapId() == 914000200) {
        ISkill skill = SkillFactory.getSkill(20000015);
        ISkill skill0 = SkillFactory.getSkill(20000014);
        getPlayer().changeSkillLevel(skill, (byte) 1, 1, -1);
        getPlayer().changeSkillLevel(skill0, (byte) 1, 1, -1);
        } else if (getPlayer().getMapId() == 914000210) {
        ISkill skill = SkillFactory.getSkill(20000016);
        getPlayer().changeSkillLevel(skill, (byte) 1, 1, -1);
        }
    }

     public void removeAranPoleArm() {
        IItem tempItem = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11);
	MapleInventoryManipulator.removeFromSlot(c.getPlayer().getClient(), MapleInventoryType.EQUIPPED, (byte) -11, tempItem.getQuantity(), false, true);
       }

    public void spawnMonster(int id, int x, int y) {
        MapleMonster monster = MapleLifeFactory.getMonster(id);
        monster.setPosition(new Point(x,y));
        getPlayer().getMap().spawnMonster(monster);
    }

    public void spawnGuide() {
        c.announce(MaplePacketCreator.spawnGuide(true));
    }

    public void removeGuide() {
        c.announce(MaplePacketCreator.spawnGuide(false));
    }

    public void displayGuide(int num) {
       c.announce(MaplePacketCreator.showInfo("UI/tutorial.img/" + num));
    }

    public void talkGuide(String message) {
       c.announce(MaplePacketCreator.talkGuide(message));
    }

    public void guideHint(int hint) {
       c.announce(MaplePacketCreator.guideHint(hint));
    }

    public void updateAranIntroState(String mode) {
       c.getPlayer().addAreaData(21002, mode);
       c.announce(MaplePacketCreator.updateAreaInfo(mode, 21002));
    }

    public void updateAranIntroState2(String mode) {
       c.getPlayer().addAreaData(21019, mode);
       c.announce(MaplePacketCreator.updateAreaInfo(mode, 21019));
    }

    public boolean getAranIntroState(String mode) {
       if (c.getPlayer().area_data.contains(mode)) {
           return true;
       }
       return false;
    }

    public void updateCygnusIntroState(String mode) {
       c.getPlayer().addAreaData(20021, mode);
       c.announce(MaplePacketCreator.updateAreaInfo(mode, 20021));
    }

    public boolean getCygnusIntroState(String mode) {
       if (c.getPlayer().area_data.contains(mode)) {
           return true;
       }
       return false;
    }

    public MobSkill getMobSkill(int skill, int level) {
        return MobSkillFactory.getMobSkill(skill, level);
    }

    public void earnTitle(String msg) {
        c.announce(MaplePacketCreator.earnTitleMessage(msg));
    }

    public void showInfoText(String msg) {
        c.announce(MaplePacketCreator.showInfoText(msg));
    }

    public void openUI(byte ui) {
        c.announce(MaplePacketCreator.openUI(ui));
    }

    public void lockUI() {
        c.announce(MaplePacketCreator.disableUI(true));
	c.announce(MaplePacketCreator.lockUI(true));
    }

    public void unlockUI() {
        c.announce(MaplePacketCreator.disableUI(false));
	c.announce(MaplePacketCreator.lockUI(false));
    }

    public void environmentChange(String env, int mode) {
        getPlayer().getMap().broadcastMessage(MaplePacketCreator.environmentChange(env, mode));
    }

    public Pyramid getPyramid() {
        return (Pyramid) getPlayer().getPartyQuest();
    }
}
