package com.xafero.tscoj.base;

import java.io.PrintWriter;
import java.io.Writer;

import com.xafero.tscoj.api.ISystem;
import com.xafero.tscoj.impl.DiagnoseParse;

public abstract class AbstractSystem implements ISystem {

	public String newLine = String.format("%n");

	public void setOut(Writer out) {
		if (out instanceof PrintWriter)
			setOutput((PrintWriter) out);
		else
			setOutput(new PrintWriter(out));
	}

	protected abstract void setOutput(PrintWriter out);

	public abstract void setDiagnose(DiagnoseParse diag);
}