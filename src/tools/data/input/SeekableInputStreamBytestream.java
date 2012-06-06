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

/**
 * Provides an abstract interface to a stream of bytes. This stream can be
 * seeked.
 * 
 * @author Frz
 * @version 1.0
 * @since 299
 */
public interface SeekableInputStreamBytestream extends ByteInputStream {
	/**
	 * Seeks the stream by the specified offset.
	 * 
	 * @param offset
	 *            Number of bytes to seek.
	 * @throws IOException
	 */
	void seek(long offset) throws IOException;

	/**
	 * Gets the current position of the stream.
	 * 
	 * @return The stream position as a long integer.
	 * @throws IOException
	 */
	long getPosition() throws IOException;
}
