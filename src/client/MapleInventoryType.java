/*
 	OrpheusMS: MapleStory Private Server based on OdinMS
    Copyright (C) 2012 Aaron Weiss

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
package client;

/**
 * @author Matze
 */
public enum MapleInventoryType {
	UNDEFINED(0), EQUIP(1), USE(2), SETUP(3), ETC(4), CASH(5), EQUIPPED(-1);
	final byte type;

	private MapleInventoryType(int type) {
		this.type = (byte) type;
	}

	public byte getType() {
		return type;
	}

	public short getBitfieldEncoding() {
		return (short) (2 << type);
	}

	public static MapleInventoryType getByType(byte type) {
		for (MapleInventoryType l : MapleInventoryType.values()) {
			if (l.getType() == type) {
				return l;
			}
		}
		return null;
	}

	public static MapleInventoryType getByWZName(String name) {
		if (name.equals("Install")) {
			return SETUP;
		} else if (name.equals("Consume")) {
			return USE;
		} else if (name.equals("Etc")) {
			return ETC;
		} else if (name.equals("Cash")) {
			return CASH;
		} else if (name.equals("Pet")) {
			return CASH;
		}
		return UNDEFINED;
	}
}
