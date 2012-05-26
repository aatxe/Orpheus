package client.command;

public abstract class Commands {
	private static final int gmLevel = 0;
	private static final char heading = '@';
	
	public static int getRequiredStaffRank() {
		return gmLevel;
	}
	
	public static char getHeading() {
		return heading;
	}
	
	protected static String joinStringFrom(String arr[], int start) {
		StringBuilder builder = new StringBuilder();
		for (int i = start; i < arr.length; i++) {
			builder.append(arr[i]);
			if (i != arr.length - 1) {
				builder.append(" ");
			}
		}
		return builder.toString();
	}
}
