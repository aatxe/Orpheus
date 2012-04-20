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
var status = 0;
var qChars = new Array ("Q1: What EXP is needed to level up from Level 1 to Level 2?#10#12#15#20#3",
    "Q1: At 1st Job Advancement, which of these is the incorrect requirement?#Warrior 35 STR#Thief 20 LUK#Magician 20 INT#Bowman 25 DEX#2",
    "Q1: When attacked by a monster, which is incorrect?#Weakened - Slower Movements#Sealed - Cannot use Skills#Darkness - Reduced Accuracy#Cursed - Reduced Experience#1",
    "Q1: When attacking a monster with skills which is incorrect?#Ice - more damage to Fire based monsters#Fire - more damage to Ice based monsters#Holy - more damage to Undead monsters#Poison - more damage to Boss monsters#4",
    "Q1: At 1st Job Advancement, Which job fully states the requirements?#Warrior#Bowman#Magician#Thief#2");
var qItems = new Array( "Q2: Which of the following Monster<-->Drop is correct?#Jr Wraith - Wraith Headband#Stirge - Stirge Wing#Slime - Squishy Bubble#Pig - Ribbon#2",
    "Q2: Which Monster --> Drop is incorrect?#Ribbon Pig - Pig Ribbon#Slime - Slime Bubble#Green Snail - Green Snail Shell#Axe Stump - Tree Branch#4",
    "Q2: Which Potion --> Effect is correct?#White Potion - Recover 250 HP#Mana Elixir - Recover 400 MP#Red Potion - Recover 100 HP#Pizza - Recover 400 HP#4",
    "Q2: Which Potion restores 50% Hp & Mp?#Elixir#Power Elixir#Ginger Ale#Cider#1",
    "Q2: Which Potion --> Effect is incorrect?#Blue Potion - Recover 100 MP#Mana Elixir - Recover 300 MP#Sunodinms Dew - Recover 3000 MP#Red Potion - Recover 50 HP#3");
var qMobs = new Array(  "Q3: Which of these monsters is the highest level?#Green Mushroom#Tree Stump#Bubbling#Axe Stump#4",
    "Q3: Maple Island does not have which monster?#Ice Sentinel#Green Snail#Blue Snail#Orange Mushroom#1",
    "Q3: Which monster may be seen on the boat from Ellinia to Orbis?#Jr Balrog#Crimson Balrog#Ice Balrog#GateKeepers#2",
    "Q3: Which monster is not on Victoria Island?#Hector#Slime#Dark Axe Stump#Iron Hog#1",
    "Q3: El Nath' does not have which monster?#Hector#Yeti#Leatty#Ligator#4",
    "Q3: Which of these monsters can fly?#Malady#Horny Mushroom#White Pang#Pepe#1",
    "Q3: Which of these monsters will not be found in Ossyria?#Jr Lioner#Jr Grupin#Luster Pixie#Ligator#4",
    "Q3: Which monster has not been seen on Maple Island?#Slime#Fire Boar#Pig#Stump#2");
var qQuests = new Array("Q4: Which Quest requires you to kill 40 Stumps?#Stump Horror Story#Pio's Collecting#Johns Flower Basket#Jane the Alchemist#1",
    "Q4: Which Quest can be repeated?#Finding The Maple History Books#I'm Bored#Mrs Ming Ming#Glass Slipper#4",
    "Q4: Which is not a 2nd Job advancment?#Mage#Fighter#Crossbowman#Assassin#1",
    "Q4: Which quest requires you to collect eggs?#Mason#Nemi#Alcaster#Arwin#2",
    "Q4: Which of these quest's allows you to gain most fames?#Shumi#Rowan#Alcaster#Arwin#2",
    "Q4: Out of these, which has the highest level requirement?#Mason the Collector#Scadurs New Fur Coat#Trading with Alien Gray#Protect Nero#1");
var qTowns = new Array( "Q5: Which town has 'Grendel the really old'?#Henesys#Perion#Kerning City#Ellinia#4",
    "Q5: Where is 'Alcaster' located?#Orbis#Sleepywood#El Nath#Ludibrium#3",
    "Q5: Who makes Shoes in 'El Nath'?#Gordon#Alcaster#Scadur#Maple Administrator#1",
    "Q5: Who makes Arrows in Henesys?#Chief Stan#Mrs Ming Ming#Vicious#Rina#3",
    "Q5: Which land has the 'Toy Factory'?#Mu Lung#Herb Town#Leafre#Ludibrium#4",
    "Q5: In which town is 'Nemi' located?#Ludibrium#Perion#Orbis#Aqua Road#1");
