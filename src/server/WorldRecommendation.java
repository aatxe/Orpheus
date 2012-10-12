package server;

public class WorldRecommendation {
	public final byte worldId;
	public final String message;
	
	public WorldRecommendation(int worldId, String message) {
		this.worldId = (byte)worldId;
		this.message = message;
	}
}
