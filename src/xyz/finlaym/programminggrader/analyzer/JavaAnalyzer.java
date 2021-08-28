package xyz.finlaym.programminggrader.analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
		List<JavaHazard> hazards = new ArrayList<JavaHazard>();
		
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
		for(JavaClass c : file.getClasses()) {
			for(JavaInstruction i : c.getClassData()) {
				for(JavaToken t : i.getTokens()) {
					if(t.getType() == JavaTokenType.DATA) {
						for(JavaToken token : getTokens(t.getValue())) {
							if(token.getType() == JavaTokenType.DATA) {
								JavaHazardType hazardLevel = getHazardLevelString(token.getValue());
								if(hazardLevel != JavaHazardType.NONE) {
									JavaHazard hazard = new JavaHazard(hazardLevel, token.getValue());
									hazards.add(hazard);
								}
							}else if(token.getType() == JavaTokenType.METHOD) {
								String cName = token.getValue();
								if(cName.startsWith("new "))
									cName = cName.substring(4);
								JavaHazardType hazardLevel = getHazardLevelClass(cName);
								if(hazardLevel != JavaHazardType.NONE) {
									JavaHazard hazard = new JavaHazard(hazardLevel, token.getValue());
									hazards.add(hazard);
								}
							}
						}
					}
				}
			}
			for(JavaMethod m : c.getMethods()) {
				for(JavaInstruction i : m.getInstructions()) {
					for(JavaToken t : i.getTokens()) {
						if(t.getType() == JavaTokenType.DATA) {
							for(JavaToken token : getTokens(t.getValue())) {
								if(token.getType() == JavaTokenType.DATA) {
									JavaHazardType hazardLevel = getHazardLevelString(token.getValue());
									if(hazardLevel != JavaHazardType.NONE) {
										JavaHazard hazard = new JavaHazard(hazardLevel, token.getValue());
										hazards.add(hazard);
									}
								}else if(token.getType() == JavaTokenType.METHOD) {
									String cName = token.getValue();
									if(cName.startsWith("new "))
										cName = cName.substring(4);
									// Only check against whole thing because this will only work if its not imported eg
									// java.io.File instead of File
									// this is because File requires java.io.File to be imported which will trigger the import translations
									//TODO: Fix this so that importing something like java.io.* won't bypass this
									JavaHazardType hazardLevel = getHazardLevelClass(cName);
									if(hazardLevel != JavaHazardType.NONE) {
										JavaHazard hazard = new JavaHazard(hazardLevel, token.getValue());
										hazards.add(hazard);
									}
								}
							}
						}
					}
				}
			}
		}
		for(String key : importTranslations.keySet()) {
			String i = importTranslations.get(key);
			String search = i+"."+key; // These are constructors
			JavaHazardType hazardLevel = getHazardLevelClass(search);
			if(hazardLevel != JavaHazardType.NONE) {
				JavaHazard hazard = new JavaHazard(hazardLevel, search);
				hazards.add(hazard);
			}
		}
		AnalysisResult result = new AnalysisResult(hazards);
		return result;
	}
	private static List<JavaToken> getTokens(String s){
		List<JavaToken> tokens = new ArrayList<JavaToken>();
		int lastI = 0;
		for(int i = 0; i < s.length(); i++) {
			if(s.toCharArray()[i] == '(') {
				String s1 = s.substring(lastI, i);
				tokens.add(new JavaToken(JavaTokenType.METHOD, 0, s1));
				lastI = i + 1;
			}else if(s.toCharArray()[i] == ')') {
				String s1 = s.substring(lastI, i);
				tokens.add(new JavaToken(JavaTokenType.DATA, 0, s1));
				lastI = i + 1;
			}
		}
		return tokens;
	}
	private static JavaHazardType getHazardLevelClass(String search) {
		for(String hazard : AnalyzerConstants.HAZARD_CLASSES) {
			Pattern p = Pattern.compile(hazard);
			Matcher m = p.matcher(search);
			if(m.lookingAt())
				return JavaHazardType.HAZARD;
		}
		for(String warn : AnalyzerConstants.WARN_CLASSES) {
			Pattern p = Pattern.compile(warn);
			Matcher m = p.matcher(search);
			if(m.lookingAt())
				return JavaHazardType.WARN;
		}
		return JavaHazardType.NONE;
	}
	private static JavaHazardType getHazardLevelString(String search) {
		search = search.replaceAll("\"", "");
		for(String hazard : AnalyzerConstants.WARN_STRINGS) {
			Pattern p = Pattern.compile(hazard);
			Matcher m = p.matcher(search);
			if(m.lookingAt())
				return JavaHazardType.DATA;
		}
		return JavaHazardType.NONE;
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
