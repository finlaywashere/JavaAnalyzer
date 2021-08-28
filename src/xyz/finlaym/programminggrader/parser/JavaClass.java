package xyz.finlaym.programminggrader.parser;

import java.util.ArrayList;
import java.util.List;

public class JavaClass {
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
		String pkg = null;
		for(JavaInstruction inst : classData) {
			if(inst.getType() == JavaInstructionType.DEFINITION) {
				boolean found = false;
				String tmp = null;
				for(JavaToken t : inst.getTokens()) {
					if(t.getType() == JavaTokenType.MODIFIER && t.getValue().equals("package")) {
						found = true;
					}else if(t.getType() == JavaTokenType.DATA)
						tmp = t.getValue();
				}
				if(found) {
					pkg = tmp;
					break;
				}
			}
		}
		return pkg;
	}
	public List<JavaInstruction> getClassData() {
		return classData;
	}
	@Override
	public String toString() {
		return name;
	}
	public String getFQN() {
		String pkg = getPkg();
		String ret = name;
		if(pkg != null)
			ret = pkg+"."+ret;
		return ret;
	}
}
