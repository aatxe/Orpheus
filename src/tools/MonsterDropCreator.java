/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc>
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package tools;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.FileOutputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NotBoundException;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;

import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;

/*
 * Originally created by Bassoe <3
 * Modified by LightPepsi <3
 * getAllItems() function, credits to Snow
 *
 */
public class MonsterDropCreator {

    private static final int COMMON_ETC_RATE = 600000; // 60% Rate
    private static final int SUPER_BOSS_ITEM_RATE = 300000; // 30% Rate
    private static final int POTION_RATE = 20000;
    private static final int ARROWS_RATE = 25000;
    private static int lastmonstercardid = 2388070;
    private static boolean addFlagData = false; // There isn't any flag on my source .. so.
    protected static String monsterQueryData = "drop_data"; // Modify this to suite your source
    protected static List<Pair<Integer, String>> itemNameCache = new ArrayList<Pair<Integer, String>>();
    protected static List<Pair<Integer, MobInfo>> mobCache = new ArrayList<Pair<Integer, MobInfo>>();
    protected static Map<Integer, Boolean> bossCache = new HashMap<Integer, Boolean>();

    public static void main(String args[]) throws FileNotFoundException, IOException, NotBoundException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException, MalformedObjectNameException {
	MapleData data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")).getData("MonsterBook.img");

	System.out.println("Warning : Use this only at your own risk!");
	System.out.println("Press any key to continue...");
	System.console().readLine();
	System.out.println("As you wish.\n\n\n\n");

	long currtime = System.currentTimeMillis();
	addFlagData = Boolean.parseBoolean(args[0]);

	System.out.println("Loading : Item name string.");
	getAllItems();
	System.out.println("Loading : Mob data.");
	getAllMobs();

	boolean first;
	StringBuilder sb = new StringBuilder();
	FileOutputStream out = new FileOutputStream("mobDrop.sql", true);

	// Things not in MonsterBook.img
	for (Map.Entry e : getDropsNotInMonsterBook().entrySet()) {
	    first = true;

	    sb.append("INSERT INTO ").append(monsterQueryData).append(" VALUES ");
	    for (Integer monsterdrop : (List<Integer>) e.getValue()) {
		final int itemid = monsterdrop;
		final int monsterId = (Integer) e.getKey();
		int rate = getChance(itemid, monsterId, bossCache.containsKey(monsterId));

		if (rate <= 100000) {
		    switch (monsterId) {
			case 9400121: // Anego
			    rate *= 5;
			    break;
			case 9400112:
			case 9400113:
			case 9400300:
			    rate *= 10;
			    break;
		    }
		}
		for (int i = 0; i < multipleDropsIncrement(itemid, monsterId); i++) {
		    if (first) {
			sb.append("(DEFAULT, ");
			first = false;
		    } else {
			sb.append(", (DEFAULT, ");
		    }
		    sb.append(monsterId).append(", ");
		    if (addFlagData) {
			sb.append("'', "); // Flags
		    }
		    sb.append(itemid).append(", ");
		    sb.append("1, 1,"); // Item min and max
		    sb.append("0, "); // Quest
		    final int num = IncrementRate(itemid, i);
		    sb.append(num == -1 ? rate : num);
		    sb.append(")");
		    first = false;
		}
		sb.append("\n");
		sb.append("-- Name : ");
		retriveNLogItemName(sb, itemid);
		sb.append("\n");
	    }
	    sb.append(";"); // Fix for mass executing SQL query
	    sb.append("\n");

	    out.write(sb.toString().getBytes());
	    sb.delete(0, 2147483647); // ;P wild guess LOL
	}

	// MonsterBook
	System.out.println("Loading : Drops from String.wz/MonsterBook.img.");
	for (MapleData dataz : data.getChildren()) {
	    int monsterId = Integer.parseInt(dataz.getName());
	    int idtoLog = monsterId;
	    first = true;

	    if (monsterId == 9400408) { // Fix for kaede castle, toad boss
		idtoLog = 9400409;
	    }
	    if (dataz.getChildByPath("reward").getChildren().size() > 0) { // Fix for Monster without any reward causing SQL error
		sb.append("INSERT INTO ").append(monsterQueryData).append(" VALUES ");
		for (MapleData drop : dataz.getChildByPath("reward")) {
		    int itemid = MapleDataTool.getInt(drop);
		    int rate = getChance(itemid, idtoLog, bossCache.containsKey(idtoLog));

		    for (Pair<Integer, MobInfo> Pair : mobCache) {
			if (Pair.getLeft() == monsterId) {
			    if (Pair.getRight().getBoss() > 0) { // Is boss or not.
				if (rate <= 100000) {
				    if (Pair.getRight().rateItemDropLevel() == 2) {
					rate *= 10;
				    } else if (Pair.getRight().rateItemDropLevel() == 3) {
					switch (monsterId) {
					    case 8810018: // HT
						rate *= 48;
					    case 8800002: // Zakum
						rate *= 45;
						break;
					    default:
						rate *= 30;
						break;
					}
				    } else {
					switch (monsterId) {
					    case 9400265: // Vergamot
					    case 9400270: // Dunas
					    case 9400273: // Nibergen
					    case 9400294: // Dunas2
						rate *= 24;
						break;
					    case 9420522: // Krexel
						rate *= 29;
						break;
					    case 9400409: // Emperor
						rate *= 35;
						break;
					    case 9400287: // Imperial guard
						rate *= 60;
						break;
					    default:
						rate *= 10;
						break;
					}
				    }
				}
			    }
			    break;
			}
		    }
		    for (int i = 0; i < multipleDropsIncrement(itemid, idtoLog); i++) {
			if (first) {
			    sb.append("(DEFAULT, ");
			    first = false;
			} else {
			    sb.append(", (DEFAULT, ");
			}
			sb.append(idtoLog).append(", ");
			if (addFlagData) {
			    sb.append("'', "); // Flags
			}
			sb.append(itemid).append(", ");
			sb.append("1, 1,"); // Item min and max
			sb.append("0, "); // Quest
			final int num = IncrementRate(itemid, i);
			sb.append(num == -1 ? rate : num);
			sb.append(")");
			first = false;
		    }
		    sb.append("\n");
		    sb.append("-- Name : ");
		    retriveNLogItemName(sb, itemid);
		    sb.append("\n");
		}
		sb.append(";"); // Fix for mass executing SQL query
	    }
	    sb.append("\n");

	    out.write(sb.toString().getBytes());
	    sb.delete(0, 2147483647); // ;P wild guess LOL
	}

	System.out.println("Loading : MonsterBook drops.");
	StringBuilder SQL = new StringBuilder();
	StringBuilder bookName = new StringBuilder();
	for (Pair<Integer, String> Pair : itemNameCache) {
	    if (Pair.getLeft() >= 2380000 && Pair.getLeft() <= lastmonstercardid) {
		bookName.append(Pair.getRight());

                if (bookName.toString().contains(" Card"))
                    bookName.delete(bookName.length() - 5, bookName.length()); // Get rid of the " Card" string

		for (Pair<Integer, MobInfo> Pair_ : mobCache) {
		    if (Pair_.getRight().getName().equalsIgnoreCase(bookName.toString())) {
			int rate = 1000;
			if (Pair_.getRight().getBoss() > 0) {
			    rate *= 25;
			}
			SQL.append("INSERT INTO ").append(monsterQueryData).append(" VALUES ");
			SQL.append("(DEFAULT, ");
			SQL.append(Pair_.getLeft()).append(", "); // Dropperid
			if (addFlagData) {
			    sb.append("'', "); // Flags
			}
			SQL.append(Pair.getLeft()).append(", "); // Itemid
			SQL.append("1, 1,"); // Item min and max
			SQL.append("0, "); // Quest
			SQL.append(rate);
			SQL.append(");\n");
                        SQL.append("-- Name : ").append(Pair.getRight()).append("\n");
			break;
		    }
		}
                bookName.delete(0, 2147483647); // ;P wild guess LOL                
	    }
	}
        System.out.println("Loading : Monster Card Data.");
        SQL.append("\n");
        int i = 1;
        int lastmonsterbookid = 0;
        for (Pair<Integer, String> Pair : itemNameCache) {
            if (Pair.getLeft() >= 2380000 && Pair.getLeft() <= lastmonstercardid) {
            bookName.append(Pair.getRight());

            if (bookName.toString().contains(" Card"))
                bookName.delete(bookName.length() - 5, bookName.length()); // Get rid of the " Card" string

                if (Pair.getLeft() == lastmonsterbookid) continue; //Fix for multiple monster shet
                for (Pair<Integer, MobInfo> Pair_ : mobCache) {
                    if (Pair_.getRight().getName().equalsIgnoreCase(bookName.toString())) {
                        SQL.append("INSERT INTO ").append("monstercarddata").append(" VALUES (");
                        SQL.append(i).append(", ");
                        SQL.append(Pair.getLeft());
                        SQL.append(", ");
                        SQL.append(Pair_.getLeft()).append(");\n");
                        lastmonsterbookid = Pair.getLeft();
                        i++;
                        break;
                    }
                }
            bookName.delete(0, 2147483647); // ;P wild guess LOL
            }
        }
        out.write(SQL.toString().getBytes());
        out.close();
	long time = System.currentTimeMillis() - currtime;
	time /= 1000;

	System.out.println("Time taken : " + time);
    }

