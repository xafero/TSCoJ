package com.xafero.tscoj.mvn;

import java.io.File;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "testCompile", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES, threadSafe = true)
public class TestCompileMojo extends AbstractCompileMojo {

	@Parameter(defaultValue = "${basedir}/src/test/ts")
	private File sourceDirectory;

	@Parameter(defaultValue = "${project.build.directory}/generated-test-sources/ts")
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
		project.addTestCompileSourceRoot(getOutputDirectory().getAbsolutePath());
	}
}