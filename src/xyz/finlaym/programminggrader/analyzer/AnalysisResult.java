package xyz.finlaym.programminggrader.analyzer;

import java.util.List;

public class AnalysisResult {
	private List<JavaHazard> hazards;

	public AnalysisResult(List<JavaHazard> hazards) {
		this.hazards = hazards;
	}
	
	public List<JavaHazard> getHazards() {
		return hazards;
	}
}
