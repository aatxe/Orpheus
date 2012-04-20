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

/**
  * @author BubblesDev
  * @npc Growlie
  */
var status = 0;
var chosen = 0;

function start() {
    if (cm.isLeader()) {
        cm.sendSimple("Growl! I am Growlie, always ready to protect this place. What brought you here?\r\n#b#L0# Please tell me what this place is all about.#l\r\n#L1# I have brought #t4001101#.#l\r\n#L2# I would like to leave this place.#l");
    } else {
        cm.sendSimple("Growl! I am Growlie, always ready to protect this place. What brought you here?\r\n#b#L0# Please tell me what this place is all about.#l\r\n#L2# I would like to leave this place.#l");
    }
}

function action(mode, type, selection) {
    if (mode < 0)
        cm.dispose();
    else {
        if (mode == 0)
            status--;
        else
            status++;
        if (status == 1) {
            if (selection == 0) {
                cm.sendNext("This place can be best described as the prime spot where you can taste the delicious rice cakes made by Moon Bunny every full moon.");
            } else if (selection == 1) {
                chosen = selection;
                if (cm.haveItem(4001101, 10)) {
                    cm.sendNext("Oh... isn't this rice cake made by Moon Bunny? Please hand me the rice cake.");
                } else {
                    cm.sendOk("I advise you to check and make sure that you have indeed gathered up #b10 #t4001101#s#k.");
                    cm.dispose();
                }
            } else if (selection == 2) {
                // not sure, probably asking if you really want to leave
                cm.sendYesNo("Are you sure you want to leave?");
            }
        } else if (status == 2) {
            if (chosen == 0) {
                cm.sendNextPrev("Gather up the primrose seeds from the primrose leaves all over this area, and plant the seeds at the footing near the crescent moon to see the primrose bloom. There are 6 types of primroses, and all of them require different footings. It is imperative that the footing fits the seed of the flower.");
            } else if (chosen == 1) {
                cm.sendNextPrev("Mmmm ... this is delicious. Please come see me next time for more #b#t4001101##k. Have a safe trip home!");
                cm.gainItem(4001101, -10);
                cm.givePartyExp(1500, cm.getClient().getChannelServer().getPartyMembers(cm.getPlayer().getParty()));
                cm.getPlayer().getMap().broadcastMessage(MaplePacketCreator.playSound("quest/party/clear"));
                cm.getPlayer().geTMap().broadcastMessage(MaplePacketCreator.showEffect("Party1/Clear"));
            } else if (chosen == 2) {
                cm.warp(910010100);
                cm.dispose();
            }
        } else if (status == 3) {
            if (chosen == 0) {
                cm.sendNextPrev("When the flowers of primrose blooms, the full moon will rise, and that's when the Moon Bunnies will appear and start pounding the mill. Your task is to fight off the monsters to make sure that Moon Bunny can concentrate on making the best rice cake possible.");
            }
        } else if (status == 4) {
            if (chosen == 0) {
                cm.sendNextPrev("I would like for you and your party members to cooperate and get me 10 rice cakes. I strongly advise you to get me the rice cakes within the allotted time.");
            }
        }
    
    }
}	