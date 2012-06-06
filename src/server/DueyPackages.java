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
package server;

import client.IItem;
import java.util.Calendar;

public class DueyPackages {
	private String sender = null;
	private IItem item = null;
	private int mesos = 0;
	private int day;
	private int month;
	private int year;
	private int packageId = 0;

	public DueyPackages(int pId, IItem item) {
		this.item = item;
		packageId = pId;
	}

	public DueyPackages(int pId) { // Meso only package.
		this.packageId = pId;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String name) {
		sender = name;
	}

	public IItem getItem() {
		return item;
	}

	public int getMesos() {
		return mesos;
	}

	public void setMesos(int set) {
		mesos = set;
	}

	public int getPackageId() {
		return packageId;
	}

	public long sentTimeInMilliseconds() {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day);
		return cal.getTimeInMillis();
	}

	public void setSentTime(String sentTime) {
		day = Integer.parseInt(sentTime.substring(0, 2));
		month = Integer.parseInt(sentTime.substring(3, 5));
		year = Integer.parseInt(sentTime.substring(6, 10));
	}
}
