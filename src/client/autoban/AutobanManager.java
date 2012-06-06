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
package client.autoban;

import client.MapleCharacter;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author kevintjuh93
 */
public class AutobanManager {
	private MapleCharacter chr;
	private Map<AutobanFactory, Integer> points = new HashMap<AutobanFactory, Integer>();
	private Map<AutobanFactory, Long> lastTime = new HashMap<AutobanFactory, Long>();
	private int misses = 0;
	private int lastmisses = 0;
	private int samemisscount = 0;
	private long spam[] = new long[20];
	private int timestamp[] = new int[20];
	private byte timestampcounter[] = new byte[20];

	public AutobanManager(MapleCharacter chr) {
		this.chr = chr;
	}

	public void addPoint(AutobanFactory fac, String reason) {
		if (lastTime.containsKey(fac)) {
			if (lastTime.get(fac) < (System.currentTimeMillis() - fac.getExpire())) {
				points.put(fac, points.get(fac) / 2); // So the points are not completely gone.
			}
		}
		if (fac.getExpire() != -1)
			lastTime.put(fac, System.currentTimeMillis());

		if (points.containsKey(fac)) {
			points.put(fac, points.get(fac) + 1);
		} else
			points.put(fac, 1);

		if (points.get(fac) >= fac.getMaximum()) {
			chr.autoban("Autobanned for " + fac.name() + " ;" + reason, 1);
			chr.sendPolice("You have been blocked by #bMooplePolice for the HACK reason#k.");
		}
	}

	public void addMiss() {
		this.misses++;
	}

	public void resetMisses() {
		if (lastmisses == misses && misses > 6) {
			samemisscount++;
		}
		if (samemisscount > 4)
			chr.autoban("Autobanned for : " + misses + " Miss godmode", 1);
		else if (samemisscount > 0)

			this.lastmisses = misses;
		this.misses = 0;
	}

	// Don't use the same type for more than 1 thing
	public void spam(int type) {
		this.spam[type] = System.currentTimeMillis();
	}

	public long getLastSpam(int type) {
		return spam[type];
	}

	/**
	 * Timestamp checker
	 * 
	 * <code>type</code>:<br>
	 * 0: HealOverTime<br>
	 * 1: Pet Food<br>
	 * 2: ItemSort<br>
	 * 3: ItemIdSort<br>
	 * 4: SpecialMove<br>
	 * 5: UseCatchItem<br>
	 * 
	 * @param type
	 *            type
	 * @return Timestamp checker
	 */
	public void setTimestamp(int type, int time) {
		if (this.timestamp[type] == time) {
			this.timestampcounter[type]++;
			if (this.timestampcounter[type] > 3) {
				chr.getClient().disconnect();
				// System.out.println("Same timestamp for type: " + type +
				// "; Character: " + chr);
			}
			return;
		}
		this.timestamp[type] = time;
	}
}
