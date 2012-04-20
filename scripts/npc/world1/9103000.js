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
 *@Author RMZero213
 * Ludibrium Maze Party Quest
 * Do not release anywhere other than RaGEZONE. Give credit if used.
 */

var status = 0;

function start() {
    status = -1;
    action(1,0,0);
}

function action(mode, type, selection){
    if (mode == -1|| (mode == 0 && status == 0))
        cm.dispose();
    else {
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            if (!cm.isLeader()) {
                cm.sendOk("Give any coupons to the leader of the party and tell them to talk to me.");
                cm.dispose();
            } else
                cm.sendYesNo("Do you have all the coupons of the party and would like to get out of here?");
        } else if (status == 1) {
            var members = cm.getPlayer().getEventInstance().getPlayers();
            cm.givePartyExp(50 * (cm.itemQuantity(4001106)), members);
            if (cm.itemQuantity(4001106) >= 30)
                cm.warpMembers(809050017, members);
             else 
                cm.warpMembers(809050016, members);
            cm.removeFromParty(4001106, members);
            cm.dispose();
        }	
    }
}