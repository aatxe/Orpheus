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
package tools;

import java.util.Calendar;
import java.util.TimeZone;
import constants.ParanoiaConstants;

/**
 * @author Aaron Weiss
 */
public class Output {
	public static void print(String message) {
		Output.print(message, true);
	}
	
	public static void print(String message, boolean newLine) {
		System.out.print("[OrpheusMS] [" + now() + "] " + message + ((newLine) ? "\n" : ""));
		if (ParanoiaConstants.PARANOIA_CONSOLE_LOGGER) {
			MapleLogger.printFormatted(MapleLogger.PARANOIA_CONSOLE, "[OrpheusMS] " + message, newLine);
		}
	}

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
		return ((hours + (TimeZone.getDefault().useDaylightTime() ? 0 : -1)) + ":" + ((minutes < 10) ? "0" + minutes : "" + minutes) + ":" + ((seconds < 10) ? "0" + seconds : "" + seconds));
	}
	
	public static String joinStringFrom(String arr[], int start) {
		StringBuilder builder = new StringBuilder();
		for (int i = start; i < arr.length; i++) {
			builder.append(arr[i]);
			if (i != arr.length - 1) {
				builder.append(" ");
			}
		}
		return builder.toString();
	}
}
