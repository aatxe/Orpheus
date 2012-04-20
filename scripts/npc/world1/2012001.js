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
 Rini (Orbis Boat Loader) 2012001
**/

function start() {
    var em = cm.getEventManager("Boats");
    if (em.getProperty("entry") == "true")
        cm.sendYesNo("It looks like there are plenty of room for this ride. Please have your ticket ready so I can let you in. The ride will be long, but you'll get to your destination just fine. What do you think? Do you wants to get on this ride?");
    else {
        //if (em.getProperty("entry") == "false" && em.getProperty("docked") == "true")
            cm.sendOk("We are just cleaning the boat from the last voyage.\r\nBoarding starts 5 minutes before departure.\r\nTry again shortly.");
        cm.dispose();
    }
}

function action(mode, type, selection){
    cm.warp(200000112);
    cm.dispose();
}