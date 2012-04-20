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
 * @Name: Cody
 * @NPC ID: 9200000
 * @Author: MrXotic
 * @Author: XxOsirisxX
 * @Author: Moogra
 */

var status = -1;
var possibleJobs = new Array();
var maps = [
/*BossMaps*/[100000005, 105070002, 105090900, 230040420, 280030000, 220080000, 240020402, 240020101, 801040100, 240060200, 610010005, 610010012, 610010013, 610010100, 610010101, 610010102, 610010103, 610010104],
/*MonsterMaps*/[100040001, 101010100, 104040000, 103000101, 103000105, 101030110, 106000002, 101030103, 101040001, 101040003, 101030001, 104010001, 105070001, 105090300, 105040306, 230020000, 230010400, 211041400, 222010000, 220080000, 220070301, 220070201, 220050300, 220010500, 250020000, 251010000, 200040000, 200010301, 240020100, 240040500, 240040000, 600020300, 801040004, 800020130, 800020400],
/*Towns*/[130000000, 300000000, 1010000, 680000000, 230000000, 101000000, 211000000, 100000000, 251000000, 103000000, 222000000, 104000000, 240000000, 220000000, 250000000, 800000000, 600000000, 221000000, 200000000, 102000000, 801000000, 105040300, 610010004, 260000000, 540010000, 120000000]];
var jobA = false;
var warper = false;
var job;
var newJob;
var chosenMap = -1;
var chosenSection = -1;

function start() {
    cm.sendSimple("#fUI/UIWindow.img/QuestIcon/3/0#\r\n#L0#World Tour#l\r\n#L1#Job Advance#l");
}

function action(mode, type, selection) {
    status++;
    if(mode != 1){
        cm.dispose();
        return;
    }
    if (!jobA && !warper)
        if (selection == 1)
            jobA = true;
        else
            warper = true;
    if (jobA)
        jobAdv(selection);
    else
        warp(selection);
}

function warp(selection){
    if (status == 0)
        cm.sendSimple("#fUI/UIWindow.img/QuestIcon/3/0#\r\n#L0#Boss Maps#l\r\n#L1#Monster Maps#l\r\n#L2#Town Maps#l");
    else if (status == 1) {
        chosenSection = selection;
        var selStr = "Select your destination.#b";
        for (var i = 0; i < maps[selection].length; i++)
            selStr += "\r\n#L" + i + "##m" + maps[selection][i] + "#";
        cm.sendSimple(selStr);
    } else if (status == 2) {
        chosenMap = selection;
        cm.sendYesNo("Do you want to go to #m" + maps[chosenSection][selection] + "#?");
    } else if (status == 3) {
        cm.warp(maps[chosenSection][chosenMap]);
        cm.dispose();
    }
}

function jobAdv(selection){
    if (status == 0) {
        newJob = cm.getJobId() + 1;
        if (cm.getJobId() % 10 == 2) {
            cm.sendOk("Hey, how's it going? I've been doing well here.");
            cm.dispose();
        } else if (cm.getJobId() % 10 >= 0 && cm.getJobId() % 100 != 0) {
            var secondJob = cm.getJobId() % 10 == 0;
            if ((secondJob && cm.getLevel() < 70) || (!secondJob && cm.getLevel() < 120)) {
                cm.sendOk("Hey, how's it going? I've been doing well here.");
                cm.dispose();
            } else
                cm.sendYesNo("Great job getting to level " + cm.getLevel() + ". Would you like to become a #b"+cm.getJobName(newJob)+"#k ?");
        } else {
            if (cm.getJobId() % 1000 == 0) {
                if (cm.getLevel() >= 10) 
                    for (var i = 1; i < 6; i++) 
                        possibleJobs.push(cm.getJobId() + 100 * i);
                else if (cm.getLevel() >= 8)
                    possibleJobs.push(200);
            } else if (cm.getLevel() >= 30) {
                switch (cm.getJobId()) {
                    case 100:
                    case 200:
                        possibleJobs.push(cm.getJobId() + 30);
                    case 300:
                    case 400:
                    case 500:
                        possibleJobs.push(cm.getJobId() + 20);
                    case 1100:
                    case 1200:
                    case 1300:
                    case 1400:
                    case 1500:
                        possibleJobs.push(cm.getJobId() + 10);
                        break;
                }
            }
            if (possibleJobs.length == 0) {
                cm.sendOk("Hey, how's it going? I've been doing well here.");
                cm.dispose();
            } else {
                var text = "There are the available jobs you can take#b";
                for (var j = 0; j < possibleJobs.length; j++)
                    text += "\r\n#L"+j+"#"+cm.getJobName(possibleJobs[j])+"#l";
                cm.sendSimple(text);
            }
        }
    } else if (status == 1 && cm.getJobId() % 100 != 0) {
        cm.changeJobById(cm.getJobId() + 1);
        cm.maxMastery();
        cm.dispose();
    } else if (status == 1) {
        cm.changeJobById(possibleJobs[selection]);
        if (cm.getJobId() % 100 == 0)
            cm.resetStats();
        cm.dispose();
    } else if (status == 2) {
        job = selection;
        cm.sendYesNo("Are you sure you want to job advance?");
    } else if (status == 3) {
        cm.changeJobById(possibleJobs[job]);
        cm.dispose();
    }
}
