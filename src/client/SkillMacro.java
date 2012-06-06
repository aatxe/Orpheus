/*
 	OrpheusMS: MapleStory Private Server based on OdinMS
    Copyright (C) 2012 Aaron Weiss

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

public class SkillMacro {
	private int skill1;
	private int skill2;
	private int skill3;
	private String name;
	private int shout;
	private int position;

	public SkillMacro(int skill1, int skill2, int skill3, String name, int shout, int position) {
		this.skill1 = skill1;
		this.skill2 = skill2;
		this.skill3 = skill3;
		this.name = name;
		this.shout = shout;
		this.position = position;
	}

	public int getSkill1() {
		return skill1;
	}

	public int getSkill2() {
		return skill2;
	}

	public int getSkill3() {
		return skill3;
	}

	public String getName() {
		return name;
	}

	public int getShout() {
		return shout;
	}

	public int getPosition() {
		return position;
	}
}
