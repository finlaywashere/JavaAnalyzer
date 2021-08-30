package xyz.finlaym.programminggrader.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xyz.finlaym.programminggrader.parser.JavaArgument;
import xyz.finlaym.programminggrader.parser.JavaClass;
import xyz.finlaym.programminggrader.parser.JavaFile;
import xyz.finlaym.programminggrader.parser.JavaImport;
import xyz.finlaym.programminggrader.parser.JavaInstruction;
import xyz.finlaym.programminggrader.parser.JavaInstructionType;
import xyz.finlaym.programminggrader.parser.JavaMethod;
import xyz.finlaym.programminggrader.parser.JavaToken;
import xyz.finlaym.programminggrader.parser.JavaTokenType;

public class JavaAnalyzer {
	public AnalysisResult analyze(JavaFile file, List<JavaHazardEntry> entries) {
		List<JavaHazard> hazards = new ArrayList<JavaHazard>();
		List<JavaToken> failedTokens = new ArrayList<JavaToken>();
		int totalTokens = 0;
		AnalyzedFile aFile = new AnalyzedFile();

		for (JavaClass c : file.getClasses()) {
			for (JavaInstruction i : c.getClassData()) {
				JavaToken name = null;
				for (JavaToken t : i.getTokens()) {
					if(t.getType() == JavaTokenType.NAME) {
						name = t;
						break;
					}
				}
				for (JavaToken t : i.getTokens()) {
					AnalysisResult r = getHazards(t, file, c, null, i,entries,name);
					hazards.addAll(r.getHazards());
					totalTokens += r.getTotalTokens();
					failedTokens.addAll(r.getFailedTokens());
					aFile.addAll(r.getResult());
				}
			}
			for (JavaMethod m : c.getMethods()) {
				for (JavaInstruction i : m.getInstructions()) {
					JavaToken name = null;
					for (JavaToken t : i.getTokens()) {
						if(t.getType() == JavaTokenType.NAME) {
							name = t;
							break;
						}
					}
					for (JavaToken t : i.getTokens()) {
						AnalysisResult r = getHazards(t, file, c, m, i,entries,name);
						hazards.addAll(r.getHazards());
						totalTokens += r.getTotalTokens();
						failedTokens.addAll(r.getFailedTokens());
						aFile.addAll(r.getResult());
					}
				}
				for(JavaArgument a : m.getArguments()) {
					AnalysisResult r = getHazards(a.getType(), file, c, m, null,entries, a.getValue());
					hazards.addAll(r.getHazards());
					totalTokens += r.getTotalTokens();
					failedTokens.addAll(r.getFailedTokens());
					aFile.addAll(r.getResult());
				}
			}
		}

		AnalysisResult result = new AnalysisResult(hazards,totalTokens,failedTokens,aFile);
		return result;
	}

