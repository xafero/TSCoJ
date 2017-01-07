package com.xafero.tscoj.base;

public class TypeScriptFile {

	private final String pkg;
	private final String name;
	private final String code;

	public TypeScriptFile(String pkg, String name, String code) {
		this.pkg = pkg;
		this.name = name;
		this.code = code;
	}

	public String getPackage() {
		return pkg;
	}

	public String getName() {
		return name;
	}

	public String getFullName() {
		return pkg + "." + name;
	}

	public String getCode() {
		return code;
	}

	public String getPath() {
		return getFullName() + ".ts";
	}
}