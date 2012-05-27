package client.command.external;

public class CommandLoaderException extends Exception {
	private static final long serialVersionUID = -7775141752143419523L;
	private int id;
	private String message;
	
	public CommandLoaderException(int id, String message) {
		this.id = id;
		this.message = message;
	}
	
	public int getIdentifier() {
		return this.id;
	}
	
	public String getMessage() {
		return this.message;
	}
}
