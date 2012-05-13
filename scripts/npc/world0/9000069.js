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
/* 	Inkwell - Item Stat Maxer
 * 	@author Aaron Weiss
*/
var status = 0;
var equip = -1;

function start() {
    cm.sendSimple("Hey there, I'm #rInkwell#k. I can edit a stat on an item for just two rice cakes. I'll make them fantastic. Pick an item.\r\n" + cm.listEquips());
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        cm.dispose();
        return;
    }
    if (status == 1) {
        equip = selection;
        if (cm.getPlayer().isGM()) {
            cm.sendSimple("You've selected #v" + cm.getItemId(equip) + "#. Which stat would you like to max?\r\n#L0#Strength#l\r\n#L1#Dexterity#l\r\n#L2#Intellect#l\r\n#L3#Luck#l\r\n#L4#Weapon Attack#l\r\n#L5#Weapon Defense#l\r\n#L6#Magic Attack#l\r\n#L7#Magic Defense#l\r\n#L8#Accuracy#l\r\n#L9#Everything!#l\r\n");
        } else { 
            cm.sendSimple("You've selected #v" + cm.getItemId(equip) + "#. Which stat would you like to max?\r\n#L0#Strength#l\r\n#L1#Dexterity#l\r\n#L2#Intellect#l\r\n#L3#Luck#\r\n#L4#Weapon Attack#l\r\n#L5#Weapon Defense#l\r\n#L6#Magic Attack#l\r\n#L7#Magic Defense#l\r\n#L8#Accuracy#l\r\n");
        }
    } else if (status == 2) {
        if (equip != -1) {
            if (cm.hasItem(4001101, 2) || cm.getPlayer().isGM()) {
                if (!cm.getPlayer().isGM()) {
                    cm.gainItem(4001101, -2);
                }
                if (selection == 0) {
                    cm.modifyItem(equip, "str", 32767);
                    cm.setItemOwner(equip);
                } else if (selection == 1) {
                    cm.modifyItem(equip, "dex", 32767);
                    cm.setItemOwner(equip);
                } else if (selection == 2) {
                    cm.modifyItem(equip, "int", 32767);
                    cm.setItemOwner(equip);
                } else if (selection == 3) {
                    cm.modifyItem(equip, "luk", 32767);
                    cm.setItemOwner(equip);
                } else if (selection == 4) {
                    cm.modifyItem(equip, "watk", 32767);
                    cm.setItemOwner(equip);
                } else if (selection == 5) {
                    cm.modifyItem(equip, "wdef", 32767);
                    cm.setItemOwner(equip);
                } else if (selection == 6) {
                    cm.modifyItem(equip, "matk", 32767);
                    cm.setItemOwner(equip);
                } else if (selection == 7) {
                    cm.modifyItem(equip, "mdef", 32767);
                    cm.setItemOwner(equip);
                } else if (selection == 8) {
                    cm.modifyItem(equip, "acc", 32767);
                    cm.setItemOwner(equip);
                } else if (selection == 9 && cm.getPlayer().isGM()) {
                    cm.makeItemEpic(equip);
                }
                cm.sendOk("Thanks! Enjoy your day! You'll have to equip it, relog or change channels to see any changes.");
            } else {
                cm.sendOk("You don't have two rice cakes!");
            }
        } else {
            cm.dispose();
        }
    } else {
        cm.dispose();
    }
}