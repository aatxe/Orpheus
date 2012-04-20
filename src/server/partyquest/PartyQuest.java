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

package server.partyquest;

import client.MapleCharacter;
import java.util.ArrayList;
import java.util.List;
import net.server.MapleParty;
import net.server.MaplePartyCharacter;
import net.server.Server;

/**
 *
 * @author kevintjuh93
 */
public class PartyQuest {
    byte channel, world;
    MapleParty party;
    List<MapleCharacter> participants = new ArrayList<MapleCharacter>();

    public PartyQuest(MapleParty party) {
        this.party = party;
        MaplePartyCharacter leader = party.getLeader();
        channel = leader.getChannel();
        world = leader.getWorld();
        int mapid = leader.getMapId();
        for (MaplePartyCharacter pchr : party.getMembers()) {
            if (pchr.getChannel() == channel && pchr.getMapId() == mapid) {
                MapleCharacter chr = Server.getInstance().getWorld(world).getChannel(channel).getPlayerStorage().getCharacterById(pchr.getId());
                if (chr != null)
                    this.participants.add(chr);
            }
        }
    }

    public MapleParty getParty() {
        return party;
    }

    public List<MapleCharacter> getParticipants() {
        return participants;
    }

    public void removeParticipant(MapleCharacter chr) throws Throwable {
        synchronized (participants) {
            participants.remove(chr);
            chr.setPartyQuest(null);
            if (participants.isEmpty()) super.finalize();
            //System.gc();
        }
    }
}
