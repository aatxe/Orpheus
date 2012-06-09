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
package tools;

import java.io.Serializable;

/**
 * @author Aaron Weiss
 */
public class Triplet<E, F, G> implements Serializable { // why? because pairs of pairs imply something else
	private static final long serialVersionUID = 9000504761605746651L;
	private E left;
	private F middle;
	private G right;

	public Triplet(E left, F middle, G right) {
		this.left = left;
		this.middle = middle;
		this.right = right;
	}

	public E getLeft() {
		return left;
	}
	
	public F getMiddle() {
		return middle;
	}

	public G getRight() {
		return right;
	}
	
	public void update(E left, F middle, G right) {
		this.left = left;
		this.middle = middle;
		this.right = right;
	}
	
	@SuppressWarnings("rawtypes")
	public boolean equals(Object o) {
	    if (this.getClass() != o.getClass()) return false;
	    if (this == (Triplet) o) return true;
	    Triplet compare = (Triplet) o;
	    return compare.getLeft() == this.getLeft() && compare.getMiddle() == this.getMiddle() && compare.getRight() == this.getRight();
	}
}

