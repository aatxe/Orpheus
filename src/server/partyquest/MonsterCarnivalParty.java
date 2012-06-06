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
package server.partyquest;

import client.MapleCharacter;
import java.util.LinkedList;
import java.util.List;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

/**
 * @author Rob //Thanks :3 - LOST MOTIVATION >=(
 */
public class MonsterCarnivalParty {

	private List<MapleCharacter> members = new LinkedList<MapleCharacter>();
	private MapleCharacter leader;
	private byte team;
	private short availableCP = 0, totalCP = 0;
	private int summons = 7;
	private boolean winner = false;

	public MonsterCarnivalParty(final MapleCharacter owner, final List<MapleCharacter> members1, final byte team1) {
		leader = owner;
		members = members1;
		team = team1;

		for (final MapleCharacter chr : members) {
			chr.setCarnivalParty(this);
			chr.setTeam(team);
		}
	}

	public final MapleCharacter getLeader() {
		return leader;
	}

	public void addCP(MapleCharacter player, int ammount) {
		totalCP += ammount;
		availableCP += ammount;
		player.addCP(ammount);
	}

	public int getTotalCP() {
		return totalCP;
	}

	public int getAvailableCP() {
		return availableCP;
	}

	public void useCP(MapleCharacter player, int ammount) {
		availableCP -= ammount;
		player.useCP(ammount);
	}

	public List<MapleCharacter> getMembers() {
		return members;
	}

	public int getTeam() {
		return team;
	}

	public void warpOut(final int map) {
		for (MapleCharacter chr : members) {
			chr.changeMap(map, 0);
			chr.setCarnivalParty(null);
			chr.setCarnival(null);
		}
		members.clear();
	}

	public void warp(final MapleMap map, final int portalid) {
		for (MapleCharacter chr : members) {
			chr.changeMap(map, map.getPortal(portalid));
		}
	}

	public void warpOut() {
		if (winner == true)
			warpOut(980000003 + (leader.getCarnival().getRoom() * 100));
		else
			warpOut(980000004 + (leader.getCarnival().getRoom() * 100));
	}

	public boolean allInMap(MapleMap map) {
		boolean status = true;
		for (MapleCharacter chr : members) {
			if (chr.getMap() != map) {
				status = false;
			}
		}
		return status;
	}

	public void removeMember(MapleCharacter chr) {
		members.remove(chr);
		chr.changeMap(980000010);
		chr.setCarnivalParty(null);
		chr.setCarnival(null);
	}

	public boolean isWinner() {
		return winner;
	}

	public void setWinner(boolean status) {
		winner = status;
	}

	public void displayMatchResult() {
		final String effect = winner ? "quest/carnival/win" : "quest/carnival/lose";

		for (final MapleCharacter chr : members) {
			chr.announce(MaplePacketCreator.showEffect(effect));
		}
	}

	public void summon() {
		this.summons--;
	}

	public boolean canSummon() {
		return this.summons > 0;
	}
}
