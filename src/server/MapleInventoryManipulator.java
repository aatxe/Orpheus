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
package server;

import java.awt.Point;
import java.util.Iterator;
import java.util.List;
import client.Equip;
import client.IItem;
import client.Item;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import constants.ItemConstants;
import constants.ServerConstants;
import tools.MaplePacketCreator;
import tools.Output;

/**
 * 
 * @author Matze
 */
public class MapleInventoryManipulator {
	public static boolean addRing(MapleCharacter chr, int itemId, int ringId) {
		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
		MapleInventoryType type = ii.getInventoryType(itemId);
		IItem nEquip = ii.getEquipById(itemId, ringId);
		byte newSlot = chr.getInventory(type).addItem(nEquip);
		if (newSlot == -1) {
			return false;
		}
		chr.getClient().announce(MaplePacketCreator.addInventorySlot(type, nEquip));
		return true;
	}

	public static boolean addById(MapleClient c, int itemId, short quantity) {
		return addById(c, itemId, quantity, null, -1, -1);
	}

	public static boolean addById(MapleClient c, int itemId, short quantity, long expiration) {
		return addById(c, itemId, quantity, null, -1, (byte) 0, expiration);
	}

	public static boolean addById(MapleClient c, int itemId, short quantity, String owner, int petid) {
		return addById(c, itemId, quantity, owner, petid, -1);
	}

	public static boolean addById(MapleClient c, int itemId, short quantity, String owner, int petid, long expiration) {
		return addById(c, itemId, quantity, owner, petid, (byte) 0, expiration);
	}

	public static boolean addById(MapleClient c, int itemId, short quantity, String owner, int petid, byte flag, long expiration) {
		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
		MapleInventoryType type = ii.getInventoryType(itemId);
		if (!type.equals(MapleInventoryType.EQUIP)) {
			short slotMax = ii.getSlotMax(c, itemId);
			List<IItem> existing = c.getPlayer().getInventory(type).listById(itemId);
			if (!ItemConstants.isRechargable(itemId)) {
				if (existing.size() > 0) { // first update all existing slots to
											// slotMax
					Iterator<IItem> i = existing.iterator();
					while (quantity > 0) {
						if (i.hasNext()) {
							Item eItem = (Item) i.next();
							short oldQ = eItem.getQuantity();
							if (oldQ < slotMax && (eItem.getOwner().equals(owner) || owner == null)) {
								short newQ = (short) Math.min(oldQ + quantity, slotMax);
								quantity -= (newQ - oldQ);
								eItem.setQuantity(newQ);
								eItem.setExpiration(expiration);
								c.announce(MaplePacketCreator.updateInventorySlot(type, eItem));
							}
						} else {
							break;
						}
					}
				}
				while (quantity > 0 || ItemConstants.isRechargable(itemId)) {
					short newQ = (short) Math.min(quantity, slotMax);
					if (newQ != 0) {
						quantity -= newQ;
						Item nItem = new Item(itemId, (byte) 0, newQ, petid);
						nItem.setFlag(flag);
						nItem.setExpiration(expiration);
						byte newSlot = c.getPlayer().getInventory(type).addItem(nItem);
						if (newSlot == -1) {
							c.announce(MaplePacketCreator.getInventoryFull());
							c.announce(MaplePacketCreator.getShowInventoryFull());
							return false;
						}
						if (owner != null) {
							nItem.setOwner(owner);
						}
						c.announce(MaplePacketCreator.addInventorySlot(type, nItem));
						if ((ItemConstants.isRechargable(itemId)) && quantity == 0) {
							break;
						}
					} else {
						c.announce(MaplePacketCreator.enableActions());
						return false;
					}
				}
			} else {
				Item nItem = new Item(itemId, (byte) 0, quantity, petid);
				nItem.setFlag(flag);
				nItem.setExpiration(expiration);
				byte newSlot = c.getPlayer().getInventory(type).addItem(nItem);
				if (newSlot == -1) {
					c.announce(MaplePacketCreator.getInventoryFull());
					c.announce(MaplePacketCreator.getShowInventoryFull());
					return false;
				}
				c.announce(MaplePacketCreator.addInventorySlot(type, nItem));
				c.announce(MaplePacketCreator.enableActions());
			}
		} else if (quantity == 1) {
			IItem nEquip = ii.getEquipById(itemId);
			nEquip.setFlag(flag);
			nEquip.setExpiration(expiration);
			if (owner != null) {
				nEquip.setOwner(owner);
			}
			byte newSlot = c.getPlayer().getInventory(type).addItem(nEquip);
			if (newSlot == -1) {
				c.announce(MaplePacketCreator.getInventoryFull());
				c.announce(MaplePacketCreator.getShowInventoryFull());
				return false;
			}
			c.announce(MaplePacketCreator.addInventorySlot(type, nEquip));
		} else {
			throw new RuntimeException("Trying to create equip with non-one quantity");
		}
		return true;
	}

