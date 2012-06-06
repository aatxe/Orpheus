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
package client.command;

import client.MapleCharacter;
import client.command.external.Commands;

/**
 * @author Aaron Weiss
 */
public abstract class EnumeratedCommands extends Commands {
	protected static void getHelp(MapleCharacter chr) {
		EnumeratedCommands.getHelp(-1, chr);
	}
	
	protected static void getHelp(int page, MapleCharacter chr) {
		chr.dropMessage("Command.getHelp() was not overridden.");
	}
	
	public static enum Command {};
}
