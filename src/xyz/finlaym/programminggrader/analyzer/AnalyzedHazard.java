package xyz.finlaym.programminggrader.analyzer;

public class AnalyzedHazard {
	private JavaHazardEntry entry;
	private AnalyzedFile file;
	public AnalyzedHazard(JavaHazardEntry entry, AnalyzedFile file) {
		this.entry = entry;
		this.file = file;
	}
	public JavaHazardEntry getEntry() {
		return entry;
	}
	public AnalyzedFile getFile() {
		return file;
	}
}
