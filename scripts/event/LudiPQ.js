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
 * @Author Raz
 * 
 * Ludi Maze PQ
 */
var exitMap;
var instanceId;
var finishMap;
var bonusMap;
var bonusTime = 60;//1 Minute
var pqTime = 3600;//60 Minutes

function init() {
    instanceId = 1;
}

function monsterValue(eim, mobId) {
    return 1;
}

function setup() {
    exitMap = em.getChannelServer().getMapFactory().getMap(922010000);//Exit
    var instanceName = "LudiPQ" + instanceId;
    var eim = em.newInstance(instanceName);
    //eim.setTimeLeft(pqTime);
    var mf = eim.getMapFactory();
    instanceId++;
    var map0 = mf.getMap(922010100);
    var map1 = mf.getMap(922010200);
    var map1_1 = mf.getMap(922010201);
    var map2 = mf.getMap(922010300);
    var map3 = mf.getMap(922010400);
    var map3_1 = mf.getMap(922010401);
    var map3_2 = mf.getMap(922010402);
    var map3_3 = mf.getMap(922010403);
    var map3_4 = mf.getMap(922010404);
    var map3_5 = mf.getMap(922010405);
    var map4 = mf.getMap(922010500);
    var map4_1 = mf.getMap(922010501);
    var map4_2 = mf.getMap(922010502);
    var map4_3 = mf.getMap(922010503);
    var map4_4 = mf.getMap(922010504);
    var map4_5 = mf.getMap(922010505);
    var map4_6 = mf.getMap(922010506);
    var map5 = mf.getMap(922010600);
    var map6 = mf.getMap(922010700);
    var map7 = mf.getMap(922010800);
    var map8 = mf.getMap(922010900);
    var map9 = mf.getMap(922011000);
    var map10 = mf.getMap(922011100);
    eim.addMapInstance(922010100,map0);
    eim.addMapInstance(922010200,map1);
    eim.addMapInstance(922010201,map1_1);
    eim.addMapInstance(922010300,map2);
    eim.addMapInstance(922010400,map3);
    eim.addMapInstance(922010401,map3_1);
    eim.addMapInstance(922010402,map3_2);
    eim.addMapInstance(922010403,map3_3);
    eim.addMapInstance(922010404,map3_4);
    eim.addMapInstance(922010405,map3_5);
    eim.addMapInstance(922010500,map4);
    eim.addMapInstance(922010501,map4_1);
    eim.addMapInstance(922010502,map4_2);
    eim.addMapInstance(922010503,map4_3);
    eim.addMapInstance(922010504,map4_4);
    eim.addMapInstance(922010505,map4_5);
    eim.addMapInstance(922010506,map4_6);
    eim.addMapInstance(922010600,map5);
    eim.addMapInstance(922010700,map6);
    eim.addMapInstance(922010800,map7);
    eim.addMapInstance(922010900,map8);
    eim.addMapInstance(922011000,map9);
    eim.addMapInstance(922011100,map10);
    var stage1Portal = eim.getMapInstance(922010100).getPortal("next00");
    stage1Portal.setScriptName("lpq1");
    var stage2Portal = eim.getMapInstance(922010200).getPortal("next00");
    stage2Portal.setScriptName("lpq2");
    var stage2PortalA = eim.getMapInstance(922010201).getPortal("out00");
    stage2PortalA.setScriptName("lpq2A");
    var stage3Portal = eim.getMapInstance(922010300).getPortal("next00");
    stage3Portal.setScriptName("lpq3");
    var stage4Portal = eim.getMapInstance(922010400).getPortal("next00");
    stage4Portal.setScriptName("lpq4");
    var stage5Portal = eim.getMapInstance(922010500).getPortal("next00");
    stage5Portal.setScriptName("lpq5");
    var stage6Portal = eim.getMapInstance(922010600).getPortal("next00");
    stage6Portal.setScriptName("lpq6");
    var stage7Portal = eim.getMapInstance(922010700).getPortal("next00");
    stage7Portal.setScriptName("lpq7");
    var stage8Portal = eim.getMapInstance(922010800).getPortal("next00");
    stage8Portal.setScriptName("lpq8");
    var stage4PortalA = eim.getMapInstance(922010401).getPortal("out00");
    stage4PortalA.setScriptName("lpq4A");
    var stage4PortalB = eim.getMapInstance(922010402).getPortal("out00");
    stage4PortalB.setScriptName("lpq4B");
    var stage4PortalC = eim.getMapInstance(922010403).getPortal("out00");
    stage4PortalC.setScriptName("lpq4C");
    var stage4PortalD = eim.getMapInstance(922010404).getPortal("out00");
    stage4PortalD.setScriptName("lpq4D");
    var stage4PortalE = eim.getMapInstance(922010405).getPortal("out00");
    stage4PortalE.setScriptName("lpq4E");
    var stage5Portal1A = eim.getMapInstance(922010500).getPortal("in01");
    stage5Portal1A.setScriptName("lpq5_1_A");
    return eim;
}

function playerEntry(eim, player) {
    var map0 = eim.getMapInstance(922010100);
    player.changeMap(map0, map0.getPortal(0));
}

function playerDead(eim, player) {
    if (player.isAlive()) { //don't trigger on death, trigger on manual revive
        if (eim.isLeader(player)) {
            var party = eim.getPlayers();
            for (var i = 0; i < party.size(); i++)
                playerExit(eim, party.get(i));
            eim.dispose();
        }
        else
            playerExit(eim, player);
    }
}

function playerDisconnected(eim, player) {
    if (eim.isLeader(player)) { //check for party leader
        //boot whole party and end
        var party = eim.getPlayers();
        for (var i = 0; i < party.size(); i++)
            if (party.get(i).equals(player))
                removePlayer(eim, player);
            else
                playerExit(eim, party.get(i));
        eim.dispose();
    }
    else
        removePlayer(eim, player);
}

function leftParty(eim, player) {
    playerExit(eim, player);
}

function disbandParty(eim) {
    //boot whole party and end
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++)
        playerExit(eim, party.get(i));
    eim.dispose();
}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    player.changeMap(exitMap, exitMap.getPortal(0));
}


function playerFinish(eim, player) {
    var map = eim.getMapInstance(922011100);
    player.changeMap(map, map.getPortal(0));
}

//for offline players
function removePlayer(eim, player) {
    eim.unregisterPlayer(player);
    player.getMap().removePlayer(player);
    player.setMap(exitMap);
}

function clearPQ(eim) {
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++)
        playerFinish(eim, party.get(i));
    eim.dispose();
}

function allMonstersDead(eim) {
}

function cancelSchedule() {
}

function timeOut() {
    var iter = em.getInstances().iterator();
    while (iter.hasNext()) {
        var eim = iter.next();
        if (eim.getPlayerCount() > 0) {
            var pIter = eim.getPlayers().iterator();
            while (pIter.hasNext())
                playerExit(eim, pIter.next());
        }
        eim.dispose();
    }
}

function startBonus() {
    var iter = em.getInstances().iterator();
    while (iter.hasNext()) {
        var eim = iter.next();
        if (eim.getPlayerCount() > 0) {
            var pIter = eim.getPlayers().iterator();
            while (pIter.hasNext())
                if(pIter.next().getMap().getId() == 922011000)
                    playerFinish(eim, pIter.next());
        }
    }

}
