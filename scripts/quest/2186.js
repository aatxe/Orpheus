/*
 	OrpheusMS: MapleStory Private Server based on OdinMS
    Copyright (C) 2012 Aaron Weiss <aaron@deviant-core.net>
    				Patrick Huy <patrick.huy@frz.cc>
					Matthias Butz <matze@odinms.de>
					Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 /*
    Author: BubblesDev 0.75
    Quest: Abel Glasses Quest
*/

function end(mode, type, selection){
    if(!qm.isQuestCompleted(2186))
        if(qm.haveItem(4031853)){
            qm.gainItem(4031853, -1);
            qm.sendNext("Quest Completed.");
            qm.forceCompleteQuest();
        }else if(qm.haveItem(4031854) || qm.haveItem(4031855)){ //When I figure out how to make a completance with just a pickup xD
            if(qm.haveItem(4031854))
                qm.gainItem(4031854, -1);
            else
                qm.gainItem(4031855, -1);
            qm.sendNext("Sorry, those aren't my glasses.");
        }
        else
            qm.c.getPlayer().dropMessage(1, "Unknown Error");
    qm.dispose();
}