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
