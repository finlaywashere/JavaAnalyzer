package xyz.finlaym.programminggrader.parser;

public class JavaArgument {
	private JavaToken type;
	private JavaToken value;
	public JavaArgument(JavaToken type, JavaToken value) {
		this.type = type;
		this.value = value;
	}
	public JavaToken getType() {
		return type;
	}
	public JavaToken getValue() {
		return value;
	}
}
