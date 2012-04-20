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
package server.quest;

import java.util.HashMap;
import java.util.Map;
import client.ISkill;
import client.MapleCharacter;
import client.MapleInventoryType;
import client.MapleJob;
import client.MapleQuestStatus;
import client.MapleStat;
import client.SkillFactory;
import constants.ItemConstants;
import constants.ServerConstants;
import provider.MapleData;
import provider.MapleDataTool;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.Randomizer;

/**
 *
 * @author Matze
 */
public class MapleQuestAction {

    private MapleQuestActionType type;
    private MapleData data;
    private MapleQuest quest;

    public MapleQuestAction(MapleQuestActionType type, MapleData data, MapleQuest quest) {
        this.type = type;
        this.data = data;
        this.quest = quest;
    }

    public boolean check(MapleCharacter c) { //LOL WTF IS THIS CRAP
        switch (type) {
            case MESO:
                int mesos = MapleDataTool.getInt(data);
                if (c.getMeso() < mesos) {
                    return false;
                }
                break;
        }
        return true;
    }

    private boolean canGetItem(MapleData item, MapleCharacter c) {
        if (item.getChildByPath("gender") != null) {
            int gender = MapleDataTool.getInt(item.getChildByPath("gender"));
            if (gender != 2 && gender != c.getGender()) {
                return false;
            }
        }
        if (item.getChildByPath("job") != null) {
            int job = MapleDataTool.getInt(item.getChildByPath("job"));
            if (job < 100) {
                if (MapleJob.getBy5ByteEncoding(job).getId() / 100 != c.getJob().getId() / 100) {
                    return false;
                }
            } else if (job != c.getJob().getId()) {
                return false;
            }
        }
        return true;
    }

