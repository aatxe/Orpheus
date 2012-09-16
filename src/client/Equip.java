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

import java.util.LinkedList;
import java.util.List;

import server.EquipLevelUpStat;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;

public class Equip extends Item implements IEquip {
	private byte upgradeSlots;
	private byte level, flag, itemLevel;
	private short str, dex, _int, luk, hp, mp, watk, matk, wdef, mdef, acc,
			avoid, hands, speed, jump, vicious;
	private float itemExp;
	private int ringid = -1;
	private boolean wear = false;
	private int skill0, skill1, skill2, skill3;

	public Equip(int id, byte position) {
		super(id, position, (short) 1);
		this.itemExp = 0;
		this.itemLevel = 1;
	}

	public Equip(int id, byte position, int slots) {
		super(id, position, (short) 1);
		this.upgradeSlots = (byte) slots;
		this.itemExp = 0;
		this.itemLevel = 1;
	}

	@Override
	public IItem copy() {
		Equip ret = new Equip(getItemId(), getPosition(), getUpgradeSlots());
		ret.str = str;
		ret.dex = dex;
		ret._int = _int;
		ret.luk = luk;
		ret.hp = hp;
		ret.mp = mp;
		ret.matk = matk;
		ret.mdef = mdef;
		ret.watk = watk;
		ret.wdef = wdef;
		ret.acc = acc;
		ret.avoid = avoid;
		ret.hands = hands;
		ret.speed = speed;
		ret.jump = jump;
		ret.flag = flag;
		ret.vicious = vicious;
		ret.upgradeSlots = upgradeSlots;
		ret.itemLevel = itemLevel;
		ret.itemExp = itemExp;
		ret.level = level;
		ret.log = new LinkedList<String>(log);
		ret.setOwner(getOwner());
		ret.setQuantity(getQuantity());
		ret.setExpiration(getExpiration());
		ret.setGiftFrom(getGiftFrom());
		return ret;
	}

	@Override
	public byte getFlag() {
		return flag;
	}

	@Override
	public byte getType() {
		return IItem.EQUIP;
	}

	public byte getUpgradeSlots() {
		return upgradeSlots;
	}

	public short getStr() {
		return str;
	}

	public short getDex() {
		return dex;
	}

	public short getInt() {
		return _int;
	}

	public short getLuk() {
		return luk;
	}

	public short getHp() {
		return hp;
	}

	public short getMp() {
		return mp;
	}

	public short getWatk() {
		return watk;
	}

	public short getMatk() {
		return matk;
	}

	public short getWdef() {
		return wdef;
	}

	public short getMdef() {
		return mdef;
	}

	public short getAcc() {
		return acc;
	}

	public short getAvoid() {
		return avoid;
	}

	public short getHands() {
		return hands;
	}

	public short getSpeed() {
		return speed;
	}

	public short getJump() {
		return jump;
	}

	public short getVicious() {
		return vicious;
	}

	@Override
	public void setFlag(byte flag) {
		this.flag = flag;
	}

	public void setStr(short str) {
		this.str = str;
	}

	public void setDex(short dex) {
		this.dex = dex;
	}

	public void setInt(short _int) {
		this._int = _int;
	}

	public void setLuk(short luk) {
		this.luk = luk;
	}

	public void setHp(short hp) {
		this.hp = hp;
	}

	public void setMp(short mp) {
		this.mp = mp;
	}

	public void setWatk(short watk) {
		this.watk = watk;
	}

	public void setMatk(short matk) {
		this.matk = matk;
	}

	public void setWdef(short wdef) {
		this.wdef = wdef;
	}

	public void setMdef(short mdef) {
		this.mdef = mdef;
	}

	public void setAcc(short acc) {
		this.acc = acc;
	}

	public void setAvoid(short avoid) {
		this.avoid = avoid;
	}

	public void setHands(short hands) {
		this.hands = hands;
	}

	public void setSpeed(short speed) {
		this.speed = speed;
	}

	public void setJump(short jump) {
		this.jump = jump;
	}

