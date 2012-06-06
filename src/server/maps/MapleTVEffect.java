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

import java.util.List;
import client.MapleCharacter;
import java.util.ArrayList;
import net.server.Server;
import server.TimerManager;
import tools.MaplePacketCreator;

/*
 * MapleTVEffect
 * @author MrXotic
 */
public class MapleTVEffect {
	private List<String> message = new ArrayList<String>(5);
	private MapleCharacter user;
	private static boolean active;
	private int type;
	private byte world;
	private MapleCharacter partner;

	public MapleTVEffect(MapleCharacter user_, MapleCharacter partner_, List<String> msg, int type_, byte world_) {
		this.message = msg;
		this.user = user_;
		this.type = type_;
		this.world = world_;
		this.partner = partner_;
		broadcastTV(true);
	}

	public static boolean isActive() {
		return active;
	}

	private void setActive(boolean set) {
		active = set;
	}

	private void broadcastTV(boolean active_) {
		Server server = Server.getInstance();
		setActive(active_);
		if (active_) {
			server.broadcastMessage(world, MaplePacketCreator.enableTV());
			server.broadcastMessage(world, MaplePacketCreator.sendTV(user, message, type <= 2 ? type : type - 3, partner));
			int delay = 15000;
			if (type == 4) {
				delay = 30000;
			} else if (type == 5) {
				delay = 60000;
			}
			TimerManager.getInstance().schedule(new Runnable() {
				@Override
				public void run() {
					broadcastTV(false);
				}
			}, delay);
		} else {
			server.broadcastMessage(world, MaplePacketCreator.removeTV());
		}
	}
}
