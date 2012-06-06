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
package server.maps;

import java.util.concurrent.ScheduledFuture;
import server.MaplePortal;
import server.TimerManager;

public class MapMonitor {
	private ScheduledFuture<?> monitorSchedule;
	private MapleMap map;
	private MaplePortal portal;

	public MapMonitor(final MapleMap map, String portal) {
		this.map = map;
		this.portal = map.getPortal(portal);
		this.monitorSchedule = TimerManager.getInstance().register(new Runnable() {
			@Override
			public void run() {
				if (map.getCharacters().size() < 1) {
					cancelAction();
				}
			}
		}, 5000);
	}

	private void cancelAction() {
		monitorSchedule.cancel(false);
		map.killAllMonsters();
		map.clearDrops();
		if (portal != null) {
			portal.setPortalStatus(MaplePortal.OPEN);
		}
		map.resetReactors();
	}
}
