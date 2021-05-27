package xyz.finlaym.programminggrader.parser;

public class JavaImport {
	private String value;
	private int line;
	public JavaImport(String value, int line) {
		this.value = value;
		this.line = line;
	}
	public String getValue() {
		return value;
	}
	public int getLine() {
		return line;
	}
}
