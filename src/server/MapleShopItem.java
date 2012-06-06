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

/**
 * 
 * @author Matze
 */
public class MapleShopItem {
	private short buyable;
	private int itemId;
	private int price;
	private int pitch;

	public MapleShopItem(short buyable, int itemId, int price, int pitch) {
		this.buyable = buyable;
		this.itemId = itemId;
		this.price = price;
		this.pitch = pitch;
	}

	public short getBuyable() {
		return buyable;
	}

	public int getItemId() {
		return itemId;
	}

	public int getPrice() {
		return price;
	}

	public int getPitch() {
		return pitch;
	}
}
