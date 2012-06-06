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

import java.awt.Point;
import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import tools.MaplePacketCreator;

/**
 * 
 * @author Jan
 */
public class MapleSummon extends AbstractAnimatedMapleMapObject {
	private MapleCharacter owner;
	private byte skillLevel;
	private int skill, hp;
	private SummonMovementType movementType;

	public MapleSummon(MapleCharacter owner, int skill, Point pos, SummonMovementType movementType) {
		this.owner = owner;
		this.skill = skill;
		this.skillLevel = owner.getSkillLevel(SkillFactory.getSkill(skill));
		if (skillLevel == 0)
			throw new RuntimeException();

		this.movementType = movementType;
		setPosition(pos);
	}

	public void sendSpawnData(MapleClient client) {
		if (this != null)
			client.getSession().write(MaplePacketCreator.spawnSummon(this, false));

	}

	public void sendDestroyData(MapleClient client) {
		client.getSession().write(MaplePacketCreator.removeSummon(this, true));
	}

	public MapleCharacter getOwner() {
		return owner;
	}

	public int getSkill() {
		return skill;
	}

	public int getHP() {
		return hp;
	}

	public void addHP(int delta) {
		this.hp += delta;
	}

	public SummonMovementType getMovementType() {
		return movementType;
	}

	public boolean isStationary() {
		return (skill == 3111002 || skill == 3211002 || skill == 5211001 || skill == 13111004);
	}

	public byte getSkillLevel() {
		return skillLevel;
	}

	@Override
	public MapleMapObjectType getType() {
		return MapleMapObjectType.SUMMON;
	}

	public final boolean isPuppet() {
		switch (skill) {
			case 3111002:
			case 3211002:
			case 13111004:
				return true;
		}
		return false;
	}
}
