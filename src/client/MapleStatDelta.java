package client;

public final class MapleStatDelta {
	public final MapleStat stat;
	public final int delta;
	
	public MapleStatDelta(MapleStat stat, int delta) {
		this.stat = stat;
		this.delta = delta;
	}
}
