package net.server.handlers.login;

import java.net.InetAddress;
import java.net.UnknownHostException;
import net.server.Server;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RegisterPicHandler extends AbstractMaplePacketHandler {
    private static Logger log = LoggerFactory.getLogger(RegisterPicHandler.class);
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readByte();
        int charId = slea.readInt();
	String macs = slea.readMapleAsciiString();
        c.updateMacs(macs);
        if (c.hasBannedMac()) {
            c.getSession().close(true);
            return;
	}
        slea.readMapleAsciiString();
        String pic = slea.readMapleAsciiString();
        if (c.getPic() == null || c.getPic().equals("")) {
            c.setPic(pic);
            try {
                if (c.getIdleTask() != null) {
                    c.getIdleTask().cancel(true);
                }
                c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
		String channelServerIP = MapleClient.getChannelServerIPFromSubnet(c.getSession().getRemoteAddress().toString().replace("/", "").split(":")[0], c.getChannel());
		if (channelServerIP.equals("0.0.0.0")) {
                    String[] socket = Server.getInstance().getIP(c.getWorld(), c.getChannel()).split(":");
                    c.announce(MaplePacketCreator.getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
                } else {
                    String[] socket = Server.getInstance().getIP(c.getWorld(), c.getChannel()).split(":");
                    c.announce(MaplePacketCreator.getServerIP(InetAddress.getByName(channelServerIP), Integer.parseInt(socket[1]), charId));
		}
            } catch (UnknownHostException e) {
		log.error("Host not found", e);
            }
       } else {
            c.getSession().close(true);
       }
    }
}