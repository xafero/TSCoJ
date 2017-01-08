package com.xafero.tscoj.mvn.util;

import static java.lang.Math.max;
import static org.codehaus.plexus.util.FileUtils.getFiles;

import java.io.File;
import java.io.IOException;

public class HashUtils {

	public static boolean checkIfNecessary(File target, Iterable<File> sources) {
		if (!target.exists())
			return true;
		long lastFolderChange = getLatestModified(target);
		for (File file : sources)
			if (file.lastModified() >= lastFolderChange)
				return true;
		return false;
	}

	public static long getLatestModified(File folder) {
		final String includes = "**/*.*";
		long latestModified = -1;
		try {
			for (Object file : getFiles(folder, includes, null))
				latestModified = max(latestModified, ((File) file).lastModified());
		} catch (IOException ioe) {
		}
		return latestModified;
	}
}