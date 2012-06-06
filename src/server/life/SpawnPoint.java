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
package server.life;

import java.awt.Point;
import java.util.concurrent.atomic.AtomicInteger;
import client.MapleCharacter;

public class SpawnPoint {
	private int monster, mobTime, team;
	private Point pos;
	private long nextPossibleSpawn;
	private int mobInterval = 5000;
	private AtomicInteger spawnedMonsters = new AtomicInteger(0);
	private boolean immobile;

	public SpawnPoint(int monster, Point pos, boolean immobile, int mobTime, int mobInterval, int team) {
		super();
		this.monster = monster;
		this.pos = new Point(pos);
		this.mobTime = mobTime;
		this.team = team;
		this.immobile = immobile;
		this.mobInterval = mobInterval;
		this.nextPossibleSpawn = System.currentTimeMillis();
	}

	public boolean shouldSpawn() {
		if (mobTime < 0 || ((mobTime != 0 || immobile) && spawnedMonsters.get() > 0) || spawnedMonsters.get() > 2) {// lol
			return false;
		}
		return nextPossibleSpawn <= System.currentTimeMillis();
	}

	public MapleMonster getMonster() {
		MapleMonster mob = new MapleMonster(MapleLifeFactory.getMonster(monster));
		mob.setPosition(new Point(pos));
		mob.setTeam(team);
		spawnedMonsters.incrementAndGet();
		mob.addListener(new MonsterListener() {
			@Override
			public void monsterKilled(MapleMonster monster, MapleCharacter highestDamageChar) {
				nextPossibleSpawn = System.currentTimeMillis();
				if (mobTime > 0) {
					nextPossibleSpawn += mobTime * 1000;
				} else {
					nextPossibleSpawn += monster.getAnimationTime("die1");
				}
				spawnedMonsters.decrementAndGet();
			}
		});
		if (mobTime == 0) {
			nextPossibleSpawn = System.currentTimeMillis() + mobInterval;
		}
		return mob;
	}

	public Point getPosition() {
		return pos;
	}
}
