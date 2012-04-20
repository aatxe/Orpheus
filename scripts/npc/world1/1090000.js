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
   @Author Moogra
   Pirate Job Advancement
*/

var status = 0;
var job = 510;
var jobName = "Gunslinger";

function start() {
    if (cm.getJobId() == 0) {
        if (cm.getLevel() >= 10)
            cm.sendNext("So you decided to become a #rPirate#k?");
        else {
            cm.sendOk("Train a bit more and I can show you the way of the #rPirate#k.")
            cm.dispose();
        }
    } else {
        if (cm.getLevel() >= 30 && cm.getJobId() == 500) {
            status = 10;
            cm.sendNext("The progress you have made is astonishing.");
        } else if (cm.getLevel() >= 70 && (cm.getJobId() == 510 || cm.getJobId() == 520))
            cm.sendOk("Please go visit #bArec#k. He resides in #bEl Nath#k.");
        else if (cm.getLevel() < 30 && cm.getJobId() == 500)
            cm.sendOk("Please come back to see me once you have trained more.");
        else if (cm.getLevel() >= 120 && cm.getJobId() > 510 && cm.getJobId()%10 == 1)
            cm.sendOk("Please go visit the 4th job advancement person.");
        else
            cm.sendOk("Please let me down...");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
        if (status == 1)
            cm.sendNextPrev("It is an important and final choice. You will not be able to turn back.");
        else if (status == 2)
            cm.sendYesNo("Do you want to become a #rPirate#k?");
        else if (status == 3) {
            if (cm.getJobId() == 0)
                cm.changeJobById(500);
            cm.sendOk("So be it! Now go with pride.");
            cm.dispose();
        } else if (status == 11)
            cm.sendNextPrev("You are now ready to take the next step as a #rGunslinger#k or #rBrawler#k.");
        else if (status == 12) {
            cm.completeQuest(2192);
            cm.completeQuest(2193);
            cm.sendSimple("What do you want to become?#b\r\n#L0#Gunslinger#l\r\n#L1#Brawler#l#k");
        }
        else if (status == 13) {
            if (selection == 1) {
                jobName = "Brawler";
                job = 520;
            }
            cm.sendYesNo("Do you want to become a #r" + jobName + "#k?");
        } else if (status == 14) {
            cm.changeJobById(job);
            cm.sendOk("Congratulations, you are now a " + jobName+ ".");
            cm.dispose();
        }
    } else if (mode == 0) {
        cm.sendOk("Come back once you have thought about it some more.");
        cm.dispose();
    }  else {
        cm.dispose();
    }
}