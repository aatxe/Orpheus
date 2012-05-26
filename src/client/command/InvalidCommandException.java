package client.command;

public class InvalidCommandException extends Exception {
	private static final long serialVersionUID = -7775141752143419523L;
	private int id;
	private String message;
	
	public InvalidCommandException(int id, String message) {
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