    private static void retriveNLogItemName(final StringBuilder sb, final int id) {
	for (Pair<Integer, String> Pair : itemNameCache) {
	    if (Pair.getLeft() == id) {
		sb.append(Pair.getRight());
		return;
	    }
	}
	sb.append("MISSING STRING, ID : ");
	sb.append(id);
    }

    private static int IncrementRate(int itemid, int times) {
	if (times == 0) {
	    if (itemid == 1002357 || itemid == 1002926 || itemid == 1002927) { // Zakum Helmet, Targa, Scar
		return 999999;
	    } else if (itemid == 1122000) { // HT necklace
		return 999999;
	    } else if (itemid == 1002972) { // Auf Haven
		return 999999;
	    }
	} else if (times == 1) {
	    if (itemid == 1002357 || itemid == 1002926 || itemid == 1002927) { // Zakum Helmet, Targa, Scar
		return 999999;
	    } else if (itemid == 1122000) { // HT necklace
		return 999999;
	    } else if (itemid == 1002972) { // Auf Haven
		return SUPER_BOSS_ITEM_RATE;
	    }
	} else if (times == 2) {
	    if (itemid == 1002357 || itemid == 1002926 || itemid == 1002927) { // Zakum Helmet, Targa, Scar
		return SUPER_BOSS_ITEM_RATE;
	    } else if (itemid == 1122000) { // HT necklace
		return SUPER_BOSS_ITEM_RATE;
	    }
	} else if (times == 3) {
	    if (itemid == 1002357 || itemid == 1002926 || itemid == 1002927) { // Zakum Helmet, Targa, Scar
		return SUPER_BOSS_ITEM_RATE;
	    }
	} else if (times == 4) {
	    if (itemid == 1002357 || itemid == 1002926 || itemid == 1002927) { // Zakum Helmet, Targa, Scar
		return SUPER_BOSS_ITEM_RATE;
	    }
	}
	return -1;
    }

