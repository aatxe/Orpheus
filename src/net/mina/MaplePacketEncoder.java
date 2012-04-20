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
package net.mina;

import client.MapleClient;
import net.MaplePacket;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class MaplePacketEncoder implements ProtocolEncoder {
    public synchronized void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        final MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        if (client != null) {
            final byte[] input = ((MaplePacket) message).getBytes();
            final byte[] unencrypted = new byte[input.length];
            System.arraycopy(input, 0, unencrypted, 0, input.length);
            final byte[] ret = new byte[unencrypted.length + 4];
            final byte[] header = client.getSendCrypto().getPacketHeader(unencrypted.length);
            MapleCustomEncryption.encryptData(unencrypted);
            synchronized (client.getSendCrypto()) {
                client.getSendCrypto().crypt(unencrypted);
                System.arraycopy(header, 0, ret, 0, 4);
                System.arraycopy(unencrypted, 0, ret, 4, unencrypted.length);
                IoBuffer out_buffer = IoBuffer.wrap(ret);
                out.write(out_buffer);
            }
        } else // no client object created yet, send unencrypted (hello)
        {
            out.write(IoBuffer.wrap(((MaplePacket) message).getBytes()));
        }
    }

    public void dispose(IoSession session) throws Exception {
    }
}
