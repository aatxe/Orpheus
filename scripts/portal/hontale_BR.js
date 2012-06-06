/*
 	OrpheusMS: MapleStory Private Server based on OdinMS
    Copyright (C) 2012 Aaron Weiss <aaron@deviant-core.net>
    				Patrick Huy <patrick.huy@frz.cc>
					Matthias Butz <matze@odinms.de>
					Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
function enter(pi) {
	if (pi.getPlayer().getMapId() == 240060000) {
		var nextMap = 240060100;
		var eim = pi.getPlayer().getEventInstance()
		var target = eim.getMapInstance(nextMap);
		var targetPortal = target.getPortal("sp");
		// only let people through if the eim is ready
		var avail = eim.getProperty("head1");
		if (avail != "yes") {
			// do nothing; send message to player
			pi.getPlayer().dropMessage(6, "Horntail\'s Seal is Blocking this Door.");
			return false;
		}else {
			pi.getPlayer().changeMap(target, targetPortal);
			if (eim.getProperty("head2spawned") != "yes") {
				eim.setProperty("head2spawned", "yes");
				eim.schedule("headTwo", 5000);
			}
			return true;
		}
	} else if (pi.getPlayer().getMapId() == 240060100) {
		var nextMap = 240060200;
		var eim = pi.getPlayer().getEventInstance()
		var target = eim.getMapInstance(nextMap);
		var targetPortal = target.getPortal("sp");
		// only let people through if the eim is ready
		var avail = eim.getProperty("head2");
		if (avail != "yes") {
			// do nothing; send message to player
			pi.getPlayer().dropMessage(6, "Horntail\'s Seal is Blocking this Door.");
			return false;
		}else {
			pi.getPlayer().changeMap(target, targetPortal);
			return true;
		}
	}
	return true;	
}