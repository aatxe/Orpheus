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
package net.server.handlers.channel;

import client.IItem;
import client.MapleCharacter;
import constants.ExpTable;
import client.MapleClient;
import client.MapleInventoryType;
import client.MaplePet;
import client.PetCommand;
import client.PetDataFactory;
import tools.Randomizer;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class PetCommandHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        int petId = slea.readInt();
        byte petIndex = chr.getPetIndex(petId);
        MaplePet pet = null;
        if (petIndex == -1) {
            return;
        } else {
            pet = chr.getPet(petIndex);
        }
        slea.readInt();
        slea.readByte();
        byte command = slea.readByte();
        PetCommand petCommand = PetDataFactory.getPetCommand(pet.getItemId(), (int) command);
        if (petCommand == null) {
            return;
        }
        boolean success = false;
        if (Randomizer.nextInt(101) <= petCommand.getProbability()) {
            success = true;
            if (pet.getCloseness() < 30000) {
                int newCloseness = pet.getCloseness() + petCommand.getIncrease();
                if (newCloseness > 30000) {
                    newCloseness = 30000;
                }
                pet.setCloseness(newCloseness);
                if (newCloseness >= ExpTable.getClosenessNeededForLevel(pet.getLevel())) {
                    pet.setLevel((byte) (pet.getLevel() + 1));
                    c.announce(MaplePacketCreator.showOwnPetLevelUp(chr.getPetIndex(pet)));
                    chr.getMap().broadcastMessage(MaplePacketCreator.showPetLevelUp(c.getPlayer(), chr.getPetIndex(pet)));
                }
                IItem petz = chr.getInventory(MapleInventoryType.CASH).getItem(pet.getPosition());
                c.announce(MaplePacketCreator.updateSlot(petz));
            }
        }
        chr.getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.commandResponse(chr.getId(), petIndex, command, success), true);
    }
}
