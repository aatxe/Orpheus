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
	Ludibrium Elevator
-- By ---------------------------------------------------------------------------------------------
	Information
-- Version Info -----------------------------------------------------------------------------------
	1.2 - Some fixes ^__^ [Sadiq]
	1.1 - Remove unused statement [Information]
	1.0 - First Version by Information
---------------------------------------------------------------------------------------------------
**/

importPackage(Packages.scripting.reactor);
var elevator_s;
var elevator_m;
var returnMap;
var arrive;

function init() {
    elevator_m = em.getChannelServer().getMapFactory().getMap(222020211);
    em.setProperty("isUp","false");
    em.setProperty("isDown","false");
    //em.getChannelServer().getMapFactory().getMap(222020200).setReactorState();
    onDown();
}

function onDown() {
    em.getChannelServer().getMapFactory().getMap(222020100).resetReactors();
    arrive = em.getChannelServer().getMapFactory().getMap(222020100);
    returnMap = em.getChannelServer().getMapFactory().getMap(222020100);
    warpToD();
    elevator_s = em.getChannelServer().getMapFactory().getMap(222020110);
    elevator_m = em.getChannelServer().getMapFactory().getMap(222020111);
    em.setProperty("isDown","true");
    em.schedule("goingUp", 60000);
}

function goingUp() {
    warpToM();
    em.setProperty("isDown","false");
    em.schedule("onUp", 50000);
//em.getChannelServer().getMapFactory().getMap(222020100).setReactorState();
}

function onUp() {
    em.getChannelServer().getMapFactory().getMap(222020200).resetReactors();
    arrive = em.getChannelServer().getMapFactory().getMap(222020200);
    returnMap = em.getChannelServer().getMapFactory().getMap(222020200);
    warpToD();
    elevator_s = em.getChannelServer().getMapFactory().getMap(222020210);
    elevator_m = em.getChannelServer().getMapFactory().getMap(222020211);
    em.setProperty("isUp","true");
    em.schedule("goingDown", 60000);
}

function goingDown() {
    warpToM();
    em.setProperty("isUp","false");
    em.schedule("onDown", 50000);
//em.getChannelServer().getMapFactory().getMap(222020200).setReactorState();
}

function warpToD() { 
    var temp1 = elevator_m.getCharacters().iterator();
    while(temp1.hasNext()) {
        temp1.next().changeMap(arrive, arrive.getPortal(0));
    }
} 


function warpToM() { 
    var temp1 = elevator_s.getCharacters().iterator();
    while(temp1.hasNext()) {
        temp1.next().changeMap(elevator_m, elevator_m.getPortal(0));
    }
} 

function cancelSchedule() {
}
