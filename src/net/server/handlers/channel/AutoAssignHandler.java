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
package net.server.handlers.channel;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * 
 * @author Generic
 */
public class AutoAssignHandler extends AbstractMaplePacketHandler {

	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		MapleCharacter chr = c.getPlayer();
		slea.skip(8);
		if (chr.getRemainingAp() < 1) {
			return;
		}
		int total = 0;
		int extras = 0;
		for (int i = 0; i < 2; i++) {
			int type = slea.readInt();
			int tempVal = slea.readInt();
			if (tempVal < 0 || tempVal > c.getPlayer().getRemainingAp()) {
				return;
			}
			total += tempVal;
			extras += gainStatByType(chr, MapleStat.getBy5ByteEncoding(type), tempVal);
		}
		int remainingAp = (chr.getRemainingAp() - total) + extras;
		chr.setRemainingAp(remainingAp);
		chr.updateSingleStat(MapleStat.AVAILABLEAP, remainingAp);
		c.announce(MaplePacketCreator.enableActions());
	}

	private int gainStatByType(MapleCharacter chr, MapleStat type, int gain) {
		int newVal = 0;
		if (type.equals(MapleStat.STR)) {
			newVal = chr.getStr() + gain;
			if (newVal > 999) {
				chr.setStr(999);
			} else {
				chr.setStr(newVal);
			}
		} else if (type.equals(MapleStat.INT)) {
			newVal = chr.getInt() + gain;
			if (newVal > 999) {
				chr.setInt(999);
			} else {
				chr.setInt(newVal);
			}
		} else if (type.equals(MapleStat.LUK)) {
			newVal = chr.getLuk() + gain;
			if (newVal > 999) {
				chr.setLuk(999);
			} else {
				chr.setLuk(newVal);
			}
		} else if (type.equals(MapleStat.DEX)) {
			newVal = chr.getDex() + gain;
			if (newVal > 999) {
				chr.setDex(999);
			} else {
				chr.setDex(newVal);
			}
		}
		if (newVal > 999) {
			chr.updateSingleStat(type, 999);
			return newVal - 999;
		}
		chr.updateSingleStat(type, newVal);
		return 0;
	}
}
