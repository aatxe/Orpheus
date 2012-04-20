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
package net;

import client.MapleClient;
import constants.ServerConstants;
import net.server.Server;
import tools.MapleAESOFB;
import tools.MaplePacketCreator;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericSeekableLittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import tools.PrintError;

public class MapleServerHandler extends IoHandlerAdapter {

    private PacketProcessor processor;
    private byte world = -1, channel = -1;

    public MapleServerHandler(PacketProcessor processor) {
        this.processor = processor;
    }

    public MapleServerHandler(PacketProcessor processor, byte channel, byte world) {
        this.processor = processor;
        this.channel = channel;
        this.world = world;
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        Runnable r = ((MaplePacket) message).getOnSend();
        if (r != null) {
            r.run();
        }
        super.messageSent(session, message);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        /*synchronized (session) {
            MapleClient client = ((MapleClient) session.getAttribute(MapleClient.CLIENT_KEY));
            if (client != null) {
                client.disconnect();
            }
        }*/
        session.close(true);
        PrintError.print(PrintError.EXCEPTION_CAUGHT, cause);
        //sessionClosed should be called
    }

    @Override
    public void sessionOpened(IoSession session) {
        if (!Server.getInstance().isOnline()) {
            session.close(true);
            return;
        }
        if (channel > -1 && world > -1) {
            if (Server.getInstance().getChannel(world, channel) == null) {
                session.close(true);
                return;
            }
        } else {
            System.out.println("IoSession with " + session.getRemoteAddress() + " opened.");
        }

        byte key[] = {0x13, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x06, 0x00, 0x00, 0x00, (byte) 0xB4, 0x00, 0x00, 0x00, 0x1B, 0x00, 0x00, 0x00, 0x0F, 0x00, 0x00, 0x00, 0x33, 0x00, 0x00, 0x00, 0x52, 0x00, 0x00, 0x00};
        byte ivRecv[] = {70, 114, 122, 82};
        byte ivSend[] = {82, 48, 120, 115};
        ivRecv[3] = (byte) (Math.random() * 255);
        ivSend[3] = (byte) (Math.random() * 255);
        MapleAESOFB sendCypher = new MapleAESOFB(key, ivSend, (short) (0xFFFF - ServerConstants.VERSION));
        MapleAESOFB recvCypher = new MapleAESOFB(key, ivRecv, (short) ServerConstants.VERSION);
        MapleClient client = new MapleClient(sendCypher, recvCypher, session);
        client.setWorld(world);
        client.setChannel(channel);
        session.write(MaplePacketCreator.getHello(ServerConstants.VERSION, ivSend, ivRecv));
        session.setAttribute(MapleClient.CLIENT_KEY, client);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        synchronized (session) {
            MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
            if (client != null) {
                try {
                    client.disconnect();                    
                } finally {
                    session.removeAttribute(MapleClient.CLIENT_KEY);  
                    client.empty();
                }
            }
        }
        super.sessionClosed(session);
    }

    @Override
    public void messageReceived(IoSession session, Object message) {
        byte[] content = (byte[]) message;
        SeekableLittleEndianAccessor slea = new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream(content));
        short packetId = slea.readShort();
        MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        MaplePacketHandler packetHandler = processor.getHandler(packetId);

        if (packetHandler != null && packetHandler.validateState(client)) {
            try {
                packetHandler.handlePacket(slea, client);
            } catch (Throwable t) {
            }
        }
    }

    @Override
    public void sessionIdle(final IoSession session, final IdleStatus status) throws Exception {
        MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        if (client != null) {
            client.sendPing();
        }
        super.sessionIdle(session, status);
    }
}
