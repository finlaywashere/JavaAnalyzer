package xyz.finlaym.programminggrader.analyzer;

public class AnalyzerConstants {
	/*
	 * A bunch of regexes that match classes/methods/strings that aren't good news
	 *
	 */
	
	public static final JavaHazardEntry[] HAZARDS = {
			// This stuff is to make sure code can't try to evade static analysis with fancy lazy loading
			// All references to these should be caught (at least method calls, might be hard to check for them as return values from methods)
			new JavaHazardEntry(JavaHazardEntryType.CLASS,"java.lang.reflect",JavaHazardType.HAZARD,"reflect"),
			new JavaHazardEntry(JavaHazardEntryType.CLASS,"java.lang.SecurityManager",JavaHazardType.HAZARD,"securitymanager"),
			new JavaHazardEntry(JavaHazardEntryType.CLASS,"java.lang.ClassLoader",JavaHazardType.HAZARD,"classloader"),
			new JavaHazardEntry(JavaHazardEntryType.CLASS,"java.lang.System.load",JavaHazardType.HAZARD,"sysload"),
			new JavaHazardEntry(JavaHazardEntryType.CLASS,"java.lang.System.getenv",JavaHazardType.WARN,"env"),
			new JavaHazardEntry(JavaHazardEntryType.CLASS,"java.lang.System.getPropert",JavaHazardType.WARN,"getprop"),
			new JavaHazardEntry(JavaHazardEntryType.CLASS,"java.lang.System.set",JavaHazardType.WARN,"setprop"),
			new JavaHazardEntry(JavaHazardEntryType.CLASS,"java.io.File",JavaHazardType.IO,"file"),
			new JavaHazardEntry(JavaHazardEntryType.CLASS,"java.net.URL",JavaHazardType.HAZARD,"url"),
			new JavaHazardEntry(JavaHazardEntryType.CLASS,"java.net.URI",JavaHazardType.HAZARD,"uri"),
			new JavaHazardEntry(JavaHazardEntryType.CLASS,"java.net.Socket",JavaHazardType.HAZARD,"socket"),
			new JavaHazardEntry(JavaHazardEntryType.CLASS,"javax.net.ssl.SSLSocket",JavaHazardType.HAZARD,"sslsocket"),
			new JavaHazardEntry(JavaHazardEntryType.STRING,"user.home",JavaHazardType.WARN,"homedir"),
			new JavaHazardEntry(JavaHazardEntryType.STRING,"user.name",JavaHazardType.WARN,"username"),
			new JavaHazardEntry(JavaHazardEntryType.STRING,"Chrome",JavaHazardType.HAZARD,"chrome"), // Chrome and firefox are blacklisted as they are part of the path to a user's cookie files
			new JavaHazardEntry(JavaHazardEntryType.STRING,"Firefox",JavaHazardType.HAZARD,"firefox"),
			new JavaHazardEntry(JavaHazardEntryType.STRING,"AppData",JavaHazardType.HAZARD,"appdata"),
	};
}
