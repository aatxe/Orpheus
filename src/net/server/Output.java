package net.server;

import java.util.Calendar;
import java.util.TimeZone;

public class Output {
	public static void print(String message) {
		Output.print(message, true);
	}
	
	public static void print(String message, boolean newLine) {
		System.out.print("[OrpheusMS] [" + now() + "] " + message + ((newLine) ? "\n" : ""));
	}

	public static void printNewLine() {
		System.out.print("\n");
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
