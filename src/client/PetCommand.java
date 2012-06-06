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

/**
 * @author Leifde
 */
public class PetCommand {
	private int petId, skillId, prob, inc;

	public PetCommand(int petId, int skillId, int prob, int inc) {
		this.petId = petId;
		this.skillId = skillId;
		this.prob = prob;
		this.inc = inc;
	}

	public int getPetId() {
		return petId;
	}

	public int getSkillId() {
		return skillId;
	}

	public int getProbability() {
		return prob;
	}

	public int getIncrease() {
		return inc;
	}
}
