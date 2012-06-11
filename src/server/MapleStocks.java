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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import net.server.Channel;
import net.server.Server;
import net.server.World;
import client.MapleCharacter;
import client.MapleStock;
import com.mysql.jdbc.Connection;
import constants.ServerConstants;
import tools.DatabaseConnection;
import tools.MapleLogger;
import tools.Output;
import tools.Pair;

/**
 * @author Aaron Weiss
 */
public class MapleStocks {
	private static MapleStocks instance;
	private ArrayList<MapleStock> stocks = new ArrayList<MapleStock>();
	private ArrayList<Pair<String, Integer>> oldTotals = null;
	
	public void add(String name, String ticker, int count, int value) {
		this.add(new MapleStock(name, ticker, count, value, 0));
	}
	
	public void add(MapleStock ms) {
		try {
			Connection con = (Connection) DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO maplestocks (`name`, `ticker`, `count`, `value`, `change`) VALUES (?, ?, ?, ?, ?)");
			ps.setString(1, ms.getName());
			ps.setString(2, ms.getTicker());
			ps.setInt(3, ms.getCount());
			ps.setInt(4, ms.getValue());
			ps.setInt(5, ms.getChange());
		} catch (SQLException e) {
			Output.print("MapleStocks failed to add a new stock. Check exceptions.log.");
			MapleLogger.print(MapleLogger.EXCEPTION_CAUGHT, e);
		}
		stocks.add(ms);
	}
	
	public String tickerOf(int id) {
		return stocks.get(id - 1).getTicker();
	}
	
	public int indexOf(String ticker) {
		int n = 0;
		for (MapleStock ms : stocks) {
			if (ms.getTicker() == ticker) {
				return n;
			}
			n++;
		}
		return -1;
	}
	
	public int indexOf(MapleStock ms) {
		return stocks.indexOf(ms);
	}
	
	public int idOf(String ticker) {
		return indexOf(ticker) + 1;
	}
	
	public int idOf(MapleStock ms) {
		return indexOf(ms) + 1;
	}
	
	public ArrayList<MapleStock> getStocks() {
		return stocks;
	}
	
	public int getTotalSold(String ticker) {
		int sum = 0;
		for (World w : Server.getInstance().getWorlds()) {
			for (Channel c : w.getChannels()) {
				for (MapleCharacter chr : c.getPlayerStorage().getAllCharacters()) {
					for (Pair<String, Integer> pair : chr.getStockPortfolio().toArrayList()) {
						if (pair.getLeft() == ticker) {
							sum += pair.getRight();
						}
					}
				}
			}
		}
		return sum;
	}
	
	public String getNameByTicker(String ticker) {
		for (MapleStock ms : stocks) {
			if (ms.getTicker().equalsIgnoreCase(ticker)) {
				return ms.getName();
			}
		}
		return null;
	}
	
	public MapleStock getStock(String ticker) {
		for (MapleStock ms : stocks) {
			if (ms.getTicker().equalsIgnoreCase(ticker)) {
				return ms;
			}
		}
		return null;
	}
	
	public boolean stockExists(String ticker) {
		for (MapleStock ms : stocks) {
			if (ms.getTicker().equalsIgnoreCase(ticker)) {
				return true;
			}
		}
		return false;
	}
	
