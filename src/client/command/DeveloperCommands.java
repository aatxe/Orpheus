package client.command;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import server.TimerManager;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleNPC;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import net.server.Channel;
import net.server.Server;
import client.MapleCharacter;
import client.MapleClient;

public class DeveloperCommands extends Commands {
	public static SeekableLittleEndianAccessor slea;
	
	public static void execute(MapleClient c, String[] sub, char heading) {
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

		Command command = Command.valueOf(sub[0]);
		switch (command) {
			default:
				chr.yellowMessage("Command: " + heading + sub[0] + ": does not exist.");
			case exprate:
				c.getWorldServer().setExpRate((byte) (Byte.parseByte(sub[1]) % 128));
				for (MapleCharacter mc : c.getWorldServer().getPlayerStorage().getAllCharacters()) {
					mc.setRates();
				}
			case gc:
	            System.gc();
			case horntail:
				chr.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8810026), chr.getPosition());
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
			case packet:
				chr.getMap().broadcastMessage(MaplePacketCreator.customPacket(joinStringFrom(sub, 1)));
			case pinkbean:
				chr.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8820009), chr.getPosition());
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
			case say:
				if (sub.length > 2) {
					victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
					String s = joinStringFrom(sub, 1);
					victim.getMap().broadcastMessage(MaplePacketCreator.getChatText(chr.getId(), s, chr.isGM(), slea.readByte()));
				} else {
					chr.message("Usage: !say playerName multi-word message");
				}
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
			case zakum:
				chr.getMap().spawnFakeMonsterOnGroundBelow(MapleLifeFactory.getMonster(8800000), chr.getPosition());
				for (int x = 8800003; x < 8800011; x++) {
					chr.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(x), chr.getPosition());
				}
		}
	}

	public static void setSLEA(SeekableLittleEndianAccessor slea) {
		DeveloperCommands.slea = slea;
	}
	
	private static enum Command {
		exprate,
		gc,
		horntail,
		npc,
		packet,
		pmob,
		pnpc,
		pinkbean,
		say,
		shutdown,
		sql,
		zakum,
	}

}
