package server;

import client.MapleBuffStat;

public final class MapleBuffStatDelta {
	public final MapleBuffStat stat;
	public final int delta;
	
	public MapleBuffStatDelta(MapleBuffStat stat, int delta) {
		this.stat = stat;
		this.delta = delta;
	}
}
