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
/* Athena Pierce
	Archer Job Advancement
*/

var status = 0;
var jobName;
var jobId;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 2) {
            cm.sendNext("You need to think about it a little more? Sure, take your time. This is not something you should take lightly. Let me know when you have made your decision.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
			
		if (cm.getJobId()==0) {
			if (status == 0) {
				cm.sendNext("So, you want to become a Bowman? Well...you need to meet some requirements to do so. You must be at least #bLevel 10#k. Let's see... Hmmm...");                
			} else if (status == 1) {
				if (cm.getPlayer().getLevel() > 9) 
					cm.sendYesNo("");
				else
					cm.sendNextPrev("You need to train more. It's not easy being a Bowman...");//dispose after
			} else if (status == 2) {
			} else if (status == 3) {
			}
		} else if (cm.getJobId()==200) {	
			if (cm.getPlayer().getLevel() >= 30) {
			if (cm.haveItem(4031012)) {
				if (status == 0) 
					cm.sendNext("You got back here safely. Well done. I knew you'd pass the tests very easily...alright, I'll make you much stronger now. Before that, though...you need to choose one of the three paths that will given to you. It will be a tough decision for you to make, but...if you have any questions, feel free to ask.");
				else if (status == 1) 
					cm.sendSimple("Alright, when you have made your decision, clock on [I'll choose my occupation!] at the very bottom.\r\n#b#L0#Please explain the characteristics of the Wizard of Fire and Poison.#l\r\n#L1#Please explain the characteristics of the Wizard of Ie and Lightning.#l\r\n#L2#Please explain the characteristics of the Cleric.#l\r\n#L3#I'll choose my occupation!#l");
				else if (status == 2) {
					if (selection == 0) 
						cm.sendNext("");
				    else if (selection == 1) 
						cm.sendNext("");
					else if (selection == 2) 
						cm.sendNext("");
					else if (selection == 3) 
						cm.sendSimple("Now, have you made up your mind? Please select your occupation for your 2nd job advancement.\r\r#b#L0#The Wizard of Fire and Poison#l\r\n#L1#The Wizard of Ice and Lightning#l\r\r#L2#Cleric#l");				
				} else if (status == 3) {
					if (selection == 0) {
						jobName = "The Wizard of Fire and Poison";
						jobId = 210;
					} else if (selection == 1) {
						jobName = "The Wizard of Ice and Lightning";
						jobId = 220;					
					} else if (selection == 2) {
						jobName = "Cleric";
						jobId = 230;
					}	
					cm.sendYesNo("So you want to make the 2nd job advancement as the #b" + jobName + "#k? You can't go back and change your job once you have made the decision...are you really sure about it?");
				} else if (status == 4) {
					if (jobId == 210) 
						cm.sendNext("");
					else if (jobId == 230) 
						cm.sendNext("Alright! You're a #bCleric#k from here on out. Clerics blow life into every living organism here with their undying faith in God. Never stop working on your faith...then one day, I'll help you become much more powerful...");
					cm.gainItem(4031012, -1);
					cm.changeJobById(jobId);
				} else if (status == 5) {
					cm.sendNextPrev("I have just given you a book that gives you the list of skills you can acquire as the " + jobName + "... In that book, you'll find a bunch of skills the " + jobName + " can learn. Your use and etc inventories have also been expanded with additional row of slots now available. Your max MP has also increased...go check and see for it yourself.");				
				} else if (status == 6) {
					cm.sendNextPrev("I have also given you a little bit of #bSP#k. Open the #bSkill Menu#k located at the bottom left corner. You'll be able to boost up the newly acquired 2nd level skills. A word of warning, though: You can't boost them up all at once. Some of the skills are only available after you have learned other skills. Make sure to remember that.");
				} else if (status == 7) {
					if (jobId == 130) 
						cm.sendNextPrev(jobName + "s needs to be strong. But remember that you can't abuse that power and use it on a weakling. Please use your enormous power the right way, because...for you to use that the right way, that is much harder than just getting stronger. Find me after you have advanced much further. I'll be waiting for you.");
				}				
			} else if (!cm.haveItem(4031008)) {		
				if (status == 0) {
					cm.sendYesNo("Well, we")
				} else if (status == 1) {
					cm.sendNext("Good thinking. You look strong, don't get me wrong, but there's still a need to test your strength and see if your are for real. The test isn't too difficult, so you'll do just fine... Here, take this letter first. Make sure you don't lose it.");
				} else if (status == 2) {
					if (cm.canHold(4031009)) {
						cm.gainItem(4031009);
						cm.sendNextPrev("Please get this letter to #bMagician Job Instructor #kwho may be around #bThe Forest North of Ellinia #kthat's near Ellinia. He's the one being the instructor now in place of me, as I am busy here. Get him the letter and he'll give you the test in place of me. For more details, hear it straight from him. Best of luck to you.");
					} else {
						cm.dispose();
					}
				} else if (status == 3) {	
					cm.dispose();
				}		
			}
			}
		} else {
			cm.sendNext("Would you like to have the power of nature in your hands? It may be a hard road, but you'll surely be rewarded in the end...");
			cm.dispose();
		}
	}
}
