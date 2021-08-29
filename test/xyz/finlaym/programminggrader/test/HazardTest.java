package xyz.finlaym.programminggrader.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;

import xyz.finlaym.programminggrader.analyzer.AnalysisResult;
import xyz.finlaym.programminggrader.analyzer.JavaAnalyzer;
import xyz.finlaym.programminggrader.parser.JavaFile;
import xyz.finlaym.programminggrader.parser.JavaParser;

class HazardTest {
	@Test
	public void testHazards() throws Exception {
		JavaParser parser = new JavaParser();
		JavaFile file = parser.parse("test123.test12345.Class2", new File("tests/"));
		if(file == null) {
			System.err.println("Error occurred while processing file!");
			fail();
		}
		JavaAnalyzer analyzer = new JavaAnalyzer();
		AnalysisResult result = analyzer.analyze(file);
		assertEquals(result.getHazards().get(0).getData(),"new ClassLoader");
		assertEquals(result.getHazards().get(1).getData(),"new java.io.File");
		assertEquals(result.getHazards().get(2).getData(),"System.getProperty");
		assertEquals(result.getHazards().get(3).getData()," \"user.home\"");
		assertEquals(result.getHazards().get(4).getData(),"new File");
	}
}
