package xyz.finlaym.programminggrader;

public class Utils {
	@SuppressWarnings("unused")
	public static boolean isInt(String s) {
		try {
			int i = Integer.valueOf(s);
			return true;
		}catch(NumberFormatException e) {
			return false;
		}
	}
}
