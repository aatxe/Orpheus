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

import tools.Triplet;

/**
 * @author Aaron Weiss
 */
public class MapleStock {
	private String name;
	private String ticker;
	private Triplet<Integer, Integer, Integer> data;
	

	public MapleStock(String ticker, int count, int value, int change) {
		this(null, ticker, new Triplet<Integer, Integer, Integer>(count, value, change));
	}
	
	public MapleStock(String name, String ticker, int count, int value, int change) {
		this(name, ticker, new Triplet<Integer, Integer, Integer>(count, value, change));
	}
	
	public MapleStock(String name, String ticker, Triplet<Integer, Integer, Integer> data) {
		if (name != null) {
			this.name = name;
		} else {
			this.name = "Unknown";
		}
		this.ticker = ticker;
		this.data = data;
	}
	
	public String getName() {
		return name;
	}
	
	public String getTicker() {
		return ticker;
	}
	
	public int getCount() {
		return data.getLeft();
	}
	
	public int getValue() {
		return data.getMiddle();
	}
	
	public int getChange() {
		return data.getRight();
	}
	
	public void update(int change) {
		this.update(new Triplet<Integer, Integer, Integer>(this.getCount(), this.getValue(), change));
	}
	
	public void update(int value, int change) {
		this.update(new Triplet<Integer, Integer, Integer>(this.getCount(), value, change));
	}
	
	public void update(int count, int value, int change) {
		this.update(new Triplet<Integer, Integer, Integer>(count, value, change));
	}
	
	public void update(Triplet<Integer, Integer, Integer> data) {
		this.data = data;
	}
	
	public boolean equals(Object o) {
		if (this.getClass() != o.getClass()) return false;
		if (this == (MapleStock) o) return true;
		MapleStock c = (MapleStock) o;
		return (c.getName() == this.getName() && c.getTicker() == this.getTicker());
	}
}