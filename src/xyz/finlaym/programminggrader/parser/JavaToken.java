package xyz.finlaym.programminggrader.parser;

public class JavaToken {
	private JavaTokenType type;
	private int line;
	private String value;
	public JavaToken(JavaTokenType type, int line, String value) {
		this.type = type;
		this.line = line;
		this.value = value;
	}
	public JavaTokenType getType() {
		return type;
	}
	public int getLine() {
		return line;
	}
	public String getValue() {
		return value;
	}
	@Override
	public String toString() {
		// This makes debugging sooo much easier
		return value;
	}
}
