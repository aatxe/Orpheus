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
package server.expeditions;

/**
 * 
 * @author kevintjuh93
 */
public enum MapleExpeditionType {
	UNDEFINED(-1), BALROG_EASY(0), BALROG_NORMAL(1), ZAKUM(2), HORNTAIL(3), CHAOS_ZAKUM(4), CHAOS_HORNTAIL(5), PINKBEAN(6);
	final int exped;
	final int limit;

	private MapleExpeditionType(int id) {
		exped = id;
		limit = 30;
	}

	private MapleExpeditionType(int id, int l) {
		exped = id;
		limit = l;
	}

	public int getId() {
		return exped;
	}

	public int getLimit() {
		return limit;
	}

	public static MapleExpeditionType getExpeditionById(int id) {
		for (MapleExpeditionType l : MapleExpeditionType.values()) {
			if (l.getId() == id) {
				return l;
			}
		}
		return MapleExpeditionType.UNDEFINED;
	}
}
