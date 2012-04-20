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

/*
        Author :        BubblesDev
        NPC Name:       Bell
        Description:    Subway ticket seller and taker off.
*/

function start() {
    if (cm.c.getPlayer().getMapId() == 103000100 || cm.c.getPlayer().getMapId() == 600010001)
        cm.sendYesNo("The ride to " + (cm.c.getPlayer().getMapId() == 103000100 ? "New Leaf City of Masteria" : "Kerning City of Victoria Island") + " takes off every 10 minutes, beginning on the hour, and it'll cost you #b5000 mesos#k. Are you sure you want to purchase #b#t" + (4031711 + parseInt(cm.c.getPlayer().getMapId() / 300000000)) + "##k?");
    else if (cm.c.getPlayer().getMapId() == 600010002 || cm.c.getPlayer().getMapId() == 600010004)
        cm.sendYesNo("Do you want to leave before the train start? There will be no refund.");
}

function action(mode, type, selection) {
    if(mode != 1){
        cm.dispose();
        return;
    }
    if (cm.c.getPlayer().getMapId() == 103000100 || cm.c.getPlayer().getMapId() == 600010001){
        if(cm.getMeso() >= 5000){
            cm.gainMeso(-5000);
            cm.gainItem(4031711 + parseInt(cm.c.getPlayer().getMapId() / 300000000), 1);
            cm.sendNext("There you got.");
        }else
            cm.sendNext("You don't have enough mesos.");
    }else{
        cm.sendNext("Alright, see you next time. Take care.");
        cm.warp(cm.c.getPlayer().getMapId() == 600010002 ? 600010001 : 103000100);
    }
    cm.dispose();
}