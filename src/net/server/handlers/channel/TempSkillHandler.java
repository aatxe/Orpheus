/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.server.handlers.channel;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Kevin
 */
public final class TempSkillHandler extends AbstractMaplePacketHandler {

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.announce(MaplePacketCreator.temporarySkills());
    }

}
