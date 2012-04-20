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
/* Dances with Balrog
	Warrior Job Advancement
	Victoria Road : Warriors' Sanctuary (102000003)
*/

var status = 0;
var jobName;
var jobId;
var slx = -1;
var sls = -1;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 1) {
            cm.sendNext("You need to think about it a little more? Sure, take your time. This is not something you should take lightly. Let me know when you have made your decision.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
			
			if (status == 0) {
				if (cm.getJobId()==0) {
					cm.sendNext("Do you wish to become a Warrior? You need to meet some criteria in order to do so. #bYou should be at least Lv. 10#k. Let's see...");					
					slx = 0;
				} else if (cm.getJobId()==100) {
					if (cm.getPlayer().getLevel() >= 30) {
						if (cm.haveItem(4031012)) {
							cm.sendNext("OHH...you came back safe! I knew you'd breeze through...I'll admit you are a strong, formidable Warrior...alright, I'll make you an even stronger Warrior than you already are right now... Before THAT! you need to choose one of the three paths that you'll be given.. it isn't going to be easy, so if you have any questions, feel free to ask.");
							slx = 3;
						} else if (!cm.haveItem(4031008)) {
							cm.sendYesNo("Whoa, you have definitely grown up! You don't look small and weak anymore...rather, now I can feel your presence as the Warrior! Impressive..so, what do you think? Do you want to get even stronger than you are right now? Pass a simple test and I'll do just that! Wanna do it?");
							slx = 2;
						} else {
							cm.sendNext("Not coded yet.");
							cm.dispose();
						}
					} else {
						cm.sendSimple("Oh, you have a question?\r\n#b#L0#What are the general characteristics of being a Warrior?#l\r\n#L1#What are the weapons that Warriors use?#l\r\n#L2#What are the armors that Warriors can use?#l\r\n#L3#What are the skills available for Warriors?#l");
						slx = 1;
					}
				} else {
					cm.sendNext("To be continued...");
					cm.dispose();
				}
			}
			if (slx == 0) {	
				if (status == 1) {
					if (cm.getPlayer().getLevel() > 9)
						cm.sendYesNo("You definitely have the look of a Warrior. You may not be there just yet, but I can already see the Warrior in you. What do you think? Do you want to become a Warrior?");
					else
						cm.sendNextPrev("I don't think you have what it takes to become a Warrior just yet. You need to be well-trained or you won't last. Improve your strength first, then find me.");
				} else if (status == 2) {
					if (cm.getPlayer().getLevel() > 9)
						cm.sendNext("From now on, you are going to be a Warrior! Please persist in your discipline. I'll enhance your abilities in hopes that you'll train to be even stronger than you are right now.");
					else
						cm.dispose();
				} else if (status == 3) {
					if (cm.getJobId() == 0) {
						cm.changeJobById(100);
						cm.resetStats();
						cm.gainItem(1302077, 1);
					}
					cm.sendNextPrev("You've gotten much stronger now. Plus, every single one of your inventories have added slots--a whole row, to be exact. See for yourself. I just gave you a little bit of #bSP#k. When you open up the #bSkill menu#k on the lower right corner of the screen, there are skills you can learn by using SP. One warning, though: you can't raise it altogether all at once. There are also skills you can acquire only after having learned a couple of skils first.");
				} else if (status == 4) {
					cm.sendNextPrev("One more warning, though it's kind of obvious. Once you have chosen your job, try your best to stay alive. Every death will cost you a certain amount of experience points, and you don't want to lose those, do you? This is all I can teach you. From here on out, it's all about training harder to become better. See me after you've sufficiently improved.");
				} else if (status == 5) {
					cm.sendNextPrev("Oh, and your stats will now reflect your new occupation as a Warrior. Click #bAuto Assign#k on your stat window to make yourself an even more formidable Warrior.");
				} else if (status == 6) {
					cm.dispose();
				}	
			} else if (slx == 1) {
				if (status == 1) {
					if (selection == 0 || sls == 0) 
						cm.sendNext("Warriors possess awesome physical strenght and power. They can also defend monsters' attacks, so there are the best when fighting up close with the monsters. With a high level of stamina, you won't be dying easily either.");
					else if (selection == 1 || sls == 1) 
						cm.sendNext("Warriors use weapons that allow them to slash, stab, or strike. You won't be able to use weapons like bows and projectile weapons. This also includes small canes.");
					else if (selection == 2 || sls == 2) 
						cm.sendNext("Warriors possess great strength and stamina, so they are able to wear tough, sturdy armor. The appearance isn't noteworthy, but it serves its purpose well and is highly regarded.");
					else if (selection == 3 || sls == 3) 
						cm.sendNext("For Warriors, the skills available are based on their awesome physical strength and power. The skills that enhance close combat performance will benefit you the most. There's also a skill that allows you to recover your HP, which I think is worth mastering.");						
					if (selection > -1) sls = selection;
				} else if (status == 2) {
					if (sls == 0)
						cm.sendNextPrev("To accurately attack the monster, however, you need a healthy dose of DEX, so don't just concentrate on increasing STR. If you want to improve rapidly, I recommend that you face stronger monsters.");				
					else if (sls == 1)
						cm.sendNextPrev("The most common weapons are swords, polearms, spears, axes, and blunt weapons. Every weapon has its advantages and disadvantages, so please do your research before choosing one. For now, try using the ones with high attack ratings.");
					else if (sls == 2)
						cm.sendNextPrev("The shields, in particular, are very effective. However, you won't be able to use a shield if you are using a weapon that requires both hands. That's a decision you'll have to make later on.");				
					else if (sls == 3)
						cm.sendNextPrev("The two attacking skills available are #bPower Strike#k and #bSlash Blast#k. Power Strike is the one that applies heavy damage to a single enemy. You can boost this skill up from the beginning.");				
				} else if (status == 3) {
					if (sls == 3) 
						cm.sendNextPrev("On the other hand, Slash Blast does not apply much damage, but attacks multiple enemies around an area at once. You can only use this once you have 1 Power Strike boosted up.");
					else 
						cm.dispose();
				} else if (status == 4) 
					cm.dispose();			
			} else if (slx == 2) {
				if (status == 1) {
					cm.sendNext("Good thinking. You look strong, don't get me wrong, but there's still a need to test your strength and see if your are for real. The test isn't too difficult, so you'll do just fine... Here, take this letter first. Make sure you don't lose it.");
				} else if (status == 2) {
					if (cm.canHold(4031008)) {
						cm.gainItem(4031008);
						cm.sendNextPrev("Please get this letter to #bWarrior Job Instructor #kwho may be around #bWest Rocky Mountain IV #kthat's near Perion. He's the one being the instructor now in place of me, as I am busy here. Get him the letter and he'll give you the test in place of me. For more details, hear it straight from him. Best of luck to you.");
					} else {
						cm.dispose();
					}
				} else if (status == 3) {	
					cm.dispose();
				}		
			} else if (slx == 3) {
				if (status == 1) 
					cm.sendSimple("Alright, when you have made your decision, clock on [I'll choose my occupation!] at the very bottom.\r\n#b#L0#Please explain the role of the Fighter.#l\r\n#L1#Please explain the role of the Page.#l\r\n#L2#Please explain the role of the Spearman.#l\r\n#L3#I'll choose my occupation!#l");
				else if (status == 2) {
					if (selection == 0) 
						cm.sendNext("");
				    else if (selection == 1) 
						cm.sendNext("");
					else if (selection == 2) 
						cm.sendNext("");
					else if (selection == 3) 
						cm.sendSimple("Hmmm, have you made up your mind? Choose the 2nd job advancement of your liking\r\r#b#L0#Fighter#l\r\n#L1#Page#l\r\r#L2#Spearman#l");				
				} else if (status == 3) {
					if (selection == 0) {
						jobName = "Fighter";
						jobId = 110;
					} else if (selection == 1) {
						jobName = "Page";
						jobId = 120;					
					} else if (selection == 2) {
						jobName = "Spearman";
						jobId = 130;
					}	
					cm.sendYesNo("So you want to make the 2nd job advancement as the #b" + jobName + "#k? Once you make the decision, you won't be able to make a job advancement with any other job. Are you sure about this?");
				} else if (status == 4) {
					if (jobId == 110) 
						cm.sendNext("Alright! You have now become the #bFighter#k! A fighter strives to become the strongest of the strong, and never stops fighting. Don't ever lose that will to fight, and push forward 24/7. I'll help you become even stronger than you already are.");
					else if (jobId == 130) 
						cm.sendNext("Alright! You have now become the #bSpearman#k! The Spearman use the power of darkness to take out the enemies, always in shadows...please believe in yourself and your awesome power as you go on in your journey...I'll help you become much stronger than you are right now.");
					
					cm.gainItem(4031012, -1);
					cm.changeJobById(jobId);
				} else if (status == 5) {
					cm.sendNextPrev("I have just given you a book that gives you the list of skills you can acquire as the " + jobName + ". In that book, you'll find a bunch of skills the " + jobName + " can learn. Your use and etc inventories have also been expanded with additional row of slots now available. Your max MP has also increased...go check and see for it yourself.");				
				} else if (status == 6) {
					cm.sendNextPrev("I have also given you a little bit of #bSP#k. Open the #bSkill Menu#k located at the bottom left corner. You'll be able to boost up the newly acquired 2nd level skills. A word of warning, though: You can't boost them up all at once. Some of the skills are only available after you have learned other skills. Make sure to remember that.");
				} else if (status == 7) {
					if (jobId == 130) 
						cm.sendNextPrev(jobName + "s needs to be strong. But remember that you can't abuse that power and use it on a weakling. Please use your enormous power the right way, because...for you to use that the right way, that is much harder than just getting stronger. Find me after you have advanced much further. I'll be waiting for you.");
				}	
			}
	}
}

