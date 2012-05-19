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
	
	protected static Integer[] loadFromCsv(final String csv) {
		cache = new ArrayList<Integer>();
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
