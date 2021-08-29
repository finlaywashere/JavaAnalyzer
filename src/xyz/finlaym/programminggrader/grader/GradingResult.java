package xyz.finlaym.programminggrader.grader;

public class GradingResult {
	private double averageCapPerCM;
	private double averageCapPerCC;
	private double averageCapPerCV;
	private double averageNameLenM;
	private double averageNameLenC;
	private double averageNameLenV;
	public GradingResult(double averageCapPerCC, double averageCapPerCM, double averageCapPerCV, double averageNameLenC, double averageNameLenM, double averageNameLenV) {
		this.averageCapPerCM = averageCapPerCM;
		this.averageCapPerCC = averageCapPerCC;
		this.averageCapPerCV = averageCapPerCV;
		this.averageNameLenM = averageNameLenM;
		this.averageNameLenC = averageNameLenC;
		this.averageNameLenV = averageNameLenV;
	}
	public double getAverageCapPerCM() {
		return averageCapPerCM;
	}
	public double getAverageCapPerCC() {
		return averageCapPerCC;
	}
	public double getAverageCapPerCV() {
		return averageCapPerCV;
	}
	public double getAverageNameLenM() {
		return averageNameLenM;
	}
	public double getAverageNameLenC() {
		return averageNameLenC;
	}
	public double getAverageNameLenV() {
		return averageNameLenV;
	}
}
