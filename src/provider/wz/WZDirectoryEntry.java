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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataEntity;
import provider.MapleDataEntry;
import provider.MapleDataFileEntry;

public class WZDirectoryEntry extends WZEntry implements MapleDataDirectoryEntry {
	private List<MapleDataDirectoryEntry> subdirs = new ArrayList<MapleDataDirectoryEntry>();
	private List<MapleDataFileEntry> files = new ArrayList<MapleDataFileEntry>();
	private Map<String, MapleDataEntry> entries = new HashMap<String, MapleDataEntry>();

	public WZDirectoryEntry(String name, int size, int checksum, MapleDataEntity parent) {
		super(name, size, checksum, parent);
	}

	public WZDirectoryEntry() {
		super(null, 0, 0, null);
	}

	public void addDirectory(MapleDataDirectoryEntry dir) {
		subdirs.add(dir);
		entries.put(dir.getName(), dir);
	}

	public void addFile(MapleDataFileEntry fileEntry) {
		files.add(fileEntry);
		entries.put(fileEntry.getName(), fileEntry);
	}

	public List<MapleDataDirectoryEntry> getSubdirectories() {
		return Collections.unmodifiableList(subdirs);
	}

	public List<MapleDataFileEntry> getFiles() {
		return Collections.unmodifiableList(files);
	}

	public MapleDataEntry getEntry(String name) {
		return entries.get(name);
	}
}
