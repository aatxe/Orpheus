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
package client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import server.MapleStocks;
import tools.DatabaseConnection;
import tools.MapleLogger;
import tools.Output;
import tools.Pair;

/**
 * @author Aaron Weiss
 */
public class MapleStockPortfolio {
	private ArrayList<Pair<String, Integer>> portfolio;
	private ArrayList<Pair<String, Integer>> newlyAdded;

	public MapleStockPortfolio() {
		this(new ArrayList<Pair<String, Integer>>());
	}
	
	public MapleStockPortfolio(ArrayList<Pair<String, Integer>> portfolio) {
		this.portfolio = portfolio;
		this.newlyAdded = new ArrayList<Pair<String, Integer>>();
	}
	
	public void add(Pair<String, Integer> shares) {
		if (!this.update(shares)) {
			portfolio.add(shares);
			newlyAdded.add(shares);
		}
	}
	
	public boolean update(Pair<String, Integer> shares) {
		for (Pair<String, Integer> pair : portfolio) {
			if (pair.getLeft() == shares.getLeft()) {
				pair.update(pair.getLeft(), pair.getRight() + shares.getRight());
				return true;
			}
		}
		return false;
	}
	
	public boolean remove(MapleStock ms) {
		return this.remove(ms, 1);
	}
	
	public boolean remove(MapleStock ms, int quantity) {
		for (Pair<String, Integer> pair : portfolio) {
			if (pair.getLeft() == ms.getTicker()) {
				if (pair.getRight() > quantity) {
					return this.update(new Pair<String, Integer>(pair.getLeft(), -quantity));
				} else if (pair.getRight() == quantity) {
					portfolio.remove(pair);
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean hasStock(MapleStock ms) {
		return this.hasStock(ms, 1);
	}
	
	public boolean hasStock(MapleStock ms, int quantity) {
		int amount = 0;
		for (Pair<String, Integer> pair : portfolio) {
			if (pair.getLeft() == ms.getTicker()) amount += pair.getRight();
			if (amount >= quantity) return true;
		}
		return false;
	}
	
	public void save(int cid) {
		if (newlyAdded.isEmpty()) {
			for (Pair<String, Integer> pair : portfolio) {
				try {
					Connection con = (Connection) DatabaseConnection.getConnection();
					PreparedStatement ps = con.prepareStatement("INSERT INTO maplestocks_data (`cid`, `stockid`, `shares`) VALUES (?, ?, ?)");
					ps.setInt(1, cid);
					ps.setInt(2, MapleStocks.getInstance().idOf(pair.getLeft()));
					ps.setInt(3, pair.getRight());
				} catch (SQLException e) {
					Output.print("Something went wrong while saving a MapleStockPortfolio.");
					MapleLogger.print(MapleLogger.EXCEPTION_CAUGHT, e);
				}
			}
			return;
		}
		for (Pair<String, Integer> pair : portfolio) {
			for (Pair<String, Integer> newPair : newlyAdded) {
				try {
					Connection con = (Connection) DatabaseConnection.getConnection();
					PreparedStatement ps;
					if (newPair.getLeft() == pair.getLeft()) {
						ps = con.prepareStatement("UPDATE maplestocks_data SET shares = ? WHERE cid = ? AND stockid = ?");
						ps.setInt(1, pair.getRight());
						ps.setInt(2, cid);
						ps.setInt(3, MapleStocks.getInstance().idOf(pair.getLeft()));
					} else {
						ps = con.prepareStatement("INSERT INTO maplestocks_data (`cid`, `stockid`, `shares`) VALUES (?, ?, ?)");
						ps.setInt(1, cid);
						ps.setInt(2, MapleStocks.getInstance().idOf(pair.getLeft()));
						ps.setInt(3, pair.getRight());
					}
				} catch (SQLException e) {
					Output.print("Something went wrong while saving a MapleStockPortfolio.");
					MapleLogger.print(MapleLogger.EXCEPTION_CAUGHT, e);
				}
			}
		}
		
	}
	
	public static MapleStockPortfolio load(int cid) {
		MapleStockPortfolio ret = null;
		try {
			ret = new MapleStockPortfolio();
			Connection con = (Connection) DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM maplestocks_data WHERE cid = ?");
			ps.setInt(1, cid);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				ret.add(new Pair<String ,Integer>(rs.getString("ticker"), rs.getInt("stocks")));
			}
		} catch (SQLException e) {
			Output.print("Failed to load MapleStockPortfolio " + cid + ".");
			MapleLogger.print(MapleLogger.EXCEPTION_CAUGHT, e);
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public Pair<String, Integer>[] toArray() {
		return (Pair<String, Integer>[]) (portfolio.toArray());
	}
	
	public ArrayList<Pair<String, Integer>> toArrayList() {
		return portfolio;
	}
}
