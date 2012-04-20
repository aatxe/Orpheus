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
package net.server.handlers.login;

import client.IItem;
import client.Item;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventory;
import client.MapleInventoryType;
import client.MapleJob;
import client.MapleSkinColor;
import client.SkillFactory;
import net.AbstractMaplePacketHandler;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class CreateCharHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        String name = slea.readMapleAsciiString();
        if (!MapleCharacter.canCreateChar(name)) {
            return;
        }
        MapleCharacter newchar = MapleCharacter.getDefault(c);
        newchar.setWorld(c.getWorld());
        int job = slea.readInt();
        int face = slea.readInt();
        newchar.setFace(face);
        newchar.setHair(slea.readInt() + slea.readInt());
        int skincolor = slea.readInt();
        if (skincolor > 3) {
            return;
        }
        newchar.setSkinColor(MapleSkinColor.getById(skincolor));
        int top = slea.readInt();
        int bottom = slea.readInt();
        int shoes = slea.readInt();
        int weapon = slea.readInt();
        newchar.setGender(slea.readByte());
        newchar.setName(name);
        if (!newchar.isGM()) {
            if (job == 0) { // Knights of Cygnus
                newchar.setJob(MapleJob.NOBLESSE);
                newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161047, (byte) 0, (short) 1));
            } else if (job == 1) { // Adventurer
                newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161001, (byte) 0, (short) 1));
            } else if (job == 2) { // Aran
                newchar.setJob(MapleJob.LEGEND);
                newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161048, (byte) 0, (short) 1));
            } else {
                c.disconnect(); //Muhaha
                System.out.println("[CHAR CREATION] A new job ID has been found: " + job); //I should ban for packet editing!
                return;
            }
        }
        //CHECK FOR EQUIPS
        MapleInventory equip = newchar.getInventory(MapleInventoryType.EQUIPPED);
        if (newchar.isGM()) {
            IItem eq_hat = MapleItemInformationProvider.getInstance().getEquipById(1002140);
            eq_hat.setPosition((byte) -1);
            equip.addFromDB(eq_hat);
            top = 1042003;
            bottom = 1062007;
            weapon = 1322013;
        }
        IItem eq_top = MapleItemInformationProvider.getInstance().getEquipById(top);
        eq_top.setPosition((byte) -5);
        equip.addFromDB(eq_top);
        IItem eq_bottom = MapleItemInformationProvider.getInstance().getEquipById(bottom);
        eq_bottom.setPosition((byte) -6);
        equip.addFromDB(eq_bottom);
        IItem eq_shoes = MapleItemInformationProvider.getInstance().getEquipById(shoes);
        eq_shoes.setPosition((byte) -7);
        equip.addFromDB(eq_shoes);
        IItem eq_weapon = MapleItemInformationProvider.getInstance().getEquipById(weapon);
        eq_weapon.setPosition((byte) -11);
        equip.addFromDB(eq_weapon.copy());
        newchar.saveToDB(false);
        c.announce(MaplePacketCreator.addNewCharEntry(newchar));
    }
}