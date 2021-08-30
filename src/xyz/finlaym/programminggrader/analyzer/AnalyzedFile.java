package xyz.finlaym.programminggrader.analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.finlaym.programminggrader.parser.JavaToken;

public class AnalyzedFile {
	private List<String> imports;
	private Map<String,String> variableTypes;
	private Map<String,Integer> variableDefinitions;
	private List<List<JavaToken>> stringTokens;
	
	public AnalyzedFile() {
		this.imports = new ArrayList<String>();
		this.variableTypes = new HashMap<String,String>();
		this.stringTokens = new ArrayList<List<JavaToken>>();
		this.variableDefinitions = new HashMap<String,Integer>();
	}
	public List<String> getImports() {
		return imports;
	}
	public Map<String, String> getVariableTypes() {
		return variableTypes;
	}
	public List<List<JavaToken>> getStringTokens() {
		return stringTokens;
	}
	public Map<String, Integer> getVariableDefinitions() {
		return variableDefinitions;
	}
	
	public void addAll(AnalyzedFile file) {
		for(String s : file.getImports()) {
			if(!this.imports.contains(s))
				this.imports.add(s);
		}
		for(String v : file.getVariableTypes().keySet()) {
			this.variableTypes.put(v,file.getVariableTypes().get(v));
		}
		for(String v : file.getVariableDefinitions().keySet()) {
			this.variableDefinitions.put(v,file.getVariableDefinitions().get(v));
		}
		this.stringTokens.addAll(file.getStringTokens());
	}
}
