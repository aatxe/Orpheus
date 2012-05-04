package client.command;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.mysql.jdbc.Connection;
import constants.ServerConstants;
import net.server.Channel;
import scripting.npc.NPCScriptManager;
import server.MapleInventoryManipulator;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import client.MapleJob;
import client.MapleRank;
import client.MapleStat;

public class PlayerCommands extends Commands {
	public static boolean execute(MapleClient c, String[] sub, char heading) {
		MapleCharacter chr = c.getPlayer();
		Channel cserv = c.getChannelServer();
		MapleCharacter victim; // For commands with targets.
		ResultSet rs; // For commands with MySQL results.

		Command command = Command.valueOf(sub[0]);
		switch (command) {
			default:
				// chr.yellowMessage("Command: " + heading + sub[0] + ": does not exist.");
				return false;
			case afk:
				if (sub.length == 1) {
					chr.setChalkboard("Away!");
				} else {
					String message = joinStringFrom(sub, 1);
					chr.setChalkboard(message);
				}
				break;
			case back:
				chr.setChalkboard("");
				chr.dropMessage("Welcome back!");
				break;
			case bugs:
				chr.dropMessage("Report bugs at https://github.com/aaronweiss74/Orpheus/issues");
				break;
			case buy:
				if (chr.getMeso() > 999999999) { // Has 999,999,999 mesos.
					chr.gainMeso(-1000000000, false); // Lose 1,000,000,000
														// mesos.
					MapleInventoryManipulator.addById(c, 4001101, (short) 1, chr.getName(), -1, -1);
					chr.message("You've lost 1,000,000,000 mesos.");
					chr.message("You now have " + chr.getItemQuantity(4001101, true) + " rice cakes.");
					chr.saveToDB(true);
				} else {
					chr.message("You cannot afford a rice cake!");
				}
				break;
			case checkgm:
				if (sub.length > 1) {
					victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
				} else {
					victim = chr;
				}
				chr.message(victim.getName() + ((victim.gmLevel() > 2) ? " is a GM." : " is not a GM."));
				break;
			case checkrebirths:
				if (sub.length > 1) {
					victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
				} else {
					victim = chr;
				}
				chr.message(victim.getName() + " has " + victim.getRebirths() + " rebirths.");
				break;
			case checkstats:
				if (sub.length > 1) {
					victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
				} else {
					victim = chr;
				}
				chr.message(victim.getName() + "'s Stats:");
				chr.message(" Level " + victim.getLevel() + " " + victim.getJob().toString());
				chr.message(" " + victim.getHp() + " / " + victim.getMaxHp() + " HP");
				chr.message(" " + victim.getMp() + " / " + victim.getMaxMp() + " MP");
				chr.message(" " + victim.getFame() + " Fame");
				chr.message(" " + victim.getRebirths() + " Rebirths");
				chr.message(" " + victim.getStr() + " Strength");
				chr.message(" " + victim.getDex() + " Dexterity");
				chr.message(" " + victim.getInt() + " Intellect");
				chr.message(" " + victim.getLuk() + " Luck");
				break;
			case cody:
				NPCScriptManager.getInstance().start(c, 9200000, null, null);
				break;
			case dispose:
				NPCScriptManager.getInstance().dispose(c);
				c.announce(MaplePacketCreator.enableActions());
				chr.message("Done.");
				break;
			case emo:
				chr.setHp(0);
				chr.updateSingleStat(MapleStat.HP, 0);
				break;
			case gmlist:
				rs = getGMList();
				chr.dropMessage("GM List");
				try {
					while (rs.next()) {
						chr.dropMessage(rs.getString("name") + " " + MapleRank.getById(rs.getInt("gm")).toString());
					}
				} catch (SQLException e) {
				}
				break;
			case heal:
				int cost = ((chr.getMaxHp() / 4) * (chr.getMaxHp() / 4));
				if (chr.getMeso() >= cost && chr.getHp() < chr.getMaxHp()) {
					chr.gainMeso(-cost, false);
					chr.setHp(chr.getMaxHp());
					chr.updateSingleStat(MapleStat.HP, chr.getMaxHp());
					chr.message("You spent " + cost + " mesos on healing.");
				} else if (chr.getHp() >= chr.getMaxHp()) {
					chr.message("You already have full health!");
				} else {
					chr.message("You cannot afford to heal.");
				}
				break;
			case help:
				chr.dropMessage(ServerConstants.SERVER_NAME + "'s PlayerCommands Help");
				chr.dropMessage("@afk - Marks you as away, or with an optional message.");
				chr.dropMessage("@back - Marks you as returned.");
				chr.dropMessage("@bugs - Tells players where to report bugs!");
				chr.dropMessage("@buy - Purchases a rice cake for 1,000,000,000 mesos.");
				chr.dropMessage("@checkgm - Checks if a specified player is a GM.");
				chr.dropMessage("@checkrebirths - Checks a player's rebirths.");
				chr.dropMessage("@checkstats - Checks a player's stats.");
				chr.dropMessage("@cody - Shortcut to Cody.");
				chr.dropMessage("@dispose - Solves NPC problems.");
				chr.dropMessage("@emo - Kills yourself.");
				chr.dropMessage("@gmlist - Presents a list of all GMs");
				chr.dropMessage("@heal - Heals you, for a fee.");
				chr.dropMessage("@help - Displays this help message.");
				chr.dropMessage("@kin - Opens a conversation with Kin.");
				chr.dropMessage("@nimakin - Opens a conversation with Nimakin.");
				chr.dropMessage("@nx - Gives you 100,000 NX for free!");
				chr.dropMessage("@rankings - Displays the top 10 players.");
				chr.dropMessage("@rebirth - Allows you to be reborn at max level.");
				chr.dropMessage("@rebirths - Displays your rebirths.");
				chr.dropMessage("@save - Saves your character to the database.");
				chr.dropMessage("@sell - Sells a rice cake for 1,000,000,000 mesos.");
				chr.dropMessage("@stat - Allows you to raise your stats using your AP.");
				chr.dropMessage("@stats - Displays your full stats.");
				chr.dropMessage("@version - Displays server version information.");
				break;
			case kin:
				NPCScriptManager.getInstance().start(c, 9900000, null, null);
				break;
			case nimakin:
				NPCScriptManager.getInstance().start(c, 9900001, null, null);
				break;
			case nx:
				if (chr.getCashShop().getCash(1) + 100000 <= Integer.MAX_VALUE) {
					chr.getCashShop().gainCash(1, 100000);
					chr.dropMessage("Added 100,000 NX.");
				} else {
					chr.dropMessage("You have too much NX already!");
				}
				break;
			case quit:
				chr.saveToDB(true);
				c.getSession().close(false);
				break;
			case rankings:
				if (sub.length > 1) {
					rs = getRankings(Boolean.parseBoolean(sub[1]));
				} else {
					rs = getRankings(false);
				}
				chr.dropMessage(ServerConstants.SERVER_NAME + " Top Player Rankings");
				int i = 1;
				try {
					while (rs.next()) {
						chr.dropMessage(i + ". " + rs.getString("name") + " (Level " + rs.getInt("level") + " " + MapleJob.getById(rs.getInt("job")).toString() + ") - " + rs.getInt("rebirths") + " Rebirths.");
						i++;
					}
				} catch (SQLException e) {
				}
				break;
			case rebirth:
				if (chr.getLevel() >= 200) {
					if (sub[1].equalsIgnoreCase("standard") || sub[1].equalsIgnoreCase("beginner")) {
						chr.rebirthBeginner();
						chr.saveToDB(true);
					} else if (sub[1].equalsIgnoreCase("cygnus") || sub[1].equalsIgnoreCase("noblesse")) {
						chr.rebirthNoblesse();
						chr.saveToDB(true);
					} else if (sub[1].equalsIgnoreCase("aran")) {
						chr.rebirthAran();
						chr.saveToDB(true);
					} else {
						chr.message("Rebirth: ");
						chr.message("To use this command, follow it with either beginner, noblesse, or aran.");
					}
				} else {
					chr.message("You must be level 200 to do this!");
				}
				break;
			case rebirths:
				chr.message("You have " + chr.getRebirths() + " rebirths.");
				break;
			case save:
				chr.saveToDB(true);
				break;
			case sell:
				if (chr.haveItem(4001101)) {
					if (chr.getMeso() <= (Integer.MAX_VALUE - 1000000000)) { // Has less than 1,147,483,647 mesos.
						MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4001101, 1, true, true);
						chr.gainMeso(1000000000, false); // Gains 1,000,000,000 mesos.
						chr.message("You've gained 1,000,000,000 mesos.");
						chr.message("You now have " + chr.getItemQuantity(4001101, true) + " rice cakes.");
						chr.saveToDB(true);
					} else {
						chr.message("You don't have enough room to sell a rice cake!");
					}
				} else {
					chr.message("You don't have any rice cakes!");
				}
				break;
			case stat:
				String stat = sub[1];
				int amount = Integer.parseInt(sub[2]);
				int currentValue = 0;
				MapleStat currentStat = MapleStat.AVAILABLEAP;
				String name = "";
				if (stat.equalsIgnoreCase("str") || stat.equalsIgnoreCase("strength")) {
					currentValue = chr.getStr();
					currentStat = MapleStat.STR;
					name = "Strength";
				} else if (stat.equalsIgnoreCase("dex") || stat.equalsIgnoreCase("dexterity")) {
					currentValue = chr.getDex();
					currentStat = MapleStat.DEX;
					name = "Dexterity";
				} else if (stat.equalsIgnoreCase("int") || stat.equalsIgnoreCase("intellect")) {
					currentValue = chr.getInt();
					currentStat = MapleStat.INT;
					name = "Intellect";
				} else if (stat.equalsIgnoreCase("luk") || stat.equalsIgnoreCase("luck")) {
					currentValue = chr.getLuk();
					currentStat = MapleStat.LUK;
					name = "Luck";
				}
				if (currentStat != MapleStat.AVAILABLEAP) {
					if ((amount > 0 && amount <= chr.getRemainingAp()) || (amount < 0 && amount + chr.getRemainingAp() <= Short.MAX_VALUE)) {
						if ((amount + currentValue <= Short.MAX_VALUE) && (amount + currentValue >= 4)) {
							chr.setStr(currentValue + amount);
		                    chr.updateSingleStat(currentStat, currentValue);
		                    chr.setRemainingAp(chr.getRemainingAp() - amount);
		                    chr.updateSingleStat(MapleStat.AVAILABLEAP, chr.getRemainingAp());
						}
					} else if (amount > chr.getRemainingAp()) {
						chr.message("You don't have enough AP for that!");
					} else if (amount + chr.getRemainingAp() > Short.MAX_VALUE) {
						chr.message("That would put you over the AP cap of " + Short.MAX_VALUE + "!");
					} else if (amount + currentValue > Short.MAX_VALUE) {
						chr.message("That would put " + name + " over the maximum of " + Short.MAX_VALUE + "!");
					} else if (amount + currentValue < 4) {
						chr.message("That would put " + name + " under the minimum of 4!");
					}
				} else {
					chr.message("Stat: ");
					chr.message(" To use this command, follow it with a stat and the amount by which to change it.");
				}
				break;
			case stats:
				chr.message(chr.getName() + "'s Stats:");
				chr.message(" Level " + chr.getLevel() + " " + chr.getJob().toString());
				chr.message(" " + chr.getHp() + " / " + chr.getMaxHp() + " HP");
				chr.message(" " + chr.getMp() + " / " + chr.getMaxMp() + " MP");
				chr.message(" " + chr.getFame() + " Fame");
				chr.message(" " + chr.getRebirths() + " Rebirths");
				chr.message(" " + chr.getStr() + " Strength");
				chr.message(" " + chr.getDex() + " Dexterity");
				chr.message(" " + chr.getInt() + " Intellect");
				chr.message(" " + chr.getLuk() + " Luck");
				break;
			case version:
				chr.message(ServerConstants.SERVER_NAME + " (Orpheus " + ServerConstants.ORPHEUS_VERSION + ")");
				break;
		}
		return true;
	}
	
	private static ResultSet getGMList() {
		try {
			Connection con = (Connection) DatabaseConnection.getConnection();
			PreparedStatement ps = (PreparedStatement) con.prepareStatement("SELECT name, gm FROM characters WHERE gm > 2 ORDER BY gm DESC, name DESC");
			return ps.executeQuery();
		} catch (SQLException ex) {
			return null;
		}
	}

	private static ResultSet getRankings(boolean noGMs) {
		try {
			Connection con = (Connection) DatabaseConnection.getConnection();
			PreparedStatement ps;
			if (!noGMs) {
				ps = (PreparedStatement) con.prepareStatement("SELECT reborns, level, name, job FROM characters WHERE gm < 2 ORDER BY rebirths DESC, level DESC, name DESC LIMIT 10");
			} else {
				ps = (PreparedStatement) con.prepareStatement("SELECT reborns, level, name, job FROM characters ORDER BY rebirths DESC, level DESC, name DESC LIMIT 10");
			}
			return ps.executeQuery();
		} catch (SQLException ex) {
			return null;
		}
	}
	
	private static enum Command {
		afk,
		back,
		bugs,
		buy, 
		checkgm, 
		checkrebirths, 
		checkstats, 
		cody, 
		dispose,
		emo,
		gmlist,
		heal,
		help,
		kin,
		nimakin,
		nx,
		quit,
		rankings,
		rebirth, 
		rebirths, 
		save,
		sell,
		stat,
		stats,
		version,
	}
}
