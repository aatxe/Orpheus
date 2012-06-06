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
package server.events.gm;

import client.MapleCharacter;
import java.util.concurrent.ScheduledFuture;
import server.TimerManager;
import tools.MaplePacketCreator;

/**
 * 
 * @author kevintjuh93
 */
public class MapleOla {
	private MapleCharacter chr;
	private long time = 0;
	private long timeStarted = 0;
	private ScheduledFuture<?> schedule = null;

	public MapleOla(final MapleCharacter chr) {
		this.chr = chr;
		this.schedule = TimerManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				if (chr.getMapId() >= 109030001 && chr.getMapId() <= 109030303)
					chr.changeMap(chr.getMap().getReturnMap());
				resetTimes();
			}
		}, 360000);
	}

	public void startOla() { // TODO: Messages
		chr.getMap().startEvent();
		chr.getClient().announce(MaplePacketCreator.getClock(360));
		this.timeStarted = System.currentTimeMillis();
		this.time = 360000;

		chr.getMap().getPortal("join00").setPortalStatus(true);
		chr.getClient().announce(MaplePacketCreator.serverNotice(0, "The portal has now opened. Press the up arrow key at the portal to enter."));
	}

	public boolean isTimerStarted() {
		return time > 0 && timeStarted > 0;
	}

	public long getTime() {
		return time;
	}

	public void resetTimes() {
		this.time = 0;
		this.timeStarted = 0;
		schedule.cancel(false);
	}

	public long getTimeLeft() {
		return time - (System.currentTimeMillis() - timeStarted);
	}
}
