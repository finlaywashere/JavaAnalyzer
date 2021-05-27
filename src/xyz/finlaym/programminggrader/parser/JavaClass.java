package xyz.finlaym.programminggrader.parser;

import java.util.ArrayList;
import java.util.List;

public class JavaClass {
	private String name;
	private int line;
	private List<JavaMethod> methods;
	private List<JavaModifier> modifiers;
	public JavaClass(String name, int line, List<JavaModifier> modifiers) {
		this.name = name;
		this.line = line;
		this.methods = new ArrayList<JavaMethod>();
		this.modifiers = modifiers;
	}
	public String getName() {
		return name;
	}
	public int getLine() {
		return line;
	}
	public List<JavaMethod> getMethods() {
		return methods;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setLine(int line) {
		this.line = line;
	}
	public void setMethods(List<JavaMethod> methods) {
		this.methods = methods;
	}
	public List<JavaModifier> getModifiers() {
		return modifiers;
	}
}
