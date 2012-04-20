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
	Trains between Orbis and Ludibrium
-- By ---------------------------------------------------------------------------------------------
	Information
-- Version Info -----------------------------------------------------------------------------------
        1.6 - Modified for ShootSource (Moogra)
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

var Orbis_btf;
var Train_to_Orbis;
var Orbis_docked;
var Ludibrium_btf;
var Train_to_Ludibrium;
var Ludibrium_docked;

function init() {
    Orbis_btf = em.getChannelServer().getMapFactory().getMap(200000122);
    Ludibrium_btf = em.getChannelServer().getMapFactory().getMap(220000111);
    Train_to_Orbis = em.getChannelServer().getMapFactory().getMap(200090110);
    Train_to_Ludibrium = em.getChannelServer().getMapFactory().getMap(200090100);
    Orbis_docked = em.getChannelServer().getMapFactory().getMap(200000100);
    Ludibrium_docked = em.getChannelServer().getMapFactory().getMap(220000100);
    Orbis_Station = em.getChannelServer().getMapFactory().getMap(200000121);
    Ludibrium_Station = em.getChannelServer().getMapFactory().getMap(220000110);
    scheduleNew();
}

function scheduleNew() {
    Ludibrium_Station.setDocked(true);
    Orbis_Station.setDocked(true);
    Ludibrium_Station.broadcastMessage(MaplePacketCreator.boatPacket(true));
    Orbis_Station.broadcastMessage(MaplePacketCreator.boatPacket(true));
    em.setProperty("docked", "true");
    em.setProperty("entry", "true");
    em.schedule("stopEntry", 240000);
    em.schedule("takeoff", 300000);
}

function stopEntry() {
    em.setProperty("entry","false");
}

function takeoff() {
    Ludibrium_Station.setDocked(false);
    Orbis_Station.setDocked(false);
    Ludibrium_Station.broadcastMessage(MaplePacketCreator.boatPacket(false));
    Orbis_Station.broadcastMessage(MaplePacketCreator.boatPacket(false));
    em.setProperty("docked","false");
    var temp1 = Orbis_btf.getCharacters().iterator();
    while(temp1.hasNext())
        temp1.next().changeMap(Train_to_Ludibrium, Train_to_Ludibrium.getPortal(0));
    var temp2 = Ludibrium_btf.getCharacters().iterator();
    while(temp2.hasNext())
        temp2.next().changeMap(Train_to_Orbis, Train_to_Orbis.getPortal(0));
    em.schedule("arrived", 600000);
}

function arrived() {
    var temp1 = Train_to_Orbis.getCharacters().iterator();
    while(temp1.hasNext())
        temp1.next().changeMap(Orbis_docked, Orbis_docked.getPortal(0));
    var temp2 = Train_to_Ludibrium.getCharacters().iterator();
    while(temp2.hasNext())
        temp2.next().changeMap(Ludibrium_docked, Ludibrium_docked.getPortal(0));
    scheduleNew();
}

function cancelSchedule() {
}
