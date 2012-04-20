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
import client.IItem;
import net.AbstractMaplePacketHandler;
import scripting.item.ItemScriptManager;
import server.MapleItemInformationProvider;
import server.MapleItemInformationProvider.scriptedItem;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Jay Estrella
 */
public final class ScriptedItemHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        slea.readInt(); // trash stamp (thx rmzero)
        byte itemSlot = (byte) slea.readShort(); // item sl0t (thx rmzero)
        int itemId = slea.readInt(); // itemId
        scriptedItem info = ii.getScriptedItemInfo(itemId);
        if (info == null) return;
        ItemScriptManager ism = ItemScriptManager.getInstance();
        IItem item = c.getPlayer().getInventory(ii.getInventoryType(itemId)).getItem(itemSlot);
        if (item == null || item.getItemId() != itemId || item.getQuantity() < 1 || !ism.scriptExists(info.getScript())) {
            return;
        }
        ism.getItemScript(c, info.getScript());
        //NPCScriptManager.getInstance().start(c, info.getNpc(), null, null);        
    }
}
