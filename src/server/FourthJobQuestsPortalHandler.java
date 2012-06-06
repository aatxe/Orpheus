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

import client.MapleCharacter;
import client.MapleJob;
import net.StringValueHolder;
import net.server.MaplePartyCharacter;
import tools.MaplePacketCreator;

/**
 * 
 * @author AngelSL
 */
public class FourthJobQuestsPortalHandler {
	public enum FourthJobQuests implements StringValueHolder {
		RUSH("s4rush"), BERSERK("s4berserk");
		private final String name;

		private FourthJobQuests(String Newname) {
			this.name = Newname;
		}

		@Override
		public String getValue() {
			return name;
		}
	}

	public static boolean handlePortal(String name, MapleCharacter c) {
		if (name.equals(FourthJobQuests.RUSH.getValue())) {
			if (!(c.getParty().getLeader().getId() == c.getId()) && !checkRush(c)) {
				c.dropMessage("You step into the portal, but it swiftly kicks you out.");
				c.getClient().announce(MaplePacketCreator.enableActions());
			}
			if (!(c.getParty().getLeader().getId() == c.getId()) && checkRush(c)) {
				c.dropMessage("You're not the party leader.");
				c.getClient().announce(MaplePacketCreator.enableActions());
				return true;
			}
			if (!checkRush(c)) {
				c.dropMessage("Someone in your party is not a 4th Job warrior.");
				c.getClient().announce(MaplePacketCreator.enableActions());
				return true;
			}
			c.getClient().getChannelServer().getEventSM().getEventManager("4jrush").startInstance(c.getParty(), c.getMap());
			return true;
		} else if (name.equals(FourthJobQuests.BERSERK.getValue())) {
			if (!c.haveItem(4031475)) {
				c.dropMessage("The portal to the Forgotten Shrine is locked");
				c.getClient().announce(MaplePacketCreator.enableActions());
				return true;
			}
			c.getClient().getChannelServer().getEventSM().getEventManager("4jberserk").startInstance(c.getParty(), c.getMap());
			return true;
		}
		return false;
	}

	private static boolean checkRush(MapleCharacter c) {
		for (MaplePartyCharacter mpc : c.getParty().getMembers()) {
			if (mpc.getJobId() % 100 != 2 || !mpc.getJob().isA(MapleJob.WARRIOR)) {
				return false;
			}
		}
		return true;
	}
}
