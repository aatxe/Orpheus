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
//importPackage(server.maps);
//importPackage(net.channel);
//importPackage(tools);
//
//function enter(pi) {
//    var mapid = 541010100;
//    var map = ChannelServer.getInstance(pi.getPlayer().getClient().getChannel()).getMapFactory().getMap(mapid);
//    var mapchars = map.getCharacters();
//    if (mapchars.isEmpty()) {
//        var mapobjects = map.getMapObjects();
//        var iter = mapobjects.iterator();
//        while (iter.hasNext()) {
//            o = iter.next();
//            if (o.getType() == MapleMapObjectType.MONSTER){
//                map.removeMapObject(o);
//            }
//        }
//        map.resetReactors();
//    } else {
//        var mapobjects = map.getMapObjects();
//        var boss = null;
//        var iter = mapobjects.iterator();
//        while (iter.hasNext()) {
//            o = iter.next();
//            if (o.getType() == MapleMapObjectType.MONSTER){
//                boss = o;
//            }
//        }
//        if (boss != null) {
//            pi.getPlayer().dropMessage(5, "The battle against the boss has already begun, so you may not enter this place.");
//            return false;
//        }
//    }
//    pi.warp(541010100, "sp");
//    return true;
//}