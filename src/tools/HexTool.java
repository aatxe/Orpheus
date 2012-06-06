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

import java.io.ByteArrayOutputStream;

public class HexTool {
	private static final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

	private static String toString(byte byteValue) {
		int tmp = byteValue << 8;
		char[] retstr = new char[] {HEX[(tmp >> 12) & 0x0F], HEX[(tmp >> 8) & 0x0F]};
		return String.valueOf(retstr);
	}

	public static String toString(byte[] bytes) {
		StringBuilder hexed = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			hexed.append(toString(bytes[i]));
			hexed.append(' ');
		}
		return hexed.substring(0, hexed.length() - 1);
	}

	public static byte[] getByteArrayFromHexString(String hex) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int nexti = 0;
		int nextb = 0;
		boolean highoc = true;
		outer: for (;;) {
			int number = -1;
			while (number == -1) {
				if (nexti == hex.length()) {
					break outer;
				}
				char chr = hex.charAt(nexti);
				if (chr >= '0' && chr <= '9') {
					number = chr - '0';
				} else if (chr >= 'a' && chr <= 'f') {
					number = chr - 'a' + 10;
				} else if (chr >= 'A' && chr <= 'F') {
					number = chr - 'A' + 10;
				} else {
					number = -1;
				}
				nexti++;
			}
			if (highoc) {
				nextb = number << 4;
				highoc = false;
			} else {
				nextb |= number;
				highoc = true;
				baos.write(nextb);
			}
		}
		return baos.toByteArray();
	}
}
