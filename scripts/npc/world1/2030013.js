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

importPackage(java.lang);
importPackage(Packages.server);

/* 
	Adobis
*/
var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            if (cm.getSquadState(MapleSquadType.ZAKUM) == 0) {
                cm.sendSimple("Would you like to assemble a team to take on the mighty Zakum?\r\n#b#L1#Lets get this going!#l\r\n\#L2#No, I think I'll wait a bit...#l");
            } else if (cm.getSquadState(MapleSquadType.ZAKUM) == 1) {
                if (cm.checkSquadLeader(MapleSquadType.ZAKUM)) {
                    cm.sendSimple("What would you like to do?\r\n\r\n#L1#View current squad members#l\r\n#L2#Close registrations#l\r\n#L3#Start the fight!#l");
                    status = 19;
                } else if (cm.isSquadMember(MapleSquadType.ZAKUM)) {
                    var noOfChars = cm.numSquadMembers(MapleSquadType.ZAKUM);
                    var toSend = "The following members make up your squad:\r\n\r\n";
                    for (var i = 1; i <= noOfChars; i++) {
                        toSend += "#L";
                        toSend += i;
                        toSend += "# ";
                        toSend += i;
                        toSend += " - ";
                        toSend += cm.getSquadMember(MapleSquadType.ZAKUM, i - 1).getName();
                        toSend += "#l";
                    }
                    System.out.println(toSend);
                    cm.sendOk(toSend);
                    cm.dispose();
                } else {
                    cm.sendSimple("Would you like to join the Zakum squad?\r\n#b#L1#Yes, I'm ready to take him on!#l\r\n\#L2#No, I think I'll wait a bit...#l");
                    status = 9;
                }
            } else {
                if (cm.checkSquadLeader(MapleSquadType.ZAKUM)) {
                    cm.sendSimple("What would you like to do?\r\n\r\n#L1#View current squad members#l\r\n#L2#Open registrations#l\r\n#L3#Start the fight!#l");
                    status = 19;
                } else if (cm.isSquadMember(MapleSquadType.ZAKUM)) {
                    var noOfChars = cm.numSquadMembers(MapleSquadType.ZAKUM);
                    var toSend = "The following members make up your squad:\r\n\r\n";
                    for (var i = 1; i <= noOfChars; i++) {
                        toSend += "#L";
                        toSend += i;
                        toSend += "# ";
                        toSend += i;
                        toSend += " - ";
                        toSend += cm.getSquadMember(MapleSquadType.ZAKUM, i - 1).getName();
                        toSend += "#l";
                    }
                    System.out.println(toSend);
                    cm.sendOk(toSend);
                    cm.dispose();
                } else {
                    cm.sendOk("Sorry but the leader of the squad has closed registrations. Try again next time.");
                    cm.dispose();
                }
            }
        } else if (status == 1) {
            if (selection == 1) {
                if (cm.createMapleSquad(MapleSquadType.ZAKUM) != null) {
                    cm.sendOk("The Zakum Squad has been created, tell your members to signup now.\r\n\r\nTalk to me again to view the current team, to close registrations, or start the fight!");
                    cm.dispose();
                } else {
                    cm.sendOk("There was an error, someone may have created a Zakum Squad before you, please inform a GM if this is not the case.");
                    cm.dispose();
                }
            } else if (selection == 2) {
                cm.sendOk("Sure, not everyone's up to challenging the might of Zakum.");
                cm.dispose();
            }
        } else if (status == 10) {
            if (selection == 1) {
                if (cm.numSquadMembers(MapleSquadType.ZAKUM) > 29) {
                    cm.sendOk("Sorry, the Zakum Squad is full.");
                    cm.dispose();
                } else {
                    if (cm.canAddSquadMember(MapleSquadType.ZAKUM)) {
                        cm.addSquadMember(MapleSquadType.ZAKUM);
                        cm.sendOk("You have signed up, please wait for further instruction from your squad leader.");
                        cm.dispose();
                    } else {
                        cm.sendOk("Sorry, but the leader has requested you not to be allowed to join.");
                        cm.dispose();
                    }
                }
            } else if (selection == 2) {
                cm.sendOk("Sure, not everyone's up to challenging the might of Zakum.");
                cm.dispose();
            }
        } else if (status == 20) {
            if (selection == 1) {
                var noOfChars = cm.numSquadMembers(MapleSquadType.ZAKUM);
                var toSend = "The following members make up your squad:\r\n\r\n";
                for (var i = 1; i <= noOfChars; i++) {
                    toSend += "#L";
                    toSend += i;
                    toSend += "# ";
                    toSend += i;
                    toSend += " - ";
                    toSend += cm.getSquadMember(MapleSquadType.ZAKUM, i - 1).getName();
                    toSend += "#l";
                /*toSend += "\r\n";*/
                }
                System.out.println(toSend);
                cm.sendOk(toSend);
            } else if (selection == 2) {
                if (cm.getSquadState(MapleSquadType.ZAKUM) == 1) {
                    cm.setSquadState(MapleSquadType.ZAKUM, 2);
                    cm.sendOk("Registrations have been closed, please talk to me again to start the fight or re-open them.");
                } else {
                    cm.setSquadState(MapleSquadType.ZAKUM, 1);
                    cm.sendOk("Registrations have been opened, please talk to me again to start the fight or re-open them.");
                }
                cm.dispose();
            } else if (selection == 3) {
                cm.setSquadState(MapleSquadType.ZAKUM, 2);
                cm.sendOk("I will now take you and your squad to #r#Zakum's Altar#");
                status = 29;
            }
        } else if (status == 21) {
            if (selection > 0) {
                cm.removeSquadMember(MapleSquadType.ZAKUM, selection - 1, true);
                cm.sendOk("The selected member has been banned.");
                cm.dispose();
            } else {
                if (cm.getSquadState(MapleSquadType.ZAKUM) == 1) {
                    cm.sendSimple("What would you like to do?\r\n\r\n#L1#View current squad members#l\r\n#L2#Close registrations#l\r\n#L3#Start the fight!#l");
                } else {
                    cm.sendSimple("What would you like to do?\r\n\r\n#L1#View current squad members#l\r\n#L2#Open registrations#l\r\n#L3#Start the fight!#l");
                }
                status = 19;
            }
        } else if (status == 30) {
            cm.warpSquadMembers(MapleSquadType.ZAKUM, 280030000);
            cm.dispose();
        }
    }
}