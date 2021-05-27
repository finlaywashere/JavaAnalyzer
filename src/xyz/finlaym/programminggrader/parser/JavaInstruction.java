package xyz.finlaym.programminggrader.parser;

import java.util.List;

public class JavaInstruction {
	private JavaInstructionType type;
	private List<JavaToken> tokens;
	private int line;
	public JavaInstruction(JavaInstructionType type, List<JavaToken> tokens, int line) {
		this.type = type;
		this.tokens = tokens;
		this.line = line;
	}
	public JavaInstructionType getType() {
		return type;
	}
	public List<JavaToken> getTokens() {
		return tokens;
	}
	public int getLine() {
		return line;
	}
}
