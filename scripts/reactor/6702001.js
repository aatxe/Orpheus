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
/**
 *6702001.js - Door of APQ
 *@author Jvlaple
 */

function act() {
	var eim = rm.getPlayer().getEventInstance();
	var party = eim.getPlayers();
	var numOpen = Integer.parseInt(eim.getProperty("openedDoors"));
	var mf = eim.getMapFactory();
	var map = mf.getMap(670010600);
	eim.setProperty("openedDoors", numOpen + 1);
	for (var i = 0; i < party.size(); i++) {
		party.get(i).changeMap(map, map.getPortal((numOpen + 1) * 2));
	}
}
	