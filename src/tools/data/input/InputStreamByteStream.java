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
import java.io.InputStream;
import tools.Output;

/**
 * Provides an abstract wrapper to a stream of bytes.
 * 
 * @author Frz
 * @version 1.0
 * @since Revision 323
 */
public class InputStreamByteStream implements ByteInputStream {
	private InputStream is;
	private long read = 0;

	/**
	 * Class constructor. Provide an input stream to wrap this around.
	 * 
	 * @param is
	 *            The input stream to wrap this object around.
	 */
	public InputStreamByteStream(InputStream is) {
		this.is = is;
	}

	/**
	 * Reads the next byte from the stream.
	 * 
	 * @return Then next byte in the stream.
	 */
	@Override
	public int readByte() {
		int temp;
		try {
			temp = is.read();
			if (temp == -1) {
				throw new RuntimeException("EOF");
			}
			read++;
			return temp;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets the number of bytes read from the stream.
	 * 
	 * @return The number of bytes read as a long integer.
	 */
	@Override
	public long getBytesRead() {
		return read;
	}

	/**
	 * Returns the number of bytes left in the stream.
	 * 
	 * @return The number of bytes available for reading as a long integer.
	 */
	@Override
	public long available() {
		try {
			return is.available();
		} catch (IOException e) {
			Output.print("ERROR" + e);
			return 0;
		}
	}
}
