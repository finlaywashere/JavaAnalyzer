package xyz.finlaym.programminggrader.grader;

import xyz.finlaym.programminggrader.parser.JavaClass;
import xyz.finlaym.programminggrader.parser.JavaFile;
import xyz.finlaym.programminggrader.parser.JavaMethod;

public class JavaGrader {
	public GradingResult grade(JavaFile file) {
		double averageNameLenC = 0;
		
		int capitalCCharsC = 0;
		for(JavaClass c : file.getClasses()) {
			averageNameLenC += c.getName().length();
			for(char c1 : c.getName().toCharArray()) {
				if(c1 > 'A' && c1 < 'Z')
					capitalCCharsC++;
			}
		}
		double averageCapPerC = averageNameLenC/capitalCCharsC;
		
		averageNameLenC /= file.getClasses().size();
		
		double averageNameLenM = 0;
		int methodCount = 0;
		int capitalCCharsM = 0;
		for(JavaClass c : file.getClasses()) {
			for(JavaMethod m : c.getMethods()) {
				if(m.getName().startsWith("GLOBAL"))
					continue;
				methodCount++;
				averageNameLenM += m.getName().length();
				for(char c1 : m.getName().toCharArray()) {
					if(c1 > 'A' && c1 < 'Z')
						capitalCCharsM++;
				}
			}
		}
		double averageCapPerM = averageNameLenM/capitalCCharsM;
		averageNameLenM /= methodCount;
		
		return new GradingResult(averageCapPerC,averageCapPerM,-1,averageNameLenC,averageNameLenM,-1);
	}
}