	public void loadAll() {
		try {
			Connection con = (Connection) DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM maplestocks ORDER BY stockid");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				stocks.add(new MapleStock(rs.getString("name"), rs.getString("ticker"), rs.getInt("count"), rs.getInt("value"), rs.getInt("change")));
			}
		} catch (SQLException ex) {
			Output.print("MapleStocks failed to load. Check exceptions.log.");
			MapleLogger.print(MapleLogger.EXCEPTION_CAUGHT, ex);
		}
	}
	
	public void calculateUpdate() {
		ArrayList<Pair<String, Integer>> totals = new ArrayList<Pair<String, Integer>>();
		for (World w : Server.getInstance().getWorlds()) {
			for (Channel c : w.getChannels()) {
				for (MapleCharacter chr : c.getPlayerStorage().getAllCharacters()) {
					for (Pair<String, Integer> pair : chr.getStockPortfolio().toArrayList()) {
						boolean add = true;
						for (Pair<String, Integer> totalPair : totals) {
							if (pair.getLeft() == totalPair.getLeft()) {
								add = false;
								totalPair.update(totalPair.getLeft(), totalPair.getRight() + pair.getRight());
							}
						}
						if (add) {
							totals.add(pair);
						}
					}
				}
			}
		}
		for (Pair<String, Integer> pair : totals) {
			for (Pair<String, Integer> oldPair : oldTotals) {
				if (oldPair.getLeft() == pair.getLeft()) {
					for (MapleStock ms : stocks) {
						if (pair.getLeft() == ms.getTicker()) {
							double changeTotal = (pair.getRight() - oldPair.getRight());
							if ((((double) Math.abs(changeTotal) / oldPair.getRight()) <= ServerConstants.STOCK_CRASH_THRESHOLD) && changeTotal < 0) {
								crashStock(ms);
							} else if (((double) Math.abs(changeTotal) / oldPair.getRight()) < ServerConstants.STOCK_DECLINE_THRESHOLD && changeTotal < 0) { 
								decreaseStock(ms);
							} else if (((double) Math.abs(changeTotal) / oldPair.getRight()) > ServerConstants.STOCK_DECLINE_THRESHOLD) {
								increaseStock(ms);
							}
						}
					}
				}
			}
		}
		oldTotals = totals;
	}
	
	public void pushUpdate() {
		try {
			Connection con = (Connection) DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM maplestocks ORDER BY stockid");
			ResultSet rs = ps.executeQuery();
			for (MapleStock ms : stocks) {
				rs.next();
				ps = con.prepareStatement("UPDATE maplestocks SET count = ?, value = ?, change = ? WHERE stockid = ?");
				ps.setInt(1, ms.getCount());
				ps.setInt(2, ms.getValue());
				ps.setInt(3, (ms.getValue() - rs.getInt("value")));
				ps.setInt(4, rs.getInt("stockid"));
				Output.print(ps.toString());
				ps.executeUpdate();
				ms.update((ms.getValue() - rs.getInt("value")));
			}
		} catch (SQLException ex) {
			Output.print("MapleStocks failed to update. Check exceptions.log.");
			MapleLogger.print(MapleLogger.EXCEPTION_CAUGHT, ex);
		}
	}
	
	public void crashStock(MapleStock ms) {
		if (ServerConstants.ALLOW_STOCK_CRASHES) {
			int value = (int) (0.50 * ms.getValue());
			if (value < 1) {
				value = 1;
			}
			ms.update(value, value - ms.getValue());
		} else {
			decreaseStock(ms);
		}
	}
	
	public void increaseStock(MapleStock ms) {
		int value = (int) (1.05 * ms.getValue());
		if (value > ServerConstants.STOCK_VALUE_CAP) {
			value = ServerConstants.STOCK_VALUE_CAP;
		}
		ms.update(value, value - ms.getValue());
	}
	
	public void decreaseStock(MapleStock ms) {
		int value = (int) (0.95 * ms.getValue());
		if (value < 1) {
			value = 1;
		}
		ms.update(value, value - ms.getValue());
	}
	
	public void clear() {
		stocks = null;
		System.gc();
		stocks = new ArrayList<MapleStock>();
	}
	
	public static MapleStocks getInstance() {
		return MapleStocks.getInstance(true);
	}
	
	public static MapleStocks getInstance(boolean loadFromDb) {
		if (instance == null) {
			instance = new MapleStocks();
			if (loadFromDb) {
				instance.loadAll();
			}
		}
		return instance;
	}

	public static void clearInstance() {
		instance.clear();
		System.gc();
	}
}