	public static boolean addFromDrop(MapleClient c, IItem item, boolean show) {
		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
		MapleInventoryType type = ii.getInventoryType(item.getItemId());
		if (ii.isPickupRestricted(item.getItemId()) && c.getPlayer().getItemQuantity(item.getItemId(), true) > 0) {
			c.announce(MaplePacketCreator.getInventoryFull());
			c.announce(MaplePacketCreator.showItemUnavailable());
			return false;
		}
		short quantity = item.getQuantity();
		if (!type.equals(MapleInventoryType.EQUIP)) {
			short slotMax = ii.getSlotMax(c, item.getItemId());
			List<IItem> existing = c.getPlayer().getInventory(type).listById(item.getItemId());
			if (!ItemConstants.isRechargable(item.getItemId())) {
				if (existing.size() > 0) { // first update all existing slots to
											// slotMax
					Iterator<IItem> i = existing.iterator();
					while (quantity > 0) {
						if (i.hasNext()) {
							Item eItem = (Item) i.next();
							short oldQ = eItem.getQuantity();
							if (oldQ < slotMax && item.getOwner().equals(eItem.getOwner())) {
								short newQ = (short) Math.min(oldQ + quantity, slotMax);
								quantity -= (newQ - oldQ);
								eItem.setQuantity(newQ);
								c.announce(MaplePacketCreator.updateInventorySlot(type, eItem, true));
							}
						} else {
							break;
						}
					}
				}
				while (quantity > 0 || ItemConstants.isRechargable(item.getItemId())) {
					short newQ = (short) Math.min(quantity, slotMax);
					quantity -= newQ;
					Item nItem = new Item(item.getItemId(), (byte) 0, newQ);
					nItem.setExpiration(item.getExpiration());
					nItem.setOwner(item.getOwner());
					byte newSlot = c.getPlayer().getInventory(type).addItem(nItem);
					if (newSlot == -1) {
						c.announce(MaplePacketCreator.getInventoryFull());
						c.announce(MaplePacketCreator.getShowInventoryFull());
						item.setQuantity((short) (quantity + newQ));
						return false;
					}
					c.announce(MaplePacketCreator.addInventorySlot(type, nItem, true));
					if ((ItemConstants.isRechargable(item.getItemId())) && quantity == 0) {
						break;
					}
				}
			} else {
				Item nItem = new Item(item.getItemId(), (byte) 0, quantity);
				byte newSlot = c.getPlayer().getInventory(type).addItem(nItem);
				if (newSlot == -1) {
					c.announce(MaplePacketCreator.getInventoryFull());
					c.announce(MaplePacketCreator.getShowInventoryFull());
					return false;
				}
				c.announce(MaplePacketCreator.addInventorySlot(type, nItem));
				c.announce(MaplePacketCreator.enableActions());
			}
		} else if (quantity == 1) {
			byte newSlot = c.getPlayer().getInventory(type).addItem(item);
			if (newSlot == -1) {
				c.announce(MaplePacketCreator.getInventoryFull());
				c.announce(MaplePacketCreator.getShowInventoryFull());
				return false;
			}
			c.announce(MaplePacketCreator.addInventorySlot(type, item, true));
		} else {
			return false;
		}
		if (show) {
			c.announce(MaplePacketCreator.getShowItemGain(item.getItemId(), item.getQuantity()));
		}
		return true;
	}

