/*
 	OrpheusMS: MapleStory Private Server based on OdinMS
    Copyright (C) 2012 Aaron Weiss

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
package client;

public class BuddylistEntry {
	private String name;
	private String group;
	private int cid;
	private byte channel;
	private boolean visible;

	/**
	 * 
	 * @param name
	 * @param characterId
	 * @param channel
	 *            should be -1 if the buddy is offline
	 * @param visible
	 */
	public BuddylistEntry(String name, String group, int characterId, byte channel, boolean visible) {
		this.name = name;
		this.group = group;
		this.cid = characterId;
		this.channel = channel;
		this.visible = visible;
	}

	/**
	 * @return the channel the character is on. If the character is offline
	 *         returns -1.
	 */
	public byte getChannel() {
		return channel;
	}

	public void setChannel(byte channel) {
		this.channel = channel;
	}

	public boolean isOnline() {
		return channel >= 0;
	}

	public void setOffline() {
		channel = -1;
	}

	public String getName() {
		return name;
	}

	public String getGroup() {
		return group;
	}

	public int getCharacterId() {
		return cid;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isVisible() {
		return visible;
	}

	public void changeGroup(String group) {
		this.group = group;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cid;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final BuddylistEntry other = (BuddylistEntry) obj;
		if (cid != other.cid) {
			return false;
		}
		return true;
	}
}
