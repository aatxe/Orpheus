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
package server.maps;

import net.LongValueHolder;

/**
 * 
 * @author AngelSL
 */
public enum FieldLimit implements LongValueHolder {
	JUMP(0x01), MOVEMENTSKILLS(0x02), SUMMON(0x04), DOOR(0x08), CHANGECHANNEL(0x10), CANNOTVIPROCK(0x40), CANNOTMINIGAME(0x80),
	// NoClue1(0x100), // APQ and a couple quest maps have this
	CANNOTUSEMOUNTS(0x200),
	// NoClue2(0x400), // Monster carnival?
	// NoClue3(0x800), // Monster carnival?
	CANNOTUSEPOTION(0x1000),
	// NoClue4(0x2000), // No notes
	// Unused(0x4000),
	// NoClue5(0x8000), // Ariant colosseum-related?
	// NoClue6(0x10000), // No notes
	CANNOTJUMPDOWN(0x20000);
	// NoClue7(0x40000); // Seems to .. disable Rush if 0x2 is set
	private long i;

	private FieldLimit(long i) {
		this.i = i;
	}

	@Override
	public long getValue() {
		return i;
	}

	public boolean check(int fieldlimit) {
		return (fieldlimit & i) == i;
	}
}