	private static AnalysisResult getHazards(JavaToken t, JavaFile file, JavaClass c, JavaMethod m, JavaInstruction i, List<JavaHazardEntry> entries, JavaToken extra) {
		List<JavaHazard> hazards = new ArrayList<JavaHazard>();
		List<JavaToken> failedTokens = new ArrayList<JavaToken>();
		int totalTokens = 0;
		AnalyzedFile aFile = new AnalyzedFile();
		if (t.getType() == JavaTokenType.DATA) {
			List<JavaToken> tokens = getTokens(t.getValue());
			for (JavaToken token : tokens) {
				totalTokens++;
				if(token.getValue().equals("")) continue;
				if (token.getType() == JavaTokenType.DATA) {
					JavaHazardEntry hazardLevel = getHazardLevelString(token.getValue(),entries);
					if (hazardLevel != null && hazardLevel.getType() != JavaHazardEntryType.FAILURE) {
						JavaHazard hazard = new JavaHazard(hazardLevel.getLevel(), token.getValue(), t.getLine(),hazardLevel);
						hazards.add(hazard);
					}
					if(hazardLevel != null && hazardLevel.getType() == JavaHazardEntryType.FAILURE)
						failedTokens.add(token);
				} else if (token.getType() == JavaTokenType.METHOD) {
					String cName = token.getValue().replaceAll("\\!", "");
					boolean found = false;
					for(JavaMethod m1 : c.getMethods()) {
						String mName = m1.getName();
						if(mName.equals(cName)) {
							found = true;
							break;
						}
					}
					if(found)
						continue;

					AnalyzedHazard hFile = getHazardLevel(cName, file, c, m, i,entries);
					if(hFile.getFile() != null)
						aFile.addAll(hFile.getFile());
					JavaHazardEntry hazardLevel = hFile.getEntry();
					if (hazardLevel != null && hazardLevel.getType() != JavaHazardEntryType.FAILURE) {
						hazards.add(new JavaHazard(hazardLevel.getLevel(), token.getValue(), t.getLine(),hazardLevel));
					}
					if(hazardLevel != null && hazardLevel.getType() == JavaHazardEntryType.FAILURE)
						failedTokens.add(token);
				}
			}
		} else if (t.getType() == JavaTokenType.METHOD) {
			totalTokens++;
			String cName = t.getValue().trim();
			AnalyzedHazard hFile = getHazardLevel(cName, file, c, m, i,entries);
			if(hFile.getFile() != null)
				aFile.addAll(hFile.getFile());
			JavaHazardEntry hazardLevel = hFile.getEntry();
			if (hazardLevel != null && hazardLevel.getType() != JavaHazardEntryType.FAILURE) {
				hazards.add(new JavaHazard(hazardLevel.getLevel(), t.getValue(), t.getLine(),hazardLevel));
			}
			if(hazardLevel != null && hazardLevel.getType() == JavaHazardEntryType.FAILURE)
				failedTokens.add(t);
		} else if(t.getType() == JavaTokenType.VARIABLE) {
			totalTokens++;
			String cName = t.getValue().trim();
			int index1 = cName.indexOf("<");
			if(index1 == -1)
				index1 = Integer.MAX_VALUE;
			int index2 = cName.indexOf("[");
			if(index2 == -1)
				index2 = Integer.MAX_VALUE;
			int max = Math.min(index1,index2);
			if(max == Integer.MAX_VALUE)
				max = cName.length();
			cName = cName.substring(0, max);
			
			String vName;
			if(m != null)
				vName = m.getName()+"@"+extra.getValue().trim();
			else
				vName = extra.getValue().trim();
			
			String pkg = findPackage(cName, file, c);
			if(pkg == null) {
				failedTokens.add(t);
			}else {
				pkg = stripLastDot(pkg);
				aFile.getVariableTypes().put(vName, pkg);
				if(m != null)
					aFile.getVariableDefinitions().put(vName, m.getLine());
				else
					aFile.getVariableDefinitions().put(vName, c.getLine());
			}
		}
		return new AnalysisResult(hazards, totalTokens, failedTokens, aFile);
	}

	private static AnalyzedHazard getHazardLevel(String cName, JavaFile file, JavaClass c, JavaMethod m, JavaInstruction i, List<JavaHazardEntry> entries) {
		if (cName.startsWith("new "))
			cName = cName.substring(4);
		if (i != null && i.getType() == JavaInstructionType.FLOW && i.getTokens().get(0).getValue().equals("for"))
			cName = cName.split(":")[0].split(" ")[0].trim();
		AnalyzedFile aFile = new AnalyzedFile();
		String tmp = findPackage(cName, file, c);
		if (tmp != null) {
			tmp = stripLastDot(tmp);
			cName = tmp;
			aFile.getImports().add(cName);
		} else {
			tmp = findVariableType(cName, c, m, file);
			if (tmp != null) {
				if(m != null)
					cName = m.getName()+"@"+stripLastDot(cName);
				aFile.getVariableTypes().put(cName, tmp);
				if(i != null)
					aFile.getVariableDefinitions().put(cName, i.getLine());
				else
					aFile.getVariableDefinitions().put(cName, m.getLine());
				cName = tmp;
			} else {
				return new AnalyzedHazard(new JavaHazardEntry(JavaHazardEntryType.FAILURE, null,null,null),null);
			}
		}
		aFile.getImports().add(cName);
		return new AnalyzedHazard(getHazardLevelClass(cName,entries),aFile);
	}

