package client;

public enum MapleRank {
	PLAYER(0), DONOR(1), SUPPORT(2), GM(3), DEVELOPER(4), ADMINISTRATOR(5);
	
	private int rankid;
	
	private MapleRank(int rankid) {
		this.rankid = rankid;
	}
	
	public int getId() {
		return rankid;
	}
	
	public static MapleRank getById(int id) {
		for (MapleRank l : MapleRank.values()) {
			if (l.getId() == id) {
				return l;
			}
		}
		return null;
	}

	public boolean isA(MapleRank maplerank) {
		return getId() == maplerank.getId();
	}
	
	public String toString() {
		switch (this) {
			case PLAYER:
				return "Player";
			case DONOR:
				return "Donator";
			case SUPPORT:
				return "Support";
			case GM:
				return "GM";
			case DEVELOPER:
				return "Developer";
			case ADMINISTRATOR:
				return "Administrator";
			default:
				return "Player";
		}
	}
}
