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
 *Pelace (On Leafre flight from Orbis) 2012022
 *By Moogra
**/

var status = 0;

function start() {
    cm.sendYesNo("Do you wish to leave the flight?");
}

function action(mode, type, selection) {
    if (mode > 0)
        status++;
    else {
        cm.dispose();
        return;
    }
    if (status == 1) {
        cm.sendNext ("All right, see you next time. Take care.");
        status++;
    } else if (status == 2) {
        cm.warp(2000000131, 0);
        cm.dispose();
    }
}
