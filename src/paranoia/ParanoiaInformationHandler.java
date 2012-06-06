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

import constants.ParanoiaConstants;

/**
 * @author Aaron Weiss
 */
public class ParanoiaInformationHandler {
	public static String getParanoiaVersion() {
		return "Paranoia v" + ParanoiaConstants.PARANOIA_VERSION;
	}
	
	public static boolean getValue(String constant) throws IllegalArgumentException {
		return ParanoiaInformationHandler.getValue(ParanoiaInformation.valueOf(constant));
	}
	
	public static boolean getValue(ParanoiaInformation pi) {
		return pi.get();
	}
	
	public static String getExplanation(String constant) throws IllegalArgumentException {
		return ParanoiaInformationHandler.getExplanation(ParanoiaInformation.valueOf(constant));
	}
	
	public static String getExplanation(ParanoiaInformation pi) {
		return pi.explain();
	}
	
	public static String getFormattedValue(String constant) throws IllegalArgumentException {
		return ParanoiaInformationHandler.getFormattedValue(ParanoiaInformation.valueOf(constant));
	}
	
	public static String getFormattedValue(ParanoiaInformation pi) {
		switch (pi) {
			default:
				return "Paranoia " + ((pi.get()) ? "uses" : "doesn't use") + " " + pi.toUsesString() + ".";
			case resetlogs:
				return "Paranoia " + ((pi.get()) ? "clears" : "doesn't clear") + " logs on startup.";
			case exactconsole:
				return "Paranoia " + ((pi.get()) ? "replicates" : "doesn't replicate") + " the console exactly.";
		}
	}
}
