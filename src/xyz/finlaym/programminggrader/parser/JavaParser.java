package xyz.finlaym.programminggrader.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JavaParser {
	public JavaFile parse(String cl, File baseFolder) throws IOException {
		if(cl.startsWith(".")) {
			System.err.println("Error refusing to parse unsafe class "+cl);
			return null;
		}
		File f = new File(baseFolder,cl.replaceAll("\\.", "/")+".java");
		
		String data = new String(Files.readAllBytes(f.toPath()));
		
		List<JavaImport> imports = new ArrayList<JavaImport>();
		imports.add(new JavaImport("java.lang.*", 0));
		List<JavaClass> classes = new ArrayList<JavaClass>();
		
		JavaClass currClass = null;
		JavaMethod currMethod = null;
		
		int last = 0;
		int line = 1;
		int bCount = 0;
		for(int i = 0; i < data.length(); i++) {
			char c = data.charAt(i);
			if(c == '\n')
				line++;
			if(c == '{' || c == ';') {
				String s = data.substring(last, i).trim();
				
				if(c == '{') {
					bCount++;
					if(currClass == null && bCount == 1) {
						String[] split = s.split(" ");
						List<JavaModifier> modifiers = new ArrayList<JavaModifier>();
						String name = null;
						for(int i1 = 0; i1 < split.length; i1++) {
							if(split[i1].toUpperCase().equals("CLASS")) {
								name = split[i1 + 1];
								break;
							}
							JavaModifier modifier;
							try {
								modifier = JavaModifier.valueOf(split[i1].toUpperCase());
							}catch(IllegalArgumentException e) {
								System.err.println("Illegal class modifier on line "+line);
								return null;
							}
							modifiers.add(modifier);
						}
						if(name == null) {
							System.err.println("Illegal class name on line "+line);
							return null;
						}
						currClass = new JavaClass(name, line, modifiers);
					}else if (currMethod == null && bCount == 2){
						String[] split = s.split(" ");
						List<JavaModifier> modifiers = new ArrayList<JavaModifier>();
						String name = null;
						String arguments = null;
						JavaToken returnType = null;
						for(int i1 = 0; i1 < split.length; i1++) {
							JavaModifier modifier;
							try {
								modifier = JavaModifier.valueOf(split[i1].toUpperCase());
							}catch(IllegalArgumentException e) {
								String ret = split[i1].split("\\(")[0].trim();
								returnType = new JavaToken(JavaTokenType.VARIABLE, line, ret);
								String s1 = "";
								for(int i2 = i1 + 1; i2 < split.length; i2++) {
									s1 += split[i2] + " ";
								}
								s1 = s1.trim();
								String[] split2 = s1.split("\\(",2);
								if(split2.length == 1 && split2[0].equals("")) {
									// This is a constructor
									name = ret;
									arguments = "";
								}else {
									name = split2[0];
									arguments = split2[1].substring(0, split2[1].lastIndexOf(")"));
								}
								break;
							}
							modifiers.add(modifier);
						}
						if(name == null) {
							System.err.println("Illegal method name on line "+line);
							return null;
						}
						List<JavaArgument> args = new ArrayList<JavaArgument>();
						if(!arguments.equals("")) {
							for(String s1 : arguments.split(",")) {
								s1 = s1.trim();
								String[] split2 = s1.split(" ",2);
								JavaToken type = new JavaToken(JavaTokenType.VARIABLE, line, split2[0]);
								JavaToken value = new JavaToken(JavaTokenType.NAME, line, split2[1]);
								args.add(new JavaArgument(type,value));
							}
						}
						currMethod = new JavaMethod(name, line, modifiers,args,returnType);
					}
				}else {
					if(s.startsWith("import ")) {
						if(currClass != null) {
							System.err.println("Illegal import on line "+line);
							return null;
						}
						String value = s.substring(7);
						// Now actually parse that class if it exists
						File importFile = new File(baseFolder,value.replaceAll("\\.", "/")+".java");
						if(importFile.exists()) {
							JavaFile jFile = parse(value, baseFolder);
							imports.addAll(jFile.getImports());
							classes.addAll(jFile.getClasses());
						}else {
							JavaImport tmpImport = new JavaImport(value, line);
							imports.add(tmpImport);
						}
					}else {
						// This is an instruction line;
						
						if(s.contains("=")) {
							String opS = "\\=";
							if(s.contains("+=")) {
								opS = "\\+\\=";
							}else if(s.contains("-=")) {
								opS = "\\-\\=";
							}else if(s.contains("*=")) {
								opS = "\\*\\=";
							} else if(s.contains("/=")) {
								opS = "/\\=";
							} else if(s.contains("|=")) {
								opS = "\\|\\=";
							} else if(s.contains("&=")) {
								opS = "\\&\\=";
							} else if(s.contains("^=")) {
								opS = "\\^\\=";
							}
							// A variable is being set
							// Type foo = bar()
							// Alternatively
							// foo = bar()
							String[] split = s.split(opS,2);
							String[] lSplit = split[0].trim().split(" ",2);
							int nameIndex = 0;
							List<JavaToken> tokens = new ArrayList<JavaToken>();
							JavaToken operation = new JavaToken(JavaTokenType.OPERATION, line, opS.replaceAll("\\\\", ""));
							tokens.add(operation);
							if(lSplit.length > 1) {
								JavaToken vType = new JavaToken(JavaTokenType.VARIABLE,line,lSplit[0]);
								nameIndex = 1;
								tokens.add(vType);
							}
							JavaToken name = new JavaToken(JavaTokenType.NAME,line,lSplit[nameIndex]);
							tokens.add(name);
							JavaToken value = new JavaToken(JavaTokenType.DATA,line,split[1].trim());
							tokens.add(value);
							JavaInstruction instruction = new JavaInstruction(JavaInstructionType.VARIABLE, tokens, line);
							if(currMethod != null) {
								currMethod.getInstructions().add(instruction);
							}else if(currClass == null) {
								System.err.println("Error: Instruction outside of class on line "+line);
								return null;
							}
						}else {
							// A function is being called (probably but not a totally safe assumption)
							if(s.contains("(")) {
								String[] split = s.split("\\(",2);
								JavaToken method = new JavaToken(JavaTokenType.METHOD,line,split[0].trim());
								String dataS = split[1];
								dataS = dataS.substring(0, dataS.lastIndexOf(")")).trim();
								JavaToken mData = new JavaToken(JavaTokenType.DATA,line,dataS);
								JavaInstruction instruction = new JavaInstruction(JavaInstructionType.CALL, Arrays.asList(method, mData), line);
								if(currMethod != null) {
									currMethod.getInstructions().add(instruction);
								}else if(currClass == null) {
									System.err.println("Error: Instruction outside of class on line "+line);
									return null;
								}
							}else if(s.equals("continue") || s.equals("break")) {
								JavaToken flow = new JavaToken(JavaTokenType.FLOW,line,s);
								JavaInstruction instruction = new JavaInstruction(JavaInstructionType.FLOW, Arrays.asList(flow), line);
								if(currMethod != null) {
									currMethod.getInstructions().add(instruction);
								}else if(currClass == null) {
									System.err.println("Error: Instruction outside of class on line "+line);
									return null;
								}
							}else if(s.startsWith("return ")) {
								String[] split = s.split(" ",2);
								List<JavaToken> tokens = new ArrayList<JavaToken>();
								JavaToken flow = new JavaToken(JavaTokenType.FLOW,line,split[0]);
								tokens.add(flow);
								if(split.length > 1) {
									JavaToken value = new JavaToken(JavaTokenType.DATA, line, split[1].trim());
									tokens.add(value);
								}
								JavaInstruction instruction = new JavaInstruction(JavaInstructionType.FLOW, tokens, line);
								if(currMethod != null) {
									currMethod.getInstructions().add(instruction);
								}else if(currClass == null) {
									System.err.println("Error: Instruction outside of class on line "+line);
									return null;
								}
							}else if(s.contains("++") || s.contains("--")) {
								List<JavaToken> tokens = new ArrayList<JavaToken>();
								String name = s;
								if(s.startsWith("++") || s.startsWith("--")) {
									name = name.substring(2);
									tokens.add(new JavaToken(JavaTokenType.BEFORE_OPERATION, line, s.substring(0, 2)));
								}
								if(s.endsWith("++") || s.endsWith("--")) {
									name = name.substring(0, name.length()-2);
									tokens.add(new JavaToken(JavaTokenType.AFTER_OPERATION, line, s.substring(s.length()-2,s.length())));
								}
								tokens.add(new JavaToken(JavaTokenType.NAME, line, name));
								JavaInstruction instruction = new JavaInstruction(JavaInstructionType.OPERATION, tokens, line);
								if(currMethod != null) {
									currMethod.getInstructions().add(instruction);
								}else if(currClass == null) {
									System.err.println("Error: Instruction outside of class on line "+line);
									return null;
								}
							}else {
								System.err.println("Found unknown data on line "+line);
							}
						}
					}
				}
				
				last = i+1;
			}else if(c == '}') {
				if(currMethod != null && bCount == 2) {
					currClass.getMethods().add(currMethod);
					currMethod = null;
				}else if(bCount == 1){
					classes.add(currClass);
					currClass = null;
				}
				bCount--;
				last = i+1;
			}
		}
		
		return new JavaFile(classes, imports);
	}
}
