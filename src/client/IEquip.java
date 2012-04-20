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
package client;

public interface IEquip extends IItem {
    public enum ScrollResult {
        SUCCESS, FAIL, CURSE
    }

    public void setUpgradeSlots(int i);
    public void setVicious(int i);
    byte getUpgradeSlots();
    byte getLevel();
    public byte getFlag();
    public short getStr();
    public short getDex();
    public short getInt();
    public short getLuk();
    public short getHp();
    public short getMp();
    public short getWatk();
    public short getMatk();
    public short getWdef();
    public short getMdef();
    public short getAcc();
    public short getAvoid();
    public short getHands();
    public short getSpeed();
    public short getJump();
    public short getVicious();
    public int getItemExp();
    public byte getItemLevel();
    public int getRingId();
    public void setRingId(int id);
    public boolean isWearing();
    public void wear(boolean yes);
}
