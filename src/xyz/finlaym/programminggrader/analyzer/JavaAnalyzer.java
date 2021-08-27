package xyz.finlaym.programminggrader.analyzer;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xyz.finlaym.programminggrader.parser.JavaClass;
import xyz.finlaym.programminggrader.parser.JavaFile;
import xyz.finlaym.programminggrader.parser.JavaImport;
import xyz.finlaym.programminggrader.parser.JavaInstruction;
import xyz.finlaym.programminggrader.parser.JavaInstructionType;
import xyz.finlaym.programminggrader.parser.JavaMethod;
import xyz.finlaym.programminggrader.parser.JavaToken;
import xyz.finlaym.programminggrader.parser.JavaTokenType;

public class JavaAnalyzer {
	public AnalysisResult analyze(JavaFile file) {
		Map<String,String> importTranslations = new HashMap<String,String>();
		for(JavaImport i : file.getImports()) {
			if(!i.getValue().endsWith("*")) {
				String[] split = i.getValue().split("\\.");
				String name = split[split.length-1];
				importTranslations.put(name, i.getValue());
			}
		}
		for(JavaClass c : file.getClasses()) {
			for(JavaMethod m : c.getMethods()) {
				for(JavaInstruction i : m.getInstructions()) {
					if(i.getType() == JavaInstructionType.CALL || i.getType() == JavaInstructionType.VARIABLE) {
						for(JavaToken t : i.getTokens()) {
							if(t.getType() == JavaTokenType.METHOD) {
								// Could be a class
								String data = t.getValue();
								do {
									boolean startsWithNew = data.startsWith("new ");
									String[] split = data.split("\\(");
									if(split.length > 1) {
										data = split[1].trim();
										data = data.substring(0, data.lastIndexOf(")"));
									}
									if(startsWithNew) {
										// Only checking for instanciation for now for simplicity
										// I don't want to have to deal with keeping track of variables yet
										String name = split[0].substring(4).trim();
										boolean found = false;
										for(JavaClass c1 : file.getClasses()) {
											if(c1.getName().equals(name)) {
												found = true;
												break;
											}
										}
										if(found) {
											continue;
										}
										if(!importTranslations.containsKey(name)) {
											String importS = findImport(file, name);
											importTranslations.put(name, importS);
											if(importS == null) {
												System.err.println("Failed to parse import on line "+t.getLine());
												return null;
											}
										}
									}
								}while(data.contains("("));
							}
						}
					}
				}
			}
		}
		for(String key : importTranslations.keySet()) {
			String i = importTranslations.get(key);
			String search = i+"."+key; // These are constructors
			int hazardLevel = getHazardLevelClass(search);
			switch (hazardLevel) {
			case 1:
				System.err.println("Warn: Suspicious class \""+search+"\" in use");
				break;
			case 2:
				System.err.println("Hazard: Potentially malicious class \""+search+"\" in use");
				break;
			default:
				break;
			}
		}
		return null;
	}
	private static int getHazardLevelClass(String search) {
		for(String hazard : AnalyzerConstants.HAZARD_CLASSES) {
			Pattern p = Pattern.compile(hazard);
			Matcher m = p.matcher(search);
			if(m.lookingAt())
				return 2;
		}
		for(String warn : AnalyzerConstants.WARN_CLASSES) {
			Pattern p = Pattern.compile(warn);
			Matcher m = p.matcher(search);
			if(m.lookingAt())
				return 1;
		}
		return 0;
	}
	private static String findImport(JavaFile file, String name) {
		if(classExists(name))
			return name;
		// This can't do recursive package searching yet which is going to be a nightmare
		// Ex: If you import java.* it won't be able to find java.reflect.Reflect
		for(JavaImport i : file.getImports()) {
			String value = i.getValue();
			if(!value.endsWith("*"))
				continue;
			value = value.substring(0, value.length()-2);
			String imp = value+"."+name;
			if(classExists(imp))
				return imp;
		}
		return null;
	}
	@SuppressWarnings("unused")
	private static boolean classExists(String name) {
		try {
			Class<?> c = Class.forName(name);
			return true;
		}catch(ClassNotFoundException e) {
			return false;
		}
	}
}
