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
Ramini (Orbis -> Leafre Loader) 2012021
**/

var status = 0;
var lm;

function start() {
    action(1, 0, 0);
    lm = cm.getEventManager("Flights");
}

function action(mode, type, selection) {
    if (status == 0) {
        cm.sendYesNo("Do you wish to board the flight?");
        status++;
    } else {
        if (mode < 1) {
            cm.dispose();
        } else {
            if ((status == 1) && (lm.getProperty("FcanBoard").equals("true"))) {
                cm.warp(200000132, 0);// before takeoff
                cm.dispose();
            }else if ((status == 1) && (lm.getProperty("FcanBoard").equals("false")) && (lm.getProperty("flightDocked").equals("true"))){
                cm.sendOk("We are just cleaning the vehicle from the last journey.\r\nBoarding starts 5 minutes before departure.\r\nTry again shortly.");
                cm.dispose();
            }else if ((status == 1) && (lm.getProperty("flightDocked").equals("false"))) {
                cm.sendOk("The flight has not arrived yet.\r\nCome back soon.");
                cm.dispose();
            }
        }
    }
}
