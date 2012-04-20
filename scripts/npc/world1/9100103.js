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
/*
 * Gachapon
 * @NPC : Gachapon - Kerning City
 * @NPC ID : 9100103
 * @author Moogra
 * Item IDs by Sam
*/

var ids = [1002130,1040042,1040094,1322052,2000005,2022113,1302001,2040106,1332002];
var status = 0;

function start() {
    if (cm.haveItem(5451000)) {
        cm.gainItem(5451000, -1);
        cm.processGachapon(ids, true);
        cm.dispose();
    } else if (cm.haveItem(5220000))
        cm.sendYesNo("You may use Gachapon. Would you like to use your Gachapon ticket?");
    else {
        cm.sendSimple("Welcome to the " + cm.getPlayer().getMap().getMapName() + " Gachapon. How may I help you?\r\n\r\n#L0#What is Gachapon?#l\r\n#L1#Where can you buy Gachapon tickets?#l");
    }
}

function action(mode, type, selection){
    if (mode == 1 && cm.haveItem(5220000)) {
        cm.processGachapon(ids, false);
        cm.dispose();
    } else {
        if (mode > 0) {
            status++;
            if (selection == 0) {
                cm.sendNext("Play Gachapon to earn rare scrolls, equipment, chairs, mastery books, and other cool items! All you need is a #bGachapon Ticket#k to be the winner of a random mix of items.");
            } else if (selection == 1) {
                cm.sendNext("Gachapon Tickets are available in the #rCash Shop#k and can be purchased using NX or Maple Points. Click on the red SHOP at the lower right hand corner of the screen to visit the #rCash Shop #kwhere you can purchase tickets.");
                cm.dispose();
            } else if (status == 2) {
                cm.sendNext("You'll find a variety of items from the " + cm.getPlayer().getMap().getMapName() + " Gachapon, but you'll most likely find several related items and scrolls since " + cm.getPlayer().getMap().getMapName() + " is known as the town.");
                cm.dispose();
            }
        }
    }
}