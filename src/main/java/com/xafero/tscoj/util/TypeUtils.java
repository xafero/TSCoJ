package com.xafero.tscoj.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptException;
import javax.tools.DiagnosticListener;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

public class TypeUtils {

	public static String getName(String path) {
		return (new File(path)).getName();
	}

	public static String readResource(ClassLoader loader, String path, String encoding) {
		try (InputStream is = loader.getResourceAsStream(path)) {
			return IOUtils.toString(is, encoding);
		} catch (IOException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	public static String patchTsc(String text) {
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

	public static String[] concat(String[] first, Collection<String> second) {
		String[] array = Arrays.copyOf(first, first.length + second.size());
		Iterator<String> it = second.iterator();
		for (int i = first.length; i < array.length; i++)
			array[i] = it.next();
		return array;
	}

	public static Map<URI, Entry<CompiledScript, String>> getResults(Map<String, String> binaries,
			Collection<String> files, Compilable engine, DiagnosticListener<?> diag) {
		Map<URI, Entry<CompiledScript, String>> result = new LinkedHashMap<>();
		for (Entry<String, String> e : binaries.entrySet()) {
			String path = e.getKey();
			if (!contains(files, path))
				continue;
			String text = e.getValue().trim();
			URI origin = URI.create("mem:" + path);
			CompiledScript binary = tryCompile(engine, text, diag, origin);
			Entry<CompiledScript, String> entry;
			entry = new SimpleImmutableEntry<>(binary, text);
			result.put(origin, entry);
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static CompiledScript tryCompile(Compilable engine, String code, DiagnosticListener diag, URI uri) {
		try {
			return engine.compile(code);
		} catch (ScriptException e) {
			diag.report(new ScriptMessage(uri, e));
			return null;
		}
	}

	private static boolean contains(Iterable<String> files, String path) {
		String base = getWithoutExt(path);
		for (String file : files) {
			String fbase = getWithoutExt(file);
			if (fbase.equalsIgnoreCase(base)) {
				String pext = FilenameUtils.getExtension(path);
				if (pext.equalsIgnoreCase("js") && !file.contains("/"))
					return true;
			}
		}
		return false;
	}

	private static String getWithoutExt(String path) {
		String ext = FilenameUtils.getExtension(path);
		return path.substring(0, path.length() - ext.length());
	}
}