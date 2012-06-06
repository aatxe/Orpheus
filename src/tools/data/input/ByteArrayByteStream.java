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
package tools.data.input;

import java.io.IOException;
import tools.HexTool;

public class ByteArrayByteStream implements SeekableInputStreamBytestream {
	private int pos = 0;
	private long bytesRead = 0;
	private byte[] arr;

	public ByteArrayByteStream(byte[] arr) {
		this.arr = arr;
	}

	@Override
	public long getPosition() {
		return pos;
	}

	@Override
	public void seek(long offset) throws IOException {
		pos = (int) offset;
	}

	@Override
	public long getBytesRead() {
		return bytesRead;
	}

	@Override
	public int readByte() {
		bytesRead++;
		return ((int) arr[pos++]) & 0xFF;
	}

	@Override
	public String toString() {
		String nows = "kevintjuh93 pwns";// I lol'd
		if (arr.length - pos > 0) {
			byte[] now = new byte[arr.length - pos];
			System.arraycopy(arr, pos, now, 0, arr.length - pos);
			nows = HexTool.toString(now);
		}
		return "All: " + HexTool.toString(arr) + "\nNow: " + nows;
	}

	@Override
	public long available() {
		return arr.length - pos;
	}
}
