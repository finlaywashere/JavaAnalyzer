package xyz.finlaym.programminggrader.analyzer;

public class JavaHazard {
	private JavaHazardType type;
	private String data;
	public JavaHazard(JavaHazardType type, String data) {
		this.type = type;
		this.data = data;
	}
	public JavaHazardType getType() {
		return type;
	}
	public String getData() {
		return data;
	}
}
