package client;

public final class ItemInventoryEntry {
	public final IItem item;
	public final MapleInventoryType type;
	
	public ItemInventoryEntry(IItem item, MapleInventoryType type) {
		this.item = item;
		this.type = type;
	}
}
