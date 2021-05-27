package xyz.finlaym.programminggrader.parser;

import java.util.List;

public class JavaFile {
	private List<JavaClass> classes;
	private List<JavaImport> imports;

	public JavaFile(List<JavaClass> classes, List<JavaImport> imports) {
		this.classes = classes;
		this.imports = imports;
	}

	public List<JavaImport> getImports() {
		return imports;
	}

	public List<JavaClass> getClasses() {
		return classes;
	}
}
