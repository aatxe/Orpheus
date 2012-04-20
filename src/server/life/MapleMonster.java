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
package server.life;

import client.ISkill;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import client.SkillFactory;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import java.awt.Point;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;
import tools.Randomizer;
import net.MaplePacket;
import net.server.Channel;
import net.server.MapleParty;
import net.server.MaplePartyCharacter;
import scripting.event.EventInstanceManager;
import server.TimerManager;
import server.life.MapleLifeFactory.BanishInfo;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.ArrayMap;
import tools.MaplePacketCreator;
import tools.Pair;

public class MapleMonster extends AbstractLoadedMapleLife {

    private MapleMonsterStats stats;
    private int hp, mp;
    private WeakReference<MapleCharacter> controller = new WeakReference<MapleCharacter>(null);
    private boolean controllerHasAggro, controllerKnowsAboutAggro;
    private Collection<AttackerEntry> attackers = new LinkedList<AttackerEntry>();
    private EventInstanceManager eventInstance = null;
    private Collection<MonsterListener> listeners = new LinkedList<MonsterListener>();
    private MapleCharacter highestDamageChar;
    private EnumMap<MonsterStatus, MonsterStatusEffect> stati = new EnumMap<MonsterStatus, MonsterStatusEffect>(MonsterStatus.class);
    private MapleMap map;
    private int VenomMultiplier = 0;
    private boolean fake = false;
    private boolean dropsDisabled = false;
    private List<Pair<Integer, Integer>> usedSkills = new ArrayList<Pair<Integer, Integer>>();
    private Map<Pair<Integer, Integer>, Integer> skillsUsed = new HashMap<Pair<Integer, Integer>, Integer>();
    private List<Integer> stolenItems = new ArrayList<Integer>();
    private int team;
    public ReentrantLock monsterLock = new ReentrantLock();

    public MapleMonster(int id, MapleMonsterStats stats) {
        super(id);
        initWithStats(stats);
    }

    public MapleMonster(MapleMonster monster) {
        super(monster);
        initWithStats(monster.stats);
    }

    private void initWithStats(MapleMonsterStats stats) {
        setStance(5);
        this.stats = stats;
        hp = stats.getHp();
        mp = stats.getMp();
    }

    public void disableDrops() {
        this.dropsDisabled = true;
    }

    public boolean dropsDisabled() {
        return dropsDisabled;
    }