    private static int multipleDropsIncrement(int itemid, int mobid) {
	switch (itemid) {
	    case 1002926: // Targa hat [Malaysia boss]
//	    case 1002906: // Scarlion hat [Malaysia boss]
	    case 1002927: // Scarlion boss hat [Malaysia boss]
//	    case 1002905: // Targa hat [Malaysia boss]
	    case 1002357: // Zakum helmet
	    case 1002390: // Zakum helmet 2
	    case 1002430: // Zakum helmet 3
		return 5;
	    case 1122000: // HT Necklace:
		return 4;
	    case 4021010: // Time rock
		return 7;
	    case 1002972: // Auf Haven Circlet
		return 2;
	    case 4000172: // Three-Tailed Foxtail
		if (mobid == 7220001) {
		    return 8;
		}
		return 1;
	    case 4000000: // Blue snail shell
	    case 4000016: // Red Snail Shell
	    case 4000019: // Snail Shell
	    case 4000003: // Tree Branch
	    case 4000005: // Leaf
	    case 4000018: // Firewood
	    case 4000195: // Seedling
	    case 4000043: // Lorang's claw
	    case 4000044: // Clang's Claw
	    case 4000284: // Yellow belt
	    case 4000285: // Red Belt
	    case 4000021: // Leather
	    case 4000283: // Bear's foot
	    case 4000073: // Soild horn
	    case 4000074: // Lucida tail
	    case 4000298: // Old paper
	    case 4000289: // Cat doll
	    case 4000166: // Shrimp Meat
	    case 4000167: // Hard Needle
	    case 4000364: // Cable Bundle
	    case 4000365: // Socket
	    case 4000356: // Flask
	    case 4000329: // Catus Stem
	    case 4000331: // Catus Flower
	    case 4000330: // Catus Thorn
	    case 4000268: // Wyvren Wing
	    case 4000269: // Wyvern Gill
	    case 4000270: // Wyvern Toenail
	    case 4000117: // Spacefood
	    case 4000118: // Small Spaceship
	    case 4000119: // Recieving app
	    case 4000113: // Clock Spring
	    case 4000114: // Table Clock
	    case 4000115: // Cog
	    case 4000029: // Lupin's banana
	    case 4000031: // Cursed Doll
	    case 4000026: // Lupin's Doll
	    case 4000032: // Ligator Skin
	    case 4000033: // Croco Skin
		// Master Monster
		if (mobid == 2220000 || mobid == 3220000 || mobid == 3220001 || mobid == 4220000 || mobid == 5220000 || mobid == 5220002 || mobid == 5220003 || mobid == 6220000 || mobid == 4000119 || mobid == 7220000 || mobid == 7220002 || mobid == 8220000 || mobid == 8220002 || mobid == 8220003) {
		    return 3;
		}
		return 1;
	}
	return 1;
    }

