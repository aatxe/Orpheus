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
	AirPlane between KC and CBD
-- By ---------------------------------------------------------------------------------------------
	Information
-- Version Info -----------------------------------------------------------------------------------
	1.0 - First Version by Information
---------------------------------------------------------------------------------------------------
**/

importPackage(Packages.tools);

//Time Setting is in millisecond
var closeTime = 240000; //The time to close the gate
var beginTime = 300000; //The time to begin the ride
var rideTime = 60000; //The time that require move to destination
var KC_bfd;
var Plane_to_CBD;
var CBD_docked;
var CBD_bfd;
var Plane_to_KC;
var KC_docked;

function init() {
    KC_bfd = em.getChannelServer().getMapFactory().getMap(540010100);
    CBD_bfd = em.getChannelServer().getMapFactory().getMap(540010001);
    Plane_to_CBD = em.getChannelServer().getMapFactory().getMap(540010101);
    Plane_to_KC = em.getChannelServer().getMapFactory().getMap(540010002);
    CBD_docked = em.getChannelServer().getMapFactory().getMap(540010000);
    KC_docked = em.getChannelServer().getMapFactory().getMap(103000000);
    scheduleNew();
}

function scheduleNew() {
    em.schedule("stopEntry", closeTime);
    em.schedule("takeoff", beginTime);
}

function stopEntry() {
    em.setProperty("entry","false");
}

function takeoff() {
    em.setProperty("entry", "true");
    var temp1 = KC_bfd.getCharacters().iterator();
    while(temp1.hasNext()) {
        temp1.next().changeMap(Plane_to_KC, Plane_to_KC.getPortal(0));
    }
    var temp2 = CBD_bfd.getCharacters().iterator();
    while(temp2.hasNext()) {
        temp2.next().changeMap(Plane_to_CBD, Plane_to_CBD.getPortal(0));
    }
    em.schedule("arrived", rideTime);
    scheduleNew();
}

function arrived() {
    var temp1 = Plane_to_CBD.getCharacters().iterator();
    while(temp1.hasNext()) {
        temp1.next().changeMap(CBD_docked, CBD_docked.getPortal(0));
    }
    var temp2 = Plane_to_KC.getCharacters().iterator();
    while(temp2.hasNext()) {
        temp2.next().changeMap(KC_docked, KC_docked.getPortal(0));
    }
}

function cancelSchedule() {
}
