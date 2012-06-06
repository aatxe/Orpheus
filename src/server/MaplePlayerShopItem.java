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

import client.IItem;

/**
 * 
 * @author Matze
 */
public class MaplePlayerShopItem {
	private IItem item;
	private short bundles;
	private int price;
	private boolean doesExist;

	public MaplePlayerShopItem(IItem item, short bundles, int price) {
		this.item = item;
		this.bundles = bundles;
		this.price = price;
		this.doesExist = true;
	}

	public void setDoesExist(boolean tf) {
		this.doesExist = tf;
	}

	public boolean isExist() {
		return doesExist;
	}

	public IItem getItem() {
		return item;
	}

	public short getBundles() {
		return bundles;
	}

	public int getPrice() {
		return price;
	}

	public void setBundles(short bundles) {
		this.bundles = bundles;
	}
}