    // 100000 = 10%
    // 10000 = 1%
    // 1000 = 0.1%
    // 100 = 0.01%
    // 10 = 0.001%
    // 1 = 0.0001%
    private static int getChance(int id, int mobid, boolean boss) {
	switch (id / 10000) {
	    case 100: // Hat
		switch (id) {
		    case 1002926: // Targa hat [Malaysia boss]
		    case 1002906: // Scarlion hat [Malaysia boss]
		    case 1002927: // Scarlion boss hat [Malaysia boss]
		    case 1002905: // Targa hat [Malaysia boss]
		    case 1002357: // Zakum helmet
		    case 1002390: // Zakum helmet 2
		    case 1002430: // Zakum helmet 3
		    case 1002972: // Auf haven
			return SUPER_BOSS_ITEM_RATE;
		}
		return 1500;
	    case 103: // Earring
		switch (id) {
		    case 1032062: // Element Pierce - Neo Tokyo
			return 100; // 7 Slots earring with +2 to str/dex/int/luk/ma
		}
		return 1000;
	    case 105: // Overall
	    case 109: // Shield
		switch (id) {
		    case 1092049: // Dragon Khanjar
			return 100;
		}
		return 700;
	    case 104: // Topwear
	    case 106: // Pants
	    case 107: // Shoes
		switch (id) {
		    case 1072369: // Squachy shoe [King slime, Kerning PQ]
			return 300000; // 30%
		}
		return 800;
	    case 108: // Gloves
	    case 110: // Cape
		return 1000;
	    case 112: // Pendant
		switch (id) {
		    case 1122000: // HT Necklace
			return SUPER_BOSS_ITEM_RATE;
		    case 1122011: // Timeless pendant lvl 30
		    case 1122012: // Timeless pendant lvl 140
			return 800000; // 80%
		}
	    case 130: // 1 Handed sword
	    case 131: // 1 Handed Axe
	    case 132: // 1 Handed BW
	    case 137: // Wand
		switch (id) {
		    case 1372049: // Zakum Tree Branch
			return 999999;
		}
		return 700;
	    case 138: // Staff
	    case 140: // 1 Handed sword and 2 Handed sword
	    case 141: // 2 Handed axe
	    case 142: // 2 Handed BW
	    case 144: // Pole arm
		return 700;
	    case 133: // Dangger
	    case 143: // Spear
	    case 145: // Bow
	    case 146: // Crossbow
	    case 147: // Claw
	    case 148: // Knuckle
	    case 149: // Gun
		return 500;
	    case 204: // Scrolls
		switch (id) {
		    case 2049000: // Chaos scroll
			return 150;
		}
		return 300;
	    case 205: // All cure potion, Antidote, eyedrop
		return 50000; // 5%
	    case 206: // Arrows
		return 30000;
	    case 228: // Skillbook
		return 30000;
	    case 229: // Mastery book
		switch (id) {
		    case 2290096: // Maple Hero 20
			return 800000; // 80% rate for HT
		    case 2290125: // Maple Hero 30
			return 100000;
		}
		return 500;
	    case 233: // Bullets and capsules
		switch (id) {
		    case 2330007: // Armor-Piercing bullet
			return 50;
		}
		return 500;
	    case 400:
		switch (id) {
		    case 4000021: // Leather
			return 50000;
		    case 4001094: // Nine spirit egg
			return 999999;
		    case 4001000: // Awren's glass shoe
			return 5000;
		    case 4000157: // Seal meat
			return 100000; // 10%
		    case 4001024: // Rubian [Guild PQ]
		    case 4001023: // Key of dimension [Ludi PQ]
			return 999999; // 100%
		    case 4000245: // Dragon Scale
		    case 4000244: // Dragon Spirit
			return 2000;
		    case 4001005: // Ancient scroll
			return 5000;
		    case 4001006: // Flaming feather
			return 10000; // 1%
		    case 4000017: // Pig's head
		    case 4000082: // Gold smelly tooth =.="
			return 40000; // 4%
		    case 4000446: // Smiling cone hat
		    case 4000451: // Expressionless cone hat
		    case 4000456: // Sad cone hat
			return 10000; // 1%
		    case 4000459: // Black armour piece
			return 20000; // 2%
		    case 4000030: // Dragon Skin
			return 60000; // 6%
		    case 4000339: // High-Tier Ninja Giant Star
			return 70000; // 7%
		    case 4007000: // Magic Powder (Brown)
		    case 4007001: // Magic Powder (White)
		    case 4007002: // Magic Powder (Blue)
		    case 4007003: // Magic Powder (Green)
		    case 4007004: // Magic Powder (Yellow)
		    case 4007005: // Magic Powder (Purple)
		    case 4007006: // Magic Powder (Red)
		    case 4007007: // Magic Powder (Black)
			return 50000; // 5%
		}
		switch (id / 1000) {
		    case 4000: // ETC
		    case 4001: // Story book, manon cry, orbis rock, eraser, certificate
			return COMMON_ETC_RATE;
		    case 4003: // Screw, Processed wood, Piece of Ice, Fairy Wing, Stiff Feather, Soft Feather
			return 200000;
		    case 4004: // Crystal Ore
		    case 4006: // Magic rock, summoning rock
			return 10000;
		    case 4005: // Crystal, refined
			return 1000;
		}
	    case 401: // mineral Ore and refined
	    case 402: // Jewel ore and refined
		switch (id) {
		    case 4020009: // Piece of time
			return 5000; // 0.5%
		    case 4021010: // Time rock
			return SUPER_BOSS_ITEM_RATE;
		}
		return 9000;
	    case 403: // Lip lock key, cracked dimension, omok, monster card
		switch (id) {
		    case 4032024: // Jumper Cable [Wolf Spider]
			return 50000; // 5%
		    case 4032181: // Silver Coin - Neo Tokyo
			return boss ? 999999 : 300000; // 30%
		    case 4032025: // T-1 Socket Adapter [Wolf Spider]
		    case 4032156: // Overload Lens - Neo Tokyo
		    case 4032155: // Afterroad Caterpillar - Neo Tokyo
		    case 4032161: // Eruwater Lazer gun - Neo Tokyo
		    case 4032163: // Maverick booster - Neo Tokyo
		    case 4032159: // Protoroad Spoiler - Neo Tokyo
			return COMMON_ETC_RATE;
		    case 4032166: // Nano Plant(Y)
		    case 4032167: // Nano Plant(Sigma)
		    case 4032168: // Nano Plant(Omega)
			return 10000; // 3%
		    case 4032158: // Twisted Radar - Neo Tokyo
		    case 4032151: // Operation unit - Neo Tokyo
		    case 4032180: // Eruwater Transmitter - Neo Tokyo
		    case 4032164: // Portalble lazer Guidance - Neo Tokyo
			return 2000; // 0.1%
		    case 4032152: // Macro Molecule Autualater - Neo Tokyo
		    case 4032153: // Conductive Polymer Gain - Neo Tokyo
		    case 4032154: // Calculating Domino - Neo Tokyo
			return 4000;
		}
		return 300;
	    case 413: // Production stimulator
		return 6000; // 0.6%
	    case 416: // attendance book, pet guide, production manual
		return 6000; // 0.6%
	}
	switch (id / 1000000) {
	    case 1: // Equipmenet, for others that's not stated.
		return 999999;
	    case 2:
		switch (id) {
		    case 2000004: // Elixir
		    case 2000005:
			return boss ? 999999 : POTION_RATE; // 5%
		    case 2000006: // Power Elixir
			// Gallopera has a higher rate x_X
			return boss ? 999999 : mobid == 9420540 ? 50000 : POTION_RATE; // 3%
		    case 2022345: // Power up Drink - Neo Tokyo
			return boss ? 999999 : 3000; // 0.3%
		    case 2012002: // Sap of Ancient Tree
			return 6000;
		    case 2020013:
		    case 2020015:
			return boss ? 999999 : POTION_RATE;
		    case 2060000:
		    case 2061000:
		    case 2060001:
		    case 2061001:
			return ARROWS_RATE;
		    case 2070000: // Subi Throwing-Stars
		    case 2070001: // Wolbi Throwing-Stars
		    case 2070002: // Mokbi Throwing-Stars
		    case 2070003: // Kumbi Throwing-Stars
		    case 2070004: // Tobi Throwing-Stars
		    case 2070008: // Snowball
		    case 2070009: // Wooden Top
		    case 2070010: // Icicle
			return 500;
		    case 2070005: // Steely Throwing-Knives"
			return 400;
		    case 2070006: // Ilbi Throwing-Stars
		    case 2070007: // Hwabi Throwing-Stars
			return 200;
//		    case 2070011: // Maple Throwing star
		    case 2070012: // Paper Fighter Plane
		    case 2070013: // Orange
			return 1500;
		    case 2070019: // Magic throwing star - Neo Tokyo
			return 100;
		    case 2210006: // Rainbow colored shell
			return 999999;
		    default: // Mana Elixir Pill, unagi.. etc
			return POTION_RATE;
		}
	    case 3:
		switch (id) {
		    case 3010007:
		    case 3010008:
			return 500;
		}
		return 2000;
	}
	System.out.println("Unhandled item chance, ID : " + id);
	return 999999;
    }

