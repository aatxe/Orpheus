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
import provider.MapleData;
import provider.MapleDataTool;
import server.maps.MapleGenericPortal;
import server.maps.MapleMapPortal;

public class PortalFactory {
	private int nextDoorPortal;

	public PortalFactory() {
		nextDoorPortal = 0x80;
	}

	public MaplePortal makePortal(int type, MapleData portal) {
		MapleGenericPortal ret = null;
		if (type == MaplePortal.MAP_PORTAL) {
			ret = new MapleMapPortal();
		} else {
			ret = new MapleGenericPortal(type);
		}
		loadPortal(ret, portal);
		return ret;
	}

	private void loadPortal(MapleGenericPortal myPortal, MapleData portal) {
		myPortal.setName(MapleDataTool.getString(portal.getChildByPath("pn")));
		myPortal.setTarget(MapleDataTool.getString(portal.getChildByPath("tn")));
		myPortal.setTargetMapId(MapleDataTool.getInt(portal.getChildByPath("tm")));
		int x = MapleDataTool.getInt(portal.getChildByPath("x"));
		int y = MapleDataTool.getInt(portal.getChildByPath("y"));
		myPortal.setPosition(new Point(x, y));
		String script = MapleDataTool.getString("script", portal, null);
		if (script != null && script.equals("")) {
			script = null;
		}
		myPortal.setScriptName(script);
		if (myPortal.getType() == MaplePortal.DOOR_PORTAL) {
			myPortal.setId(nextDoorPortal);
			nextDoorPortal++;
		} else {
			myPortal.setId(Integer.parseInt(portal.getName()));
		}
	}
}
