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
	if (pi.isQuestStarted(1041))
		pi.warp(1010100, 4);
	else if (pi.isQuestStarted(1042))
		pi.warp(1010200, 4);
	else if (pi.isQuestStarted(1043))
		pi.warp(1010300, 4);
	else if (pi.isQuestStarted(1044))
		pi.warp(1010400, 4);
	else {
		pi.message("Only the adventurers that have been trained by Mai may enter.");
		return false;
	}
	return true;
}