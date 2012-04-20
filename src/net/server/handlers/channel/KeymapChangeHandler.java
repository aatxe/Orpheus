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

import client.ISkill;
import client.MapleClient;
import client.MapleKeyBinding;
import client.SkillFactory;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public final class KeymapChangeHandler extends AbstractMaplePacketHandler {
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (slea.available() != 8) {
            slea.readInt();
            int numChanges = slea.readInt();
            for (int i = 0; i < numChanges; i++) {
                int key = slea.readInt();
                int type = slea.readByte();
                int action = slea.readInt();
                ISkill skill = SkillFactory.getSkill(action);
                if (skill != null && c.getPlayer().getSkillLevel(skill) < 1) {
                    continue;
                }
                c.getPlayer().changeKeybinding(key, new MapleKeyBinding(type, action));
            }
        }
    }
}
