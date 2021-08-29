package xyz.finlaym.programminggrader.analyzer;

public class JavaHazard {
	private JavaHazardType type;
	private String data;
	private JavaHazardEntry entry;
	private int line;
	
	public JavaHazard(JavaHazardType type, String data, int line, JavaHazardEntry entry) {
		this.type = type;
		this.data = data;
		this.line = line;
		this.entry = entry;
	}
	public JavaHazardType getType() {
		return type;
	}
	public String getData() {
		return data;
	}
	public int getLine() {
		return line;
	}
	public JavaHazardEntry getEntry() {
		return entry;
	}
	
}
