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

/**
-- Odin JavaScript --------------------------------------------------------------------------------
	Bullet Taxi of the Danger Zone
-- By ---------------------------------------------------------------------------------------------
	Information
-- Version Info -----------------------------------------------------------------------------------
	1.2 - Fix and shortened by Moogra
	1.1 - Statement fix, add support to multiple place [Information]
	1.0 - First Version by Information
---------------------------------------------------------------------------------------------------
**/

var toMap = new Array(211040200,220050300,240030000);
var cost = new Array(10000,25000,55000);
var location;
var status = 0;

function start() {
    switch(cm.getPlayer().getMapId()) {
        case toMap[0]: location = 0; break;
        case toMap[1]: location = 1; break;
        case toMap[2]: location = 2; break;
    }
    cm.sendNext("Hello there! This Bullet Taxi will take you to any danger zone from #m"+cm.getPlayer().getMap().getId()+"# to #b#m"+toMap[location]+"##k on this Ossyria Continent! The transportation fee of #b"+cost+" meso#k may seem expensive, but not that much when you want to easily transport through danger zones!");
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else if (mode == 0)
        cm.sendNext("Hmm... think it over. This taxi is worth its service! You will never regret it!");
    else
        status++;
    if (status == 1)
        cm.sendYesNo("#bDo you want to pay meso#k and transport to #b#m"+toMap[location]+"##k?");
    else if (status == 2) {
        if (cm.getMeso() < cost) {
            cm.sendNext("You don't seem to have enough mesos. I am terribly sorry, but I cannot help you unless you pay up. Bring in the mesos by hunting more and come back when you have enough.");
        } else {
            cm.warp(toMap[location]);
            cm.gainMeso(-cost[location]);
        }
        cm.dispose();
    }
}
