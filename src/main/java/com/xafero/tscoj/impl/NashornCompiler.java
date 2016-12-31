package com.xafero.tscoj.impl;

import static com.xafero.tscoj.util.TypeUtils.concat;
import static com.xafero.tscoj.util.TypeUtils.getName;
import static com.xafero.tscoj.util.TypeUtils.getResults;
import static com.xafero.tscoj.util.TypeUtils.patchTsc;
import static com.xafero.tscoj.util.TypeUtils.readResource;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.tools.DiagnosticListener;

import org.apache.commons.io.IOUtils;

import com.xafero.natra.api.INativeParams;
import com.xafero.natra.common.AbstractNativeTranslator;
import com.xafero.tscoj.api.IExecutor;
import com.xafero.tscoj.base.AbstractSystem;
import com.xafero.tscoj.base.TypeScriptFile;
import com.xafero.tscoj.util.ScriptMessage;

public class NashornCompiler<T extends ScriptEngine & Compilable>
		extends AbstractNativeTranslator<CompiledScript, String> implements Closeable {

	private static final String TSV = "2.1.4";
	private static final String TSP = "META-INF/resources/webjars/typescript/" + TSV + "/lib";
	private static final String TSC = TSP + "/" + "tsc.js";
	private static final String LDT = TSP + "/" + "lib.d.ts";

	private static final String SYS = "META-INF/resources/utils.js";
	private static final String ENC = "UTF8";

	private final ClassLoader loader;
	private final T engine;
	private final Bindings scope;

	private IExecutor exec;
	private AbstractSystem sys;

	public NashornCompiler(NashornCompilerFactory factory) {
		this(factory, NashornCompiler.class);
	}

	public NashornCompiler(NashornCompilerFactory factory, Class<?> type) {
		this(factory, type.getClassLoader());
	}

	public NashornCompiler(NashornCompilerFactory factory, ClassLoader loader) {
		this(factory, loader, new ScriptEngineManager(loader));
	}

	@SuppressWarnings("unchecked")
	public NashornCompiler(NashornCompilerFactory factory, ClassLoader loader, ScriptEngineManager manager) {
		this(factory, loader, (T) manager.getEngineByExtension("js"));
	}

	public NashornCompiler(NashornCompilerFactory factory, ClassLoader loader, T engine) {
		super(factory);
		this.loader = loader;
		this.engine = engine;
		this.scope = engine.createBindings();
	}

	@Override
	protected synchronized Map<URI, Entry<CompiledScript, String>> translateImpl(INativeParams params) {
		Map<URI, String> sources = params.getSources();
		Writer out = params.getOutput();
		DiagnosticListener<URI> diag = params;
		List<TypeScriptFile> files = toFiles(sources);
		List<String> fileNames = new ArrayList<>(files.size());
		if (exec == null)
			try {
				prepareTypeScriptCompiler();
			} catch (ScriptException e) {
				diag.report(new ScriptMessage(e));
				return Collections.emptyMap();
			}
		IOUtils.closeQuietly(sys);
		sys.setOut(out);
		sys.setDiagnose(new DiagnoseParse(diag));
		sys.push(getName(LDT), readResource(loader, LDT, ENC));
		for (TypeScriptFile file : files) {
			fileNames.add(file.getPath());
			sys.push(file.getPath(), file.getCode());
		}
		// "--diagnostics", "--listFiles", "--listEmittedFiles"
		final String[] baseArgs = { "--declaration", "--traceResolution", "--removeComments", "--target", "es3" };
		String[] args = concat(baseArgs, fileNames);
		exec.executeCommandLine(args);
		return getResults(sys.dump(), fileNames, engine, diag);
	}

	@Override
	public Object run(Object binary, DiagnosticListener<URI> diag) {
		try {
			return ((CompiledScript) binary).eval();
		} catch (ScriptException e) {
			diag.report(new ScriptMessage(e));
			return null;
		}
	}

	@Override
	public void close() throws IOException {
		scope.clear();
		sys.close();
	}

	private void prepareTypeScriptCompiler() throws ScriptException {
		String tsc = patchTsc(readResource(loader, TSC, ENC));
		CompiledScript tscScript = engine.compile(tsc);
		scope.put("exporter", this);
		tscScript.eval(scope);
	}

	private List<TypeScriptFile> toFiles(Map<URI, String> sources) {
		List<TypeScriptFile> files = new LinkedList<>();
		for (Entry<URI, String> e : sources.entrySet()) {
			String url = e.getKey().getRawSchemeSpecificPart();
			url = url.replace('/', '.');
			int lidx = url.lastIndexOf('.');
			String pkg = url.substring(0, lidx);
			String name = url.substring(lidx + 1);
			String code = e.getValue();
			files.add(new TypeScriptFile(pkg, name, code));
		}
		return files;
	}

	private <O> O castJs(Class<O> type, String key) {
		try {
			String txt = String.format("new %s(%s)", type.getName(), key);
			return type.cast(engine.eval(txt, scope));
		} catch (ScriptException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	public void inject(Bindings tsc) {
		sys = castJs(AbstractSystem.class, readResource(loader, SYS, ENC));
		tsc.put("sys", sys);
	}

	public void receive(Object tsc) throws ScriptException {
		scope.put("ts", tsc);
		exec = castJs(IExecutor.class, "ts");
	}
}