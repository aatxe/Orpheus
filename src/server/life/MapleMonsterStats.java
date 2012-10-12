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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import server.life.MapleLifeFactory.BanishInfo;
import server.life.MapleLifeFactory.loseItem;
import server.life.MapleLifeFactory.selfDestruction;

/**
 * @author Frz
 */
public class MapleMonsterStats {
	private int exp, hp, mp, level, PADamage, dropPeriod, cp, buffToGive,
			removeAfter;
	private boolean boss, undead, ffaLoot, isExplosiveReward, firstAttack,
			removeOnMiss;
	private String name;
	private Map<String, Integer> animationTimes = new HashMap<String, Integer>();
	private Map<Element, ElementalEffectiveness> resistance = new HashMap<Element, ElementalEffectiveness>();
	private List<Integer> revives = Collections.emptyList();
	private byte tagColor, tagBgColor;
	private List<MobSkillEntry> skills = new ArrayList<MobSkillEntry>();
	private CoolDamageEntry cool = null;
	private BanishInfo banish = null;
	private List<loseItem> loseItem = null;
	private selfDestruction selfDestruction = null;

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public int getHp() {
		return hp;
	}

	public void setHp(int hp) {
		this.hp = hp;
	}

	public int getMp() {
		return mp;
	}

	public void setMp(int mp) {
		this.mp = mp;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int removeAfter() {
		return removeAfter;
	}

	public void setRemoveAfter(int removeAfter) {
		this.removeAfter = removeAfter;
	}

	public int getDropPeriod() {
		return dropPeriod;
	}

	public void setDropPeriod(int dropPeriod) {
		this.dropPeriod = dropPeriod;
	}

	public void setBoss(boolean boss) {
		this.boss = boss;
	}

	public boolean isBoss() {
		return boss;
	}

	public void setFfaLoot(boolean ffaLoot) {
		this.ffaLoot = ffaLoot;
	}

	public boolean isFfaLoot() {
		return ffaLoot;
	}

	public void setAnimationTime(String name, int delay) {
		animationTimes.put(name, delay);
	}

	public int getAnimationTime(String name) {
		Integer ret = animationTimes.get(name);
		if (ret == null) {
			return 500;
		}
		return ret.intValue();
	}

	public boolean isMobile() {
		return animationTimes.containsKey("move") || animationTimes.containsKey("fly");
	}

	public List<Integer> getRevives() {
		return revives;
	}

	public void setRevives(List<Integer> revives) {
		this.revives = revives;
	}

	public void setUndead(boolean undead) {
		this.undead = undead;
	}

	public boolean getUndead() {
		return undead;
	}

	public void setEffectiveness(Element e, ElementalEffectiveness ee) {
		resistance.put(e, ee);
	}

	public ElementalEffectiveness getEffectiveness(Element e) {
		ElementalEffectiveness elementalEffectiveness = resistance.get(e);
		if (elementalEffectiveness == null) {
			return ElementalEffectiveness.NORMAL;
		} else {
			return elementalEffectiveness;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte getTagColor() {
		return tagColor;
	}

	public void setTagColor(int tagColor) {
		this.tagColor = (byte) tagColor;
	}

	public byte getTagBgColor() {
		return tagBgColor;
	}

	public void setTagBgColor(int tagBgColor) {
		this.tagBgColor = (byte) tagBgColor;
	}

	public void setSkills(List<MobSkillEntry> entries) {
		for (MobSkillEntry entry : entries) {
			this.skills.add(entry);
		}
	}

	public List<MobSkillEntry> getSkills() {
		return Collections.unmodifiableList(this.skills);
	}

	public int getNoSkills() {
		return this.skills.size();
	}

	public boolean hasSkill(int skillId, int level) {
		for (MobSkillEntry skill : skills) {
			if (skill.skillId == skillId && skill.level == level) {
				return true;
			}
		}
		return false;
	}

	public void setFirstAttack(boolean firstAttack) {
		this.firstAttack = firstAttack;
	}

	public boolean isFirstAttack() {
		return firstAttack;
	}

	public void setBuffToGive(int buff) {
		this.buffToGive = buff;
	}

	public int getBuffToGive() {
		return buffToGive;
	}

	void removeEffectiveness(Element e) {
		resistance.remove(e);
	}

	public BanishInfo getBanishInfo() {
		return banish;
	}

	public void setBanishInfo(BanishInfo banish) {
		this.banish = banish;
	}

	public int getPADamage() {
		return PADamage;
	}

	public void setPADamage(int PADamage) {
		this.PADamage = PADamage;
	}

	public int getCP() {
		return cp;
	}

	public void setCP(int cp) {
		this.cp = cp;
	}

	public List<loseItem> loseItem() {
		return loseItem;
	}

	public void addLoseItem(loseItem li) {
		if (loseItem == null) {
			loseItem = new LinkedList<loseItem>();
		}
		loseItem.add(li);
	}

	public selfDestruction selfDestruction() {
		return selfDestruction;
	}

	public void setSelfDestruction(selfDestruction sd) {
		this.selfDestruction = sd;
	}

	public void setExplosiveReward(boolean isExplosiveReward) {
		this.isExplosiveReward = isExplosiveReward;
	}

	public boolean isExplosiveReward() {
		return isExplosiveReward;
	}

	public void setRemoveOnMiss(boolean removeOnMiss) {
		this.removeOnMiss = removeOnMiss;
	}

	public boolean removeOnMiss() {
		return removeOnMiss;
	}

	public void setCool(CoolDamageEntry cool) {
		this.cool = cool;
	}

	public CoolDamageEntry getCool() {
		return cool;
	}
}
