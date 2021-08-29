package xyz.finlaym.programminggrader.analyzer;

import java.util.List;

import xyz.finlaym.programminggrader.parser.JavaToken;

public class AnalysisResult {
	private List<JavaHazard> hazards;
	private int totalTokens;
	private List<JavaToken> failedTokens;

	public AnalysisResult(List<JavaHazard> hazards, int totalTokens, List<JavaToken> failedTokens) {
		this.hazards = hazards;
		this.totalTokens = totalTokens;
		this.failedTokens = failedTokens;
	}
	
	public List<JavaHazard> getHazards() {
		return hazards;
	}

	public int getTotalTokens() {
		return totalTokens;
	}

	public List<JavaToken> getFailedTokens() {
		return failedTokens;
	}
}
