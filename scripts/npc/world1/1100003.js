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
        Author : XxOsirisxX (BubblesDev)
        NPC Name: Shiny Stone
*/

status = -1;

function start() {
    cm.sendNext("Eh, Hello...again. Do you want to leave Ereve and go somewhere else? If so, you've come to the right place. I operate a ferry that goes from Ereve to #bEllinia#k.");
}

function action(mode, type, selection){
    status++;
    if (status == 2 && type == 1) {
        cm.gainMeso(-1000);
        cm.warp(130000000);
        mode = -1;
    }
    if (mode != 1) {
        cm.dispose();
        return;
    }
    if (status == 0)
        cm.sendYesNo("Ummm, are you trying to leave Ereve again? I can take you to #bEllinia#k if you want...\r\n\r\nYou'll have to pay a fee of #b1000#k Mesos.");
    else if (status == 1)
        if (cm.getMeso() > 1000)
            cm.sendNext("I'll take you off of the ride. Talk to me back any time.");
        else {
            cm.sendNext("I don't think you have enough mesos, double check your inventory.");
            cm.dispose();
        }
}
