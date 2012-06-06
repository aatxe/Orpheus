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

public enum MapleJob {
	BEGINNER(0),

	WARRIOR(100), FIGHTER(110), CRUSADER(111), HERO(112), PAGE(120), WHITEKNIGHT(121), PALADIN(122), SPEARMAN(130), DRAGONKNIGHT(131), DARKKNIGHT(132),

	MAGICIAN(200), FP_WIZARD(210), FP_MAGE(211), FP_ARCHMAGE(212), IL_WIZARD(220), IL_MAGE(221), IL_ARCHMAGE(222), CLERIC(230), PRIEST(231), BISHOP(232),

	BOWMAN(300), HUNTER(310), RANGER(311), BOWMASTER(312), CROSSBOWMAN(320), SNIPER(321), MARKSMAN(322),

	THIEF(400), ASSASSIN(410), HERMIT(411), NIGHTLORD(412), BANDIT(420), CHIEFBANDIT(421), SHADOWER(422),

	PIRATE(500), BRAWLER(510), MARAUDER(511), BUCCANEER(512), GUNSLINGER(520), OUTLAW(521), CORSAIR(522),

	MAPLELEAF_BRIGADIER(800), GM(900), SUPERGM(910),

	NOBLESSE(1000), DAWNWARRIOR1(1100), DAWNWARRIOR2(1110), DAWNWARRIOR3(1111), DAWNWARRIOR4(1112), BLAZEWIZARD1(1200), BLAZEWIZARD2(1210), BLAZEWIZARD3(1211), BLAZEWIZARD4(1212), WINDARCHER1(1300), WINDARCHER2(1310), WINDARCHER3(1311), WINDARCHER4(1312), NIGHTWALKER1(1400), NIGHTWALKER2(1410), NIGHTWALKER3(1411), NIGHTWALKER4(1412), THUNDERBREAKER1(1500), THUNDERBREAKER2(1510), THUNDERBREAKER3(1511), THUNDERBREAKER4(1512),

	LEGEND(2000), ARAN1(2100), ARAN2(2110), ARAN3(2111), ARAN4(2112);

	final int jobid;

	private MapleJob(int id) {
		jobid = id;
	}

	public int getId() {
		return jobid;
	}

	public static MapleJob getById(int id) {
		for (MapleJob l : MapleJob.values()) {
			if (l.getId() == id) {
				return l;
			}
		}
		return null;
	}

	public static MapleJob getBy5ByteEncoding(int encoded) {
		switch (encoded) {
			case 2:
				return WARRIOR;
			case 4:
				return MAGICIAN;
			case 8:
				return BOWMAN;
			case 16:
				return THIEF;
			case 32:
				return PIRATE;
			case 1024:
				return NOBLESSE;
			case 2048:
				return DAWNWARRIOR1;
			case 4096:
				return BLAZEWIZARD1;
			case 8192:
				return WINDARCHER1;
			case 16384:
				return NIGHTWALKER1;
			case 32768:
				return THUNDERBREAKER1;
			default:
				return BEGINNER;
		}
	}

	public boolean isA(MapleJob basejob) {
		return getId() >= basejob.getId() && getId() / 100 == basejob.getId() / 100;
	}
	
	public String toString() {
		switch (this) {
			case BEGINNER:
				return "Beginner";
			case WARRIOR:
				return "Warrior";
			case FIGHTER:
				return "Fighter";
			case CRUSADER:
				return "Crusader";
			case HERO:
				return "Hero";
			case PAGE:
				return "Page";
			case WHITEKNIGHT:
				return "White Knight";
			case PALADIN:
				return "Paladin";
			case SPEARMAN:
				return "Spearman";
			case DRAGONKNIGHT:
				return "Dragon Knight";
			case DARKKNIGHT:
				return "Dark Knight";
			case MAGICIAN:
				return "Magician";
			case FP_WIZARD:
				return "Fire/Posion Wizard";
			case FP_MAGE:
				return "Fire/Posion Mage";
			case FP_ARCHMAGE:
				return "Fire/Posion Archmage";
			case IL_WIZARD:
				return "Ice/Lightning Wizard";
			case IL_MAGE:
				return "Ice/Lightning Mage";
			case IL_ARCHMAGE:
				return "Ice/Lightning Archmage";
			case CLERIC:
				return "Cleric";
			case PRIEST:
				return "Priest";
			case BISHOP:
				return "Bishop";
			case BOWMAN:
				return "Bowman";
			case HUNTER:
				return "Hunter";
			case RANGER:
				return "Ranger";
			case BOWMASTER:
				return "Bowmaster";
			case CROSSBOWMAN:
				return "Crossbowman";
			case SNIPER:
				return "Sniper";
			case MARKSMAN:
				return "Marksman";
			case THIEF:
				return "Thief";
			case ASSASSIN:
				return "Assassin";
			case HERMIT:
				return "Hermit";
			case NIGHTLORD:
				return "Night Lord";
			case BANDIT:
				return "Bandit";
			case CHIEFBANDIT:
				return "Chief Bandit";
			case SHADOWER:
				return "Shadower";
			case PIRATE:
				return "Pirate";
			case BRAWLER:
				return "Brawler";
			case MARAUDER:
				return "Marauder";
			case BUCCANEER:
				return "Buccaneer";
			case GUNSLINGER:
				return "Gunslinger";
			case OUTLAW:
				return "Outlaw";
			case CORSAIR:
				return "Corsair";
			case MAPLELEAF_BRIGADIER:
				return "Maple Leaf Brigadier";
			case GM:
				return "GM";
			case SUPERGM:
				return "SuperGM";
			case NOBLESSE:
				return "Noblesse";
			case DAWNWARRIOR1:
				return "Dawn Warrior (Rank 1)";
			case DAWNWARRIOR2:
				return "Dawn Warrior (Rank 2)";
			case DAWNWARRIOR3:
				return "Dawn Warrior (Rank 3)";
			case DAWNWARRIOR4:
				return "Dawn Warrior (Rank 4)";
			case BLAZEWIZARD1:
				return "Blaze Wizard (Rank 1)";
			case BLAZEWIZARD2:
				return "Blaze Wizard (Rank 2)";
			case BLAZEWIZARD3:
				return "Blaze Wizard (Rank 3)";
			case BLAZEWIZARD4:
				return "Blaze Wizard (Rank 4)";
			case WINDARCHER1:
				return "Wind Archer (Rank 1)";
			case WINDARCHER2:
				return "Wind Archer (Rank 2)";
			case WINDARCHER3:
				return "Wind Archer (Rank 3)";
			case WINDARCHER4:
				return "Wind Archer (Rank 4)";
			case NIGHTWALKER1:
				return "Night Walker (Rank 1)";
			case NIGHTWALKER2:
				return "Night Walker (Rank 2)";
			case NIGHTWALKER3:
				return "Night Walker (Rank 3)";
			case NIGHTWALKER4:
				return "Night Walker (Rank 4)";
			case THUNDERBREAKER1:
				return "Thunder Breaker (Rank 1)";
			case THUNDERBREAKER2:
				return "Thunder Breaker (Rank 2)";
			case THUNDERBREAKER3:
				return "Thunder Breaker (Rank 3)";
			case THUNDERBREAKER4:
				return "Thunder Breaker (Rank 4)";
			case LEGEND:
				return "Legend";
			case ARAN1:
				return "Aran (Rank 1)";
			case ARAN2:
				return "Aran (Rank 2)";
			case ARAN3:
				return "Aran (Rank 3)";
			case ARAN4:
				return "Aran (Rank 4)";
			default:
				return "Unknown";
		}
	}
}