    private static Map<Integer, List<Integer>> getDropsNotInMonsterBook() {
	Map<Integer, List<Integer>> drops = new HashMap<Integer, List<Integer>>();

	List<Integer> IndiviualMonsterDrop = new ArrayList();

	// Bodyguard A
	IndiviualMonsterDrop.add(4000139); // Bodyguard A's Tie Pin
	IndiviualMonsterDrop.add(2002011); // Pain Reliever
	IndiviualMonsterDrop.add(2002011); // Pain Reliever
	IndiviualMonsterDrop.add(2002011); // Pain Reliever
	IndiviualMonsterDrop.add(2000004); // Elixir
	IndiviualMonsterDrop.add(2000004); // Elixir

	drops.put(9400112, IndiviualMonsterDrop);

	IndiviualMonsterDrop = new ArrayList();

	// Bodyguard B
	IndiviualMonsterDrop.add(4000140); // Bodyguard B's Bullet Shell
	IndiviualMonsterDrop.add(2022027); // Yakisoba (x2)
	IndiviualMonsterDrop.add(2022027); // Yakisoba (x2)
	IndiviualMonsterDrop.add(2000004); // Elixir
	IndiviualMonsterDrop.add(2000004); // Elixir
	IndiviualMonsterDrop.add(2002008); // Sniper Pill
	IndiviualMonsterDrop.add(2002008); // Sniper Pill

	drops.put(9400113, IndiviualMonsterDrop);

	IndiviualMonsterDrop = new ArrayList();

	// The boss
	IndiviualMonsterDrop.add(4000141); // Big Boss's flashlight
	IndiviualMonsterDrop.add(2000004); // Elixir
	IndiviualMonsterDrop.add(2040813); // Cursed Scroll for Gloves for HP 30%
	IndiviualMonsterDrop.add(2041030); // Cursed Scroll for Cape for HP 70%
	IndiviualMonsterDrop.add(2041040); // Cursed Scroll for Cape for LUK 70%
	IndiviualMonsterDrop.add(1072238); // Violet Snowshoes
	IndiviualMonsterDrop.add(1032026); // Gold Emerald Earrings
	IndiviualMonsterDrop.add(1372011); // Zhu-Ge-Liang Wand

	drops.put(9400300, IndiviualMonsterDrop);

	IndiviualMonsterDrop = new ArrayList();

	// Dreamy ghost, himes
	IndiviualMonsterDrop.add(4000225); // Kimono Piece
	IndiviualMonsterDrop.add(2000006); // Mana Elixir
	IndiviualMonsterDrop.add(2000004); // Elixir
	IndiviualMonsterDrop.add(2070013); // Orange
	IndiviualMonsterDrop.add(2002005); // Sniper Potion
	IndiviualMonsterDrop.add(2022018); // Kinoko Ramen (Roasted Pork)
	IndiviualMonsterDrop.add(2040306); // Cursed Scroll for Earring for DEX 70%
	IndiviualMonsterDrop.add(2043704); // Cursed Scroll for Wand for Magic Att 70%
	IndiviualMonsterDrop.add(2044605); // Cursed Scroll for Crossbow for ATT 30%
	IndiviualMonsterDrop.add(2041034); // Cursed Scroll for Cape for STR 70%
	IndiviualMonsterDrop.add(1032019); // Crystal Flower Earrings
	IndiviualMonsterDrop.add(1102013); // White Justice Cape
	IndiviualMonsterDrop.add(1322026); // Colorful Tube
	IndiviualMonsterDrop.add(1092015); // Steel Ancient Shield
	IndiviualMonsterDrop.add(1382016); // Pyogo Mushroom
	IndiviualMonsterDrop.add(1002276); // Red Falcon
	IndiviualMonsterDrop.add(1002403); // Blue Arlic Helmet
	IndiviualMonsterDrop.add(1472027); // Green Scarab

	drops.put(9400013, IndiviualMonsterDrop);

	IndiviualMonsterDrop = new ArrayList();

	// Zakum
	IndiviualMonsterDrop.add(1372049); // Zakum Tree Branch

	drops.put(8800002, IndiviualMonsterDrop);

	IndiviualMonsterDrop = new ArrayList();

	// Horntail
	IndiviualMonsterDrop.add(4001094); // Nine Spirit Egg
	IndiviualMonsterDrop.add(2290125); // Maple Warrior 30

	drops.put(8810018, IndiviualMonsterDrop);

	IndiviualMonsterDrop = new ArrayList();

	// Lady Boss
	IndiviualMonsterDrop.add(4000138); // Lady Boss Comb
	IndiviualMonsterDrop.add(4010006); // Gold Ore
	IndiviualMonsterDrop.add(2000006); // Mana Elixir
	IndiviualMonsterDrop.add(2000011); // Mana Elixir Pill
	IndiviualMonsterDrop.add(2020016); // Cheesecake
	IndiviualMonsterDrop.add(2022024); // Takoyaki (Octopus Ball)
	IndiviualMonsterDrop.add(2022026); // Yakisoba
	IndiviualMonsterDrop.add(2043705); // Cursed Scroll for Wand for Magic Att 30%
	IndiviualMonsterDrop.add(2040716); // Cursed Scroll for Shoes for Speed 30%
	IndiviualMonsterDrop.add(2040908); // Cursed Scroll for Shield for HP 70%
	IndiviualMonsterDrop.add(2040510); // Cursed Scroll for Overall Armor for DEF 70%
	IndiviualMonsterDrop.add(1072239); // Yellow Snowshoes
	IndiviualMonsterDrop.add(1422013); // Leomite
	IndiviualMonsterDrop.add(1402016); // Devil's Sunrise
	IndiviualMonsterDrop.add(1442020); // Hellslayer
	IndiviualMonsterDrop.add(1432011); // Fairfrozen
	IndiviualMonsterDrop.add(1332022); // Angelic Betrayal
	IndiviualMonsterDrop.add(1312015); // Bipennis
	IndiviualMonsterDrop.add(1382010); // Dark Ritual
	IndiviualMonsterDrop.add(1372009); // Magicodar
	IndiviualMonsterDrop.add(1082085); // Red Willow
	IndiviualMonsterDrop.add(1332022); // Angelic Betrayal
	IndiviualMonsterDrop.add(1472033); // Casters

	drops.put(9400121, IndiviualMonsterDrop);

	IndiviualMonsterDrop = new ArrayList();

	// Wolf Spider
	IndiviualMonsterDrop.add(4032024); // Jumper Cable
	IndiviualMonsterDrop.add(4032025); // T-1 Socket Adapter
	IndiviualMonsterDrop.add(4020006); // Topaz Ore
	IndiviualMonsterDrop.add(4020008); // Black Crystal Ore
	IndiviualMonsterDrop.add(4010001); // Steel Ore
	IndiviualMonsterDrop.add(4004001); // Wisdom Crystal Ore
	IndiviualMonsterDrop.add(2070006); // Ilbi Throwing Star
	IndiviualMonsterDrop.add(2044404); // Cursed Scroll for Pole Arm for ATT 70%
	IndiviualMonsterDrop.add(2044702); // Scroll for Claw for ATT 10%
	IndiviualMonsterDrop.add(2044305); // Cursed Scroll for Spear for ATT 30^
	IndiviualMonsterDrop.add(1102029); // White Seraph Cape
	IndiviualMonsterDrop.add(1032023); // Strawberry Earrings
	IndiviualMonsterDrop.add(1402004); // Blue Screamer
	IndiviualMonsterDrop.add(1072210); // Red Rivers Boots
	IndiviualMonsterDrop.add(1040104); // Orihalcon Platine
	IndiviualMonsterDrop.add(1060092); // Orihalcon Platine Pants
	IndiviualMonsterDrop.add(1082129); // Purple Imperial
	IndiviualMonsterDrop.add(1442008); // The Gold Dragon
	IndiviualMonsterDrop.add(1072178); // Purple Enigma Shoes
	IndiviualMonsterDrop.add(1050092); // Green Oriental Fury Coat
	IndiviualMonsterDrop.add(1002271); // Green Galaxy
	IndiviualMonsterDrop.add(1051053); // Red Requierre
	IndiviualMonsterDrop.add(1382008); // Kage
	IndiviualMonsterDrop.add(1002275); // Blue Falcon
	IndiviualMonsterDrop.add(1051082); // Red Anes
	IndiviualMonsterDrop.add(1050064); // Dark Linnex
	IndiviualMonsterDrop.add(1472028); // Blue Scarab
	IndiviualMonsterDrop.add(1072193); // Brown Osfa Boots
	IndiviualMonsterDrop.add(1072172); // Green Pirate Boots
	IndiviualMonsterDrop.add(1002285); // Blood Nightfox

	drops.put(9400545, IndiviualMonsterDrop);

	IndiviualMonsterDrop = new ArrayList();

	return drops;
    }

