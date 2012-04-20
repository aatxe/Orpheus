/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/* Warrior Job Instructor
	Warrior 2nd Job Advancement
	Victoria Road : West Rocky Mountain IV (102020300)
*/

var status;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 1)
            status++;
        else
            status--;
			
		if (cm.getPlayer().getMapId() == 108000300) {
			if (cm.haveItem(4031013, 30)) {
				if (status == 0)
					cm.sendNext("Ohhhhh.. you collected all 30 Dark Marbles!! It should have been difficult.. just incredible! Alright. You've passed the test and for that, I'll reward you #bThe Proof of a Hero#k. Take that and go back to Perion.");
				else if (status == 1) {
					cm.removeItem(4031013);
					cm.gainItem(4031008, -1);
					cm.gainItem(4031012);
					cm.warp(102020300, 0);
					cm.dispose();
				}
			} else {
				cm.sendOk("This text is random because I don't know the text that GMS uses yet. \r\n Anyways, collect 30 Dark Marbles.");
				cm.dispose();
			}
		} else {
			if (cm.haveItem(4031008)) {
				if (status == 0)
					cm.sendNext("Hmmm...it is definitely the letter from #bDances with Balrog#k...so you came all the way here to take the test and make the 2nd job advancement as the warrior. Alright, I'll explain the test to you. Don't sweat it too much, it's not that complicated.")
				else if (status == 1)
					cm.sendNextPrev("I'll send you to a hidden map. You'll see monsters you don't normally see. They look the same like the regular ones, but with a totally different attitude. They neither boost your experience level nor provide you with item.");
				else if (status == 2)
					cm.sendNextPrev("You'll be able to acquire a marble called #b#t4031013##k while knocking down those monsters. It is a special marble made out of their sinister, evil minds. Collect 30 of those, and then go talk to a colleague of mine in there. That's how you pass the test.");
				else if (status == 3)
					cm.sendYesNo("Once you go inside, you can't leave untill you take care of your mission. If you die, your experience level will decrease..so you better really buckle up and get ready...well, do you want to go for it now?");
				else if (status == 4)
					cm.sendNext("Alright I'll let you in! Defeat the monsters inside, collect 30 Dark Marbles, then strike up a conversation with a colleague of mine inside. He'll give you #bThe Proof of a Hero#k, the proof that you've passed the test. Best of luck to you.");
				else if (status == 5) {
					cm.warp(108000300, 0);
					cm.dispose();
				}
			} else {
				cm.sendOk("Pls mesarz");
				cm.dispose();
			}
		}
    }
}	