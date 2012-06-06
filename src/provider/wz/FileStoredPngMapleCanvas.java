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
package provider.wz;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import provider.MapleCanvas;

public class FileStoredPngMapleCanvas implements MapleCanvas {
	private File file;
	private int width;
	private int height;
	private BufferedImage image;

	public FileStoredPngMapleCanvas(int width, int height, File fileIn) {
		this.width = width;
		this.height = height;
		this.file = fileIn;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public BufferedImage getImage() {
		loadImageIfNecessary();
		return image;
	}

	private void loadImageIfNecessary() {
		if (image == null) {
			try {
				image = ImageIO.read(file);
				// replace the dimensions loaded from the wz by the REAL
				// dimensions from the image - should be equal tho
				width = image.getWidth();
				height = image.getHeight();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
