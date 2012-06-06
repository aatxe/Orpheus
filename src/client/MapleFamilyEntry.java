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

public class MapleFamilyEntry {
	private int familyId;
	private int rank, reputation, totalReputation, todaysRep, totalJuniors,
			juniors, chrid;
	private String familyName;

	public int getId() {
		return familyId;
	}

	public void setFamilyId(int familyId) {
		this.familyId = familyId;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getChrId() {
		return chrid;
	}

	public void setChrId(int chrid) {
		this.chrid = chrid;
	}

	public int getReputation() {
		return reputation;
	}

	public int getTodaysRep() {
		return todaysRep;
	}

	public void setReputation(int reputation) {
		this.reputation = reputation;
	}

	public void setTodaysRep(int today) {
		this.todaysRep = today;
	}

	public void gainReputation(int gain) {
		this.reputation += gain;
		this.totalReputation += gain;
	}

	public int getTotalJuniors() {
		return totalJuniors;
	}

	public void setTotalJuniors(int totalJuniors) {
		this.totalJuniors = totalJuniors;
	}

	public int getJuniors() {
		return juniors;
	}

	public void setJuniors(int juniors) {
		this.juniors = juniors;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public String getFamilyName() {
		return familyName;
	}

	public int getTotalReputation() {
		return totalReputation;
	}

	public void setTotalReputation(int totalReputation) {
		this.totalReputation = totalReputation;
	}
}