    public void setMap(MapleMap map) {
        this.map = map;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMaxHp() {
        return stats.getHp();
    }

    public int getMp() {
        return mp;
    }

    public void setMp(int mp) {
        if (mp < 0) {
            mp = 0;
        }
        this.mp = mp;
    }

    public int getMaxMp() {
        return stats.getMp();
    }

    public int getExp() {
        return stats.getExp();
    }

    int getLevel() {
        return stats.getLevel();
    }

    public int getCP() {
        return stats.getCP();
    }

    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    public int getVenomMulti() {
        return this.VenomMultiplier;
    }

    public void setVenomMulti(int multiplier) {
        this.VenomMultiplier = multiplier;
    }

    public MapleMonsterStats getStats() {
        return stats;
    }

    public boolean isBoss() {
        return stats.isBoss() || isHT();
    }

    public int getAnimationTime(String name) {
        return stats.getAnimationTime(name);
    }

    private List<Integer> getRevives() {
        return stats.getRevives();
    }

    private byte getTagColor() {
        return stats.getTagColor();
    }

    private byte getTagBgColor() {
        return stats.getTagBgColor();
    }

    /**
     *
     * @param from the player that dealt the damage
     * @param damage
     * @param updateAttackTime
     */
    public void damage(MapleCharacter from, int damage, boolean updateAttackTime) {
        AttackerEntry attacker = null;
        if (from.getParty() != null) {
            attacker = new PartyAttackerEntry(from.getParty().getId(), from.getClient().getChannelServer());
        } else {
            attacker = new SingleAttackerEntry(from, from.getClient().getChannelServer());
        }
        boolean replaced = false;
        for (AttackerEntry aentry : attackers) {
            if (aentry.equals(attacker)) {
                attacker = aentry;
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            attackers.add(attacker);
        }
        int rDamage = Math.max(0, Math.min(damage, this.hp));
        attacker.addDamage(from, rDamage, updateAttackTime);
        this.hp -= rDamage;
        int remhppercentage = (int) Math.ceil((this.hp * 100.0) / getMaxHp());
        if (remhppercentage < 1) {
            remhppercentage = 1;
        }
        long okTime = System.currentTimeMillis() - 4000;
        if (hasBossHPBar()) {
            from.getMap().broadcastMessage(makeBossHPBarPacket(), getPosition());
        } else if (!isBoss()) {
            for (AttackerEntry mattacker : attackers) {
                for (AttackingMapleCharacter cattacker : mattacker.getAttackers()) {
                    if (cattacker.getAttacker().getMap() == from.getMap()) {
                        if (cattacker.getLastAttackTime() >= okTime) {
                            cattacker.getAttacker().getClient().getSession().write(MaplePacketCreator.showMonsterHP(getObjectId(), remhppercentage));
                        }
                    }
                }
            }
        }
    }

    public void heal(int hp, int mp) {
        int hp2Heal = getHp() + hp;
        int mp2Heal = getMp() + mp;
        if (hp2Heal >= getMaxHp()) {
            hp2Heal = getMaxHp();
        }
        if (mp2Heal >= getMaxMp()) {
            mp2Heal = getMaxMp();
        }
        setHp(hp2Heal);
        setMp(mp2Heal);
        getMap().broadcastMessage(MaplePacketCreator.healMonster(getObjectId(), hp));
    }

    public boolean isAttackedBy(MapleCharacter chr) {
        for (AttackerEntry aentry : attackers) {
            if (aentry.contains(chr)) {
                return true;
            }
        }
        return false;
    }

    public void giveExpToCharacter(MapleCharacter attacker, int exp, boolean highestDamage, int numExpSharers) {
        if (highestDamage) {
            if (eventInstance != null) {
                eventInstance.monsterKilled(attacker, this);
            }
            highestDamageChar = attacker;
        }
        if (attacker.getHp() > 0) {
            int personalExp = exp;
            if (exp > 0) {
                Integer holySymbol = attacker.getBuffedValue(MapleBuffStat.HOLY_SYMBOL);
                if (holySymbol != null) {
                    if (numExpSharers == 1) {
                        personalExp *= 1.0 + (holySymbol.doubleValue() / 500.0);
                    } else {
                        personalExp *= 1.0 + (holySymbol.doubleValue() / 100.0);
                    }
                }
                if (stati.containsKey(MonsterStatus.SHOWDOWN)) {
                    personalExp *= (stati.get(MonsterStatus.SHOWDOWN).getStati().get(MonsterStatus.SHOWDOWN).doubleValue() / 100.0 + 1.0);
                }
            }
            if (exp < 0) {//O.O ><
                personalExp = Integer.MAX_VALUE;
            }
            attacker.gainExp(personalExp, true, false, highestDamage);
            attacker.increaseEquipExp(personalExp);//better place
            attacker.mobKilled(this.getId());
        }
    }

    public MapleCharacter killBy(MapleCharacter killer) {
        long totalBaseExpL = (this.getExp() * killer.getClient().getPlayer().getExpRate());
        int totalBaseExp = (int) (Math.min(Integer.MAX_VALUE, totalBaseExpL));
        AttackerEntry highest = null;
        int highdamage = 0;
        for (AttackerEntry attackEntry : attackers) {
            if (attackEntry.getDamage() > highdamage) {
                highest = attackEntry;
                highdamage = attackEntry.getDamage();
            }
        }
        for (AttackerEntry attackEntry : attackers) {
            attackEntry.killedMob(killer.getMap(), (int) Math.ceil(totalBaseExp * ((double) attackEntry.getDamage() / getMaxHp())), attackEntry == highest);
        }
        if (this.getController() != null) { // this can/should only happen when a hidden gm attacks the monster
            getController().getClient().getSession().write(MaplePacketCreator.stopControllingMonster(this.getObjectId()));
            getController().stopControllingMonster(this);
        }
        final List<Integer> toSpawn = this.getRevives();
        if (toSpawn != null) {
            final MapleMap reviveMap = killer.getMap();
            if (toSpawn.contains(9300216) && reviveMap.getId() > 925000000 && reviveMap.getId() < 926000000) {
                reviveMap.broadcastMessage(MaplePacketCreator.playSound("Dojang/clear"));
                reviveMap.broadcastMessage(MaplePacketCreator.showEffect("dojang/end/clear"));
            }
            Pair<Integer, String> timeMob = reviveMap.getTimeMob();
            if (timeMob != null) {
                if (toSpawn.contains(timeMob.getLeft())) {
                    reviveMap.broadcastMessage(MaplePacketCreator.serverNotice(6, timeMob.getRight()));
                }

                if (timeMob.getLeft() == 9300338 && (reviveMap.getId() >= 922240100 && reviveMap.getId() <= 922240119)) {
                    if (!reviveMap.containsNPC(9001108)) {
                        MapleNPC npc = MapleLifeFactory.getNPC(9001108);
                        npc.setPosition(new Point(172, 9));
                        npc.setCy(9);
                        npc.setRx0(172 + 50);
                        npc.setRx1(172 - 50);
                        npc.setFh(27);
                        reviveMap.addMapObject(npc);
                        reviveMap.broadcastMessage(MaplePacketCreator.spawnNPC(npc));
                    } else {
                        reviveMap.toggleHiddenNPC(9001108);
                    }
                }
            }
            for (Integer mid : toSpawn) {
                final MapleMonster mob = MapleLifeFactory.getMonster(mid);
                if (eventInstance != null) {
                    eventInstance.registerMonster(mob);
                }
                mob.setPosition(getPosition());
                if (dropsDisabled()) {
                    mob.disableDrops();
                }
                TimerManager.getInstance().schedule(new Runnable() {

                    @Override
                    public void run() {
                        reviveMap.spawnMonster(mob);
                    }
                }, getAnimationTime("die1"));
            }
        }
        if (eventInstance != null) {
            eventInstance.unregisterMonster(this);
        }
        for (MonsterListener listener : listeners.toArray(new MonsterListener[listeners.size()])) {
            listener.monsterKilled(this, highestDamageChar);
        }
        MapleCharacter ret = highestDamageChar;
        highestDamageChar = null; // may not keep hard references to chars outside of PlayerStorage or MapleMap
        return ret;
    }

    public boolean isAlive() {
        return this.hp > 0;
    }

    public MapleCharacter getController() {
        return controller.get();
    }

    public void setController(MapleCharacter controller) {
        this.controller = new WeakReference<MapleCharacter>(controller);
    }

    public void switchController(MapleCharacter newController, boolean immediateAggro) {
        MapleCharacter controllers = getController();
        if (controllers == newController) {
            return;
        }
        if (controllers != null) {
            controllers.stopControllingMonster(this);
            controllers.getClient().getSession().write(MaplePacketCreator.stopControllingMonster(getObjectId()));
        }
        newController.controlMonster(this, immediateAggro);
        setController(newController);
        if (immediateAggro) {
            setControllerHasAggro(true);
        }
        setControllerKnowsAboutAggro(false);
    }

    public void addListener(MonsterListener listener) {
        listeners.add(listener);
    }

    public boolean isControllerHasAggro() {
        return fake ? false : controllerHasAggro;
    }

    public void setControllerHasAggro(boolean controllerHasAggro) {
        if (fake) {
            return;
        }
        this.controllerHasAggro = controllerHasAggro;
    }

    public boolean isControllerKnowsAboutAggro() {
        return fake ? false : controllerKnowsAboutAggro;
    }

    public void setControllerKnowsAboutAggro(boolean controllerKnowsAboutAggro) {
        if (fake) {
            return;
        }
        this.controllerKnowsAboutAggro = controllerKnowsAboutAggro;
    }

    public MaplePacket makeBossHPBarPacket() {
        return MaplePacketCreator.showBossHP(getId(), getHp(), getMaxHp(), getTagColor(), getTagBgColor());
    }

    public boolean hasBossHPBar() {
        return (isBoss() && getTagColor() > 0) || isHT();
    }

    private boolean isHT() {
        return getId() == 8810018;
    }

    @Override
    public void sendSpawnData(MapleClient c) {
        if (!isAlive()) {
            return;
        }
        if (isFake()) {
            c.getSession().write(MaplePacketCreator.spawnFakeMonster(this, 0));
        } else {
            c.getSession().write(MaplePacketCreator.spawnMonster(this, false));
        }
        if (stati.size() > 0) {
            for (final MonsterStatusEffect mse : this.stati.values()) {
                c.getSession().write(MaplePacketCreator.applyMonsterStatus(getObjectId(), mse));
            }
        }
        if (hasBossHPBar()) {
            if (this.getMap().countMonster(8810026) > 2 && this.getMap().getId() == 240060200) {
                this.getMap().killAllMonsters();
                return;
            }
            c.getSession().write(makeBossHPBarPacket());
        }
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.getSession().write(MaplePacketCreator.killMonster(getObjectId(), false));
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.MONSTER;
    }

    public void setEventInstance(EventInstanceManager eventInstance) {
        this.eventInstance = eventInstance;
    }

    public boolean isMobile() {
        return stats.isMobile();
    }

    public ElementalEffectiveness getEffectiveness(Element e) {
        if (stati.size() > 0 && stati.get(MonsterStatus.DOOM) != null) {
            return ElementalEffectiveness.NORMAL; // like blue snails
        }
        return stats.getEffectiveness(e);
    }

    public boolean applyStatus(MapleCharacter from, final MonsterStatusEffect status, boolean poison, long duration) {
        return applyStatus(from, status, poison, duration, false);
    }

    public boolean applyStatus(MapleCharacter from, final MonsterStatusEffect status, boolean poison, long duration, boolean venom) {
        switch (stats.getEffectiveness(status.getSkill().getElement())) {
            case IMMUNE:
            case STRONG:
            case NEUTRAL:
                return false;
            case NORMAL:
            case WEAK:
                break;
            default: {
                System.out.println("Unknown elemental effectiveness: " + stats.getEffectiveness(status.getSkill().getElement()));
                return false;
            }
        }
        if (status.getSkill().getId() == 2111006) { // fp compo
            ElementalEffectiveness effectiveness = stats.getEffectiveness(Element.POISON);
            if (effectiveness == ElementalEffectiveness.IMMUNE || effectiveness == ElementalEffectiveness.STRONG) {
                return false;
            }
        } else if (status.getSkill().getId() == 2211006) { // il compo
            ElementalEffectiveness effectiveness = stats.getEffectiveness(Element.ICE);
            if (effectiveness == ElementalEffectiveness.IMMUNE || effectiveness == ElementalEffectiveness.STRONG) {
                return false;
            }
        } else if (status.getSkill().getId() == 4120005 || status.getSkill().getId() == 4220005 || status.getSkill().getId() == 14110004) {// venom
            if (stats.getEffectiveness(Element.POISON) == ElementalEffectiveness.WEAK) {
                return false;
            }
        }
        if (poison && getHp() <= 1) {
            return false;
        }

        final Map<MonsterStatus, Integer> statis = status.getStati();
        if (stats.isBoss()) {
            if (!(statis.containsKey(MonsterStatus.SPEED)
                    && statis.containsKey(MonsterStatus.NINJA_AMBUSH)
                    && statis.containsKey(MonsterStatus.WATK))) {
                return false;
            }
        }
        for (MonsterStatus stat : statis.keySet()) {
            final MonsterStatusEffect oldEffect = stati.get(stat);
            if (oldEffect != null) {
                oldEffect.removeActiveStatus(stat);
                if (oldEffect.getStati().isEmpty()) {
                    oldEffect.cancelTask();
                    oldEffect.cancelDamageSchedule();
                }
            }
        }
        TimerManager timerManager = TimerManager.getInstance();
        final Runnable cancelTask = new Runnable() {

            @Override
            public void run() {
                if (isAlive()) {
                    MaplePacket packet = MaplePacketCreator.cancelMonsterStatus(getObjectId(), status.getStati());
                    map.broadcastMessage(packet, getPosition());
                    if (getController() != null && !getController().isMapObjectVisible(MapleMonster.this)) {
                        getController().getClient().getSession().write(packet);
                    }
                }
                for (MonsterStatus stat : status.getStati().keySet()) {
                    stati.remove(stat);
                }
                setVenomMulti(0);
                status.cancelDamageSchedule();
            }
        };
        if (poison) {
            int poisonLevel = from.getSkillLevel(status.getSkill());
            int poisonDamage = Math.min(Short.MAX_VALUE, (int) (getMaxHp() / (70.0 - poisonLevel) + 0.999));
            status.setValue(MonsterStatus.POISON, Integer.valueOf(poisonDamage));
            status.setDamageSchedule(timerManager.register(new DamageTask(poisonDamage, from, status, cancelTask, 0), 1000, 1000));
        } else if (venom) {
            if (from.getJob() == MapleJob.NIGHTLORD || from.getJob() == MapleJob.SHADOWER || from.getJob().isA(MapleJob.NIGHTWALKER3)) {
                int poisonLevel = 0, matk = 0, id = from.getJob().getId();
                int skill = (id == 412 ? 4120005 : (id == 422 ? 4220005 : 14110004));
                poisonLevel = from.getSkillLevel(SkillFactory.getSkill(skill));
                if (poisonLevel <= 0) {
                    return false;
                }
                matk = SkillFactory.getSkill(skill).getEffect(poisonLevel).getMatk();
                int luk = from.getLuk();
                int maxDmg = (int) Math.ceil(Math.min(Short.MAX_VALUE, 0.2 * luk * matk));
                int minDmg = (int) Math.ceil(Math.min(Short.MAX_VALUE, 0.1 * luk * matk));
                int gap = maxDmg - minDmg;
                if (gap == 0) {
                    gap = 1;
                }
                int poisonDamage = 0;
                for (int i = 0; i < getVenomMulti(); i++) {
                    poisonDamage += (Randomizer.nextInt(gap) + minDmg);
                }
                poisonDamage = Math.min(Short.MAX_VALUE, poisonDamage);
                status.setValue(MonsterStatus.POISON, Integer.valueOf(poisonDamage));
                status.setDamageSchedule(timerManager.register(new DamageTask(poisonDamage, from, status, cancelTask, 0), 1000, 1000));
            } else {
                return false;
            }

        } else if (status.getSkill().getId() == 4111003 || status.getSkill().getId() == 14111001) { //Shadow Web
            status.setDamageSchedule(timerManager.schedule(new DamageTask((int) (getMaxHp() / 50.0 + 0.999), from, status, cancelTask, 1), 3500));
        } else if (status.getSkill().getId() == 4121004 || status.getSkill().getId() == 4221004) { // Ninja Ambush
            final ISkill skill = SkillFactory.getSkill(status.getSkill().getId());
            final byte level = from.getSkillLevel(skill);
            final int damage = (int) ((from.getStr() + from.getLuk()) * (1.5 + (level * 0.05)) * skill.getEffect(level).getDamage());
            /*if (getHp() - damage <= 1)  { make hp 1 betch
            damage = getHp() - (getHp() - 1);
            }*/

            status.setValue(MonsterStatus.NINJA_AMBUSH, Integer.valueOf(damage));
            status.setDamageSchedule(timerManager.register(new DamageTask(damage, from, status, cancelTask, 2), 1000, 1000));
        }
        for (MonsterStatus stat : status.getStati().keySet()) {
            stati.put(stat, status);
        }
        int animationTime = status.getSkill().getAnimationTime();
        MaplePacket packet = MaplePacketCreator.applyMonsterStatus(getObjectId(), status);
        map.broadcastMessage(packet, getPosition());
        if (getController() != null && !getController().isMapObjectVisible(this)) {
            getController().getClient().getSession().write(packet);
        }
        status.setCancelTask(timerManager.schedule(cancelTask, duration + animationTime));
        return true;
    }

    public void applyMonsterBuff(final Map<MonsterStatus, Integer> stats, final int x, int skillId, long duration, MobSkill skill, final List<Integer> reflection) {
        TimerManager timerManager = TimerManager.getInstance();
        final Runnable cancelTask = new Runnable() {

            @Override
            public void run() {
                if (isAlive()) {
                    MaplePacket packet = MaplePacketCreator.cancelMonsterStatus(getObjectId(), stats);
                    map.broadcastMessage(packet, getPosition());
                    if (getController() != null && !getController().isMapObjectVisible(MapleMonster.this)) {
                        getController().getClient().getSession().write(packet);
                    }
                    for (final MonsterStatus stat : stats.keySet()) {
                        stati.remove(stat);
                    }
                }
            }
        };
        final MonsterStatusEffect effect = new MonsterStatusEffect(stats, null, skill, true);
        MaplePacket packet = MaplePacketCreator.applyMonsterStatus(getObjectId(), effect);
        map.broadcastMessage(packet, getPosition());
        if (getController() != null && !getController().isMapObjectVisible(this)) {
            getController().getClient().getSession().write(packet);
        }
        timerManager.schedule(cancelTask, duration);
    }

    public boolean isBuffed(MonsterStatus status) {
        return stati.containsKey(status);
    }

    public void setFake(boolean fake) {
        this.fake = fake;
    }

    public boolean isFake() {
        return fake;
    }

    public MapleMap getMap() {
        return map;
    }

    public List<Pair<Integer, Integer>> getSkills() {
        return stats.getSkills();
    }

    public boolean hasSkill(int skillId, int level) {
        return stats.hasSkill(skillId, level);
    }

    public boolean canUseSkill(MobSkill toUse) {
        if (toUse == null) {
            return false;
        }
        for (Pair<Integer, Integer> skill : usedSkills) {
            if (skill.getLeft() == toUse.getSkillId() && skill.getRight() == toUse.getSkillLevel()) {
                return false;
            }
        }
        if (toUse.getLimit() > 0) {
            if (this.skillsUsed.containsKey(new Pair<Integer, Integer>(toUse.getSkillId(), toUse.getSkillLevel()))) {
                int times = this.skillsUsed.get(new Pair<Integer, Integer>(toUse.getSkillId(), toUse.getSkillLevel()));
                if (times >= toUse.getLimit()) {
                    return false;
                }
            }
        }
        if (toUse.getSkillId() == 200) {
            Collection<MapleMapObject> mmo = getMap().getMapObjects();
            int i = 0;
            for (MapleMapObject mo : mmo) {
                if (mo.getType() == MapleMapObjectType.MONSTER) {
                    i++;
                }
            }
            if (i > 100) {
                return false;
            }
        }
        return true;
    }

    public void usedSkill(final int skillId, final int level, long cooltime) {
        this.usedSkills.add(new Pair<Integer, Integer>(skillId, level));
        if (this.skillsUsed.containsKey(new Pair<Integer, Integer>(skillId, level))) {
            int times = this.skillsUsed.get(new Pair<Integer, Integer>(skillId, level)) + 1;
            this.skillsUsed.remove(new Pair<Integer, Integer>(skillId, level));
            this.skillsUsed.put(new Pair<Integer, Integer>(skillId, level), times);
        } else {
            this.skillsUsed.put(new Pair<Integer, Integer>(skillId, level), 1);
        }
        final MapleMonster mons = this;
        TimerManager tMan = TimerManager.getInstance();
        tMan.schedule(
                new Runnable() {

                    @Override
                    public void run() {
                        mons.clearSkill(skillId, level);
                    }
                }, cooltime);
    }

    public void clearSkill(int skillId, int level) {
        int index = -1;
        for (Pair<Integer, Integer> skill : usedSkills) {
            if (skill.getLeft() == skillId && skill.getRight() == level) {
                index = usedSkills.indexOf(skill);
                break;
            }
        }
        if (index != -1) {
            usedSkills.remove(index);
        }
    }

    public int getNoSkills() {
        return this.stats.getNoSkills();
    }

    public boolean isFirstAttack() {
        return this.stats.isFirstAttack();
    }

    public int getBuffToGive() {
        return this.stats.getBuffToGive();
    }

    private final class DamageTask implements Runnable {

        private final int dealDamage;
        private final MapleCharacter chr;
        private final MonsterStatusEffect status;
        private final Runnable cancelTask;
        private final int type;
        private final MapleMap map;

        private DamageTask(int dealDamage, MapleCharacter chr, MonsterStatusEffect status, Runnable cancelTask, int type) {
            this.dealDamage = dealDamage;
            this.chr = chr;
            this.status = status;
            this.cancelTask = cancelTask;
            this.type = type;
            this.map = chr.getMap();
        }

        @Override
        public void run() {
            int damage = dealDamage;
            if (damage >= hp) {
                damage = hp - 1;
                if (type == 1 || type == 2) {
                    map.broadcastMessage(MaplePacketCreator.damageMonster(getObjectId(), damage), getPosition());
                    cancelTask.run();
                    status.getCancelTask().cancel(false);
                }
            }
            if (hp > 1 && damage > 0) {
                damage(chr, damage, false);
                if (type == 1) {
                    map.broadcastMessage(MaplePacketCreator.damageMonster(getObjectId(), damage), getPosition());
                }
            }
        }
    }

    public String getName() {
        return stats.getName();
    }

    private class AttackingMapleCharacter {

        private MapleCharacter attacker;
        private long lastAttackTime;

        public AttackingMapleCharacter(MapleCharacter attacker, long lastAttackTime) {
            super();
            this.attacker = attacker;
            this.lastAttackTime = lastAttackTime;
        }

        public long getLastAttackTime() {
            return lastAttackTime;
        }

        public MapleCharacter getAttacker() {
            return attacker;
        }
    }

    private interface AttackerEntry {

        List<AttackingMapleCharacter> getAttackers();

        public void addDamage(MapleCharacter from, int damage, boolean updateAttackTime);

        public int getDamage();

        public boolean contains(MapleCharacter chr);

        public void killedMob(MapleMap map, int baseExp, boolean mostDamage);
    }

    private class SingleAttackerEntry implements AttackerEntry {

        private int damage;
        private int chrid;
        private long lastAttackTime;
        private Channel cserv;

        public SingleAttackerEntry(MapleCharacter from, Channel cserv) {
            this.chrid = from.getId();
            this.cserv = cserv;
        }

        @Override
        public void addDamage(MapleCharacter from, int damage, boolean updateAttackTime) {
            if (chrid == from.getId()) {
                this.damage += damage;
            } else {
                throw new IllegalArgumentException("Not the attacker of this entry");
            }
            if (updateAttackTime) {
                lastAttackTime = System.currentTimeMillis();
            }
        }

        @Override
        public List<AttackingMapleCharacter> getAttackers() {
            MapleCharacter chr = cserv.getPlayerStorage().getCharacterById(chrid);
            if (chr != null) {
                return Collections.singletonList(new AttackingMapleCharacter(chr, lastAttackTime));
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        public boolean contains(MapleCharacter chr) {
            return chrid == chr.getId();
        }

        @Override
        public int getDamage() {
            return damage;
        }

        @Override
        public void killedMob(MapleMap map, int baseExp, boolean mostDamage) {
            MapleCharacter chr = map.getCharacterById(chrid);
            if (chr != null) {
                giveExpToCharacter(chr, baseExp, mostDamage, 1);
            }
        }

        @Override
        public int hashCode() {
            return chrid;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SingleAttackerEntry other = (SingleAttackerEntry) obj;
            return chrid == other.chrid;
        }
    }

    private static class OnePartyAttacker {

        public MapleParty lastKnownParty;
        public int damage;
        public long lastAttackTime;

        public OnePartyAttacker(MapleParty lastKnownParty, int damage) {
            this.lastKnownParty = lastKnownParty;
            this.damage = damage;
            this.lastAttackTime = System.currentTimeMillis();
        }
    }

    private class PartyAttackerEntry implements AttackerEntry {

        private int totDamage;
        private Map<Integer, OnePartyAttacker> attackers;
        private Channel cserv;
        private int partyid;

        public PartyAttackerEntry(int partyid, Channel cserv) {
            this.partyid = partyid;
            this.cserv = cserv;
            attackers = new HashMap<Integer, OnePartyAttacker>(6);
        }

        @Override
        public List<AttackingMapleCharacter> getAttackers() {
            List<AttackingMapleCharacter> ret = new ArrayList<AttackingMapleCharacter>(attackers.size());
            for (Entry<Integer, OnePartyAttacker> entry : attackers.entrySet()) {
                MapleCharacter chr = cserv.getPlayerStorage().getCharacterById(entry.getKey());
                if (chr != null) {
                    ret.add(new AttackingMapleCharacter(chr, entry.getValue().lastAttackTime));
                }
            }
            return ret;
        }

        private Map<MapleCharacter, OnePartyAttacker> resolveAttackers() {
            Map<MapleCharacter, OnePartyAttacker> ret = new HashMap<MapleCharacter, OnePartyAttacker>(attackers.size());
            for (Entry<Integer, OnePartyAttacker> aentry : attackers.entrySet()) {
                MapleCharacter chr = cserv.getPlayerStorage().getCharacterById(aentry.getKey());
                if (chr != null) {
                    ret.put(chr, aentry.getValue());
                }
            }
            return ret;
        }

        @Override
        public boolean contains(MapleCharacter chr) {
            return attackers.containsKey(chr.getId());
        }

        @Override
        public int getDamage() {
            return totDamage;
        }

        @Override
        public void addDamage(MapleCharacter from, int damage, boolean updateAttackTime) {
            OnePartyAttacker oldPartyAttacker = attackers.get(from.getId());
            if (oldPartyAttacker != null) {
                oldPartyAttacker.damage += damage;
                oldPartyAttacker.lastKnownParty = from.getParty();
                if (updateAttackTime) {
                    oldPartyAttacker.lastAttackTime = System.currentTimeMillis();
                }
            } else {
                // TODO actually this causes wrong behaviour when the party changes between attacks
                // only the last setup will get exp - but otherwise we'd have to store the full party
                // constellation for every attack/everytime it changes, might be wanted/needed in the
                // future but not now
                OnePartyAttacker onePartyAttacker = new OnePartyAttacker(from.getParty(), damage);
                attackers.put(from.getId(), onePartyAttacker);
                if (!updateAttackTime) {
                    onePartyAttacker.lastAttackTime = 0;
                }
            }
            totDamage += damage;
        }

        @Override
        public void killedMob(MapleMap map, int baseExp, boolean mostDamage) {
            Map<MapleCharacter, OnePartyAttacker> attackers_ = resolveAttackers();
            MapleCharacter highest = null;
            int highestDamage = 0;
            Map<MapleCharacter, Integer> expMap = new ArrayMap<MapleCharacter, Integer>(6);
            for (Entry<MapleCharacter, OnePartyAttacker> attacker : attackers_.entrySet()) {
                MapleParty party = attacker.getValue().lastKnownParty;
                double averagePartyLevel = 0;
                List<MapleCharacter> expApplicable = new ArrayList<MapleCharacter>();
                for (MaplePartyCharacter partychar : party.getMembers()) {
                    if (attacker.getKey().getLevel() - partychar.getLevel() <= 5 || getLevel() - partychar.getLevel() <= 5) {
                        MapleCharacter pchr = cserv.getPlayerStorage().getCharacterByName(partychar.getName());
                        if (pchr != null) {
                            if (pchr.isAlive() && pchr.getMap() == map) {
                                expApplicable.add(pchr);
                                averagePartyLevel += pchr.getLevel();
                            }
                        }
                    }
                }
                double expBonus = 1.0;
                if (expApplicable.size() > 1) {
                    expBonus = 1.10 + 0.05 * expApplicable.size();
                    averagePartyLevel /= expApplicable.size();
                }
                int iDamage = attacker.getValue().damage;
                if (iDamage > highestDamage) {
                    highest = attacker.getKey();
                    highestDamage = iDamage;
                }
                double innerBaseExp = baseExp * ((double) iDamage / totDamage);
                double expFraction = (innerBaseExp * expBonus) / (expApplicable.size() + 1);
                for (MapleCharacter expReceiver : expApplicable) {
                    Integer oexp = expMap.get(expReceiver);
                    int iexp;
                    if (oexp == null) {
                        iexp = 0;
                    } else {
                        iexp = oexp.intValue();
                    }
                    double expWeight = (expReceiver == attacker.getKey() ? 2.0 : 1.0);
                    double levelMod = expReceiver.getLevel() / averagePartyLevel;
                    if (levelMod > 1.0 || this.attackers.containsKey(expReceiver.getId())) {
                        levelMod = 1.0;
                    }
                    iexp += (int) Math.round(expFraction * expWeight * levelMod);
                    expMap.put(expReceiver, Integer.valueOf(iexp));
                }
            }
            for (Entry<MapleCharacter, Integer> expReceiver : expMap.entrySet()) {
                giveExpToCharacter(expReceiver.getKey(), expReceiver.getValue(), mostDamage ? expReceiver.getKey() == highest : false, expMap.size());
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + partyid;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PartyAttackerEntry other = (PartyAttackerEntry) obj;
            if (partyid != other.partyid) {
                return false;
            }
            return true;
        }
    }

    public void addStolen(int itemId) {
        stolenItems.add(itemId);
    }

    public void setTempEffectiveness(Element e, ElementalEffectiveness ee, long milli) {
        final Element fE = e;
        final ElementalEffectiveness fEE = stats.getEffectiveness(e);
        if (!stats.getEffectiveness(e).equals(ElementalEffectiveness.WEAK)) {
            stats.setEffectiveness(e, ee);
            TimerManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    stats.removeEffectiveness(fE);
                    stats.setEffectiveness(fE, fEE);
                }
            }, milli);
        }
    }

    public BanishInfo getBanish() {
        return stats.getBanishInfo();
    }

    public void setBoss(boolean boss) {
        this.stats.setBoss(boss);
    }

    public int getDropPeriodTime() {
        return stats.getDropPeriod();
    }

    public int getPADamage() {
        return stats.getPADamage();
    }

    public Map<MonsterStatus, MonsterStatusEffect> getStati() {
        return stati;
    }

    public final void empty() {
        try {
            this.monsterLock.unlock();
        } catch (Exception e) {
        } //who cares
        this.monsterLock = null;
        final Iterator<MonsterStatusEffect> mseIt = stati.values().iterator();
        MonsterStatusEffect mse;
        while (mseIt.hasNext()) {
            mse = mseIt.next();
            mse.cancelDamageSchedule();
            mse.cancelTask();
            mseIt.remove();
            mse = null;
        }
        this.stati = null;
        this.usedSkills = null;
        this.listeners = null;
        this.skillsUsed = null;
        this.stats = null;
        this.highestDamageChar = null;
        this.map = null;
        this.eventInstance = null;
        this.attackers = null;
        this.stolenItems = null;
        if (controller.get() != null) {
            controller.get().stopControllingMonster(this);
        }
        controller.clear();
        this.controller = null;
    }
}
