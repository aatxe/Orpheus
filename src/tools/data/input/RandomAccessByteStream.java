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
import java.io.RandomAccessFile;
import tools.Output;

/**
 * Provides an abstract layer to a byte stream. This layer can be accessed
 * randomly.
 * 
 * @author Frz
 * @version 1.0
 * @since Revision 323
 */
public class RandomAccessByteStream implements SeekableInputStreamBytestream {
	private RandomAccessFile raf;
	private long read = 0;

	public RandomAccessByteStream(RandomAccessFile raf) {
		super();
		this.raf = raf;
	}

	@Override
	public int readByte() {
		int temp;
		try {
			temp = raf.read();
			if (temp == -1) {
				throw new RuntimeException("EOF");
			}
			read++;
			return temp;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void seek(long offset) throws IOException {
		raf.seek(offset);
	}

	@Override
	public long getPosition() throws IOException {
		return raf.getFilePointer();
	}

	@Override
	public long getBytesRead() {
		return read;
	}

	@Override
	public long available() {
		try {
			return raf.length() - raf.getFilePointer();
		} catch (IOException e) {
			Output.print("ERROR " + e);
			return 0;
		}
	}
}
