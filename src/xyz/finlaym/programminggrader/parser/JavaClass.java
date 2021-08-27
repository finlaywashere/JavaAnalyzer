package xyz.finlaym.programminggrader.parser;

import java.util.ArrayList;
import java.util.List;

public class JavaClass {
	private String pkg;
	private String name;
	private int line;
	private List<JavaMethod> methods;
	private List<JavaModifier> modifiers;
	private List<JavaInstruction> classData;
	public JavaClass(String name, int line, List<JavaModifier> modifiers, List<JavaInstruction> classData) {
		this.name = name;
		this.line = line;
		this.methods = new ArrayList<JavaMethod>();
		this.modifiers = modifiers;
		this.classData = classData;
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
	public List<JavaModifier> getModifiers() {
		return modifiers;
	}
	public String getPkg() {
		return pkg;
	}
	public List<JavaInstruction> getClassData() {
		return classData;
	}
	@Override
	public String toString() {
		return name;
	}
}
