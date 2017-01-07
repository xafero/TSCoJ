package com.xafero.tscoj.impl;

import java.net.URI;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;

public class DiagnoseParse {

	private final DiagnosticListener<URI> diag;

	public DiagnoseParse(DiagnosticListener<URI> diag) {
		this.diag = diag;
	}

	public void process(String[] parts) {
		String[] first = parts[0].split(Pattern.quote("("), 2);
		final String file = first[0];
		final String message = parts[1].trim();
		String[] second = first[1].split(Pattern.quote(","));
		final int line = Integer.parseInt(second[0]);
		final int column = Integer.parseInt(second[1]);
		diag.report(new Diagnostic<URI>() {
			@Override
			public Kind getKind() {
				return Kind.ERROR;
			}

			@Override
			public URI getSource() {
				return URI.create("file:" + file);
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
				return line;
			}

			@Override
			public long getColumnNumber() {
				return column;
			}

			@Override
			public String getCode() {
				return null;
			}

			@Override
			public String getMessage(Locale locale) {
				return message;
			}
		});
	}
}