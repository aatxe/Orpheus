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
/* Ludi PQ Crack Reactor ^_^
 *@Author Jvlaple
  *2201003.js
 */
 
function act() {
    if (rm.getPlayer().getMapId() == 922010900) {
        rm.mapMessage(5, "Alishar has been summoned.");
        rm.spawnMonster(9300012, 941, 184);
    } else if(rm.getPlayer().getMapId() == 922010700) {
        rm.mapMessage(5, "Rombard has been summoned somewhere in the map.");
        rm.spawnMonster(9300010, 1, -211);
    }
}