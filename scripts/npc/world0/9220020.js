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
/* 	Charles - FM NPC
 * 	@author Aaron Weiss
*/
var status = 0;

function start() {
    cm.sendSimple("Hey there, I'm #rCharles#k. What would you like to do today?\r\n#L0#Warrior Armor Shop#l\r\n#L1#Magician Armor Shop#l\r\n#L2#Thief Armor Shop#l\r\n#L3#Bowman Armor Shop#l\r\n#L4#Pirate Armor Shop#l\r\n#L5#Other Shops#l\r\n");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        cm.dispose();
        return;
    }
    if (status == 1) {
        if (selection == 0) {
            cm.openShop(100100);
        } else if (selection == 1) {
            cm.openShop(101101);
        } else if (selection == 2) {
            cm.openShop(102102);
        } else if (selection == 3) {
            cm.openShop(103103);
        } else if (selection == 4) {
            cm.openShop(104104);
        } else if (selection == 5) {
            cm.sendSimple("#r#eOther Shops#n#k\r\n#L0#Weapons Shop#l\r\n#L1#Maple Shop#l\r\n#L2#Timeless Shop#l\r\n#L3#Megaphone Shop#l\r\n#L4#Chair Shop#l\r\n#L5#Utilities Shop#l\r\n");
        }
    } else if (status == 2) {
        if (selection == 0) {
            cm.openShop(105105);
        } else if (selection == 1) {
            cm.openShop(106106);
        } else if (selection == 2) {
            cm.openShop(107107);
        } else if (selection == 3) {
            cm.openShop(9999993);
        } else if (selection == 4) {
            cm.openShop(9999994);
        } else if (selection == 5) {
            cm.openShop(9999999);
        } else {
            cm.dispose();
        }
    }
}