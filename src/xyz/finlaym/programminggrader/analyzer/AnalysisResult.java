package xyz.finlaym.programminggrader.analyzer;

import java.util.List;

import xyz.finlaym.programminggrader.parser.JavaToken;

public class AnalysisResult {
	private List<JavaHazard> hazards;
	private int totalTokens;
	private List<JavaToken> failedTokens;
	private AnalyzedFile result;

	public AnalysisResult(List<JavaHazard> hazards, int totalTokens, List<JavaToken> failedTokens, AnalyzedFile result) {
		this.hazards = hazards;
		this.totalTokens = totalTokens;
		this.failedTokens = failedTokens;
		this.result = result;
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

	public AnalyzedFile getResult() {
		return result;
	}
	
}
