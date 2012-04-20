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
	Cabin between Orbis and Leafre
-- By ---------------------------------------------------------------------------------------------
	Information
-- Version Info -----------------------------------------------------------------------------------
	1.5 - Fix for infinity looping [Information]
	1.4 - Ship/boat is now showed
	    - Removed temp message[Information]
	    - Credit to Snow/superraz777 for old source
	    - Credit to Titan/Kool for the ship/boat packet
	1.3 - Removing some function since is not needed [Information]
	    - Remove register player menthod [Information]
	1.2 - It should be 2 ships not 1 [Information]
	1.1 - Add timer variable for easy edit [Information]
	1.0 - First Version by Information
---------------------------------------------------------------------------------------------------
**/

importPackage(Packages.tools);

//Time Setting is in millisecond
var closeTime = 240000; //The time to close the gate
var beginTime = 300000; //The time to begin the ride
var rideTime = 600000; //The time that require move to destination
var Orbis_btf;
var Cabin_to_Orbis;
var Orbis_docked;
var Leafre_btf;
var Cabin_to_Leafre;
var Leafre_docked;

function init() {
    Orbis_btf = em.getChannelServer().getMapFactory().getMap(200000132);
    Leafre_btf = em.getChannelServer().getMapFactory().getMap(240000111);
    Cabin_to_Orbis = em.getChannelServer().getMapFactory().getMap(200090210);
    Cabin_to_Leafre = em.getChannelServer().getMapFactory().getMap(200090200);
    Orbis_docked = em.getChannelServer().getMapFactory().getMap(200000100);
    Leafre_docked = em.getChannelServer().getMapFactory().getMap(240000100);
    Orbis_Station = em.getChannelServer().getMapFactory().getMap(200000131);
    Leafre_Station = em.getChannelServer().getMapFactory().getMap(240000110);
    scheduleNew();
}

function scheduleNew() {
    Leafre_Station.setDocked(true);
    Orbis_Station.setDocked(true);
    Leafre_Station.broadcastMessage(MaplePacketCreator.boatPacket(true));
    Orbis_Station.broadcastMessage(MaplePacketCreator.boatPacket(true));
    em.setProperty("docked", "true");
    em.setProperty("entry", "true");
    em.schedule("stopEntry", closeTime);
    em.schedule("takeoff", beginTime);
}

function stopEntry() {
    em.setProperty("entry","false");
}

function takeoff() {
    Leafre_Station.setDocked(false);
    Orbis_Station.setDocked(false);
    Leafre_Station.broadcastMessage(MaplePacketCreator.boatPacket(false));
    Orbis_Station.broadcastMessage(MaplePacketCreator.boatPacket(false));
    em.setProperty("docked","false");
    var temp1 = Orbis_btf.getCharacters().iterator();
    while(temp1.hasNext()) {
        temp1.next().changeMap(Cabin_to_Leafre, Cabin_to_Leafre.getPortal(0));
    }
    var temp2 = Leafre_btf.getCharacters().iterator();
    while(temp2.hasNext()) {
        temp2.next().changeMap(Cabin_to_Orbis, Cabin_to_Orbis.getPortal(0));
    }
    em.schedule("arrived", rideTime);
}

function arrived() {
    var temp1 = Cabin_to_Orbis.getCharacters().iterator();
    while(temp1.hasNext())
        temp1.next().changeMap(Orbis_docked, Orbis_docked.getPortal(0));
    var temp2 = Cabin_to_Leafre.getCharacters().iterator();
    while(temp2.hasNext())
        temp2.next().changeMap(Leafre_docked, Leafre_docked.getPortal(0));
    scheduleNew();
}
function cancelSchedule() {
}