	public static boolean checkSpace(MapleClient c, int itemid, int quantity, String owner) {
		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
		MapleInventoryType type = ii.getInventoryType(itemid);
		if (!type.equals(MapleInventoryType.EQUIP)) {
			short slotMax = ii.getSlotMax(c, itemid);
			List<IItem> existing = c.getPlayer().getInventory(type).listById(itemid);
			if (!ItemConstants.isRechargable(itemid)) {
				if (existing.size() > 0) // first update all existing slots to
											// slotMax
				{
					for (IItem eItem : existing) {
						short oldQ = eItem.getQuantity();
						if (oldQ < slotMax && owner.equals(eItem.getOwner())) {
							short newQ = (short) Math.min(oldQ + quantity, slotMax);
							quantity -= (newQ - oldQ);
						}
						if (quantity <= 0) {
							break;
						}
					}
				}
			}
			final int numSlotsNeeded;
			if (slotMax > 0) {
				numSlotsNeeded = (int) (Math.ceil(((double) quantity) / slotMax));
			} else if (ItemConstants.isRechargable(itemid)) {
				numSlotsNeeded = 1;
			} else {
				numSlotsNeeded = 1;
				Output.print("checkSpace error");
			}
			return !c.getPlayer().getInventory(type).isFull(numSlotsNeeded - 1);
		} else {
			return !c.getPlayer().getInventory(type).isFull();
		}
	}

	public static void removeFromSlot(MapleClient c, MapleInventoryType type, byte slot, short quantity, boolean fromDrop) {
		removeFromSlot(c, type, slot, quantity, fromDrop, false);
	}

	public static void removeFromSlot(MapleClient c, MapleInventoryType type, byte slot, short quantity, boolean fromDrop, boolean consume) {
		IItem item = c.getPlayer().getInventory(type).getItem(slot);
		boolean allowZero = consume && ItemConstants.isRechargable(item.getItemId());
		c.getPlayer().getInventory(type).removeItem(slot, quantity, allowZero);
		if (item.getQuantity() == 0 && !allowZero) {
			c.announce(MaplePacketCreator.clearInventoryItem(type, item.getPosition(), fromDrop));
		} else {
			c.announce(MaplePacketCreator.updateInventorySlot(type, (Item) item, fromDrop));
		}
	}

	public static void removeById(MapleClient c, MapleInventoryType type, int itemId, int quantity, boolean fromDrop, boolean consume) {
		List<IItem> items = c.getPlayer().getInventory(type).listById(itemId);
		int remremove = quantity;
		for (IItem item : items) {
			if (remremove <= item.getQuantity()) {
				removeFromSlot(c, type, item.getPosition(), (short) remremove, fromDrop, consume);
				remremove = 0;
				break;
			} else {
				remremove -= item.getQuantity();
				removeFromSlot(c, type, item.getPosition(), item.getQuantity(), fromDrop, consume);
			}
		}
		if (remremove > 0) {
			throw new RuntimeException("[h4x] Not enough items available (" + itemId + ", " + (quantity - remremove) + "/" + quantity + ")");
		}
	}

	public static void move(MapleClient c, MapleInventoryType type, byte src, byte dst) {
		if (src < 0 || dst < 0) {
			return;
		}
		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
		IItem source = c.getPlayer().getInventory(type).getItem(src);
		IItem initialTarget = c.getPlayer().getInventory(type).getItem(dst);
		if (source == null) {
			return;
		}
		short olddstQ = -1;
		if (initialTarget != null) {
			olddstQ = initialTarget.getQuantity();
		}
		short oldsrcQ = source.getQuantity();
		short slotMax = ii.getSlotMax(c, source.getItemId());
		c.getPlayer().getInventory(type).move(src, dst, slotMax);
		if (!type.equals(MapleInventoryType.EQUIP) && initialTarget != null && initialTarget.getItemId() == source.getItemId() && !ItemConstants.isRechargable(source.getItemId())) {
			if ((olddstQ + oldsrcQ) > slotMax) {
				c.announce(MaplePacketCreator.moveAndMergeWithRestInventoryItem(type, src, dst, (short) ((olddstQ + oldsrcQ) - slotMax), slotMax));
			} else {
				c.announce(MaplePacketCreator.moveAndMergeInventoryItem(type, src, dst, ((Item) c.getPlayer().getInventory(type).getItem(dst)).getQuantity()));
			}
		} else {
			c.announce(MaplePacketCreator.moveInventoryItem(type, src, dst));
		}
	}

