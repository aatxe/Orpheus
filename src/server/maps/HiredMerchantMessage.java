package server.maps;

public final class HiredMerchantMessage {
	public final byte slot;
	public final String message;
	
	public HiredMerchantMessage(String message, byte slot) {
		this.slot = slot;
		this.message = message;
	}
}
