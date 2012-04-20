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
package scripting.quest;

import java.util.HashMap;
import java.util.Map;
import javax.script.Invocable;
import client.MapleClient;
import client.MapleQuestStatus;
import java.lang.reflect.UndeclaredThrowableException;
import scripting.AbstractScriptManager;
import server.quest.MapleQuest;

/**
 *
 * @author RMZero213
 */
public class QuestScriptManager extends AbstractScriptManager {
    private Map<MapleClient, QuestActionManager> qms = new HashMap<MapleClient, QuestActionManager>();
    private Map<MapleClient, QuestScript> scripts = new HashMap<MapleClient, QuestScript>();
    private static QuestScriptManager instance = new QuestScriptManager();

    public synchronized static QuestScriptManager getInstance() {
        return instance;
    }

    public void start(MapleClient c, short questid, int npc) {
        MapleQuest quest = MapleQuest.getInstance(questid);
        if (!c.getPlayer().getQuest(quest).getStatus().equals(MapleQuestStatus.Status.NOT_STARTED) || !c.getPlayer().getMap().containsNPC(npc)) {
            dispose(c);
            return;
        }
        try {
            QuestActionManager qm = new QuestActionManager(c, questid, npc, true);
            if (qms.containsKey(c)) {
                return;
            }
            qms.put(c, qm);
            Invocable iv = getInvocable("quest/" + questid + ".js", c);
            if (iv == null) {
                qm.dispose();
                return;
            }
            engine.put("qm", qm);
            QuestScript qs = iv.getInterface(QuestScript.class);
            scripts.put(c, qs);
            qs.start((byte) 1, (byte) 0, 0); // start it off as something
        } catch (UndeclaredThrowableException ute) {
            ute.printStackTrace();
            System.out.println("Error executing Quest script. (" + quest + ") UndeclaredThrowableException.");
            dispose(c);
        }
    }

    public void start(MapleClient c, byte mode, byte type, int selection) {
        QuestScript qs = scripts.get(c);
        if (qs != null) {
            try {
                qs.start(mode, type, selection);
            } catch (Exception e) {
                System.out.println("Error executing Quest script. (" + c.getQM().getQuest() + ") " + e);
                dispose(c);
            }
        }
    }

    public void end(MapleClient c, short questid, int npc) {
        MapleQuest quest = MapleQuest.getInstance(questid);
        if (!c.getPlayer().getQuest(quest).getStatus().equals(MapleQuestStatus.Status.STARTED) || !c.getPlayer().getMap().containsNPC(npc)) {
            dispose(c);
            return;
        }
        try {
            QuestActionManager qm = new QuestActionManager(c, questid, npc, false);
            if (qms.containsKey(c)) {
                return;
            }
            qms.put(c, qm);
            Invocable iv = getInvocable("quest/" + questid + ".js", c);
            if (iv == null) {
                qm.dispose();
                return;
            }
            engine.put("qm", qm);
            QuestScript qs = iv.getInterface(QuestScript.class);
            scripts.put(c, qs);
            qs.end((byte) 1, (byte) 0, 0); // start it off as something
        } catch (Exception e) {
            System.out.println("Error executing Quest script. (" + quest + ") " + e);
            dispose(c);
        }
    }

    public void end(MapleClient c, byte mode, byte type, int selection) {
        QuestScript qs = scripts.get(c);
        if (qs != null) {
            try {
                qs.end(mode, type, selection);
            } catch (Exception e) {
                System.out.println("Error executing Quest script. (" + c.getQM().getQuest() + ") " + e);
                dispose(c);
            }
        }
    }

    public void dispose(QuestActionManager qm, MapleClient c) {
        qms.remove(c);
        scripts.remove(c);
        resetContext("quest/" + qm.getQuest() + ".js", c);
    }

    public void dispose(MapleClient c) {
        QuestActionManager qm = qms.get(c);
        if (qm != null) {
            dispose(qm, c);
        }
    }

    public QuestActionManager getQM(MapleClient c) {
        return qms.get(c);
    }
}
