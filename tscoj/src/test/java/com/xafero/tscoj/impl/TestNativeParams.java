package com.xafero.tscoj.impl;

import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.tools.Diagnostic;

import com.xafero.natra.api.INativeParams;

public class TestNativeParams implements INativeParams {

	private final Map<URI, String> sources;
	private final Writer out;

	public TestNativeParams(String code) {
		sources = new LinkedHashMap<>();
		sources.put(URI.create("mem:" + "script.ts"), code);
		out = new StringWriter();
	}

	@Override
	public void report(Diagnostic<? extends URI> diag) {
		System.out.println(diag);
	}

	@Override
	public Map<URI, String> getSources() {
		return sources;
	}

	@Override
	public Writer getOutput() {
		return out;
	}

	@Override
	public int getErrors() {
		return 0;
	}

	@Override
	public int getWarnings() {
		return 0;
	}

	@Override
	public String toString() {
		return out.toString();
	}
}