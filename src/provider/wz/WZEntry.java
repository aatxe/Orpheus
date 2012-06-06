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
package provider.wz;

import provider.MapleDataEntity;
import provider.MapleDataEntry;

public class WZEntry implements MapleDataEntry {
	private String name;
	private int size;
	private int checksum;
	private int offset;
	private MapleDataEntity parent;

	public WZEntry(String name, int size, int checksum, MapleDataEntity parent) {
		super();
		this.name = name;
		this.size = size;
		this.checksum = checksum;
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	public int getSize() {
		return size;
	}

	public int getChecksum() {
		return checksum;
	}

	public int getOffset() {
		return offset;
	}

	public MapleDataEntity getParent() {
		return parent;
	}
}
