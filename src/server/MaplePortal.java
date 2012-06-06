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
package server;

import java.awt.Point;
import client.MapleClient;

public interface MaplePortal {
	public final int MAP_PORTAL = 2;
	public final int DOOR_PORTAL = 6;
	public static boolean OPEN = true;
	public static boolean CLOSED = false;

	int getType();

	int getId();

	Point getPosition();

	String getName();

	String getTarget();

	String getScriptName();

	void setScriptName(String newName);

	void setPortalStatus(boolean newStatus);

	boolean getPortalStatus();

	int getTargetMapId();

	void enterPortal(MapleClient c);

	void setPortalState(boolean state);

	boolean getPortalState();
}
