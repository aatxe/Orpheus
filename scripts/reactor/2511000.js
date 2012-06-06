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
/*
2511000- Reactor for PPQ [Pirate PQ]
@author Jvlaple
*/

function act() {
    var eim = rm.getPlayer().getEventInstance();
    var now = eim.getProperty("openedBoxes");
    var nextNum = now + 1;
    eim.setProperty("openedBoxes", nextNum);
    rm.spawnMonster(9300109, 3);
    rm.spawnMonster(9300110, 5);
    rm.mapMessage(5, "Some monsters are summoned.");
}