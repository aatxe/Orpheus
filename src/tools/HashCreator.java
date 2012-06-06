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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Aaron Weiss
 */
public class HashCreator {
	private static MessageDigest hash;
	
	public static void reset() {
		hash = null;	
	}
	
	public static void setMessageDigest(MessageDigest md) {
		hash = md;
	}
	
	public static String getHash(String message) throws NoSuchAlgorithmException {
		if (hash == null) {
			hash = MessageDigest.getInstance("SHA-1");
		}
		return HashCreator.getHash(hash, message);
	}
	
	public static String getHash(String type, String message) throws NoSuchAlgorithmException {
		return HashCreator.getHash(MessageDigest.getInstance(type), message);
	}
	
	public static String getHash(MessageDigest md, String message) {
		md.update(message.getBytes());
		return HexTool.toString(md.digest()).replace(" ", "").toLowerCase();
    }
	
	// Just a simple tester for the hash calculator.
	public static void main(String[] args) { 
		try {
			System.out.println(getHash("password"));
			reset();
			setMessageDigest(MessageDigest.getInstance("SHA1"));
			System.out.println(getHash("password"));
			System.out.println(getHash(MessageDigest.getInstance("SHA1"), "password"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
}
