package gm.server.handler;

import gm.GMPacketHandler;
import net.server.Server;
import org.apache.mina.core.session.IoSession;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author kevintjuh93
 */
public class ChatHandler implements GMPacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, IoSession session) {
        Server.getInstance().gmChat(slea.readMapleAsciiString(), (String) session.getAttribute("NAME"));
    }
    
}
