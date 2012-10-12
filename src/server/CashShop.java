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

import client.IEquip;
import client.IItem;
import client.Item;
import client.ItemFactory;
import client.ItemInventoryEntry;
import client.MapleInventoryType;
import client.MaplePet;
import constants.ItemConstants;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.DatabaseConnection;

/*
 * @author Flav
 */
public class CashShop {
	public static class CashItem {

		private int sn, itemId, price;
		private long period;
		private short count;
		private boolean onSale;

		private CashItem(int sn, int itemId, int price, long period, short count, boolean onSale) {
			this.sn = sn;
			this.itemId = itemId;
			this.price = price;
			this.period = (period == 0 ? 90 : period);
			this.count = count;
			this.onSale = onSale;
		}

		public int getSN() {
			return sn;
		}

		public int getItemId() {
			return itemId;
		}

		public int getPrice() {
			return price;
		}

		public short getCount() {
			return count;
		}

		public boolean isOnSale() {
			return onSale;
		}

		public IItem toItem() {
			MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
			IItem item;

			int petid = -1;

			if (ItemConstants.isPet(itemId))
				petid = MaplePet.createPet(itemId);

			if (ii.getInventoryType(itemId).equals(MapleInventoryType.EQUIP)) {
				item = ii.getEquipById(itemId);
			} else {
				item = new Item(itemId, (byte) 0, count, petid);
			}

			if (ItemConstants.EXPIRING_ITEMS)
				item.setExpiration(period == 1 ? System.currentTimeMillis() + (1000 * 60 * 60 * 4 * period) : System.currentTimeMillis() + (1000 * 60 * 60 * 24 * period));

			item.setSN(sn);
			return item;
		}
	}

	public static class SpecialCashItem {
		private int sn, modifier;
		private byte info; // ?

		public SpecialCashItem(int sn, int modifier, byte info) {
			this.sn = sn;
			this.modifier = modifier;
			this.info = info;
		}

		public int getSN() {
			return sn;
		}

		public int getModifier() {
			return modifier;
		}

		public byte getInfo() {
			return info;
		}
	}

	public static class CashItemFactory {

		private static final Map<Integer, CashItem> items = new HashMap<Integer, CashItem>();
		private static final Map<Integer, List<Integer>> packages = new HashMap<Integer, List<Integer>>();
		private static final List<SpecialCashItem> specialcashitems = new ArrayList<SpecialCashItem>();

		static {
			MapleDataProvider etc = MapleDataProviderFactory.getDataProvider(new File("wz/Etc.wz"));

			for (MapleData item : etc.getData("Commodity.img").getChildren()) {
				int sn = MapleDataTool.getIntConvert("SN", item);
				int itemId = MapleDataTool.getIntConvert("ItemId", item);
				int price = MapleDataTool.getIntConvert("Price", item, 0);
				long period = MapleDataTool.getIntConvert("Period", item, 1);
				short count = (short) MapleDataTool.getIntConvert("Count", item, 1);
				boolean onSale = MapleDataTool.getIntConvert("OnSale", item, 0) == 1;
				items.put(sn, new CashItem(sn, itemId, price, period, count, onSale));
			}

			for (MapleData cashPackage : etc.getData("CashPackage.img").getChildren()) {
				List<Integer> cPackage = new ArrayList<Integer>();

				for (MapleData item : cashPackage.getChildByPath("SN").getChildren()) {
					cPackage.add(Integer.parseInt(item.getData().toString()));
				}

				packages.put(Integer.parseInt(cashPackage.getName()), cPackage);
			}
			PreparedStatement ps = null;
			ResultSet rs = null;
			try {
				ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM specialcashitems");
				rs = ps.executeQuery();
				while (rs.next()) {
					specialcashitems.add(new SpecialCashItem(rs.getInt("sn"), rs.getInt("modifier"), rs.getByte("info")));
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			} finally {
				try {
					if (rs != null)
						rs.close();
					if (ps != null)
						ps.close();
				} catch (SQLException ex) {
				}
			}
		}

		public static CashItem getItem(int sn) {
			return items.get(sn);
		}

		public static List<IItem> getPackage(int itemId) {
			List<IItem> cashPackage = new ArrayList<IItem>();

			for (int sn : packages.get(itemId)) {
				cashPackage.add(getItem(sn).toItem());
			}

			return cashPackage;
		}

		public static boolean isPackage(int itemId) {
			return packages.containsKey(itemId);
		}

		public static List<SpecialCashItem> getSpecialCashItems() {
			return specialcashitems;
		}

		public static void reloadSpecialCashItems() {// Yay?
			specialcashitems.clear();
			PreparedStatement ps = null;
			ResultSet rs = null;
			try {
				ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM specialcashitems");
				rs = ps.executeQuery();
				while (rs.next()) {
					specialcashitems.add(new SpecialCashItem(rs.getInt("sn"), rs.getInt("modifier"), rs.getByte("info")));
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			} finally {
				try {
					if (rs != null)
						rs.close();
					if (ps != null)
						ps.close();
				} catch (SQLException ex) {
				}
			}
		}
	}

	private int accountId, characterId, nxCredit, maplePoint, nxPrepaid;
	private boolean opened;
	private ItemFactory factory;
	private List<IItem> inventory = new ArrayList<IItem>();
	private List<Integer> wishList = new ArrayList<Integer>();
	private int notes = 0;

	public CashShop(int accountId, int characterId, int jobType) throws SQLException {
		this.accountId = accountId;
		this.characterId = characterId;

		if (jobType == 0) {
			factory = ItemFactory.CASH_EXPLORER;
		} else if (jobType == 1) {
			factory = ItemFactory.CASH_CYGNUS;
		} else if (jobType == 2) {
			factory = ItemFactory.CASH_ARAN;
		}

		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT `nxCredit`, `maplePoint`, `nxPrepaid` FROM `accounts` WHERE `id` = ?");
			ps.setInt(1, accountId);
			rs = ps.executeQuery();

			if (rs.next()) {
				this.nxCredit = rs.getInt("nxCredit");
				this.maplePoint = rs.getInt("maplePoint");
				this.nxPrepaid = rs.getInt("nxPrepaid");
			}

			rs.close();
			ps.close();

			for (ItemInventoryEntry entry : factory.loadItems(accountId, false)) {
				inventory.add(entry.item);
			}

			ps = con.prepareStatement("SELECT `sn` FROM `wishlists` WHERE `charid` = ?");
			ps.setInt(1, characterId);
			rs = ps.executeQuery();

			while (rs.next()) {
				wishList.add(rs.getInt("sn"));
			}

			rs.close();
			ps.close();
		} finally {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		}
	}

