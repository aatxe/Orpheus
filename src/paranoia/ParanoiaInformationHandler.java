package paranoia;

import constants.ParanoiaConstants;

public class ParanoiaInformationHandler {
	public static String getParanoiaVersion() {
		return "Paranoia v" + ParanoiaConstants.PARANOIA_VERSION;
	}
	
	public static boolean getValue(String constant) throws IllegalArgumentException {
		return ParanoiaInformationHandler.getValue(ParanoiaInformation.valueOf(constant));
	}
	
	public static boolean getValue(ParanoiaInformation pi) {
		return pi.get();
	}
	
	public static String getExplanation(String constant) throws IllegalArgumentException {
		return ParanoiaInformationHandler.getExplanation(ParanoiaInformation.valueOf(constant));
	}
	
	public static String getExplanation(ParanoiaInformation pi) {
		return pi.explain();
	}
	
	public static String getFormattedValue(String constant) throws IllegalArgumentException {
		return ParanoiaInformationHandler.getFormattedValue(ParanoiaInformation.valueOf(constant));
	}
	
	public static String getFormattedValue(ParanoiaInformation pi) {
		switch (pi) {
			default:
				return "Paranoia " + ((pi.get()) ? "uses" : "doesn't use") + " " + pi.toUsesString() + ".";
			case resetlogs:
				return "Paranoia " + ((pi.get()) ? "clears" : "doesn't clear") + " logs on startup.";
			case exactconsole:
				return "Paranoia " + ((pi.get()) ? "replicates" : "doesn't replicate") + " the console exactly.";
		}
	}
}