	private static List<JavaToken> getTokens(String s) { 	
		List<JavaToken> tokens = new ArrayList<JavaToken>();
		int lastI = 0;
		int count = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.toCharArray()[i] == '(' || s.toCharArray()[i] == '<' || s.toCharArray()[i] == '[') {
				count++;
				if(count > 1) {
					//TODO: Improve this as only doing 1 layer of argument parsing allows bypasses through function arguments
					continue;
				}
				String s1 = s.substring(lastI, i);
				tokens.add(new JavaToken(JavaTokenType.METHOD, 0, s1));
				lastI = i + 1;
			} else if (s.toCharArray()[i] == ')' || s.toCharArray()[i] == '>' || s.toCharArray()[i] == ']') {
				count--;
				if(count > 0) continue;
				String s1 = s.substring(lastI, i);
				tokens.add(new JavaToken(JavaTokenType.DATA, 0, s1));
			}
		}
		while (tokens.size() > 0 && tokens.get(tokens.size() - 1).getValue().equals("")) {
			tokens.remove(tokens.size() - 1);
		}
		return tokens;
	}

	private static JavaHazardEntry getHazardLevelClass(String search, List<JavaHazardEntry> entries) {
		for (JavaHazardEntry e : entries) {
			if(e.getType() != JavaHazardEntryType.CLASS)
				continue;
			Pattern p = Pattern.compile(e.getRegex());
			Matcher m = p.matcher(search);
			if (m.lookingAt())
				return e;
		}
		return null;
	}

	private static JavaHazardEntry getHazardLevelString(String search, List<JavaHazardEntry> entries) {
		search = search.replaceAll("\"", "").trim();
		for (JavaHazardEntry e : entries) {
			Pattern p = Pattern.compile(e.getRegex());
			Matcher m = p.matcher(search);
			if (m.lookingAt())
				return e;
		}
		return null;
	}

	private static String findVariableType(String c, JavaClass curr, JavaMethod method, JavaFile file) {
		String name = getFirstDot(c);
		if (method != null) {
			String fqn = getFQN(method.getInstructions(), file, name, curr, method);
			if (fqn != null)
				return stripLastDot(fqn);
		}
		String fqn = getFQN(curr.getClassData(), file, name, curr, method);
		if (fqn != null)
			return stripLastDot(fqn);
		return null;
	}

	private static String getFQN(List<JavaInstruction> instructions, JavaFile file, String name, JavaClass curr, JavaMethod method) {
		for(JavaArgument arg : method.getArguments()) {
			String aName = arg.getValue().getValue().trim();
			if(aName.equals(name)) {
				String fqn = findPackage(arg.getType().getValue().trim(), file, curr);
				return fqn;
			}
		}
		for (JavaInstruction inst : instructions) {
			if (inst.getType() == JavaInstructionType.VARIABLE) {
				String vName = null;
				String vType = null;
				for (JavaToken t : inst.getTokens()) {
					if (t.getType() == JavaTokenType.NAME) {
						vName = t.getValue();
					} else if (t.getType() == JavaTokenType.VARIABLE) {
						vType = t.getValue();
					}
				}
				if(vType == null || vName == null) continue;
				if(vType.contains("<")) {
					vType = vType.split("\\<")[0];
				}
				if (vName != null && name.equals(vName)) {
					String fqn = findPackage(vType, file, curr);
					return fqn;
				}
			} else if (inst.getType() == JavaInstructionType.FLOW) {
				if(inst.getTokens().get(0).getValue().equals("for")) {
					String data = inst.getTokens().get(1).getValue();
					if(data.contains(":")) {
						String[] split = data.split(":",2);
						String[] split2 = split[0].split(" ",2);
						if(split2[1].trim().equals(name)) {
							String fqn = findPackage(split2[0].trim(), file, curr);
							return fqn;
						}
					}else {
						String[] split = data.split("\\;",3);
						String[] split2 = split[0].split(" ",2);
						if(split2.length < 2)
							continue;
						if(split2[1].trim().equals(name)) {
							String fqn = findPackage(split2[0].trim(), file, curr);
							return fqn;
						}
					}
				}
			}
		}
		return null;
	}

	private static String findPackage(String c, JavaFile f, JavaClass curr) {
		if (classExists(c))
			return c + "." + getLastDot(c);
		String withoutLast = stripLastDot(c);
		if (classExists(withoutLast))
			return c;
		for (JavaClass cl : f.getClasses()) {
			if (cl.getFQN().equals(c))
				return c + "." + getLastDot(c);
			if (cl.getFQN().equals(withoutLast))
				return c;
			if (cl.getName().equals(c))
				return cl.getFQN() + "." + c;
			if (cl.getName().equals(withoutLast))
				return cl.getFQN() + "." + c;
		}
		// By here the class is definitely not in the format package.class or
		// package.class.method
		String pkg = curr.getPkg();
		for (JavaClass cl : f.getClasses()) {
			if (cl.getPkg().equals(pkg) && cl.getName().equals(c))
				return cl.getPkg() + "." + c + "." + getLastDot(c);
			if (cl.getPkg().equals(pkg) && cl.getName().equals(withoutLast))
				return cl.getPkg() + "." + c;
		}
		// By here the class is definitely not in the same package and is in the format
		// of class or class.method
		for (JavaImport imp : f.getImports()) {
			String value = imp.getValue();
			if (!value.endsWith("*")) {
				String last = getLastDot(value);
				if (last.equals(c) || last.equals(withoutLast))
					return value + "." + getLastDot(value);
			} else {
				value = stripLastDot(value);
				// Search through package like a pleb
				if (classExists(value + "." + c))
					return value + "." + c + "." + getLastDot(c);
				if (classExists(value + "." + withoutLast))
					return value + "." + c;
			}
		}
		return null; // No clue here
	}

	private static String stripLastDot(String s) {
		String[] split = s.split("\\.");
		String ret = "";
		for (int i = 0; i < split.length - 1; i++) {
			ret += "." + split[i];
		}
		if (ret.length() > 0)
			ret = ret.substring(1);
		return ret;
	}

	private static String getLastDot(String s) {
		String[] split = s.split("\\.");
		return split[split.length - 1];
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
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}