	public int getCash(int type) {
		switch (type) {
			case 1:
				return nxCredit;
			case 2:
				return maplePoint;
			case 4:
				return nxPrepaid;
		}

		return 0;
	}

	public void gainCash(int type, int cash) {
		switch (type) {
			case 1:
				nxCredit += cash;
				break;
			case 2:
				maplePoint += cash;
				break;
			case 4:
				nxPrepaid += cash;
				break;
		}
	}

	public boolean isOpened() {
		return opened;
	}

	public void open(boolean b) {
		opened = b;
	}

	public List<IItem> getInventory() {
		return inventory;
	}

	public IItem findByCashId(int cashId) {
		boolean isRing = false;
		IEquip equip = null;
		for (IItem item : inventory) {
			if (item.getType() == IItem.EQUIP) {
				equip = (IEquip) item;
				isRing = equip.getRingId() > -1;
			}
			if ((item.getPetId() > -1 ? item.getPetId() : isRing ? equip.getRingId() : item.getCashId()) == cashId) {
				return item;
			}
		}

		return null;
	}

	public void addToInventory(IItem item) {
		inventory.add(item);
	}

	public void removeFromInventory(IItem item) {
		inventory.remove(item);
	}

	public List<Integer> getWishList() {
		return wishList;
	}

	public void clearWishList() {
		wishList.clear();
	}

	public void addToWishList(int sn) {
		wishList.add(sn);
	}

	public void gift(int recipient, String from, String message, int sn) {
		gift(recipient, from, message, sn, -1);
	}

	public void gift(int recipient, String from, String message, int sn, int ringid) {
		PreparedStatement ps = null;
		try {
			ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO `gifts` VALUES (DEFAULT, ?, ?, ?, ?, ?)");
			ps.setInt(1, recipient);
			ps.setString(2, from);
			ps.setString(3, message);
			ps.setInt(4, sn);
			ps.setInt(5, ringid);
			ps.executeUpdate();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException ex) {
			}
		}
	}

	public List<GiftEntry> loadGifts() {
		List<GiftEntry> gifts = new ArrayList<GiftEntry>();
		Connection con = DatabaseConnection.getConnection();

		try {
			PreparedStatement ps = con.prepareStatement("SELECT * FROM `gifts` WHERE `to` = ?");
			ps.setInt(1, characterId);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				notes++;
				CashItem cItem = CashItemFactory.getItem(rs.getInt("sn"));
				IItem item = cItem.toItem();
				IEquip equip = null;
				item.setGiftFrom(rs.getString("from"));
				if (item.getType() == MapleInventoryType.EQUIP.getType()) {
					equip = (IEquip) item;
					equip.setRingId(rs.getInt("ringid"));
					gifts.add(new GiftEntry(equip, rs.getString("message")));
				} else
					gifts.add(new GiftEntry(item, rs.getString("message")));

				if (CashItemFactory.isPackage(cItem.getItemId())) { // Packages
																	// never
																	// contains
																	// a ring
					for (IItem packageItem : CashItemFactory.getPackage(cItem.getItemId())) {
						packageItem.setGiftFrom(rs.getString("from"));
						addToInventory(packageItem);
					}
				} else {
					addToInventory(equip == null ? item : equip);
				}
			}

			rs.close();
			ps.close();
			ps = con.prepareStatement("DELETE FROM `gifts` WHERE `to` = ?");
			ps.setInt(1, characterId);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}

		return gifts;
	}

	public int getAvailableNotes() {
		return notes;
	}

	public void decreaseNotes() {
		notes--;
	}

	public void save() throws SQLException {
		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = con.prepareStatement("UPDATE `accounts` SET `nxCredit` = ?, `maplePoint` = ?, `nxPrepaid` = ? WHERE `id` = ?");
		ps.setInt(1, nxCredit);
		ps.setInt(2, maplePoint);
		ps.setInt(3, nxPrepaid);
		ps.setInt(4, accountId);
		ps.executeUpdate();
		ps.close();
		List<ItemInventoryEntry> itemsWithType = new ArrayList<ItemInventoryEntry>();

		for (IItem item : inventory) {
			itemsWithType.add(new ItemInventoryEntry(item, MapleItemInformationProvider.getInstance().getInventoryType(item.getItemId())));
		}

		factory.saveItems(itemsWithType, accountId);
		ps = con.prepareStatement("DELETE FROM `wishlists` WHERE `charid` = ?");
		ps.setInt(1, characterId);
		ps.executeUpdate();
		ps = con.prepareStatement("INSERT INTO `wishlists` VALUES (DEFAULT, ?, ?)");
		ps.setInt(1, characterId);

		for (int sn : wishList) {
			ps.setInt(2, sn);
			ps.executeUpdate();
		}

		ps.close();
	}
}
