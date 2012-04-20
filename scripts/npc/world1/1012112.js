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
/**
 * @author BubblesDev
 * @NPC Tory
 */
var status = 0;
var chosen = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (cm.getPlayer().getMapId() == 100000200){
            if (!cm.isLeader()) {
                if (status == 0) {
                    cm.sendNext("Hi there! I'm Tory. This place is covered with mysterious aura of the full moon, and no one person can enter here by him/herself.");
                } else if (status == 1) {
                    cm.sendSimple("If you'd like to enter here, the leader of your party will have to talk to me. Talk to your party leader about this.");
                } 
            } else {
                if (status == 0) {
                    cm.sendNext("I'm Tory. Inside here is a beautiful hill where the primrose blooms. There's a tiger that lives in the hill, Growlie, and he seems to be looking for something to eat.");
                } else if (status == 1) {
                    cm.sendSimple("Would you like to head over to the hill of primrose and join forces with your party members to help Growlie out?\r\n#b#L0# Yes, I will go.#l");
                } else if (status == 2) {
                    if (cm.getParty() == null) {
                        cm.sendOk("You are not in a party.");
                        cm.dispose();
                        return;
                    } else {
                        var cango = true;
                        var things = cm.getClient().getChannelServer().getPartyMembers(cm.getPlayer().getParty());
                        var onmap = 0;
                        for (var i = 0; i < things.size(); i++) {
                            if (things.get(i).getMap().getId() == cm.getPlayer().getParty().getLeader().getMapid()) {
                                if (things.get(i).getLevel() < 10) {
                                    cango = false;
                                    break;
                                }
                                onmap++;
                            }
                        }
                        if (onmap < 3 || cm.getClient().getChannelServer().getMapFactory().getMap(910010000).getAllPlayer().getSize() > 0)
                            cango = false;
                        if (cango) {
                            cm.getClient().getChannelServer().getMapFactory().getMap(910010000).resetRiceCakes(); // lol lame method
                            cm.getClient().getChannelServer().getMapFactory().getMap(910010000).setAllowHPQSummon(false); // lol lame method
                            cm.warpParty(910010000);
                            cm.dispose();
                        } else {
                            // insert message here
                            cm.sendOk("UNDER CONSTRUCTION");
                            cm.dispose();
                        }
                    }
                }
            }
        } else if (cm.getPlayer().getMap().getId() == 910010100) {
            if (status == 0) {
                cm.sendSimple("I appreciate you giving some rice cakes for the hungry Growlie. It looks like you have nothing else to do now. Would you like to leave this place?\r\n#L0#I want to give you the rest of my rice cakes.#l\r\n#L1#Yes, please get me out of here.#l");
            } else if (status == 1) {
                chosen = selection;
                if (selection == 0) {
                    if (cm.getPlayer().getGivenRiceCakes() >= 20) {
                        if (cm.getPlayer().getGottenRiceHat()) {
                            cm.sendNext("Do you like the hat I gave you? I ate so much of your rice cake that I will have to say no to your offer of rice cake for a little while.");
                            cm.dispose();
                        } else {
                            cm.sendYesNo("I appreciate the thought, but I am okay now. I still have some of the rice cakes you gave me stored at home. To show you my appreciation, I prepared a small gift for you. Would you like to accept it?");
                        }
                    }
                } else if (selection == 1) {
                    cm.warp(100000200);
                    cm.dispose();
                }
                cm.dispose();
            } else if (status == 2) {
                if (chosen == 1) {
                    if (cm.canHold(1002798)) { // we will let them try again if they can't
                        cm.gainItem(1002798);
                        cm.setGottenRiceHat(true);
                        cm.sendNext("It will really go well with you. I promise.");
                    } else {
                        cm.getPlayer().dropMessage(1, "EQUIP inventory full.");
                    }
                    cm.dispose();
                } else if (cm.getPlayer().getGivenRiceCakes() < 20) {
                    cm.sendOk("Thank you for rice cake number " + cm.getPlayer().getGivenRiceCakes() + "!! I really appreciate it!");
                }
            }
        } else if (cm.getPlayer().getMap().getId() == 910010400) {
            if (status == 0) {
                cm.sendNext("Are you guys done putting a good whooping on those pigs? It looks like you'll have nothing else to do here now. Would you like to leave this place? \r\n#b#L0# Yes, I'd like to leave here.#l");
            } else if (status == 1) {
                if (cm.getParty() != null)
                    cm.warpParty(100000200);
                cm.dispose();
            }
        }
    }
}