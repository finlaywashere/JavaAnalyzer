package xyz.finlaym.programminggrader.parser;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JavaParser {
	public JavaFile parse(String cl, File baseFolder) throws Exception {
		if (cl.startsWith(".")) {
			System.err.println("Error refusing to parse unsafe class " + cl);
			return null;
		}
		File f = new File(baseFolder, cl.replaceAll("\\.", "/") + ".java");

		String data = new String(Files.readAllBytes(f.toPath()));

		List<JavaImport> imports = new ArrayList<JavaImport>();
		imports.add(new JavaImport("java.lang.*", 0));
		List<JavaClass> classes = new ArrayList<JavaClass>();

		JavaClass currClass = null;
		JavaMethod currMethod = null;

		List<JavaInstruction> classData = new ArrayList<JavaInstruction>();

		JavaToken pkgDN = new JavaToken(JavaTokenType.MODIFIER, 0, "package");
		JavaToken pkgDV = new JavaToken(JavaTokenType.DATA, 0, "");
		JavaInstruction pkgInst = new JavaInstruction(JavaInstructionType.DEFINITION, Arrays.asList(pkgDN, pkgDV), 0);

		int last = 0;
		int line = 1;
		int bCount = 0;
		boolean chars = false;
		boolean slashComment = false;
		boolean autoComment = false;
		for (int i = 0; i < data.length(); i++) {
			char c = data.charAt(i);
			if (c == '\n')
				line++;
			if (slashComment && c != '\n')
				continue;
			if (slashComment && c == '\n') {
				slashComment = false;
				last = i + 1;
				continue;
			}
			if (autoComment && c != '*')
				continue;
			if (autoComment && c == '*') {
				if (i != data.length() - 1 && data.charAt(i + 1) == '/') {
					autoComment = false;
					last = i + 2;
					continue;
				}
			}
			if ((c > 'a' && c < 'z') || (c > 'A' && c < 'Z')) {
				chars = true;
			}
			if (!chars && c == '/') {
				if (i != data.length() - 1) {
					if (data.charAt(i + 1) == '/') {
						slashComment = true;
						continue;
					} else if (data.charAt(i + 1) == '*') {
						autoComment = true;
						continue;
					}
				}
			}

			if (c == '{' || c == ';') {
				String s = data.substring(last, i).trim();
				if (c == '{')
					bCount++;
				if (c == '{' && !s.startsWith("if") && !s.startsWith("for") && !s.startsWith("while")) {
					if (currClass == null && bCount == 1) {
						String[] split = s.split(" ");
						List<JavaModifier> modifiers = new ArrayList<JavaModifier>();
						String name = null;
						for (int i1 = 0; i1 < split.length; i1++) {
							if (split[i1].toUpperCase().equals("CLASS")) {
								name = split[i1 + 1];
								classData.add(pkgInst);
								break;
							}
							JavaModifier modifier;
							try {
								modifier = JavaModifier.valueOf(split[i1].toUpperCase());
							} catch (IllegalArgumentException e) {
								System.err.println("Illegal class modifier on line " + line);
								return null;
							}
							modifiers.add(modifier);
						}
						if (name == null) {
							System.err.println("Illegal class name on line " + line);
							return null;
						}
						currClass = new JavaClass(name, line, modifiers, classData);
					} else if (currMethod == null && bCount == 2) {
						String[] split = s.split(" ");
						List<JavaModifier> modifiers = new ArrayList<JavaModifier>();
						String name = null;
						String arguments = null;
						JavaToken returnType = null;
						for (int i1 = 0; i1 < split.length; i1++) {
							JavaModifier modifier;
							try {
								modifier = JavaModifier.valueOf(split[i1].toUpperCase());
							} catch (IllegalArgumentException e) {
								String ret = split[i1].split("\\(")[0].trim();
								returnType = new JavaToken(JavaTokenType.VARIABLE, line, ret);
								String s1 = "";
								for (int i2 = i1 + 1; i2 < split.length; i2++) {
									s1 += split[i2] + " ";
								}
								s1 = s1.trim();
								String[] split2 = s1.split("\\(", 2);
								if (split2.length == 1 && split2[0].equals("")) {
									// This is a constructor
									name = ret;
									arguments = "";
								} else {
									name = split2[0];
									arguments = split2[1].substring(0, split2[1].lastIndexOf(")"));
								}
								break;
							}
							modifiers.add(modifier);
						}
						if (name == null) {
							System.err.println("Illegal method name on line " + line);
							return null;
						}
						List<JavaArgument> args = new ArrayList<JavaArgument>();
						if (!arguments.equals("")) {
							for (String s1 : arguments.split(",")) {
								s1 = s1.trim();
								String[] split2 = s1.split(" ", 2);
								JavaToken type = new JavaToken(JavaTokenType.VARIABLE, line, split2[0]);
								JavaToken value = new JavaToken(JavaTokenType.NAME, line, split2[1]);
								args.add(new JavaArgument(type, value));
							}
						}
						currMethod = new JavaMethod(name, line, modifiers, args, returnType);
					}
				} else {
					if (s.startsWith("import ")) {
						if (currClass != null) {
							System.err.println("Illegal import on line " + line);
							return null;
						}
						String value = s.substring(7);
						// Now actually parse that class if it exists
						File importFile = new File(baseFolder, value.replaceAll("\\.", "/") + ".java");
						if (importFile.exists()) {
							JavaFile jFile = parse(value, baseFolder);
							imports.addAll(jFile.getImports());
							classes.addAll(jFile.getClasses());
						} else {
							JavaImport tmpImport = new JavaImport(value, line);
							imports.add(tmpImport);
						}
					} else {
						List<JavaInstruction> inst = strToInstruction(s, c, currClass, currMethod, line);
						if (currMethod != null) {
							currMethod.getInstructions().addAll(inst);
						} else if (inst.size() > 0 && inst.get(0).getType() == JavaInstructionType.DEFINITION
								|| inst.get(0).getType() == JavaInstructionType.VARIABLE) {
							if (inst.get(0).getTokens().get(0).getValue().equals("package")) {
								pkgInst = inst.get(0);
							} else {
								currClass.getClassData().addAll(inst);
							}
						} else {
							System.err.println("Invalid data outside of method on line " + line);
						}
					}
				}

				last = i + 1;
				chars = false;
			} else if (c == '}') {
				if (currMethod != null && bCount == 2) {
					currClass.getMethods().add(currMethod);
					currMethod = null;
				} else if (bCount == 1) {
					classes.add(currClass);
					currClass = null;
				}
				bCount--;
				last = i + 1;
				chars = false;
			}
		}

		return new JavaFile(classes, imports);
	}

	private static List<JavaInstruction> strToInstruction(String s, char c, JavaClass currClass, JavaMethod currMethod,int line) {
		s = s.trim();
		// This is an instruction line;

		if (s.contains("=")) {
			String opS = "\\=";
			if (s.contains("+=")) {
				opS = "\\+\\=";
			} else if (s.contains("-=")) {
				opS = "\\-\\=";
			} else if (s.contains("*=")) {
				opS = "\\*\\=";
			} else if (s.contains("/=")) {
				opS = "/\\=";
			} else if (s.contains("|=")) {
				opS = "\\|\\=";
			} else if (s.contains("&=")) {
				opS = "\\&\\=";
			} else if (s.contains("^=")) {
				opS = "\\^\\=";
			}
			// A variable is being set
			// Type foo = bar()
			// Alternatively
			// foo = bar()
			String[] split = s.split(opS, 2);
			String[] lSplit = split[0].trim().split(" ");
			int typeIndex = 0;
			List<JavaToken> tokens = new ArrayList<JavaToken>();
			for (int i2 = 0; i2 < lSplit.length; i2++) {
				String s1 = lSplit[i2];
				try {
					JavaModifier modifier = JavaModifier.valueOf(s1.toUpperCase());
					JavaToken token = new JavaToken(JavaTokenType.MODIFIER, line, modifier.toString());
					tokens.add(token);
				} catch (IllegalArgumentException e) {
					typeIndex = i2;
					break;
				}
			}
			if (lSplit.length - typeIndex > 1) {
				JavaToken vType = new JavaToken(JavaTokenType.VARIABLE, line, lSplit[typeIndex]);
				typeIndex++;
				tokens.add(vType);
			}
			JavaToken name = new JavaToken(JavaTokenType.NAME, line, lSplit[typeIndex]);
			tokens.add(name);
			JavaToken operation = new JavaToken(JavaTokenType.OPERATION, line, opS.replaceAll("\\\\", ""));
			tokens.add(operation);
			JavaToken value = new JavaToken(JavaTokenType.DATA, line, split[1].trim());
			tokens.add(value);
			JavaInstruction instruction = new JavaInstruction(JavaInstructionType.VARIABLE, tokens, line);
			return Arrays.asList(instruction);
		} else {
			// A function is being called or a variable is being defined(probably but not a
			// totally safe assumption)
			if (s.startsWith("if") || s.startsWith("while") || s.startsWith("for")) {
				String[] split = s.split("\\(", 2);
				if (c == '{') {
					JavaToken type = new JavaToken(JavaTokenType.FLOW, line, split[0].trim());
					JavaToken args = new JavaToken(JavaTokenType.DATA, line, split[1]);
					JavaInstruction inst = new JavaInstruction(JavaInstructionType.FLOW, Arrays.asList(type, args), line);
					return Arrays.asList(inst);
				} else {
					int flowEnd = 0;
					int count = 1;
					for (int i1 = 0; i1 < split[1].length(); i1++) {
						char c1 = split[1].charAt(i1);
						if (c1 == '(')
							count++;
						if (c1 == ')')
							count--;
						if (count == 0) {
							flowEnd = i1 + 1;
							break;
						}
					}
					String flow = split[1].substring(0, flowEnd - 1).trim();
					String action = split[1].substring(flowEnd).trim();
					while(action.startsWith("//")) {
						action = action.substring(action.indexOf('\n')).trim();
					}
					JavaToken type = new JavaToken(JavaTokenType.FLOW, line, split[0].trim());
					JavaToken args = new JavaToken(JavaTokenType.DATA, line, flow.trim());
					List<JavaInstruction> instructions = new ArrayList<JavaInstruction>();
					instructions.add(new JavaInstruction(JavaInstructionType.FLOW, Arrays.asList(type, args), line));

					List<JavaInstruction> actI = strToInstruction(action, c, currClass, currMethod, line);
					instructions.addAll(actI);
					return instructions;
				}
			} else if (s.equals("continue") || s.equals("break")) {
				JavaToken flow = new JavaToken(JavaTokenType.FLOW, line, s);
				JavaInstruction instruction = new JavaInstruction(JavaInstructionType.FLOW, Arrays.asList(flow), line);
				return Arrays.asList(instruction);
			} else if (s.startsWith("return ")) {
				String[] split = s.split(" ", 2);
				List<JavaToken> tokens = new ArrayList<JavaToken>();
				JavaToken flow = new JavaToken(JavaTokenType.FLOW, line, split[0]);
				tokens.add(flow);
				if (split.length > 1) {
					JavaToken value = new JavaToken(JavaTokenType.DATA, line, split[1].trim());
					tokens.add(value);
				}
				JavaInstruction instruction = new JavaInstruction(JavaInstructionType.FLOW, tokens, line);
				return Arrays.asList(instruction);
			} else if (s.contains("++") || s.contains("--")) {
				List<JavaToken> tokens = new ArrayList<JavaToken>();
				String name = s;
				if (s.startsWith("++") || s.startsWith("--")) {
					name = name.substring(2);
					tokens.add(new JavaToken(JavaTokenType.BEFORE_OPERATION, line, s.substring(0, 2)));
				}
				if (s.endsWith("++") || s.endsWith("--")) {
					name = name.substring(0, name.length() - 2);
					tokens.add(new JavaToken(JavaTokenType.AFTER_OPERATION, line,
							s.substring(s.length() - 2, s.length())));
				}
				tokens.add(new JavaToken(JavaTokenType.NAME, line, name));
				JavaInstruction instruction = new JavaInstruction(JavaInstructionType.OPERATION, tokens, line);
				return Arrays.asList(instruction);
			} else if (s.startsWith("package")) {
				JavaToken pkgT = new JavaToken(JavaTokenType.MODIFIER, line, s.substring(0, 7));
				String s2 = s.substring(7).trim();
				JavaToken dataT = new JavaToken(JavaTokenType.DATA, line, s2);
				JavaInstruction inst = new JavaInstruction(JavaInstructionType.DEFINITION, Arrays.asList(pkgT, dataT),
						line);
				return Arrays.asList(inst);
			} else if (s.contains("(")) {
				String[] split = s.split("\\(", 2);
				JavaToken method = new JavaToken(JavaTokenType.METHOD, line, split[0].trim());
				String dataS = split[1];
				dataS = dataS.substring(0, dataS.lastIndexOf(")")).trim();
				JavaToken mData = new JavaToken(JavaTokenType.DATA, line, dataS);
				JavaInstruction instruction = new JavaInstruction(JavaInstructionType.CALL,
						Arrays.asList(method, mData), line);
				return Arrays.asList(instruction);
			} else {
				// This is probably a variable definition
				String[] split = s.split(" ");
				List<JavaToken> tokens = new ArrayList<JavaToken>();
				int typeIndex = 0;
				for (int i2 = 0; i2 < split.length; i2++) {
					String s1 = split[i2];
					try {
						JavaModifier modifier = JavaModifier.valueOf(s1.toUpperCase());
						JavaToken token = new JavaToken(JavaTokenType.MODIFIER, line, modifier.toString());
						tokens.add(token);
					} catch (IllegalArgumentException e) {
						typeIndex = i2;
						break;
					}
				}
				JavaToken type = new JavaToken(JavaTokenType.VARIABLE, line, split[typeIndex]);
				tokens.add(type);
				JavaToken name = new JavaToken(JavaTokenType.NAME, line, split[typeIndex + 1]);
				tokens.add(name);
				JavaInstruction instruction = new JavaInstruction(JavaInstructionType.VARIABLE, tokens, line);
				return Arrays.asList(instruction);
			}
		}
	}
}
