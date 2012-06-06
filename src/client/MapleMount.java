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
package client;

import java.util.concurrent.ScheduledFuture;
import server.TimerManager;
import tools.MaplePacketCreator;

/**
 * @author PurpleMadness Patrick :O
 */
public class MapleMount {
	private int itemid;
	private int skillid;
	private int tiredness;
	private int exp;
	private int level;
	private ScheduledFuture<?> tirednessSchedule;
	private MapleCharacter owner;
	private boolean active;

	public MapleMount(MapleCharacter owner, int id, int skillid) {
		this.itemid = id;
		this.skillid = skillid;
		this.tiredness = 0;
		this.level = 1;
		this.exp = 0;
		this.owner = owner;
		active = true;
	}

	public int getItemId() {
		return itemid;
	}

	public int getSkillId() {
		return skillid;
	}

	/**
	 * 1902000 - Hog 1902001 - Silver Mane 1902002 - Red Draco 1902005 - Mimiana
	 * 1902006 - Mimio 1902007 - Shinjou 1902008 - Frog 1902009 - Ostrich
	 * 1902010 - Frog 1902011 - Turtle 1902012 - Yeti
	 * 
	 * @return the id
	 */
	public int getId() {
		if (this.itemid < 1903000) {
			return itemid - 1901999;
		}
		return 5;
	}

	public int getTiredness() {
		return tiredness;
	}

	public int getExp() {
		return exp;
	}

	public int getLevel() {
		return level;
	}

	public void setTiredness(int newtiredness) {
		this.tiredness = newtiredness;
		if (tiredness < 0) {
			tiredness = 0;
		}
	}

	public void increaseTiredness() {
		this.tiredness++;
		owner.getMap().broadcastMessage(MaplePacketCreator.updateMount(owner.getId(), this, false));
		if (tiredness > 99) {
			this.tiredness = 95;
			owner.dispelSkill(owner.getJobType() * 10000000 + 1004);
		}
	}

	public void setExp(int newexp) {
		this.exp = newexp;
	}

	public void setLevel(int newlevel) {
		this.level = newlevel;
	}

	public void setItemId(int newitemid) {
		this.itemid = newitemid;
	}

	public void startSchedule() {
		this.tirednessSchedule = TimerManager.getInstance().register(new Runnable() {
			@Override
			public void run() {
				increaseTiredness();
			}
		}, 60000, 60000);
	}

	public void cancelSchedule() {
		if (this.tirednessSchedule != null) {
			this.tirednessSchedule.cancel(false);
		}
	}

	public void setActive(boolean set) {
		this.active = set;
	}

	public boolean isActive() {
		return active;
	}

	public void empty() {
		cancelSchedule();
		this.tirednessSchedule = null;
		this.owner = null;
	}
}
