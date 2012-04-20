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
-- Odin JavaScript --------------------------------------------------------------------------------
	Regular Cab - Victoria Road : Kerning City (103000000)
-- By ---------------------------------------------------------------------------------------------
	Xterminator
-- Version Info -----------------------------------------------------------------------------------
        2.0 - Second Version by Moogra
	1.0 - First Version by Xterminator
---------------------------------------------------------------------------------------------------
**/

var status = 0;
var maps = Array(104000000, 102000000, 101000000, 100000000);
var cost = Array(1000, 800, 1200, 1000);
var selectedMap = -1;

function start() {
    cm.sendNext("What's up? I drive the Regular Cab. If you want to go from town to town safely and fast, then ride our cab. We'll glady take you to your destination with an affordable price.");
}

function action(mode, type, selection) {
    if (mode == -1 || (status == 1 && mode == 0)) {
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.sendNext("There's a lot to see in this town, too. Come back and find us when you need to go to a different town.");
            cm.dispose();
            return;
        }
        status++;
        if (status == 1) {
            var selStr = "";
            if (cm.getJobId() == 0)
                selStr += "We have a special 90% discount for beginners. #b";
            selStr += "Choose your destination, for fees will change from place to place.#b";
            for (var i = 0; i < maps.length; i++)
                selStr += "\r\n#L" + i + "##m" + maps[i] + "# (" + cost[i] + " mesos)#l";
            cm.sendSimple(selStr);
        } else if (status == 2) {
            cm.sendYesNo("You don't have anything else to do here, huh? Do you really want to go to #b#m" + maps[selection] + "##k? It'll cost you #b"+ cost[selection] + " mesos#k.");
            selectedMap = selection;
        } else if (status == 3) {
            if (cm.getJobId() == 0) {
                if (cm.getMeso() < cost[selection] / 10) {
                    cm.sendNext("You don't have enough mesos. Sorry to say this, but without them, you won't be able to ride the cab.");
                    cm.dispose();
                } else {
                    cm.gainMeso(-(cost[selectedMap] / 10));
                }
            } else {
                if (cm.getMeso() < cost[selection]) {
                    cm.sendNext("You don't have enough mesos. Sorry to say this, but without them, you won't be able to ride the cab.");
                    cm.dispose();
                } else {
                    cm.gainMeso(-cost[selectedMap]);
                }
                cm.warp(maps[selectedMap], 0);
                cm.dispose();
            }
        }
    }
}