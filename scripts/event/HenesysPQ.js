/* 
 * This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
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

/* 
*
*Henesys PQ
*
*/
// Significant maps
// 100000200 - Pig Park
// 910010000 - 1st Stage
// 910010100 - Shortcut
// 910010200 - Bonus
// 910010300 - Exit
// Significant items
// 4001101 - Rice Cake
// Significant monsters
// 9300061 - Bunny
// 9300062 - Flyeye
// 9300063 - Stirge
// 9300064 - Goblin Fires
// Significant NPCs
// 1012112 - Troy
// 1012113 - Tommy
// 1012114 - Growlie
// map effects
// Map/Obj/Effect/quest/gate/3 - warp activation glow
// quest/party/clear - CLEAR text
// Party1/Clear - clear sound
/* INSERT monsterdrops (monsterid,itemid,chance) VALUES (9300061,4001101,1);
 */


importPackage(Packages.net.world);
importPackage(Packages.tools);

var exitMap;
var instanceId;
var minPlayers = 3;
var pqTime = 600;//10 Minutes

function init() {
    instanceId = 1;
}

function monsterValue(eim, mobId) {
    return 1;
}

function setup() {
    exitMap = em.getChannelServer().getMapFactory().getMap(910010300); // <exit>
    var instanceName = "HenesysPQ" + instanceId;
    var eim = em.newInstance(instanceName);
    var mf = eim.getMapFactory();
    instanceId++;
    var map = mf.getMap(910010300);
    map.shuffleReactors();
    eim.addMapInstance(910010300,map);
    var firstPortal = eim.getMapInstance(910010000).getPortal("next00");
    firstPortal.setScriptName("hpq1");
    em.schedule("timeOut", 1000 * 60 * 15);
    return eim;
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(910010300);
    player.changeMap(map, map.getPortal(0));
//player.getClient().getSession().write(MaplePacketCreator.getClock(1800));
}

function playerDead(eim, player) {
    if (player.isAlive()) { //don't trigger on death, trigger on manual revive
        if (eim.isLeader(player)) { //check for party leader, boot whole party and end
            var party = eim.getPlayers();
            for (var i = 0; i < party.size(); i++)
                playerExit(eim, party.get(i));
            eim.dispose();
        } else { //boot dead player. If only 2 players are left, uncompletable:
            var partyz = eim.getPlayers();
            if (partyz.size() < minPlayers) {
                for (var j = 0; j < partyz.size(); j++)
                    playerExit(eim,partyz.get(j));
                eim.dispose();
            } else
                playerExit(eim, player);
        }
    }
}

function playerDisconnected(eim, player) {
    if (eim.isLeader(player)) { //check for party leader
        //boot whole party and end
        var party = eim.getPlayers();
        for (var i = 0; i < party.size(); i++) {
            if (party.get(i).equals(player)) {
                removePlayer(eim, player);
            }
            else {
                playerExit(eim, party.get(i));
            }
        }
        eim.dispose();
    }
    else { //boot d/ced player
        // If only 2 players are left, uncompletable:
        var partyz = eim.getPlayers();
        if (partyz.size() < minPlayers) {
            for (var j = 0; j < partyz.size(); j++)
                playerExit(eim,partyz.get(j));
            eim.dispose();
        }
        else
            playerExit(eim, player);
    }
}

function leftParty(eim, player) {// If only 2 players are left, uncompletable:
    var party = eim.getPlayers();
    if (party.size() < minPlayers) {
        for (var i = 0; i < party.size(); i++)
            playerExit(eim,party.get(i));
        eim.dispose();
    }
    else
        playerExit(eim, player);
}

function disbandParty(eim) {
    //boot whole party and end
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        playerExit(eim, party.get(i));
    }
    eim.dispose();
}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    player.changeMap(exitMap, exitMap.getPortal(0));
}

//for offline players
function removePlayer(eim, player) {
    eim.unregisterPlayer(player);
    player.getMap().removePlayer(player);
    player.setMap(exitMap);
}

function clearPQ(eim) {
    //HPQ does nothing special with winners
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++)
        playerExit(eim, party.get(i));
    eim.dispose();
}

function allMonstersDead(eim) {
//do nothing; HPQ has nothing to do with monster killing
}

function cancelSchedule() {
}

function timeOut() {
    var iter = em.getInstances().iterator();
    while (iter.hasNext()) {
        var eim = iter.next();
        if (eim.getPlayerCount() > 0) {
            var pIter = eim.getPlayers().iterator();
            while (pIter.hasNext()) {
                playerExit(eim, pIter.next());
            }
        }
        eim.dispose();
    }
}

function dispose() {
}