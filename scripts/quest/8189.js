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
/* 	Author: 		Blue
	Name:	 		Garnox
	Map(s): 		New Leaf City : Town Center
	Description: 		Quest - Pet Re-Evolution
*/
importPackage(Packages.server);

var status = -1;

function end(mode, type, selection) {
	if (mode == -1) {
		qm.dispose();
	} else {
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			qm.sendYesNo("Alright then, let's do this again, shall we? As usual, it's going to be random, and I'm going to take away one of your Rock of Evolutions. \r\n\r #r#eReady?#n#k");
		} else if (status == 1) {
			qm.sendNextPrev("Then here we go...! #rHYAHH!#k");
		} else if (status == 2) {
			var pet = 0;
			if (qm.getPlayer().getPet(0).getItemId() >= 5000029 && qm.getPlayer().getPet(0).getItemId() <= 5000033) {
				var pet = 0;
			} else if (qm.getPlayer().getPet(1).getItemId() >= 5000029 && qm.getPlayer().getPet(0).getItemId() <= 5000033) {
				var pet = 1;
			} else if (qm.getPlayer().getPet(2).getItemId() >= 5000029 && qm.getPlayer().getPet(0).getItemId() <= 5000033) {
				var pet = 2;
			} else {
				qm.sendOk("Something wrong, try again.");
				qm.dispose();
			}
			var id = qm.getChar().getPet(pet).getItemId();
			var name = qm.getChar().getPet(pet).getName();
			var level = qm.getChar().getPet(pet).getLevel();
			var closeness = qm.getChar().getPet(pet).getCloseness();
			var fullness = qm.getChar().getPet(pet).getFullness();
			if (id < 5000029 || id > 5000033) {
				qm.sendOk("Something wrong, try again.");
				qm.dispose();
			}
			var rand = 1 + Math.floor(Math.random() * 10);
			var after = 0;
			if (rand >= 1 && rand <= 3) {
				after = 5000030;
			} else if (rand >= 4 && rand <= 6) {
				after = 5000031;
			} else if (rand >= 7 && rand <= 9) {
				after = 5000032;
			} else if (rand == 10) {
				after = 5000033;
			} else {
				qm.sendOk("Something wrong. Try again.");
				qm.dispose();
			}
			if (name.equals(MapleItemInformationProvider.getInstance().getName(id))) {
				name = MapleItemInformationProvider.getInstance().getName(after);
			}
			qm.getPlayer().unequipAllPets();
			MaplePet.deletePet(id, qm.getPlayer().getClient());
			qm.gainItem(id, -1);
			qm.gainItem(5380000, -1);
			qm.gainPet(after, name, level, closeness, fullness);
			qm.sendOk("Woo! It worked again! #rYou may find your new pet under your 'CASH' inventory.\r It used to be a #i" + id + "##t" + id + "#, and now it's \r a #i" + after + "##t" + after + "#!#k \r\n Come back with 10,000 mesos and another Rock of Evolution if you don't like it!\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v"+after+"# #t"+after+"#");
			qm.dispose();
		}
	}
}