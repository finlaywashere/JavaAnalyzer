package xyz.finlaym.programminggrader;

import java.io.File;
import java.util.Arrays;

import xyz.finlaym.programminggrader.analyzer.AnalysisResult;
import xyz.finlaym.programminggrader.analyzer.AnalyzerConstants;
import xyz.finlaym.programminggrader.analyzer.JavaAnalyzer;
import xyz.finlaym.programminggrader.analyzer.JavaHazard;
import xyz.finlaym.programminggrader.grader.GradingResult;
import xyz.finlaym.programminggrader.grader.JavaGrader;
import xyz.finlaym.programminggrader.parser.JavaArgument;
import xyz.finlaym.programminggrader.parser.JavaClass;
import xyz.finlaym.programminggrader.parser.JavaFile;
import xyz.finlaym.programminggrader.parser.JavaImport;
import xyz.finlaym.programminggrader.parser.JavaInstruction;
import xyz.finlaym.programminggrader.parser.JavaInstructionType;
import xyz.finlaym.programminggrader.parser.JavaMethod;
import xyz.finlaym.programminggrader.parser.JavaModifier;
import xyz.finlaym.programminggrader.parser.JavaParser;
import xyz.finlaym.programminggrader.parser.JavaToken;
import xyz.finlaym.programminggrader.parser.JavaTokenType;

public class Main {

	public static void main(String[] args) throws Exception{
		JavaParser parser = new JavaParser();
		JavaFile file = parser.parse("DaemonUtils", new File("tests/"));
		if(file == null) {
			System.err.println("Error occurred while processing file!");
			System.exit(1);
		}
		JavaAnalyzer analyzer = new JavaAnalyzer();
		AnalysisResult result = analyzer.analyze(file, Arrays.asList(AnalyzerConstants.HAZARDS));
		
		JavaGrader grader = new JavaGrader();
		GradingResult gResult = grader.grade(file);

		System.out.println("\n!!!PARSED DATA!!!\n");
		for(JavaImport i : file.getImports()) {
			System.out.println("Import: "+i.getValue());
		}
		System.out.println();
		for(JavaClass c : file.getClasses()) {
			System.out.println("Class: "+c.getName());
			System.out.print("Modifiers: ");
			for(JavaModifier m : c.getModifiers()) {
				System.out.print(m.toString()+" ");
			}
			System.out.println();
			System.out.println();
			for(JavaMethod m : c.getMethods()) {
				System.out.println("Method: "+m.getName());
				System.out.print("Modifiers: ");
				for(JavaModifier mod : m.getModifiers()) {
					System.out.print(mod.toString()+" ");
				}
				System.out.println();
				System.out.print("Arguments: ");
				for(JavaArgument a : m.getArguments()) {
					System.out.print(a.getType().getValue()+" "+a.getValue().getValue()+" ");
				}
				System.out.println();
				System.out.println("Returns: "+m.getReturnType().getValue());
			}
			System.out.println();
			System.out.println();
			for(JavaInstruction i : c.getClassData()) {
				if(i.getType() == JavaInstructionType.DEFINITION) {
					continue;
				}
				String name = "unknown";
				String value = "none";
				for(JavaToken t : i.getTokens()) {
					if(t.getType() == JavaTokenType.NAME) {
						name = t.getValue();
					}else if(t.getType() == JavaTokenType.DATA) {
						value = t.getValue();
					}
				}
				System.out.println("Data:\nName: "+name+"\nValue: "+value);
				System.out.println();
			}
		}
		System.out.println("\n!!!ANALYSIS!!!\n");
		for(JavaHazard hazard : result.getHazards()) {
			System.out.println("Type: "+hazard.getType()+", Data: "+hazard.getData()+", Line: "+hazard.getLine());
		}
		
		System.out.println("\n!!!GRADING!!!\n");
		System.out.println("Capital to letters ratio for classes: "+gResult.getAverageCapPerCC());
		System.out.println("Capital to letters ratio for methods: "+gResult.getAverageCapPerCM());
		System.out.println("Capital to letters ratio for variables: "+gResult.getAverageCapPerCV());
		System.out.println("Average class name length: "+gResult.getAverageNameLenC());
		System.out.println("Average method name length: "+gResult.getAverageNameLenM());
		System.out.println("Average variable name length: "+gResult.getAverageNameLenV());
	}
}
