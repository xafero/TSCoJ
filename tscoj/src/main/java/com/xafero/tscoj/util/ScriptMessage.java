package com.xafero.tscoj.util;

import java.net.URI;
import java.util.Locale;

import javax.script.ScriptException;
import javax.tools.Diagnostic;

public class ScriptMessage implements Diagnostic<URI> {

	private final URI source;
	private final ScriptException error;

	public ScriptMessage(ScriptException error) {
		this(null, error);
	}

	public ScriptMessage(URI source, ScriptException error) {
		this.source = source;
		this.error = error;
	}

	@Override
	public Kind getKind() {
		return Kind.ERROR;
	}

	@Override
	public URI getSource() {
		return source == null ? URI.create("file:" + error.getFileName()) : source;
	}

	@Override
	public long getPosition() {
		return -1;
	}

	@Override
	public long getStartPosition() {
		return -1;
	}

	@Override
	public long getEndPosition() {
		return -1;
	}

	@Override
	public long getLineNumber() {
		return error.getLineNumber();
	}

	@Override
	public long getColumnNumber() {
		return error.getColumnNumber();
	}

	@Override
	public String getCode() {
		return null;
	}

	@Override
	public String getMessage(Locale locale) {
		return error.getMessage();
	}
}