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
/*
 *@Author:     Moogra, Traitor
 *@Map(s):     All Dojo fighting maps
 *@Function:   Spawns dojo monsters and handles time
*/
importPackage(Packages.server.life);
importPackage(Packages.tools);

function start(ms) {
    try {
        ms.getPlayer().resetEnteredScript();
        var stage = (ms.getPlayer().getMap().getId() / 100) % 100;
        if (stage % 6 == 1)
            ms.getPlayer().setDojoStart();
        if (ms.getPlayer().getMap().getCharacters().size() == 1)
            ms.getPlayer().showDojoClock();
        if (stage % 6 > 0) {
            var realstage = stage - ((stage / 6) | 0);
            ms.getClient().getSession().write(MaplePacketCreator.getEnergy("energy", ms.getPlayer().getDojoEnergy()));
            var mob = MapleLifeFactory.getMonster(9300183 + realstage);
            if (mob != null && ms.getPlayer().getMap().getMonsterById(9300183 + realstage) == null && ms.getPlayer().getMap().getMonsterById(9300216) == null) {
                mob.setBoss(false);
                ms.getPlayer().getMap().spawnDojoMonster(mob);
                ms.getClient().getSession().write(MaplePacketCreator.playSound("Dojang/start"));
                ms.getClient().getSession().write(MaplePacketCreator.showEffect("dojang/start/stage"));
                ms.getClient().getSession().write(MaplePacketCreator.showEffect("dojang/start/number/" + realstage));
            }
        }
    } catch(err) {
        ms.getPlayer().dropMessage(err);
    }
}