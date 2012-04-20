/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
 Hak - Warp Bird
*/

var status = 0;
var maps = new Array(251000000, 200000100);
var mapNames = new Array("Herb Town", "Orbis");

var maps2 = new Array(1);
maps2[0] = 250000100;
var mapNames2 = new Array(1);
mapNames2[0] = "Mu Lung";

var status = 0;
var selectedMap = -1;

function start() {
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (cm.getPlayer().getMapId() == 251000000) {
		maps = maps2;
		mapNames = mapNames2;
	}

	if (status == 0) {
		var where = "Where do you want to go today?";
		for (var i = 0; i < maps.length; i++) {
			where += "\r\n#L" + i + "# " + mapNames[i] + "#l";
		}
		cm.sendSimple(where);
		status++;
	} else {
		if ((status == 1 && type == 1 && selection == -1 && mode == 0) || mode == -1) {
			cm.dispose();
		} else {
			if (status == 1) {
					cm.sendNext ("Alright, see you next time. Take care.");
					selectedMap = selection;
					status++
			} else if (status == 2) {
					cm.warp(maps[selectedMap], 0);
					cm.dispose();
			}
		}
	}
}

