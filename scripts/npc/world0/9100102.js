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
New gachapon format by dillusion @ SgPalMs
Perion Gachapon
 */
var ids = [2060001, 2061001, 2000005, 4020004, 4010000, 4020003, 4010002, 2000002, 2040805, 2044702, 2043302, 2041020, 2040502, 2043102, 2044302, 1050018, 1332026, 1032032, 1032032, 1452004, 1040070, 1041066, 1040073, 1041068, 1041008, 1041063, 1002137, 1061080, 1002064, 1002145, 1382007, 1040019, 1002073, 1040035, 1040048, 1332008, 1060043, 1040058, 1472007, 1041044, 1060032, 1002056, 1312001, 1050005, 1060028, 1040012, 1061015, 1002086, 1002055, 1082036, 1002011, 1061087, 1002056, 1002058, 1412004, 1041028, 1082150, 1082148, 1082146, 1082145, 2340000, 1302021, 1432013, 1002359, 1002393];
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