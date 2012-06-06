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
By kevintjuh93
*/
function enter(pi) { 
	if (pi.getAranIntroState("cmd=o")) {
		pi.blockPortal();
		return false;
	}
	pi.updateAranIntroState("cmd=o;normal=o;arr0=o;arr1=o;arr2=o;mo1=o;chain=o;mo2=o;mo3=o;mo4=o");
    pi.blockPortal();
    pi.message("You can use a Command Attack by pressing both the arrow key and the attack key after a Consecutive Attack.");
    pi.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialGuide3");   
    return true; 
}  