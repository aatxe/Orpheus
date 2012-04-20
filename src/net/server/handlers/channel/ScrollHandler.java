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

import java.util.List;
import client.IEquip;
import client.IItem;
import client.Item;
import client.MapleClient;
import client.SkillFactory;
import client.MapleInventory;
import client.MapleInventoryType;
import client.IEquip.ScrollResult;
import client.ISkill;
import net.AbstractMaplePacketHandler;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * @author Matze
 * @author Frz
 */
public final class ScrollHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readInt(); // whatever...
        byte slot = (byte) slea.readShort();
        byte dst = (byte) slea.readShort();
        byte ws = (byte) slea.readShort();
        boolean whiteScroll = false; // white scroll being used?
        boolean legendarySpirit = false; // legendary spirit skill
        if ((ws & 2) == 2) {
            whiteScroll = true;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        IEquip toScroll = (IEquip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(dst);
        ISkill LegendarySpirit = SkillFactory.getSkill(1003);
        if (c.getPlayer().getSkillLevel(LegendarySpirit) > 0 && dst >= 0) {
            legendarySpirit = true;
            toScroll = (IEquip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(dst);
        }
        byte oldLevel = toScroll.getLevel();
        if (((IEquip) toScroll).getUpgradeSlots() < 1) {
            c.announce(MaplePacketCreator.getInventoryFull());
            return;
        }
        MapleInventory useInventory = c.getPlayer().getInventory(MapleInventoryType.USE);
        IItem scroll = useInventory.getItem(slot);
        IItem wscroll = null;
        List<Integer> scrollReqs = ii.getScrollReqs(scroll.getItemId());
        if (scrollReqs.size() > 0 && !scrollReqs.contains(toScroll.getItemId())) {
            c.announce(MaplePacketCreator.getInventoryFull());
            return;
        }
        if (whiteScroll) {
            wscroll = useInventory.findById(2340000);
            if (wscroll == null || wscroll.getItemId() != 2340000) {
                whiteScroll = false;
            }
        }
        if (scroll.getItemId() != 2049100 && !isCleanSlate(scroll.getItemId())) {
            if (!canScroll(scroll.getItemId(), toScroll.getItemId())) {
                return;
            }
        }
        if (scroll.getQuantity() < 1) {
            return;
        }
        IEquip scrolled = (IEquip) ii.scrollEquipWithId(toScroll, scroll.getItemId(), whiteScroll, c.getPlayer().isGM());
        ScrollResult scrollSuccess = IEquip.ScrollResult.FAIL; // fail
        if (scrolled == null) {
            scrollSuccess = IEquip.ScrollResult.CURSE;
        } else if (scrolled.getLevel() > oldLevel || (isCleanSlate(scroll.getItemId()) && scrolled.getLevel() == oldLevel + 1)) {
            scrollSuccess = IEquip.ScrollResult.SUCCESS;
        }
        useInventory.removeItem(scroll.getPosition(), (short) 1, false);
        if (whiteScroll) {
            useInventory.removeItem(wscroll.getPosition(), (short) 1, false);
            if (wscroll.getQuantity() < 1) {
                c.announce(MaplePacketCreator.clearInventoryItem(MapleInventoryType.USE, wscroll.getPosition(), false));
            } else {
                c.announce(MaplePacketCreator.updateInventorySlot(MapleInventoryType.USE, (Item) wscroll));
            }
        }
        if (scrollSuccess == IEquip.ScrollResult.CURSE) {
            c.announce(MaplePacketCreator.scrolledItem(scroll, toScroll, true));
            if (dst < 0) {
                c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).removeItem(toScroll.getPosition());
            } else {
                c.getPlayer().getInventory(MapleInventoryType.EQUIP).removeItem(toScroll.getPosition());
            }
        } else {
            c.announce(MaplePacketCreator.scrolledItem(scroll, scrolled, false));
        }
        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.getScrollEffect(c.getPlayer().getId(), scrollSuccess, legendarySpirit));
        if (dst < 0 && (scrollSuccess == IEquip.ScrollResult.SUCCESS || scrollSuccess == IEquip.ScrollResult.CURSE)) {
            c.getPlayer().equipChanged();
        }
    }

    private boolean isCleanSlate(int scrollId) {
        return scrollId > 2048999 && scrollId < 2049004;
    }

    public boolean canScroll(int scrollid, int itemid) {
        return (scrollid / 100) % 100 == (itemid / 10000) % 100;
    }
}
