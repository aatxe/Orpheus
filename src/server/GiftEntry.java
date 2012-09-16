package server;

import client.IItem;

public final class GiftEntry {
	public final IItem item;
	public final String message;
	
	public GiftEntry(IItem item, String message) {
		this.item = item;
		this.message = message;
	}
}