    public void run(MapleCharacter c, Integer extSelection) {
        MapleQuestStatus status;
        switch (type) {
            case EXP:
                status = c.getQuest(quest);
                if (status.getStatus() == MapleQuestStatus.Status.NOT_STARTED && status.getForfeited() > 0) {
                    break;
                }
                if (c.isBeginnerJob()) {
                    c.gainExp(MapleDataTool.getInt(data), true, true);
                } else {
                    c.gainExp(MapleDataTool.getInt(data) * ServerConstants.QUEST_EXP_RATE, true, true);
                }
                break;
            case ITEM:
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                Map<Integer, Integer> props = new HashMap<Integer, Integer>();
                for (MapleData iEntry : data.getChildren()) {
                    if (iEntry.getChildByPath("prop") != null && MapleDataTool.getInt(iEntry.getChildByPath("prop")) != -1 && canGetItem(iEntry, c)) {
                        for (int i = 0; i < MapleDataTool.getInt(iEntry.getChildByPath("prop")); i++) {
                            props.put(props.size(), MapleDataTool.getInt(iEntry.getChildByPath("id")));
                        }
                    }
                }
                int selection = 0;
                int extNum = 0;
                if (props.size() > 0) {
                    selection = props.get(Randomizer.nextInt(props.size()));
                }
                for (MapleData iEntry : data.getChildren()) {
                    if (!canGetItem(iEntry, c)) {
                        continue;
                    }
                    if (iEntry.getChildByPath("prop") != null) {
                        if (MapleDataTool.getInt(iEntry.getChildByPath("prop")) == -1) {
                            if (extSelection != extNum++) {
                                continue;
                            }
                        } else if (MapleDataTool.getInt(iEntry.getChildByPath("id")) != selection) {
                            continue;
                        }
                    }
                    if (MapleDataTool.getInt(iEntry.getChildByPath("count"), 0) < 0) { // remove items
                        int itemId = MapleDataTool.getInt(iEntry.getChildByPath("id"));
                        MapleInventoryType iType = ii.getInventoryType(itemId);
                        short quantity = (short) (MapleDataTool.getInt(iEntry.getChildByPath("count"), 0) * -1);
                        MapleInventoryManipulator.removeById(c.getClient(), iType, itemId, quantity, true, false);
                        c.getClient().getSession().write(MaplePacketCreator.getShowItemGain(itemId, (short) MapleDataTool.getInt(iEntry.getChildByPath("count"), 0), true));
                    } else { // add items
                        int itemId = MapleDataTool.getInt(iEntry.getChildByPath("id"));
                        short quantity = (short) MapleDataTool.getInt(iEntry.getChildByPath("count"), 0);
                        if (c.getInventory(MapleItemInformationProvider.getInstance().getInventoryType(itemId)).getNextFreeSlot() > -1) {
                            MapleInventoryManipulator.addById(c.getClient(), itemId, quantity);
                            c.getClient().getSession().write(MaplePacketCreator.getShowItemGain(itemId, quantity, true));
                        } else {
                            c.dropMessage(1, "Inventory Full");
                        }
                    }
                }
                break;
            case NEXTQUEST:
                status = c.getQuest(quest);
                int nextQuest = MapleDataTool.getInt(data);
                if (status.getStatus() == MapleQuestStatus.Status.NOT_STARTED && status.getForfeited() > 0) {
                    break;
                }
                c.getClient().getSession().write(MaplePacketCreator.updateQuestFinish((short) quest.getId(), status.getNpc(), (short) nextQuest));
                break;
            case MESO:
                status = c.getQuest(quest);
                if (status.getStatus() == MapleQuestStatus.Status.NOT_STARTED && status.getForfeited() > 0) {
                    break;
                }
                c.gainMeso(MapleDataTool.getInt(data) * ServerConstants.QUEST_MESO_RATE, true, false, true);
                break;
            case QUEST:
                for (MapleData qEntry : data) {
                    int questid = MapleDataTool.getInt(qEntry.getChildByPath("id"));
                    int stat = MapleDataTool.getInt(qEntry.getChildByPath("state"));
                    c.updateQuest(new MapleQuestStatus(MapleQuest.getInstance(questid), MapleQuestStatus.Status.getById(stat)));
                }
                break;
            case SKILL:
                for (MapleData sEntry : data) {
                    int skillid = MapleDataTool.getInt(sEntry.getChildByPath("id"));
                    byte skillLevel = (byte) MapleDataTool.getInt(sEntry.getChildByPath("skillLevel"));
                    int masterLevel = MapleDataTool.getInt(sEntry.getChildByPath("masterLevel"));
                    ISkill skillObject = SkillFactory.getSkill(skillid);
                    boolean shouldLearn = false;
                    MapleData applicableJobs = sEntry.getChildByPath("job");
                    for (MapleData applicableJob : applicableJobs) {
                        MapleJob job = MapleJob.getById(MapleDataTool.getInt(applicableJob));
                        if (c.getJob() == job) {
                            shouldLearn = true;
                            break;
                        }
                    }
                    if (skillObject.isBeginnerSkill()) {
                        shouldLearn = true;
                    }
                    skillLevel = (byte) Math.max(skillLevel, c.getSkillLevel(skillObject));
                    masterLevel = Math.max(masterLevel, c.getMasterLevel(skillObject));
                    if (shouldLearn) {
                        c.changeSkillLevel(skillObject, skillLevel, masterLevel, -1);
                    }
                }
                break;
            case FAME:
                status = c.getQuest(quest);
                if (status.getStatus() == MapleQuestStatus.Status.NOT_STARTED && status.getForfeited() > 0) {
                    break;
                }
                c.addFame(MapleDataTool.getInt(data));
                c.updateSingleStat(MapleStat.FAME, c.getFame());
                int fameGain = MapleDataTool.getInt(data);
                c.getClient().getSession().write(MaplePacketCreator.getShowFameGain(fameGain));
                break;
            case BUFF:
                status = c.getQuest(quest);
                if (status.getStatus() == MapleQuestStatus.Status.NOT_STARTED && status.getForfeited() > 0) {
                    break;
                }
                MapleItemInformationProvider.getInstance().getItemEffect(MapleDataTool.getInt(data)).applyTo(c);
                break;
            case PETSKILL:
                status = c.getQuest(quest);
                if (status.getStatus() == MapleQuestStatus.Status.NOT_STARTED && status.getForfeited() > 0) {
                    break;
                }
                int flag = MapleDataTool.getInt("petskill", data);
                c.getPet(0).setFlag((byte) ItemConstants.getFlagByInt(flag));
                break;
            default:
        }
    }
}
