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

import client.MapleClient;
import tools.Randomizer;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Xotic & BubblesDev
 */
public final class MobDamageMobFriendlyHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int attacker = slea.readInt();
        slea.readInt(); //charId
        int damaged = slea.readInt();
        int damage = Randomizer.nextInt(((c.getPlayer().getMap().getMonsterByOid(damaged).getMaxHp() / 13 + c.getPlayer().getMap().getMonsterByOid(attacker).getPADamage() * 10)) * 2 + 500); //Beng's formula.
        if (c.getPlayer().getMap().getMonsterByOid(damaged) == null || c.getPlayer().getMap().getMonsterByOid(attacker) == null) {
            return;
        }
        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.MobDamageMobFriendly(c.getPlayer().getMap().getMonsterByOid(damaged), damage), c.getPlayer().getMap().getMonsterByOid(damaged).getPosition());
        c.announce(MaplePacketCreator.enableActions());
    }
}
