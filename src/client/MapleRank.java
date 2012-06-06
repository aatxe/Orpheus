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
/**
 * @author Aaron Weiss
 */
package client;

public enum MapleRank {
	PLAYER(0), DONOR(1), SUPPORT(2), GM(3), DEVELOPER(4), ADMINISTRATOR(5);
	
	private int rankid;
	
	private MapleRank(int rankid) {
		this.rankid = rankid;
	}
	
	public int getId() {
		return rankid;
	}
	
	public static MapleRank getById(int id) {
		for (MapleRank l : MapleRank.values()) {
			if (l.getId() == id) {
				return l;
			}
		}
		return null;
	}

	public boolean isA(MapleRank maplerank) {
		return getId() == maplerank.getId();
	}
	
	public String toStringWithArticle() {
		switch (this) {
			case PLAYER:
				return "a player";
			case DONOR:
				return "a donator";
			case SUPPORT:
				return "Support";
			case GM:
				return "a GM";
			case DEVELOPER:
				return "a Developer";
			case ADMINISTRATOR:
				return "an Administrator";
			default:
				return "Player";
		}
	}
	
	public String toString() {
		switch (this) {
			case PLAYER:
				return "Player";
			case DONOR:
				return "Donator";
			case SUPPORT:
				return "Support";
			case GM:
				return "GM";
			case DEVELOPER:
				return "Developer";
			case ADMINISTRATOR:
				return "Administrator";
			default:
				return "Player";
		}
	}
}
