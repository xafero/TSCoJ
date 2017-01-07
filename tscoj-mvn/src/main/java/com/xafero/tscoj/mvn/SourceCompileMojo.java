package com.xafero.tscoj.mvn;

import java.io.File;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "compile", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class SourceCompileMojo extends AbstractCompileMojo {

	@Parameter(defaultValue = "${basedir}/src/main/ts")
	private File sourceDirectory;

	@Parameter(defaultValue = "${project.build.directory}/generated-sources/ts")
	private File outputDirectory;

	@Override
	protected File getSourceDirectory() {
		return sourceDirectory;
	}

	@Override
	protected File getOutputDirectory() {
		return outputDirectory;
	}

	@Override
	protected void addSourceFolderToProject(MavenProject project) {
		project.addCompileSourceRoot(getOutputDirectory().getAbsolutePath());
	}
}