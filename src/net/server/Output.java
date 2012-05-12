package net.server;

import java.util.Calendar;
import java.util.TimeZone;
import tools.MapleLogger;
import constants.ParanoiaConstants;

public class Output {
	public static void print(String message) {
		Output.print(message, true);
	}
	
	public static void print(String message, boolean newLine) {
		System.out.print("[OrpheusMS] [" + now() + "] " + message + ((newLine) ? "\n" : ""));
		if (ParanoiaConstants.PARANOIA_CONSOLE_LOGGER) {
			MapleLogger.printFormatted(MapleLogger.PARANOIA_CONSOLE, "[OrpheusMS] " + message + ((newLine) ? "\n" : ""));
		}
	}

	@SuppressWarnings("unused")
	public static void printNewLine() {
		System.out.print("\n");
		if (ParanoiaConstants.PARANOIA_CONSOLE_LOGGER && ParanoiaConstants.REPLICATE_CONSOLE_EXACTLY) {
			MapleLogger.printFormatted(MapleLogger.PARANOIA_CONSOLE, "");
		}
	}
	
	public static String now() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
		int hours = calendar.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE);
		int seconds = calendar.get(Calendar.SECOND);
		// int milliseconds = calendar.get(Calendar.MILLISECOND);
		return ((hours + (TimeZone.getDefault().useDaylightTime() ? 1 : 0)) + ":" + ((minutes < 10) ? "0" + minutes : "" + minutes) + ":" + ((seconds < 10) ? "0" + seconds : "" + seconds));
	}
}
