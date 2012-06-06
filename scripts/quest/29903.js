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
	Author : 		kevintjuh93
	Description: 		Quest - Master Adventurer
	Quest ID : 		29903
*/

var status = -1;

function start(mode, type, selection) {
		if (qm.forceStartQuest()) qm.showInfoText("You have earned the <Master Adventurer> title. You can receive a Medal from NPC Dalair.");
		qm.dispose();
}


function end(mode, type, selection) {
    status++;
    if (mode != 1) 
        qm.dispose();
    else {
        if (status == 0) 
            qm.sendNext("Congratulations on earning your honorable #b<Master Adventurer>#k title. I wish you the best of luck in your future endeavors! Keep up the good work.\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n #v1142110:# #t1142110# 1");
        else if (status == 1) {
			if (qm.canHold(1142110)) {
				qm.gainItem(1142110);
				qm.forceCompleteQuest();
			} else 
				qm.sendNext("Please make room in your inventory");//NOT GMS LIKE
			
			qm.dispose();        
		}
    }
	
}