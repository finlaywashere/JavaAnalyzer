package xyz.finlaym.programminggrader.analyzer;

public class AnalyzerConstants {
	/*
	 * A bunch of regexes that match classes/methods that aren't good news
	 * Methods are fed in in the format of
	 * This shouldn't trigger for just classes but instead methods ex the constructor
	 * package.class.method
	 */
	
	public static final String[] HAZARD_CLASSES = {
			// This stuff is to make sure code can't try to evade static analysis with fancy lazy loading
			// All references to these should be caught (at least method calls, might be hard to check for them as return values from methods)
			"[java.lang.reflect].*",
			"[java.lang.SecurityManager].*",
			"[java.lang.ClassLoader].*",
			"[java.lang.System.load].*",
			"[java.lang.System.getenv]",
			"[java.lang.System.getPropert].*",
			"[java.lang.System.set].*",
			
	};
	
	public static final String[] WARN_CLASSES = {
			"java.io.File.*",
			"java.net.URL.*",
			"java.net.URI.*",
			"java.net.Socket.*",
			"javax.net.ssl.SSLSocket.*",
			
	};
	
	/*
	 * These are strings that are bad to appear inside code
	 */
	public static final String[] WARN_STRINGS = {
			"user.home",
			"user.name",
			"Chrome", // Chrome and firefox are blacklisted as they are part of the path to a user's cookie files
			"Firefox",
			"AppData",
			
	};
}
