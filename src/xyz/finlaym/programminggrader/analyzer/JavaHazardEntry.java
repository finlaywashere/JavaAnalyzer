package xyz.finlaym.programminggrader.analyzer;

public class JavaHazardEntry {
	private JavaHazardEntryType type;
	private String regex;
	private JavaHazardType level;
	private String common;
	
	public JavaHazardEntryType getType() {
		return type;
	}
	public String getRegex() {
		return regex;
	}
	public JavaHazardType getLevel() {
		return level;
	}
	public String getCommon() {
		return common;
	}
	public JavaHazardEntry(JavaHazardEntryType type, String regex, JavaHazardType level, String common) {
		this.type = type;
		this.regex = regex;
		this.level = level;
		this.common = common;
	}
	
}
