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
package client.command;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import paranoia.BlacklistHandler;
import paranoia.ParanoiaInformation;
import paranoia.ParanoiaInformationHandler;
import constants.ParanoiaConstants;
import constants.ServerConstants;
import server.TimerManager;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleNPC;
import tools.DatabaseConnection;
import tools.MapleLogger;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import net.server.Channel;
import net.server.Server;
import client.MapleCharacter;
import client.MapleClient;

/**
 * @author Aaron Weiss
 */
public class DeveloperCommands extends EnumeratedCommands {
	private static SeekableLittleEndianAccessor slea;
	private static final int gmLevel = 4;
	private static final char heading = '!';
	
	@SuppressWarnings("unused")
	public static boolean execute(MapleClient c, String[] sub, char heading) {
		MapleCharacter chr = c.getPlayer();
		Channel cserv = c.getChannelServer();
		MapleCharacter victim; // For commands with targets.
		ResultSet rs; // For commands with MySQL results.
        MapleNPC npc;
		int npcId = 0;
        int mobTime = 0;
        int xpos = 0;
        int ypos = 0;
        int fh = 0;
        
        try {
			Command command = Command.valueOf(sub[0]);
			switch (command) {
				default:
					// chr.yellowMessage("Command: " + heading + sub[0] + ": does not exist.");
					return false;
				case coords:
		            xpos = chr.getPosition().x;
		            ypos = chr.getPosition().y;
		            fh = chr.getMap().getFootholds().findBelow(chr.getPosition()).getId();
		            chr.dropMessage("Position: (" + xpos + ", " + ypos + ")");
		            chr.dropMessage("Foothold ID: " + fh);
		            break;
				case droprate:
					c.getWorldServer().setDropRate(Integer.parseInt(sub[1]));
					for (MapleCharacter mc : c.getWorldServer().getPlayerStorage().getAllCharacters()) {
						mc.setRates();
					}
					Server.getInstance().broadcastMessage(chr.getWorld(), MaplePacketCreator.serverNotice(1, "[Notice] The drop rate has changed to " + sub[1] + "."));
					chr.message("Done.");
					break;
				case exprate:
					c.getWorldServer().setExpRate(Integer.parseInt(sub[1]));
					for (MapleCharacter mc : c.getWorldServer().getPlayerStorage().getAllCharacters()) {
						mc.setRates();
					}
					Server.getInstance().broadcastMessage(chr.getWorld(), MaplePacketCreator.serverNotice(1, "[Notice] The experience rate has changed to " + sub[1] + "."));
					chr.message("Done.");
					break;
				case gc:
		            System.gc();
					break;
				case help:
					if (sub.length > 1) {
						if (sub[1].equalsIgnoreCase("dev")) {
							if (sub.length > 2 && ServerConstants.PAGINATE_HELP) {
								getHelp(Integer.parseInt(sub[2]), chr);
							} else {
								getHelp(chr);
							}
							break;
						} else {
							return false;
						}
					} else {
						return false;
					}
				case horntail:
					chr.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8810026), chr.getPosition());
					break;
				case mesorate:
					c.getWorldServer().setMesoRate(Integer.parseInt(sub[1]));
					for (MapleCharacter mc : c.getWorldServer().getPlayerStorage().getAllCharacters()) {
						mc.setRates();
					}
					Server.getInstance().broadcastMessage(chr.getWorld(), MaplePacketCreator.serverNotice(1, "[Notice] The meso rate has changed to " + sub[1] + "."));
					chr.message("Done.");
					break;
				case npc:
					npc = MapleLifeFactory.getNPC(Integer.parseInt(sub[1]));
					if (npc != null) {
						npc.setPosition(chr.getPosition());
						npc.setCy(chr.getPosition().y);
						npc.setRx0(chr.getPosition().x + 50);
						npc.setRx1(chr.getPosition().x - 50);
						npc.setFh(chr.getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId());
						chr.getMap().addMapObject(npc);
						chr.getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc));
					}
					break;
				case packet:
					chr.getMap().broadcastMessage(MaplePacketCreator.customPacket(joinStringFrom(sub, 1)));
					break;
				case paranoia:
					if (ParanoiaConstants.ALLOW_QUERY_COMMAND) {
						if (sub.length > 1) {
							if (sub[1].equalsIgnoreCase("help")) {
								chr.dropMessage("Paranoia Information Querying Help");
								for (ParanoiaInformation pi : ParanoiaInformation.values()) {
									chr.dropMessage(pi.name() + " - " + pi.explain());
								}
							} else {
								chr.dropMessage(ParanoiaInformationHandler.getFormattedValue(sub[1]));
							}
						} else {
							chr.dropMessage("Usage: !paranoia value || !paranoia help");
						}
					} else {
						chr.dropMessage("Paranoia Information Querying is forbidden by the server.");
					}
					break;
				case pinkbean:
					chr.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8820009), chr.getPosition());
					break;
				case playernpc:
					if (sub.length > 2) {
						chr.playerNPC(c.getChannelServer().getPlayerStorage().getCharacterByName(sub[1]), Integer.parseInt(sub[2]));
					} else if (sub.length == 2) {
						chr.playerNPC(chr, Integer.parseInt(sub[1]));
					} else {
						chr.dropMessage("Usage: !playernpc characterName scriptId || !playernpc scriptId");
					}
				case pmob:
					npcId = Integer.parseInt(sub[1]);
					mobTime = Integer.parseInt(sub[2]);
					xpos = chr.getPosition().x;
					ypos = chr.getPosition().y;
					fh = chr.getMap().getFootholds().findBelow(chr.getPosition()).getId();
					if (sub[2] == null) {
						mobTime = 0;
					}
					MapleMonster mob = MapleLifeFactory.getMonster(npcId);
					if (mob != null && !mob.getName().equals("MISSINGNO")) {
						mob.setPosition(chr.getPosition());
						mob.setCy(ypos);
						mob.setRx0(xpos + 50);
						mob.setRx1(xpos - 50);
						mob.setFh(fh);
						try {
							Connection con = DatabaseConnection.getConnection();
							PreparedStatement ps = con.prepareStatement("INSERT INTO spawns ( idd, f, fh, cy, rx0, rx1, type, x, y, mid, mobtime ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
							ps.setInt(1, npcId);
							ps.setInt(2, 0);
							ps.setInt(3, fh);
							ps.setInt(4, ypos);
							ps.setInt(5, xpos + 50);
							ps.setInt(6, xpos - 50);
							ps.setString(7, "m");
							ps.setInt(8, xpos);
							ps.setInt(9, ypos);
							ps.setInt(10, chr.getMapId());
							ps.setInt(11, mobTime);
							ps.executeUpdate();
						} catch (SQLException e) {
							chr.dropMessage("Failed to save mob to the database.");
						}
						chr.getMap().addMonsterSpawn(mob, mobTime, 0);
					} else {
						chr.dropMessage("You have entered an invalid mob ID.");
					}
					break;
				case pnpc:
					npcId = Integer.parseInt(sub[1]);
					npc = MapleLifeFactory.getNPC(npcId);
					xpos = chr.getPosition().x;
					ypos = chr.getPosition().y;
					fh = chr.getMap().getFootholds().findBelow(chr.getPosition()).getId();
					if (npc != null && !npc.getName().equals("MISSINGNO")) {
						npc.setPosition(chr.getPosition());
						npc.setCy(ypos);
						npc.setRx0(xpos + 50);
						npc.setRx1(xpos - 50);
						npc.setFh(fh);
						try {
							Connection con = DatabaseConnection.getConnection();
							PreparedStatement ps = con.prepareStatement("INSERT INTO spawns ( idd, f, fh, cy, rx0, rx1, type, x, y, mid ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
							ps.setInt(1, npcId);
							ps.setInt(2, 0);
							ps.setInt(3, fh);
							ps.setInt(4, ypos);
							ps.setInt(5, xpos + 50);
							ps.setInt(6, xpos - 50);
							ps.setString(7, "n");
							ps.setInt(8, xpos);
							ps.setInt(9, ypos);
							ps.setInt(10, chr.getMapId());
							ps.executeUpdate();
						} catch (SQLException e) {
							chr.dropMessage("Failed to save NPC to the database.");
						}
						chr.getMap().addMapObject(npc);
						chr.getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc));
					} else {
						chr.dropMessage("You have entered an invalid NPC id.");
					}
					break;
				case reloadblacklist:
					if (ServerConstants.USE_PARANOIA && ParanoiaConstants.ENABLE_BLACKLISTING && ParanoiaConstants.ALLOW_RELOADBLACKLIST_COMMAND) {
						BlacklistHandler.reloadBlacklist();
						chr.message("Done.");
					} else if (!ParanoiaConstants.ENABLE_BLACKLISTING) {
						chr.dropMessage("Blacklisting is disabled on the server.");
					} else if (!ParanoiaConstants.ALLOW_RELOADBLACKLIST_COMMAND) {
						chr.dropMessage("Reloading blacklist is forbidden by the server.");
					}
					break;
				case say:
					if (sub.length > 2) {
						victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
						final String s = joinStringFrom(sub, 2);
						victim.getMap().broadcastMessage(MaplePacketCreator.getChatText(victim.getId(), s, victim.isGM(), slea.readByte()));
					} else {
						chr.message("Usage: !say playerName multi-word message");
					}
					break;
				case shutdown:
					if (sub.length == 2) {
						int time = 60000;
						if (sub[1].equalsIgnoreCase("now")) {
							time = 1;
						} else {
							time *= Integer.parseInt(sub[1]);
						}
						TimerManager.getInstance().schedule(Server.getInstance().shutdown(false), time);
					} else {
						chr.message("Usage: !shutdown time || !shutdown now");
					}
					break;
				case sql:
					if (sub[1] == "true") {
						String name = sub[1];
						final String query = joinStringFrom(sub, 2);
						try {
							PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(query);
							rs = ps.executeQuery();
							while (rs.next()) {
								chr.dropMessage(String.valueOf(rs.getObject(name)));
							}
							rs.close();
							ps.close();
						} catch (SQLException e) {
							chr.message("Query Failed: " + query);
						}
					} else {
						final String query = joinStringFrom(sub, 1);
						try {
							PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(query);
							ps.executeUpdate();
							ps.close();
							chr.message("Completed: " + query);
						} catch (SQLException e) {
							chr.message("Query Failed: " + query);
						}
					}
					break;
				case updaterankings:
					updateRankings();
					break;
				case zakum:
					chr.getMap().spawnFakeMonsterOnGroundBelow(MapleLifeFactory.getMonster(8800000), chr.getPosition());
					for (int x = 8800003; x < 8800011; x++) {
						chr.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(x), chr.getPosition());
					}
					break;
			}
			if (ServerConstants.USE_PARANOIA && ParanoiaConstants.PARANOIA_COMMAND_LOGGER && ParanoiaConstants.LOG_DEVELOPER_COMMANDS) {
				MapleLogger.printFormatted(MapleLogger.PARANOIA_COMMAND, "[" + c.getPlayer().getName() + "] Used " + heading + sub[0] + ((sub.length > 1) ? " with parameters: " + joinStringFrom(sub, 1) : "."));
			}
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	public static void setSLEA(SeekableLittleEndianAccessor slea) {
		DeveloperCommands.slea = slea;
	}
	
	public static void updateRankings() {
		try {
			Connection con = (Connection) DatabaseConnection.getConnection();
			PreparedStatement ps;
			ResultSet rs;
			ps = (PreparedStatement) con.prepareStatement("SELECT id, rank, rankMove FROM characters WHERE gm < 2 ORDER BY rebirths DESC, level DESC, name DESC");
			rs = ps.executeQuery();
			int n = 1;
			while (rs.next()) {
				ps = (PreparedStatement) con.prepareStatement("UPDATE characters SET rank = ?, rankMove = ? WHERE id = ?");
				ps.setInt(1, n);
				ps.setInt(2, rs.getInt("rank") - n);
				ps.setInt(3, rs.getInt("id"));
				ps.executeUpdate();
				n++;
			}
		} catch (SQLException e) {
			MapleLogger.print(MapleLogger.EXCEPTION_CAUGHT, e);
		}	
	}
	
	protected static void getHelp(MapleCharacter chr) {
		DeveloperCommands.getHelp(-1, chr);
	}

	protected static void getHelp(int page, MapleCharacter chr) {
		int pageNumber = (int) (Command.values().length / ServerConstants.ENTRIES_PER_PAGE);
		if (Command.values().length % ServerConstants.ENTRIES_PER_PAGE > 0) {
    		pageNumber++;
    	}
		if (page <= 0 || pageNumber == 1) {
			chr.dropMessage(ServerConstants.SERVER_NAME + "'s DeveloperCommands Help");
			for (Command cmd : Command.values()) {
				chr.dropMessage(heading + cmd.name() + " - " + cmd.getDescription());
			}
		} else {
	        if (page > pageNumber) {
	        	page = pageNumber;
	        }
	        int lastPageEntry = (Command.values().length - Math.max(0, Command.values().length - (page * ServerConstants.ENTRIES_PER_PAGE)));
	        lastPageEntry -= 1;
			chr.dropMessage(ServerConstants.SERVER_NAME + "'s DeveloperCommands Help (Page " + page + " / " + pageNumber + ")");
	        for (int i = lastPageEntry; i < lastPageEntry + ServerConstants.ENTRIES_PER_PAGE; i++) {
				chr.dropMessage(heading + Command.values()[i].name() + " - " + Command.values()[i].getDescription());
	        }
		}
	}
	
	public static int getRequiredStaffRank() {
		return gmLevel;
	}
	
	public static char getHeading() {
		return heading;
	}
	
	private static enum Command {
		coords("Prints your current coordinates."),
		droprate("Sets the server-wide drop rate."),
		exprate("Sets the server-wide experience rate."),
		gc("Runs the garbage collector."),
		help("Displays this help message."),
		horntail("Summons Horntail at your position."),
		mesorate("Sets the server-wide meso rate."),
		npc("Spawns an NPC at your position."),
		packet("Executes a custom packet."),
		paranoia("Gathers information about Paranoia."),
		playernpc("Adds an NPC that mimics you or another player."),
		pmob("Permanently spawns a mob at your position."),
		pnpc("Permanently spawns an NPC at your position."),
		pinkbean("Summons Pinkbean at your position."),
		reloadblacklist("Reloads the Paranoia blacklist."),
		say("Forces a victim to say something."),
		shutdown("Shutdowns the server."),
		sql("Executes an SQL query."),
		updaterankings("Forces an update of the rankings."),
		zakum("Summons Zakum at your position.");

	    private final String description;
	    
	    private Command(String description){
	        this.description = description;
	    }
	    
	    public String getDescription() {
	    	return this.description;
	    }
	}

}
