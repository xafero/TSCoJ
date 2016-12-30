package com.xafero.tscoj.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;

import com.xafero.tscoj.api.IExecutor;
import com.xafero.tscoj.api.ITranspiler;
import com.xafero.tscoj.base.AbstractSystem;

public class NashornCompiler<T extends ScriptEngine & Compilable> implements ITranspiler {

	private static final String TSV = "2.1.4";
	private static final String TSP = "META-INF/resources/webjars/typescript/" + TSV + "/lib";

	private static final String TSC = TSP + "/" + "tsc.js";
	public static final String LDT = TSP + "/" + "lib.d.ts";

	private static final String SYS = "META-INF/resources/utils.js";
	private static final String ENC = "UTF8";
	private static final String CODE_PREFIX = "code";
	private static final String CODE = CODE_PREFIX + ".ts";

	private final T engine;
	private final Bindings scope;
	private final ClassLoader loader;

	private AbstractSystem sys;
	private IExecutor exec;

	public NashornCompiler() {
		this(NashornCompiler.class);
	}

	public NashornCompiler(Class<?> type) {
		this(type.getClassLoader());
	}

	public NashornCompiler(ClassLoader loader) {
		this(loader, new ScriptEngineManager(loader));
	}

	@SuppressWarnings("unchecked")
	public NashornCompiler(ClassLoader loader, ScriptEngineManager manager) {
		this(loader, (T) manager.getEngineByExtension("js"));
	}

	public NashornCompiler(ClassLoader loader, T engine) {
		this.loader = loader;
		this.engine = engine;
		this.scope = engine.createBindings();
	}

	@Override
	public String compile(String code) {
		try {
			return toJavaScript(code).get(CODE_PREFIX + ".js");
		} catch (IOException | ScriptException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	private String readResource(String path) {
		try (InputStream is = loader.getResourceAsStream(path)) {
			return IOUtils.toString(is, ENC);
		} catch (IOException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	@Override
	public void close() throws IOException {
		scope.clear();
		sys.close();
	}

	private synchronized Map<String, String> toJavaScript(String code) throws IOException, ScriptException {
		if (exec == null) {
			String tsc = patchTsc(readResource(TSC));
			CompiledScript tscScript = engine.compile(tsc);
			scope.put("exporter", this);
			tscScript.eval(scope);
		}
		sys.close();
		sys.push(getName(LDT), readResource(LDT));
		sys.push(CODE, code);
		// "--diagnostics", "--listFiles", "--listEmittedFiles"
		String[] args = { "--declaration", "--traceResolution", "--removeComments", "--target", "es3", CODE };
		exec.executeCommandLine(args);
		Map<String, String> result = new LinkedHashMap<>();
		Map<String, String> mem = sys.dump();
		for (Entry<String, String> e : mem.entrySet()) {
			String path = e.getKey();
			if (!(path.startsWith(CODE_PREFIX) && (path.endsWith(".js") || path.endsWith(".d.ts"))))
				continue;
			String got = e.getValue().trim();
			result.put(path, got);
		}
		return result;
	}

	private String getName(String path) {
		return (new File(path)).getName();
	}

	private String patchTsc(String text) {
		String pattern = "ts.executeCommandLine(ts.sys.args);";
		int idx = text.indexOf(pattern);
		String realCode = text.substring(0, idx);
		idx = realCode.indexOf("ts.Diagnostics = {");
		idx = realCode.substring(0, idx).lastIndexOf("var ts;");
		String first = realCode.substring(0, idx);
		String second = realCode.substring(idx);
		return String.format("%s %n " + "exporter.inject(ts); %n " + "%s %n " + "exporter.receive(ts); %n", first,
				second, realCode);
	}

	public void inject(Bindings tsc) {
		if (sys != null)
			return;
		sys = castJs(AbstractSystem.class, readResource(SYS));
		tsc.put("sys", sys);
	}

	public void receive(Object tsc) throws ScriptException {
		if (exec != null)
			return;
		scope.put("ts", tsc);
		exec = castJs(IExecutor.class, "ts");
	}

	private <O> O castJs(Class<O> type, String key) {
		try {
			String txt = String.format("new %s(%s)", type.getName(), key);
			return type.cast(engine.eval(txt, scope));
		} catch (ScriptException e) {
			throw new UnsupportedOperationException(e);
		}
	}
}