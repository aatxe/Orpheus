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

package client.autoban;

import client.MapleCharacter;

/**
 *
 * @author kevintjuh93
 */
public enum AutobanFactory {
    MOB_COUNT,
    FIX_DAMAGE,
    HIGH_HP_HEALING,
    FAST_HP_HEALING(15),
    FAST_MP_HEALING(15),
    GACHA_EXP,
    TUBI(20, 15000),
    SHORT_ITEM_VAC,
    ITEM_VAC,
    FAST_ATTACK(10, 30000),
    MPCON(25, 30000);
    
    private int points;
    private long expiretime;

    private AutobanFactory() {
        this(1, -1);
    }

    private AutobanFactory(int points) {
        this.points = points;
        this.expiretime = -1;
    }

    private AutobanFactory(int points, long expire) {
        this.points = points;
        this.expiretime = expire;
    }

    public int getMaximum() {
        return points;
    }

    public long getExpire() {
        return expiretime;
    }

    public void addPoint(AutobanManager ban, String reason) {
        ban.addPoint(this, reason);
    }

    public void autoban(MapleCharacter chr, String value) {
        chr.autoban("Autobanned for (" + this.name() + ": " + value + ")", 1);
        chr.sendPolice("You have been blocked by #bMooplePolice#k for the HACK reason.");
    }
}
