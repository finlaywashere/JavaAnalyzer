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
										// Only checking for instantiation for now for simplicity
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
											String importS = findPackage(name,file,c);
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
								String tmp = findPackage(cName, file, c);
								if(tmp != null)
									cName = tmp;
								else {
									tmp = findVariableType(cName,c,null,file);
									if(tmp != null)
										cName = tmp;
									else {
										System.err.println("Error: Failed to find type for data at line "+i.getLine());
										continue;
									}
								}
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
									String tmp = findPackage(cName, file, c);
									if(tmp != null)
										cName = tmp;
									else {
										tmp = findVariableType(cName,c,m,file);
										if(tmp != null)
											cName = tmp;
										else {
											System.err.println("Error: Failed to find type for data at line "+i.getLine());
											continue;
										}
									}
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
	private static String findVariableType(String c, JavaClass curr, JavaMethod method, JavaFile file) {
		String name = getFirstDot(c);
		if(method != null) {
			String fqn = getFQN(method.getInstructions(), file, name, curr);
			if(fqn != null)
				return stripLastDot(fqn);
		}
		String fqn = getFQN(curr.getClassData(), file, name, curr);
		if(fqn != null)
			return stripLastDot(fqn);
		return null;
	}
	private static String getFQN(List<JavaInstruction> instructions, JavaFile file, String name, JavaClass curr) {
		for(JavaInstruction inst : instructions) {
			if(inst.getType() == JavaInstructionType.VARIABLE) {
				String vName = null;
				String vType = null;
				for(JavaToken t : inst.getTokens()) {
					if(t.getType() == JavaTokenType.NAME) {
						vName = t.getValue();
					}else if(t.getType() == JavaTokenType.VARIABLE) {
						vType = t.getValue();
					}
				}
				if(vName != null && name.equals(vName)) {
					String fqn = findPackage(vType, file, curr);
					return fqn;
				}
			}
		}
		return null;
	}
	private static String findPackage(String c, JavaFile f, JavaClass curr) {
		if(classExists(c))
			return c+"."+getLastDot(c);
		String withoutLast = stripLastDot(c);
		if(classExists(withoutLast))
			return c;
		for(JavaClass cl : f.getClasses()) {
			if(cl.getFQN().equals(c))
				return c+"."+getLastDot(c);
			if(cl.getFQN().equals(withoutLast))
				return c;
			if(cl.getName().equals(c))
				return cl.getFQN()+"."+c;
			if(cl.getName().equals(withoutLast))
				return cl.getFQN()+"."+c;
		}
		// By here the class is definitely not in the format package.class or package.class.method
		String pkg = curr.getPkg();
		for(JavaClass cl : f.getClasses()) {
			if(cl.getPkg().equals(pkg) && cl.getName().equals(c))
				return cl.getPkg()+"."+c+"."+getLastDot(c);
			if(cl.getPkg().equals(pkg) && cl.getName().equals(withoutLast))
				return cl.getPkg()+"."+c;
		}
		// By here the class is definitely not in the same package and is in the format of class or class.method
		for(JavaImport imp : f.getImports()) {
			String value = imp.getValue();
			if(!value.endsWith("*")) {
				String last = getLastDot(value);
				if(last.equals(c) || last.equals(withoutLast))
					return value;
			}else {
				value = stripLastDot(value);
				// Search through package like a pleb
				if(classExists(value+"."+c))
					return value+"."+c+"."+getLastDot(c);
				if(classExists(value+"."+withoutLast))
					return value+"."+c;
			}
		}
		return null; // No clue here
	}
	private static String stripLastDot(String s) {
		String[] split = s.split("\\.");
		String ret = "";
		for(int i = 0; i < split.length-1; i++) {
			ret += "."+split[i];
		}
		if(ret.length() > 0)
			ret = ret.substring(1);
		return ret;
	}
	private static String getLastDot(String s) {
		String[] split = s.split("\\.");
		return split[split.length-1];
	}
	private static String getFirstDot(String s) {
		String[] split = s.split("\\.");
		return split[0];
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