	public static void equip(MapleClient c, byte src, byte dst) {
		Equip source = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(src);
		Equip target = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(dst);
		if (source == null || !MapleItemInformationProvider.getInstance().canWearEquipment(c.getPlayer(), source)) {
			c.announce(MaplePacketCreator.enableActions());
			return;
		} else if ((((source.getItemId() >= 1902000 && source.getItemId() <= 1902002) || source.getItemId() == 1912000) && c.getPlayer().isCygnus()) || ((source.getItemId() >= 1902005 && source.getItemId() <= 1902007) || source.getItemId() == 1912005) && !c.getPlayer().isCygnus()) {// Adventurer
																																																																							// taming
																																																																							// equipment
			return;
		}
		if (MapleItemInformationProvider.getInstance().isUntradeableOnEquip(source.getItemId())) {
			source.setFlag((byte) ItemConstants.UNTRADEABLE);
		}
		if (source.getRingId() > -1) {
			c.getPlayer().getRingById(source.getRingId()).equip();
		}
		if (dst == -6) { // unequip the overall
			IItem top = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -5);
			if (top != null && isOverall(top.getItemId())) {
				if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).isFull()) {
					c.announce(MaplePacketCreator.getInventoryFull());
					c.announce(MaplePacketCreator.getShowInventoryFull());
					return;
				}
				unequip(c, (byte) -5, c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
			}
		} else if (dst == -5) {
			final IItem bottom = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -6);
			if (bottom != null && isOverall(source.getItemId())) {
				if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).isFull()) {
					c.announce(MaplePacketCreator.getInventoryFull());
					c.announce(MaplePacketCreator.getShowInventoryFull());
					return;
				}
				unequip(c, (byte) -6, c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
			}
		} else if (dst == -10) {// check if weapon is two-handed
			IItem weapon = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11);
			if (weapon != null && MapleItemInformationProvider.getInstance().isTwoHanded(weapon.getItemId())) {
				if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).isFull()) {
					c.announce(MaplePacketCreator.getInventoryFull());
					c.announce(MaplePacketCreator.getShowInventoryFull());
					return;
				}
				unequip(c, (byte) -11, c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
			}
		} else if (dst == -11) {
			IItem shield = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -10);
			if (shield != null && MapleItemInformationProvider.getInstance().isTwoHanded(source.getItemId())) {
				if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).isFull()) {
					c.announce(MaplePacketCreator.getInventoryFull());
					c.announce(MaplePacketCreator.getShowInventoryFull());
					return;
				}
				unequip(c, (byte) -10, c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
			}
		}
		if (dst == -18) {
			if (c.getPlayer().getMount() != null) {
				c.getPlayer().getMount().setItemId(source.getItemId());
			}
		}
		if (source.getItemId() == 1122017) {
			c.getPlayer().equipPendantOfSpirit();
		}
		// 1112413, 1112414, 1112405 (Lilin's Ring)
		source = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(src);
		target = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(dst);
		c.getPlayer().getInventory(MapleInventoryType.EQUIP).removeSlot(src);
		if (target != null) {
			c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).removeSlot(dst);
		}
		source.setPosition(dst);
		c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).addFromDB(source);
		if (target != null) {
			target.setPosition(src);
			c.getPlayer().getInventory(MapleInventoryType.EQUIP).addFromDB(target);
		}
		if (c.getPlayer().getBuffedValue(MapleBuffStat.BOOSTER) != null && isWeapon(source.getItemId())) {
			c.getPlayer().cancelBuffStats(MapleBuffStat.BOOSTER);
		}
		c.announce(MaplePacketCreator.moveInventoryItem(MapleInventoryType.EQUIP, src, dst, (byte) 2));
		c.getPlayer().forceUpdateItem(MapleInventoryType.EQUIPPED, source);
		c.getPlayer().equipChanged();
	}

	public static void unequip(MapleClient c, byte src, byte dst) {
		Equip source = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(src);
		Equip target = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(dst);
		if (dst < 0) {
			Output.print("Unequipping to negative slot.");
		}
		if (source == null) {
			return;
		}
		if (target != null && src <= 0) {
			c.announce(MaplePacketCreator.getInventoryFull());
			return;
		}
		if (source.getItemId() == 1122017) {
			c.getPlayer().unequipPendantOfSpirit();
		}
		if (source.getRingId() > -1) {
			c.getPlayer().getRingById(source.getRingId()).unequip();
		}
		c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).removeSlot(src);
		if (target != null) {
			c.getPlayer().getInventory(MapleInventoryType.EQUIP).removeSlot(dst);
		}
		source.setPosition(dst);
		c.getPlayer().getInventory(MapleInventoryType.EQUIP).addFromDB(source);
		if (target != null) {
			target.setPosition(src);
			c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).addFromDB(target);
		}
		c.announce(MaplePacketCreator.moveInventoryItem(MapleInventoryType.EQUIP, src, dst, (byte) 1));
		c.getPlayer().equipChanged();
	}

	public static void drop(MapleClient c, MapleInventoryType type, byte src, short quantity) {
		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
		if (src < 0) {
			type = MapleInventoryType.EQUIPPED;
		}
		IItem source = c.getPlayer().getInventory(type).getItem(src);
		int itemId = source.getItemId();
		if (itemId >= 5000000 && itemId <= 5000100) {
			return;
		}
		if (type == MapleInventoryType.EQUIPPED && itemId == 1122017) {
			c.getPlayer().unequipPendantOfSpirit();
		}
		if (c.getPlayer().getItemEffect() == itemId && source.getQuantity() == 1) {
			c.getPlayer().setItemEffect(0);
			c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.itemEffect(c.getPlayer().getId(), 0));
		} else if (itemId == 5370000 || itemId == 5370001) {
			if (c.getPlayer().getItemQuantity(itemId, false) == 1) {
				c.getPlayer().setChalkboard(null);
			}
		}
		if (c.getPlayer().getItemQuantity(itemId, true) < quantity || quantity < 0 || source == null || quantity == 0 && !ItemConstants.isRechargable(itemId)) {
			return;
		}
		Point dropPos = new Point(c.getPlayer().getPosition());
		if (quantity < source.getQuantity() && !ItemConstants.isRechargable(itemId)) {
			IItem target = source.copy();
			target.setQuantity(quantity);
			source.setQuantity((short) (source.getQuantity() - quantity));
			c.announce(MaplePacketCreator.dropInventoryItemUpdate(type, source));
			boolean weddingRing = source.getItemId() == 1112803 || source.getItemId() == 1112806 || source.getItemId() == 1112807 || source.getItemId() == 1112809;
			if (weddingRing) {
				c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos);
			} else if (c.getPlayer().getMap().getEverlast()) {
				if ((ii.isDropRestricted(target.getItemId()) && !ServerConstants.DROP_UNTRADEABLE_ITEMS) || MapleItemInformationProvider.getInstance().isCash(target.getItemId())) {
					c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos);
				} else {
					c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos, true, false);
				}
			} else if ((ii.isDropRestricted(target.getItemId()) && !ServerConstants.DROP_UNTRADEABLE_ITEMS) || MapleItemInformationProvider.getInstance().isCash(target.getItemId())) {
				c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos);
			} else {
				c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos, true, true);
			}
		} else {
			c.getPlayer().getInventory(type).removeSlot(src);
			c.announce(MaplePacketCreator.dropInventoryItem((src < 0 ? MapleInventoryType.EQUIP : type), src));
			if (src < 0) {
				c.getPlayer().equipChanged();
			}
			if (c.getPlayer().getMap().getEverlast()) {
				if ((ii.isDropRestricted(itemId) && !ServerConstants.DROP_UNTRADEABLE_ITEMS)) {
					c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos);
				} else {
					c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos, true, false);
				}
			} else if ((ii.isDropRestricted(itemId) && !ServerConstants.DROP_UNTRADEABLE_ITEMS)) {
				c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos);
			} else {
				c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos, true, true);
			}
		}
	}

	private static boolean isOverall(int itemId) {
		return itemId / 10000 == 105;
	}

	private static boolean isWeapon(int itemId) {
		return itemId >= 1302000 && itemId < 1492024;
	}
}
