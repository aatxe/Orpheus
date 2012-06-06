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
package server.life;

/**
 * 
 * @author Danny (Leifde)
 */
public class MobAttackInfo {
	private boolean isDeadlyAttack;
	private int mpBurn;
	private int diseaseSkill;
	private int diseaseLevel;
	private int mpCon;

	public MobAttackInfo(int mobId, int attackId) {
	}

	public void setDeadlyAttack(boolean isDeadlyAttack) {
		this.isDeadlyAttack = isDeadlyAttack;
	}

	public boolean isDeadlyAttack() {
		return isDeadlyAttack;
	}

	public void setMpBurn(int mpBurn) {
		this.mpBurn = mpBurn;
	}

	public int getMpBurn() {
		return mpBurn;
	}

	public void setDiseaseSkill(int diseaseSkill) {
		this.diseaseSkill = diseaseSkill;
	}

	public int getDiseaseSkill() {
		return diseaseSkill;
	}

	public void setDiseaseLevel(int diseaseLevel) {
		this.diseaseLevel = diseaseLevel;
	}

	public int getDiseaseLevel() {
		return diseaseLevel;
	}

	public void setMpCon(int mpCon) {
		this.mpCon = mpCon;
	}

	public int getMpCon() {
		return mpCon;
	}
}