var correctAnswer = 0;

function start() {
    if (cm.haveItem(4031058) || !cm.isQuestStarted(100102)) {
        if (cm.haveItem(4031058)) cm.sendOk("#h #,You have the #t4031058# already. Please don't waste my time.");
        if (!cm.isQuestStarted(100102)) cm.sendOk("#h #, You have not started the quest yet!")
        cm.dispose();
    } else {
        cm.sendNext("Welcome #h #, I am #p2030006#.\r\nYou have travelled far to reach this stage.");
    }
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 0) {
            cm.sendOk("See you next time.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1)
            cm.sendNextPrev("#h #, if you give me a #bDark Crystal#k and I will let you try to answer 5 questions, get them all correct, and you will obtain the #v4031058# #bNecklace of Wisdom#k.");
        else if (status == 2) {
            if (!cm.haveItem(4005004)) {
                cm.sendOk("#h #, you don't have any #bDark Crystal#k's.");
                cm.dispose();
            } else {
                cm.gainItem(4005004, -1);
                cm.sendSimple("Let me test your knowledge of #bCharacters#k.\r\n\r\n" + getQuestion(qChars[Math.floor(Math.random() * qChars.length)]));
                status = 2;
            }
        } else if (status == 3) {
            if (selection == correctAnswer)
                cm.sendOk("#h # you are correct.\nReady for the next question ?");
            else {
                cm.sendOk("You got the first question wrong!\r\nI am surpodinmsd you managed to find your way here.\r\nGet another Dark Crystal and try again");
                cm.dispose();
            }
        } else if (status == 4)
            cm.sendSimple("Now your knowledge of #bItems and Drops#k.\r\n\r\n" + getQuestion(qItems[Math.floor(Math.random() * qItems.length)]));
        else if (status == 5) {
            if (selection == correctAnswer)
                cm.sendOk("#h # you are correct.\nReady for the next question ?");
            else {
                cm.sendOk("You got the second question wrong!\r\n(These are the easy ones)\r\nGet another Dark Crystal and try again");
                cm.dispose();
            }
        } else if (status == 6) {
            cm.sendSimple("Now your knowledge of #bMonsters#k.\r\n\r\n" + getQuestion(qMobs[Math.floor(Math.random() * qMobs.length)]));
            status = 6;
        } else if (status == 7) {
            if (selection == correctAnswer)
                cm.sendOk("#h # you are correct.\nReady for the next question ?");
            else {
                cm.sendOk("You got the third question wrong!\r\nHave you played this game long ?.\r\nGet another Dark Crystal and try again");
                cm.dispose();
            }
        } else if (status == 8)
            cm.sendSimple("Now your knowledge of #bQuests#k.\r\n\r\n" + getQuestion(qQuests[Math.floor(Math.random() * qQuests.length)]));
        else if (status == 9) {
            if (selection == correctAnswer) {
                cm.sendOk("#h # you are correct.\nReady for the next question ?");
                status = 9;
            } else {
                cm.sendOk("You got the fourth question wrong!\r\nYou were so close too.\r\nGet another Dark Crystal and try again");
                cm.dispose();
            }
        } else if (status == 10) {
            cm.sendSimple("Final question.\r\nLets test your knowledge of #bTowns and NPC's#k.\r\n\r\n" + getQuestion(qTowns[Math.floor(Math.random() * qTowns.length)]));
            status = 10;
        } else if (status == 11) {
            if (selection == correctAnswer) {
                cm.gainItem(4031058, 1);
                cm.sendOk("Congratulations #h #, you are indeed worthy to be elevated to 3rd Job status.\r\nTake this #v4031058# necklace as proof you have passed my test.");
                cm.dispose();
            } else {
                cm.sendOk("Aww #h #, you failed on the final question....\r\nI bet that's annoying.\r\nGet another Dark Crystal and try again");
                cm.dispose();
            }
        }
    }
}
function getQuestion(qSet){
    var q = qSet.split("#");
    var qLine = q[0] + "\r\n\r\n#L0#" + q[1] + "#l\r\n#L1#" + q[2] + "#l\r\n#L2#" + q[3] + "#l\r\n#L3#" + q[4] + "#l";
    correctAnswer = parseInt(q[5],10);
    correctAnswer--;
    return qLine;
}