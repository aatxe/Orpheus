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
/* 	Author: Moogra
	NPC Name: 		?????????????
	Map(s): 		New Leaf City
	Description: 		Quest - Pet Evolution
*/
importPackage(Packages.client);
importPackage(Packages.server);

var status = -1;

function start(mode, type, selection) {
//nothing here?
}

function end(mode, type, selection) {
    status++;
    if (mode != 1) {
        if(type == 1 && mode == 0)
            status -= 2;
        else{
            qm.dispose();
            return;
        }
    }
    if (status == 0)
        qm.sendNext("Great job on finding your evolution materials. I will now give you a dragon.");
    else if (status == 1) {
        if (qm.isQuestCompleted(4659))
            qm.dropMessage(1, "how did this get here?");
        else if (qm.canHold(5000033)){
//            var closeness = qm.getPlayer().getPet(0).getCloseness();
//            var level = qm.getPlayer().getPet(0).getLevel();
//            var fullness = qm.getPlayer().getPet(0).getFullness();
            qm.gainItem(5380000, -1);
            qm.gainItem(5000029, -1);
            var rand = (Math.random() * 4) | 0;
            qm.gainItem(5000029 + rand);
//            var petId = MaplePet.createPet(rand + 5000030, level, closeness, fullness);
//            if (petId == -1) return;
//            MapleInventoryManipulator.addById(qm.getClient(), rand+5000030, 1, null, petId);
        } else
            qm.dropMessage(1,"Your inventory is full");
        qm.dispose();
    }
}