    private static void getAllItems() {
	MapleDataProvider data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz"));

	List<Pair<Integer, String>> itemPairs = new ArrayList<Pair<Integer, String>>();
	MapleData itemsData;

	itemsData = data.getData("Cash.img");
	for (MapleData itemFolder : itemsData.getChildren()) {
	    int itemId = Integer.parseInt(itemFolder.getName());
	    String itemName = MapleDataTool.getString("name", itemFolder, "NO-NAME");
	    itemPairs.add(new Pair<Integer, String>(itemId, itemName));
	}

	itemsData = data.getData("Consume.img");
	for (MapleData itemFolder : itemsData.getChildren()) {
	    int itemId = Integer.parseInt(itemFolder.getName());
	    String itemName = MapleDataTool.getString("name", itemFolder, "NO-NAME");
	    itemPairs.add(new Pair<Integer, String>(itemId, itemName));
	}

	itemsData = data.getData("Eqp.img").getChildByPath("Eqp");
	for (MapleData eqpType : itemsData.getChildren()) {
	    for (MapleData itemFolder : eqpType.getChildren()) {
		int itemId = Integer.parseInt(itemFolder.getName());
		String itemName = MapleDataTool.getString("name", itemFolder, "NO-NAME");
		itemPairs.add(new Pair<Integer, String>(itemId, itemName));
	    }
	}

	itemsData = data.getData("Etc.img").getChildByPath("Etc");
	for (MapleData itemFolder : itemsData.getChildren()) {
	    int itemId = Integer.parseInt(itemFolder.getName());
	    String itemName = MapleDataTool.getString("name", itemFolder, "NO-NAME");
	    itemPairs.add(new Pair<Integer, String>(itemId, itemName));
	}

	itemsData = data.getData("Ins.img");
	for (MapleData itemFolder : itemsData.getChildren()) {
	    int itemId = Integer.parseInt(itemFolder.getName());
	    String itemName = MapleDataTool.getString("name", itemFolder, "NO-NAME");
	    itemPairs.add(new Pair<Integer, String>(itemId, itemName));
	}

	itemsData = data.getData("Pet.img");
	for (MapleData itemFolder : itemsData.getChildren()) {
	    int itemId = Integer.parseInt(itemFolder.getName());
	    String itemName = MapleDataTool.getString("name", itemFolder, "NO-NAME");
	    itemPairs.add(new Pair<Integer, String>(itemId, itemName));
	}
	itemNameCache.addAll(itemPairs);
    }

