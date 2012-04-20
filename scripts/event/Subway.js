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
-- Odin JavaScript --------------------------------------------------------------------------------
	Subway Train between Kerning City and New Leaf City
-- By ---------------------------------------------------------------------------------------------
	Information
-- Version Info -----------------------------------------------------------------------------------
	1.2 - Fix timer map [Information]
	1.1 - Fix for infinity looping [Information]
	1.0 - First Version by Information
	    - Thanks for Shogi for the whole information
---------------------------------------------------------------------------------------------------
**/

importPackage(Packages.tools);

//Time Setting is in millisecond
var closeTime = 240000; //The time to close the gate
var beginTime = 300000; //The time to begin the ride
var rideTime = 60000; //The time that require move to destination
var KC_Waiting;
var Subway_to_KC;
var KC_docked;
var NLC_Waiting;
var Subway_to_NLC;
var NLC_docked;
var toggleMsg = false;

function init() {
    KC_Waiting = em.getChannelServer().getMapFactory().getMap(600010004);
    NLC_Waiting = em.getChannelServer().getMapFactory().getMap(600010002);
    Subway_to_KC = em.getChannelServer().getMapFactory().getMap(600010003);
    Subway_to_NLC = em.getChannelServer().getMapFactory().getMap(600010005);
    KC_docked = em.getChannelServer().getMapFactory().getMap(103000100);
    NLC_docked = em.getChannelServer().getMapFactory().getMap(600010001);
    scheduleNew();
}

function scheduleNew() {
    em.setProperty("docked", "true");
    em.setProperty("entry", "true");
    if(toggleMsg) {
        KC_docked.broadcastMessage(MaplePacketCreator.serverNotice(0, "The subway train to NLC has arrived."));
        NLC_docked.broadcastMessage(MaplePacketCreator.serverNotice(0, "The subway train to KC has arrived."));
    }
    em.schedule("stopEntry", closeTime);
    em.schedule("takeoff", beginTime);
}

function stopEntry() {
    em.setProperty("entry","false");
}

function takeoff() {
    em.setProperty("docked","false");
    var temp1 = KC_Waiting.getCharacters().iterator();
    while(temp1.hasNext()) {
        temp1.next().changeMap(Subway_to_NLC, Subway_to_NLC.getPortal(0));
    }
    var temp2 = NLC_Waiting.getCharacters().iterator();
    while(temp2.hasNext()) {
        temp2.next().changeMap(Subway_to_KC, Subway_to_KC.getPortal(0));
    }
    if(toggleMsg) {
        KC_docked.broadcastMessage(MaplePacketCreator.serverNotice(0, "The subway train to NLC has taken off."));
        NLC_docked.broadcastMessage(MaplePacketCreator.serverNotice(0, "The subway train to KC has taken off."));
    }
    var temp = rideTime / 1000;
    Subway_to_KC.broadcastMessage(MaplePacketCreator.getClock(temp));
    Subway_to_NLC.broadcastMessage(MaplePacketCreator.getClock(temp));
    em.schedule("arrived", rideTime);
}

function arrived() {
    var temp1 = Subway_to_KC.getCharacters().iterator();
    while(temp1.hasNext()) {
        temp1.next().changeMap(KC_docked, KC_docked.getPortal(0));
    }
    var temp2 = Subway_to_NLC.getCharacters().iterator();
    while(temp2.hasNext()) {
        temp2.next().changeMap(NLC_docked, NLC_docked.getPortal(0));
    }
    scheduleNew();
}

function cancelSchedule() {
}
