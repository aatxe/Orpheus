package gm;

import org.apache.mina.core.session.IoSession;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Kevin
 */
public interface GMPacketHandler {

    void handlePacket(SeekableLittleEndianAccessor slea, IoSession session);
}
