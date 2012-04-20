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
/*
@	Author : Raz
@
@	NPC = Red Sign
@	Map = Ludibrium <Eos Tower [101st Floor]>
@	NPC MapId = 221024500
@	Function = Start LPQ
@
*/
var status = 0;
var minlvl = 0;
var maxlvl = 200;
var minplayers = 0;
var maxplayers = 6;
var time = 60;//Minutes//Does Nothing
var open = true;

function start() {
    cm.sendNext("Lets start the Ludi-PQ!");
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    }else if (mode == 0){
        cm.dispose();
    }else{
        if (mode == 1)
            status++;
        else
            status--;
        var em = cm.getEventManager("LudiPQ");
        if (status == 1){
            if (hasParty() == false) {//NO PARTY
                cm.sendOk("From here on above, this place is full of dangerous objects and monsters, so I can't let you make your way up anymore. If you're interested in saving us and bring peace back into Ludibrium, however, that's a different story. If you want to defeat a powerful creature residing at the very top, then please gather up your party members. It won't be easy. but ... I think you can do it");
                cm.dispose();
            }else if (isLeader() == false) {//NOT LEADER
                cm.sendOk("If you want to try the quest, please tell the #bleader of your party#k to talk to me.");
                cm.dispose();
            }else if (checkPartyLevels() == false){//WRONG LEVELS
                cm.sendOk("Please check that all your party members are between the levels of #b" + minlvl + "~" + maxlvl)
                cm.dispose();
            }else if (checkPartySize() == false){//PARTY SIZE WRONG
                cm.sendOk("Your party does not consist of #b" + minplayers + "~" + maxplayers + "#k, therefore making you ineligible to participate in this party quest. Please adjust your party members to make fit");
                cm.dispose();
            }else if (em == null){//EVENT ERROR
                cm.sendOk("ERROR IN EVENT");
                cm.dispose();
            }else if (open == false){//MANUALLY CLOSED
                cm.sendOk("The Ludi-PQ has been #rclosed!#k");
                cm.dispose();
            }else{//CAN START EVENT
                cm.sendOk("The Ludi-PQ Is #gopen!#k");
            }
        }else if (status == 2){//START EVENT
            em.startInstance(cm.getParty(),cm.getPlayer().getMap());
            var eim = cm.getPlayer().getEventInstance();
            var party = eim.getPlayers();
            cm.removeFromParty(4001022, party);//Item to Remove
            cm.dispose();
        }
    }
}
	

function getPartySize(){
    if(cm.getPlayer().getParty() == null)
        return 0;
    return (cm.getPlayer().getParty().getMembers().size());
    
}

function isLeader(){
    if(cm.getParty() == null)
        return false;
    return cm.isLeader();
}

function checkPartySize(){
    var size = 0;
    if(cm.getPlayer().getParty() == null)
        size = 0;
    else
        size = (cm.getPlayer().getParty().getMembers().size());
    if(size < minplayers || size > maxplayers)
        return false;
    return true;
}

function checkPartyLevels(){
    var pass = true;
    var party = cm.getPlayer().getParty().getMembers();
    if(cm.getPlayer().getParty() == null)
        pass = false;
    else{
        for (var i = 0; i < party.size() && pass; i++) {
            if ((party.get(i).getLevel() < minlvl) || (party.get(i).getLevel() > maxlvl) || (party.get(i).getMapid() != cm.getMapId()))
                pass = false;
            
        }
    }
    return pass;
}

function hasParty(){
    if(cm.getPlayer().getParty() == null)
        return false;
    return true;
}

					
