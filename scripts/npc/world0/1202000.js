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
/* 	Tutorial Lilin - Fix for Broken Aran Quests
 * 	@author Aaron Weiss
*/
var status = 0;

function start() {
    cm.sendOk("Hello there, Aran. This is hard to explain, but the universe is sort of... broken, for you. Allow me to fix that for you.");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        cm.dispose();
        return;
    }
    if (status == 1) {
        if (cm.getJobId() == 2000) {
            cm.gainItem(1442079, 1); // gain your weapon back.
            cm.changeJobById(2100); // make you an Aran.
            cm.sendOk("Here's your stuff. Now, let's get out of here.");
        } else {
            cm.sendOk("You're not a Legend! You shouldn't be here!");
        }
    } else if (status == 2) {
        cm.warp(910000000); // to the free market!
        cm.dispose();
    }
}