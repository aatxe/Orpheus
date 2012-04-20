package gm.server.handler;

import client.MapleCharacter;
import gm.GMPacketCreator;
import gm.GMPacketHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.server.Channel;
import net.server.Server;
import org.apache.mina.core.session.IoSession;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author kevintjuh93
 */
public class PlayerListHandler implements GMPacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, IoSession session) {
        List<String> playerList = new ArrayList<String>();
        for (Channel ch : Server.getInstance().getAllChannels()) {
            Collection<MapleCharacter> list = ch.getPlayerStorage().getAllCharacters();
            synchronized (list) {
                for (MapleCharacter chr : list) {
                    if (!chr.isGM()) {
                        playerList.add(chr.getName());
                    }
                }
            }
        }
        session.write(GMPacketCreator.sendPlayerList(playerList));
    }
}
