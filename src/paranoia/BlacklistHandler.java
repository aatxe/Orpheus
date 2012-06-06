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
package paranoia;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import client.MapleClient;
import tools.MapleLogger;
import tools.Output;

/**
 * @author Aaron Weiss
 */
public class BlacklistHandler {
	public static final String BLACKLIST = "blacklist.csv";
	private static ArrayList<Integer> cache;
	
	public static void printBlacklistLog(String s, Integer accountId) {
		MapleLogger.printFormatted(MapleLogger.PARANOIA_BLACKLIST + MapleClient.getAccountNameById(accountId) + ".log", s);
	}
	
	public static void addToBlacklist(Integer accountId) {
		cache.add(accountId);
		try {
			FileOutputStream out = new FileOutputStream(BlacklistHandler.BLACKLIST, true);
			out.write(accountId.byteValue());
		} catch (IOException e) {
			Output.print("Something went wrong while updating the blacklist.");
			MapleLogger.print(MapleLogger.EXCEPTION_CAUGHT, e);
		}
	}
	
	public static boolean isBlacklisted(int accountId) {
		if (cache == null) {
			loadFromCsv(BlacklistHandler.BLACKLIST);
		}
		return cache.contains(accountId);
	}
	
	public static Integer[] getBlacklistedAccountIds() {
		if (cache != null) {
			return (Integer[]) cache.toArray();
		} else {
			return loadFromCsv(BlacklistHandler.BLACKLIST);
		}
	}
	
	public static void reloadBlacklist() {
		loadFromCsv(BlacklistHandler.BLACKLIST);
	}
	
	protected static Integer[] loadFromCsv(final String csv) {
		cache = new ArrayList<Integer>();
		System.gc(); // garbage collect, to make sure we clean up the old cache object.
		try {
			FileInputStream in = new FileInputStream(csv);
			BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
			while (br.ready()) {
				String line = br.readLine();
				String[] sp = line.split(",");
				for (String s : sp) {
					cache.add(Integer.parseInt(s));
				}
			}
		} catch (FileNotFoundException e) {
			Output.print("blacklist.csv is missing.");
			MapleLogger.print(MapleLogger.EXCEPTION_CAUGHT, e);
		} catch (IOException e) {
			Output.print("Something went wrong while loading the blacklist.");
			MapleLogger.print(MapleLogger.EXCEPTION_CAUGHT, e);
		}
		return (Integer[]) cache.toArray();
	}
}