	public void setVicious(short vicious) {
		this.vicious = vicious;
	}

	public void setUpgradeSlots(byte upgradeSlots) {
		this.upgradeSlots = upgradeSlots;
	}

	public byte getLevel() {
		return level;
	}

	public void setLevel(byte level) {
		this.level = level;
	}

	public void gainLevel(MapleClient c, boolean timeless) {
		List<EquipLevelUpStat> stats = MapleItemInformationProvider.getInstance().getItemLevelupStats(getItemId(), itemLevel, timeless);
		for (EquipLevelUpStat stat : stats) {
			if (stat.name.equals("incDEX"))
				dex += stat.amount;
			else if (stat.name.equals("incSTR"))
				str += stat.amount;
			else if (stat.name.equals("incINT"))
				_int += stat.amount;
			else if (stat.name.equals("incLUK"))
				luk += stat.amount;
			else if (stat.name.equals("incMHP"))
				hp += stat.amount;
			else if (stat.name.equals("incMMP"))
				mp += stat.amount;
			else if (stat.name.equals("incPAD"))
				watk += stat.amount;
			else if (stat.name.equals("incMAD"))
				matk += stat.amount;
			else if (stat.name.equals("incPDD"))
				wdef += stat.amount;
			else if (stat.name.equals("incMDD"))
				mdef += stat.amount;
			else if (stat.name.equals("incEVA"))
				avoid += stat.amount;
			else if (stat.name.equals("incACC"))
				acc += stat.amount;
			else if (stat.name.equals("incSpeed"))
				speed += stat.amount;
			else if (stat.name.equals("incJump"))
				jump += stat.amount;
			else if (stat.name.equals("Skill0"))
				skill0 = stat.amount;
			else if (stat.name.equals("Skill1"))
				skill1 = stat.amount;
			else if (stat.name.equals("Skill2"))
				skill2 = stat.amount;
			else if (stat.name.equals("Skill3"))
				skill3 = stat.amount;
		}
		this.itemLevel++;
		c.announce(MaplePacketCreator.showEquipmentLevelUp());
		c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showForeignEffect(c.getPlayer().getId(), 15));
		c.getPlayer().forceUpdateItem(MapleInventoryType.EQUIPPED, this);
	}

	public int getItemExp() {
		return (int) itemExp;
	}

	public void gainItemExp(MapleClient c, int gain, boolean timeless) {
		int expneeded = timeless ? (10 * itemLevel + 70) : (5 * itemLevel + 65);
		float modifier = 364 / expneeded;
		float exp = (expneeded / (1000000 * modifier * modifier)) * gain;
		itemExp += exp;
		if (itemExp >= 364) {
			itemExp = (itemExp - 364);
			gainLevel(c, timeless);
		} else
			c.getPlayer().forceUpdateItem(MapleInventoryType.EQUIPPED, this);
	}

	public void setItemExp(int exp) {
		this.itemExp = exp;
	}

	public void setItemLevel(byte level) {
		this.itemLevel = level;
	}

	@Override
	public void setQuantity(short quantity) {
		if (quantity < 0 || quantity > 1) {
			throw new RuntimeException("Setting the quantity to " + quantity + " on an equip (itemid: " + getItemId() + ")");
		}
		super.setQuantity(quantity);
	}

	public void setUpgradeSlots(int i) {
		this.upgradeSlots = (byte) i;
	}

	public void setVicious(int i) {
		this.vicious = (short) i;
	}

	public int getRingId() {
		return ringid;
	}

	public void setRingId(int id) {
		this.ringid = id;
	}

	public boolean isWearing() {
		return wear;
	}

	public void wear(boolean yes) {
		wear = yes;
	}

	public byte getItemLevel() {
		return itemLevel;
	}
	
	public int getSkill0() {
		return skill0;
	}
	
	public int getSkill1() {
		return skill1;
	}
	
	public int getSkill2() {
		return skill2;
	}
	
	public int getSkill3() {
		return skill3;
	}
}