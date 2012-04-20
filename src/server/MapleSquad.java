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
package server;

import client.MapleCharacter;
import java.util.LinkedList;
import java.util.List;
import net.MaplePacket;
import tools.MaplePacketCreator;

/**
 *
 * @author Danny
 */
public class MapleSquad {
    private MapleCharacter leader;
    private List<MapleCharacter> members = new LinkedList<MapleCharacter>();
    private List<MapleCharacter> bannedMembers = new LinkedList<MapleCharacter>();
    private int ch;
    private int status = 0;

    public MapleSquad(int ch, MapleCharacter leader) {
        this.leader = leader;
        this.members.add(leader);
        this.ch = ch;
        this.status = 1;
    }

    public MapleCharacter getLeader() {
        return leader;
    }

    public boolean containsMember(MapleCharacter member) {
        for (MapleCharacter mmbr : members) {
            if (mmbr.getId() == member.getId()) {
                return true;
            }
        }
        return false;
    }

    public boolean isBanned(MapleCharacter member) {
        for (MapleCharacter banned : bannedMembers) {
            if (banned.getId() == member.getId()) {
                return true;
            }
        }
        return false;
    }

    public List<MapleCharacter> getMembers() {
        return members;
    }

    public int getSquadSize() {
        return members.size();
    }

    public boolean addMember(MapleCharacter member) {
        if (isBanned(member)) {
            return false;
        } else {
            members.add(member);
            MaplePacket packet = MaplePacketCreator.serverNotice(5, member.getName() + " has joined the fight!");
            getLeader().getClient().getSession().write(packet);
            return true;
        }
    }

    public void banMember(MapleCharacter member, boolean ban) {
        int index = -1;
        for (MapleCharacter mmbr : members) {
            if (mmbr.getId() == member.getId()) {
                index = members.indexOf(mmbr);
            }
        }
        members.remove(index);
        if (ban) {
            bannedMembers.add(member);
        }
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public boolean equals(MapleSquad other) {
        if (other.ch == ch) {
            if (other.leader.getId() == leader.getId()) {
                return true;
            }
        }
        return false;
    }
}
