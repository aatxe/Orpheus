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
package client.command.external;

/**
 * @author Aaron Weiss
 */
public class CommandLoaderException extends Exception {
	private static final long serialVersionUID = -7775141752143419523L;
	private int id;
	private String message;
	
	public CommandLoaderException(int id, String message) {
		this.id = id;
		this.message = message;
	}
	
	public int getIdentifier() {
		return this.id;
	}
	
	public String getMessage() {
		return this.message;
	}
}