    public static void getAllMobs() {
	List<Pair<Integer, MobInfo>> itemPairs = new ArrayList<Pair<Integer, MobInfo>>();
	MapleDataProvider data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz"));
	MapleDataProvider mobData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Mob.wz"));
	MapleData mob = data.getData("Mob.img");

	int id;
	MapleData monsterData;

	for (MapleData itemFolder : mob.getChildren()) { // Get the list of mobs from String.wz
	    id = Integer.parseInt(itemFolder.getName());

	    try {
		monsterData = mobData.getData(StringUtil.getLeftPaddedStr(Integer.toString(id) + ".img", '0', 11));
		final int boss = id == 8810018 ? 1 : MapleDataTool.getIntConvert("boss", monsterData.getChildByPath("info"), 0);

		if (boss > 0) {
		    bossCache.put(id, true);
		}

		MobInfo mobInfo = new MobInfo(
			boss, // fix for HT
			MapleDataTool.getIntConvert("rareItemDropLevel", monsterData.getChildByPath("info"), 0), 
			MapleDataTool.getString("name", itemFolder, "NO-NAME"));

		itemPairs.add(new Pair<Integer, MobInfo>(id, mobInfo));
	    } catch (Exception fe) {
	    }
	}
	mobCache.addAll(itemPairs);
    }

    public static class MobInfo {

	public int boss;
	public int rareItemDropLevel;
	public String name;

	public MobInfo(int boss, int rareItemDropLevel, String name) {
	    this.boss = boss;
	    this.rareItemDropLevel = rareItemDropLevel;
	    this.name = name;
	}

	public int getBoss() {
	    return boss;
	}

	public int rateItemDropLevel() {
	    return rareItemDropLevel;
	}

	public String getName() {
	    return name;
	}
    }
}