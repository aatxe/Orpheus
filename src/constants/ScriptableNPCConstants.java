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
package constants;

// This file is pretty much here to prevent ServerConstants from getting messy.
public class ScriptableNPCConstants {
	// An array of NPC IDs to make scriptable.
	public static final int[] SCRIPTABLE_NPCS = new int[]{9010000, 1202000};
	
	// An array of descriptions of said NPCs.
	public static final String[] SCRIPTABLE_NPCS_DESC = new String[]{"Maple Administrator", "Tutorial Lilin"};
}
