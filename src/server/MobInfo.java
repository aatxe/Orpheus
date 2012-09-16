package server;

public class MobInfo {

	public final int boss;
	public final int rareItemDropLevel;
	public final String name;

	public MobInfo(int boss, int rareItemDropLevel, String name) {
		this.boss = boss;
		this.rareItemDropLevel = rareItemDropLevel;
		this.name = name;
	}
}