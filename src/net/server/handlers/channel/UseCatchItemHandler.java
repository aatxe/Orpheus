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
import client.MapleInventoryType;
import client.autoban.AutobanManager;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.life.MapleMonster;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * 
 * @author kevintjuh93
 */
public final class UseCatchItemHandler extends AbstractMaplePacketHandler {
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		MapleCharacter chr = c.getPlayer();
		AutobanManager abm = chr.getAutobanManager();
		abm.setTimestamp(5, slea.readInt());
		slea.readShort();
		int itemId = slea.readInt();
		int monsterid = slea.readInt();

		MapleMonster mob = chr.getMap().getMonsterByOid(monsterid);
		if (chr.getInventory(MapleItemInformationProvider.getInstance().getInventoryType(itemId)).countById(itemId) <= 0) {
			return;
		}
		if (mob == null) {
			return;
		}
		switch (itemId) {
			case 2270000:
				if (mob.getId() == 9300101) {
					chr.getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
					mob.getMap().killMonster(mob, null, false);
					MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
					MapleInventoryManipulator.addById(c, 1902000, (short) 1, "", -1);
				}
				c.getSession().write(MaplePacketCreator.enableActions());
				break;
			case 2270001:
				if (mob.getId() == 9500197) {
					if ((abm.getLastSpam(10) + 1000) < System.currentTimeMillis()) {
						if (mob.getHp() < ((mob.getMaxHp() / 10) * 4)) {
							chr.getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
							mob.getMap().killMonster(mob, null, false);
							MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
							MapleInventoryManipulator.addById(c, 4031830, (short) 1, "", -1);
						} else {
							abm.spam(10);
							c.getSession().write(MaplePacketCreator.catchMessage(0));
						}
					}
					c.getSession().write(MaplePacketCreator.enableActions());
				}
				break;
			case 2270002:
				if (mob.getId() == 9300157) {
					if ((abm.getLastSpam(10) + 800) < System.currentTimeMillis()) {
						if (mob.getHp() < ((mob.getMaxHp() / 10) * 4)) {
							if (Math.random() < 0.5) { // 50% chance
								chr.getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
								mob.getMap().killMonster(mob, null, false);
								MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
								MapleInventoryManipulator.addById(c, 4031868, (short) 1, "", -1);
							} else {
								chr.getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 0));
							}
							abm.spam(10);
						} else {
							c.getSession().write(MaplePacketCreator.catchMessage(0));
						}
					}
					c.getSession().write(MaplePacketCreator.enableActions());
				}
				break;
			case 2270003:
				if (mob.getId() == 9500320) {
					if (mob.getHp() < ((mob.getMaxHp() / 10) * 4)) {
						chr.getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
						mob.getMap().killMonster(mob, null, false);
						MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
						MapleInventoryManipulator.addById(c, 4031887, (short) 1, "", -1);
					} else {
						c.getSession().write(MaplePacketCreator.catchMessage(0));
					}
				}
				c.getSession().write(MaplePacketCreator.enableActions());
				break;
			case 2270005:
				if (mob.getId() == 9300187) {
					if (mob.getHp() < ((mob.getMaxHp() / 10) * 3)) {
						chr.getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
						mob.getMap().killMonster(mob, null, false);
						MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
						MapleInventoryManipulator.addById(c, 2109001, (short) 1, "", -1);
					} else {
						c.getSession().write(MaplePacketCreator.catchMessage(0));
					}
				}
				c.getSession().write(MaplePacketCreator.enableActions());
				break;
			case 2270006:
				if (mob.getId() == 9300189) {
					if (mob.getHp() < ((mob.getMaxHp() / 10) * 3)) {
						chr.getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
						mob.getMap().killMonster(mob, null, false);
						MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
						MapleInventoryManipulator.addById(c, 2109002, (short) 1, "", -1);
					} else {
						c.getSession().write(MaplePacketCreator.catchMessage(0));
					}
				}
				c.getSession().write(MaplePacketCreator.enableActions());
				break;
			case 2270007:
				if (mob.getId() == 9300191) {
					if (mob.getHp() < ((mob.getMaxHp() / 10) * 3)) {
						chr.getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
						mob.getMap().killMonster(mob, null, false);
						MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
						MapleInventoryManipulator.addById(c, 2109003, (short) 1, "", -1);
					} else {
						c.getSession().write(MaplePacketCreator.catchMessage(0));
					}
				}
				c.getSession().write(MaplePacketCreator.enableActions());
				break;
			case 2270004:
				if (mob.getId() == 9300175) {
					if (mob.getHp() < ((mob.getMaxHp() / 10) * 4)) {
						chr.getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
						mob.getMap().killMonster(mob, null, false);
						MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
						MapleInventoryManipulator.addById(c, 4001169, (short) 1, "", -1);
					} else {
						c.getSession().write(MaplePacketCreator.catchMessage(0));
					}
				}
				c.getSession().write(MaplePacketCreator.enableActions());
				break;
			case 2270008:
				if (mob.getId() == 9500336) {
					if ((abm.getLastSpam(10) + 3000) < System.currentTimeMillis()) {
						abm.spam(10);
						chr.getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
						mob.getMap().killMonster(mob, null, false);
						MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
						MapleInventoryManipulator.addById(c, 2022323, (short) 1, "", -1);
					} else {
						chr.message("You cannot use the Fishing Net yet.");
					}
					c.getSession().write(MaplePacketCreator.enableActions());
				}
				break;
			default:
				// System.out.println("UseCatchItemHandler: \r\n" +
				// slea.toString());
		}
	}
}
