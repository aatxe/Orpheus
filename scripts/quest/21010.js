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
	Author : kevintjuh93
*/
importPackage(Packages.client);

var status = -1;

function start(mode, type, selection) {
    status++;
    if (mode != 1) {
	if(type == 1 && mode == 0) {
		qm.sendNext("Oh, no need to decline my offer. It's no big deal. It's just a potion. Well, let me know if you change your mind.");
		qm.dispose();
 	}else{
		qm.dispose();
		return;
	}
    }

    if (status == 0) {
	qm.sendNext("Hm, what's a human doing on this island? Wait, it's Lilin. What are you doing here, Lilin? And who's that besides you? Is it someone you know, Lilin? What? The hero, you say?");
    } else if (status == 1) {
	qm.sendNextPrev("#i4001170#");
    } else if (status == 2) {
	qm.sendNextPrev("Ah, this must be the hero you and your clan have been waiting for. Am I right, Lilin? Ah, I knew you weren't just accompanying an average passerby...");
    } else if (status == 3) { 
	qm.sendAcceptDecline("Oh, but it seems our hero has become very weak since the Black Mage's curse. It's only makes sense, considering that the hero has been asleep for hundreds of years.\r\n#bHere, I'll give you a HP Recovery Potion..");		
    } else if (status == 4) {
       	if (qm.c.getPlayer().getHp() >= 50) {
            	qm.c.getPlayer().setHp(25);
            	qm.c.getPlayer().updateSingleStat(MapleStat.HP, 25);
        } 
	if (!qm.haveItem(2000022))
        qm.gainItem(2000022, 1);
	qm.forceStartQuest();
	qm.sendNext("Drink it first. Then we'll talk.", 9);
    } else if (status == 5) {
	qm.sendNextPrev("#b(How do I drink the potion? I don't remember..)", 3);
    } else if (status == 6) {	
	qm.guideHint(14);
        qm.dispose();		
		}	
}

function end(mode, type, selection) {
    status++;
    if (mode != 1) {
        if(type == 1 && mode == 0)
            qm.dispose();
        else{
            qm.dispose();
            return;
        }
    }
    if (status == 0)
        if (qm.c.getPlayer().getHp() < 50) {
            qm.sendNext("Don't feel like you need to save this potion for later use. Just drink it! It's not much, but it'll be enough to restore some of your HP.");
            qm.dispose();
        } else
            qm.sendNext("We've been digging and digging inside the Ice Cane in the hope of finding a hero, but I never thought I'd actually see the day... The prophecy was true! You were right, Lilin! Now that one of the legendary hero has returned, we have no reason to fear the Black Mage!");
    else if (status == 1)
        qm.sendNextPrev("Oh, I've kept you too long. I'm sorry, I got a little carried away. I'm sure the other Penguins feel the same way. I know you're busy, but could you #bstop and talk to the other Penguins #kon your way to town? They would be so honored.\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v2000022# 3 #t2000022#\r\n#v2000023# 3 #t2000023#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 16 exp");
    else if (status == 2) {
           if(qm.canHold(2000022) && qm.canHold(2000023)){
        if(!qm.isQuestCompleted(21010)) {
            qm.gainExp(16);
            qm.gainItem(2000022, 3);
            qm.gainItem(2000023, 3);
		}
            qm.forceCompleteQuest();
	    qm.sendNextPrev("Oh, you've leveled up! You may have even received some skill points. In Maple World, you can acquire 3 skill points every time you level up. Press the #bK key #kto view the Skill window.", 9);
        }else
            qm.dropMessage(1,"Your inventory is full");        
    } else if (status == 3) {
	qm.sendNextPrev("#b(Everyone's been so nice to me, but I just can't remember anything. Am I really a hero? I should check my skills and see. But how do I check them?)", 3);
    } else if (status == 4) {
	qm.guideHint(15);
	qm.dispose();
    }
}