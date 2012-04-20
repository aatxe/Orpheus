/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package server.maps;

import client.ISkill;
import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import java.awt.Point;
import java.awt.Rectangle;
import net.MaplePacket;
import server.MapleStatEffect;
import server.life.MapleMonster;
import server.life.MobSkill;
import tools.MaplePacketCreator;

/**
 *
 * @author LaiLaiNoob
 */
public class MapleMist extends AbstractMapleMapObject {
    private Rectangle mistPosition;
    private MapleCharacter owner = null;
    private MapleMonster mob = null;
    private MapleStatEffect source;
    private MobSkill skill;
    private boolean isMobMist, isPoisonMist;
    private int skillDelay;

    public MapleMist(Rectangle mistPosition, MapleMonster mob, MobSkill skill) {
        this.mistPosition = mistPosition;
        this.mob = mob;
        this.skill = skill;
        isMobMist = true;
        isPoisonMist = true;
        skillDelay = 0;
    }

    public MapleMist(Rectangle mistPosition, MapleCharacter owner, MapleStatEffect source) {
        this.mistPosition = mistPosition;
        this.owner = owner;
        this.source = source;
        this.skillDelay = 8;
        this.isMobMist = false;
        switch (source.getSourceId()) {
            case 4221006: // Smoke Screen
                isPoisonMist = false;
                break;
            case 2111003: // FP mist
            case 12111005: // Flame Gear
            case 14111006: // Poison Bomb
                isPoisonMist = true;
                break;
        }
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.MIST;
    }

    @Override
    public Point getPosition() {
        return mistPosition.getLocation();
    }

    public ISkill getSourceSkill() {
        return SkillFactory.getSkill(source.getSourceId());
    }

    public boolean isMobMist() {
        return isMobMist;
    }

    public boolean isPoisonMist() {
        return isPoisonMist;
    }

    public int getSkillDelay() {
        return skillDelay;
    }

    public MapleMonster getMobOwner() {
        return mob;
    }

    public MapleCharacter getOwner() {
        return owner;
    }

    public Rectangle getBox() {
        return mistPosition;
    }

    @Override
    public void setPosition(Point position) {
        throw new UnsupportedOperationException();
    }

    public MaplePacket makeDestroyData() {
        return MaplePacketCreator.removeMist(getObjectId());
    }

    public MaplePacket makeSpawnData() {
        if (owner != null) {
            return MaplePacketCreator.spawnMist(getObjectId(), owner.getId(), getSourceSkill().getId(), owner.getSkillLevel(SkillFactory.getSkill(source.getSourceId())), this);
        }
        return MaplePacketCreator.spawnMist(getObjectId(), mob.getId(), skill.getSkillId(), skill.getSkillLevel(), this);
    }

    public MaplePacket makeFakeSpawnData(int level) {
        if (owner != null) {
            return MaplePacketCreator.spawnMist(getObjectId(), owner.getId(), getSourceSkill().getId(), level, this);
        }
        return MaplePacketCreator.spawnMist(getObjectId(), mob.getId(), skill.getSkillId(), skill.getSkillLevel(), this);
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.getSession().write(makeSpawnData());
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.getSession().write(makeDestroyData());
    }

    public boolean makeChanceResult() {
        return source.makeChanceResult();
    }
}
