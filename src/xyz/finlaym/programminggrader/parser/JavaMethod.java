package xyz.finlaym.programminggrader.parser;

import java.util.ArrayList;
import java.util.List;

public class JavaMethod {
	private List<JavaInstruction> instructions;
	private String name;
	private int line;
	private List<JavaModifier> modifiers;
	private List<JavaArgument> arguments;
	private JavaToken returnType;
	
	public JavaMethod(String name, int line, List<JavaModifier> modifiers, List<JavaArgument> arguments, JavaToken returnType) {
		this.instructions = new ArrayList<JavaInstruction>();
		this.name = name;
		this.line = line;
		this.modifiers = modifiers;
		this.arguments = arguments;
		this.returnType = returnType;
	}
	public List<JavaInstruction> getInstructions() {
		return instructions;
	}
	public String getName() {
		return name;
	}
	public int getLine() {
		return line;
	}
	public void setInstructions(List<JavaInstruction> instructions) {
		this.instructions = instructions;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setLine(int line) {
		this.line = line;
	}
	public List<JavaModifier> getModifiers() {
		return modifiers;
	}
	public List<JavaArgument> getArguments() {
		return arguments;
	}
	public JavaToken getReturnType() {
		return returnType;
	}
}
