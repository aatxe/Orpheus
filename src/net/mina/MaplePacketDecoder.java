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
package net.mina;

import client.MapleClient;
import tools.MapleAESOFB;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class MaplePacketDecoder extends CumulativeProtocolDecoder {
	private static final String DECODER_STATE_KEY = MaplePacketDecoder.class.getName() + ".STATE";

	private static class DecoderState {
		public int packetlength = -1;
	}

	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		final MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
		DecoderState decoderState = (DecoderState) session.getAttribute(DECODER_STATE_KEY);
		if (decoderState == null) {
			decoderState = new DecoderState();
			session.setAttribute(DECODER_STATE_KEY, decoderState);
		}
		if (in.remaining() >= 4 && decoderState.packetlength == -1) {
			int packetHeader = in.getInt();
			if (!client.getReceiveCrypto().checkPacket(packetHeader)) {
				session.close(true);
				return false;
			}
			decoderState.packetlength = MapleAESOFB.getPacketLength(packetHeader);
		} else if (in.remaining() < 4 && decoderState.packetlength == -1) {
			return false;
		}
		if (in.remaining() >= decoderState.packetlength) {
			byte decryptedPacket[] = new byte[decoderState.packetlength];
			in.get(decryptedPacket, 0, decoderState.packetlength);
			decoderState.packetlength = -1;
			client.getReceiveCrypto().crypt(decryptedPacket);
			MapleCustomEncryption.decryptData(decryptedPacket);
			out.write(decryptedPacket);
			return true;
		}
		return false;
	}
}
