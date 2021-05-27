package xyz.finlaym.programminggrader.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class JavaParser {
	public JavaFile parse(File f) throws IOException {
		String data = new String(Files.readAllBytes(f.toPath()));
		
		List<JavaImport> imports = new ArrayList<JavaImport>();
		List<JavaClass> classes = new ArrayList<JavaClass>();
		
		JavaClass currClass = null;
		JavaMethod currMethod = null;
		
		int last = 0;
		int line = 1;
		for(int i = 0; i < data.length(); i++) {
			char c = data.charAt(i);
			if(c == '\n')
				line++;
			if(c == '{' || c == ';') {
				String s = data.substring(last, i).trim();
				
				if(c == '{') {
					if(currClass == null) {
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
					}else {
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
								returnType = new JavaToken(JavaTokenType.VARIABLE, line, split[i1]);
								String s1 = "";
								for(int i2 = i1 + 1; i2 < split.length; i2++) {
									s1 += split[i2] + " ";
								}
								s1 = s1.trim();
								String[] split2 = s1.split("\\(",2);
								name = split2[0];
								arguments = split2[1].substring(0, split2[1].lastIndexOf(")"));
								break;
							}
							modifiers.add(modifier);
						}
						if(name == null) {
							System.err.println("Illegal method name on line "+line);
							return null;
						}
						List<JavaArgument> args = new ArrayList<JavaArgument>();
						for(String s1 : arguments.split(",")) {
							s1 = s1.trim();
							String[] split2 = s1.split(" ",2);
							JavaToken type = new JavaToken(JavaTokenType.VARIABLE, line, split2[0]);
							JavaToken value = new JavaToken(JavaTokenType.NAME, line, split2[1]);
							args.add(new JavaArgument(type,value));
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
						JavaImport tmpImport = new JavaImport(value, line);
						imports.add(tmpImport);
					}
				}
				
				last = i+1;
			}else if(c == '}') {
				if(currMethod != null) {
					currClass.getMethods().add(currMethod);
					currMethod = null;
				}else {
					classes.add(currClass);
					currClass = null;
				}
				last = i+1;
			}
		}
		
		return new JavaFile(classes, imports);
	}
}
