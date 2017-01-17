package com.xafero.tscoj.mvn;

import static com.xafero.tscoj.mvn.util.HashUtils.checkIfNecessary;
import static com.xafero.tscoj.util.TypeUtils.concat;
import static com.xafero.tscoj.util.TypeUtils.getName;
import static com.xafero.tscoj.util.TypeUtils.patchTsc;
import static com.xafero.tscoj.util.TypeUtils.readResource;
import static org.codehaus.plexus.util.FileUtils.fileRead;
import static org.codehaus.plexus.util.FileUtils.fileWrite;
import static org.codehaus.plexus.util.FileUtils.getFiles;
import static org.codehaus.plexus.util.StringUtils.stripStart;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.sonatype.plexus.build.incremental.BuildContext;

import com.xafero.tscoj.api.IExecutor;
import com.xafero.tscoj.base.AbstractSystem;
import com.xafero.tscoj.impl.DiagnoseParse;

public abstract class AbstractCompileMojo extends AbstractMojo implements DiagnosticListener<URI> {

	private static final String TSV = "2.1.4";
	private static final String TSP = "META-INF/resources/webjars/typescript/" + TSV + "/lib";
	private static final String TSC = TSP + "/" + "tsc.js";
	private static final String LDT = TSP + "/" + "lib.d.ts";

	private static final String SYS = "META-INF/resources/utils.js";
	private static final String ENC = "UTF8";

	protected abstract File getSourceDirectory();

	protected abstract File getOutputDirectory();

	protected abstract void addSourceFolderToProject(MavenProject project);

	@Component
	private BuildContext buildContext;

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject project;

	@Parameter(defaultValue = "tmp/")
	private String prefixIgnore;

	private ClassLoader loader = AbstractSystem.class.getClassLoader();
	private ScriptEngineManager mgr = new ScriptEngineManager(loader);
	private ScriptEngine engine = mgr.getEngineByExtension("js");
	private Bindings scope = engine.createBindings();

	private IExecutor exec;
	private AbstractSystem sys;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		info("Using '%s' for JavaScript...", engine.getClass().getName());
		DiagnosticListener<URI> diag = this;
		Writer out = new PrintWriter(System.out);
		File src = getSourceDirectory();
		File dest = getOutputDirectory();
		int count = 0;
		try {
			final String includes = "**/*.js,**/*.ts";
			final String excludes = null;
			@SuppressWarnings("unchecked")
			List<File> files = src.exists() ? getFiles(src, includes, excludes) : Collections.EMPTY_LIST;
			if (!files.isEmpty() && checkIfNecessary(dest, files)) {
				info("Reading files from '%s'...", src);
				buildContext.removeMessages(src);
				prepareTypeScriptCompiler(loader, (Compilable) engine, scope);
				IOUtils.closeQuietly(sys);
				sys.setOut(out);
				sys.setDiagnose(new DiagnoseParse(diag));
				sys.push(getName(LDT), readResource(loader, LDT, ENC));
				List<String> fileNames = new ArrayList<>(files.size());
				for (File file : files) {
					String relative = file.getAbsolutePath().replace(src + File.separator, "");
					fileNames.add(relative);
					String code = fileRead(file, ENC);
					sys.push(relative, code);
					info(" Adding '%s' to be compiled...", relative);
				}
				// "--pretty"
				final String[] baseArgs = { "--allowJs", "--declaration", "--diagnostics", "--emitDecoratorMetadata",
						"--experimentalDecorators", "--listFiles", "--listEmittedFiles", "--outFile", "deploy.js",
						"--declarationDir", "info", "--removeComments", "--sourceMap", "--stripInternal",
						"--traceResolution", "--target", "es3" };
				String[] args = concat(baseArgs, fileNames);
				exec.executeCommandLine(args);
				Map<String, String> dump = sys.dump();
				for (Entry<String, String> e : dump.entrySet()) {
					String name = stripStart(e.getKey(), "/\\");
					if (fileNames.contains(name) || name.startsWith(getPrefixIgnore()))
						continue;
					info("  Storing compiled '%s' (#%s)...", name, count + 1);
					String code = e.getValue();
					File tgt = new File(dest, name);
					tgt.getParentFile().mkdirs();
					fileWrite(tgt.getAbsolutePath(), ENC, code);
					count++;
				}
			}
			if (count > 0) {
				buildContext.refresh(dest);
				info("Processed %d file(s) into '%s'!", count, dest);
			} else {
				info("No file(s) needs to be processed into '%s'.", dest);
			}
			addSourceFolderToProject(project);
			info("Source directory '%s' added.", dest);
		} catch (Exception e) {
			throw new MojoExecutionException(src + " & " + dest, e);
		}
	}

	private void prepareTypeScriptCompiler(ClassLoader loader, Compilable engine, Bindings scope)
			throws ScriptException {
		String tsc = patchTsc(readResource(loader, TSC, ENC));
		CompiledScript tscScript = engine.compile(tsc);
		scope.put("exporter", this);
		tscScript.eval(scope);
	}

	private void info(String format, Object... args) {
		if (getLog().isInfoEnabled())
			getLog().info(String.format(format, args));
	}

	@Override
	public void report(Diagnostic<? extends URI> diag) {
		info("[%s] (%s:%s) %s", diag.getKind(), diag.getLineNumber(), diag.getColumnNumber(), diag.getMessage(null));
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

	public String getPrefixIgnore() {
		return prefixIgnore;
	}
}
