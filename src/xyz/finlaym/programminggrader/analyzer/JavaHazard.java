package xyz.finlaym.programminggrader.analyzer;

public class JavaHazard {
	private JavaHazardType type;
	private String data;
	private int line;
	
	public JavaHazard(JavaHazardType type, String data, int line) {
		this.type = type;
		this.data = data;
		this.line = line;
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
}
