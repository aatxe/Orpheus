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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataProvider;

public class XMLWZFile implements MapleDataProvider {
	private File root;
	private WZDirectoryEntry rootForNavigation;

	public XMLWZFile(File fileIn) {
		root = fileIn;
		rootForNavigation = new WZDirectoryEntry(fileIn.getName(), 0, 0, null);
		fillMapleDataEntitys(root, rootForNavigation);
	}

	private void fillMapleDataEntitys(File lroot, WZDirectoryEntry wzdir) {
		for (File file : lroot.listFiles()) {
			String fileName = file.getName();
			if (file.isDirectory() && !fileName.endsWith(".img")) {
				WZDirectoryEntry newDir = new WZDirectoryEntry(fileName, 0, 0, wzdir);
				wzdir.addDirectory(newDir);
				fillMapleDataEntitys(file, newDir);
			} else if (fileName.endsWith(".xml")) {
				wzdir.addFile(new WZFileEntry(fileName.substring(0, fileName.length() - 4), 0, 0, wzdir));
			}
		}
	}

	@Override
	public MapleData getData(String path) {
		File dataFile = new File(root, path + ".xml");
		File imageDataDir = new File(root, path);
		if (!dataFile.exists()) {
			throw new RuntimeException("Datafile " + path + " does not exist in " + root.getAbsolutePath());
		}
		FileInputStream fis;
		try {
			fis = new FileInputStream(dataFile);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Datafile " + path + " does not exist in " + root.getAbsolutePath());
		}
		final XMLDomMapleData domMapleData;
		try {
			domMapleData = new XMLDomMapleData(fis, imageDataDir.getParentFile());
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return domMapleData;
	}

	@Override
	public MapleDataDirectoryEntry getRoot() {
		return rootForNavigation;
	}
}
