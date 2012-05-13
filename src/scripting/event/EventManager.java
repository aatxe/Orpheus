/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package scripting.event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptException;
import client.MapleCharacter;
import com.mysql.jdbc.Connection;
import constants.ServerConstants;
import net.server.Channel;
import net.server.MapleParty;
import net.server.Server;
import server.TimerManager;
import server.maps.MapleMap;
import tools.DatabaseConnection;
import tools.MapleLogger;

/**
 * 
 * @author Matze
 */
public class EventManager {
	private Invocable iv;
	private Channel cserv;
	private Map<String, EventInstanceManager> instances = new HashMap<String, EventInstanceManager>();
	private Properties props = new Properties();
	private String name;
	private ScheduledFuture<?> schedule = null;

	public EventManager(Channel cserv, Invocable iv, String name) {
		this.iv = iv;
		this.cserv = cserv;
		this.name = name;
	}

	public void cancel() {
		try {
			iv.invokeFunction("cancelSchedule", (Object) null);
		} catch (ScriptException ex) {
			ex.printStackTrace();
		} catch (NoSuchMethodException ex) {
			ex.printStackTrace();
		}
	}

	public void schedule(String methodName, long delay) {
		schedule(methodName, null, delay);
	}

	public void schedule(final String methodName, final EventInstanceManager eim, long delay) {
		schedule = TimerManager.getInstance().schedule(new Runnable() {
			public void run() {
				try {
					iv.invokeFunction(methodName, eim);
				} catch (ScriptException ex) {
					Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
				} catch (NoSuchMethodException ex) {
					Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}, delay);
	}

	public void cancelSchedule() {
		schedule.cancel(true);
	}

	public ScheduledFuture<?> scheduleAtTimestamp(final String methodName, long timestamp) {
		return TimerManager.getInstance().scheduleAtTimestamp(new Runnable() {
			public void run() {
				try {
					iv.invokeFunction(methodName, (Object) null);
				} catch (ScriptException ex) {
					Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
				} catch (NoSuchMethodException ex) {
					Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}, timestamp);
	}

	public Channel getChannelServer() {
		return cserv;
	}

	public EventInstanceManager getInstance(String name) {
		return instances.get(name);
	}

	public Collection<EventInstanceManager> getInstances() {
		return Collections.unmodifiableCollection(instances.values());
	}

	public EventInstanceManager newInstance(String name) {
		EventInstanceManager ret = new EventInstanceManager(this, name);
		instances.put(name, ret);
		return ret;
	}

	public void disposeInstance(String name) {
		instances.remove(name);
	}

	public Invocable getIv() {
		return iv;
	}

	public void setProperty(String key, String value) {
		props.setProperty(key, value);
	}

	public String getProperty(String key) {
		return props.getProperty(key);
	}

	public String getName() {
		return name;
	}

	// PQ method: starts a PQ
	public void startInstance(MapleParty party, MapleMap map) {
		try {
			EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", (Object) null));
			eim.registerParty(party, map);
		} catch (ScriptException ex) {
			Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchMethodException ex) {
			Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	// non-PQ method for starting instance
	public void startInstance(EventInstanceManager eim, String leader) {
		try {
			iv.invokeFunction("setup", eim);
			eim.setProperty("leader", leader);
		} catch (ScriptException ex) {
			Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchMethodException ex) {
			Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public String getTipName() {
		return ServerConstants.TIP_NAME;
	}
	
	public void updateRankings() {
		for (Channel chan : Server.getInstance().getAllChannels()) {
            for (MapleCharacter plyrs : chan.getPlayerStorage().getAllCharacters()) {
                plyrs.saveToDB(true);
            }
        }
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
}
