package de.oftik.jproconv;

import java.io.File;

/**
 * Turns a source file based on it's root directory and a target directory into
 * the matching target file. Source' root directory is always shorter (strlen)
 * than source file since the source file resides in either the root directory
 * or one of its sub directories.
 *
 * @author onkobu
 *
 */
public class TargetFileGenerator {
	private final String fromEnding;
	private final String toEnding;

	public TargetFileGenerator(String fromEnding, String toEnding) {
		super();
		this.fromEnding = fromEnding;
		this.toEnding = toEnding;
	}

	/*
	 * Create target file.
	 */
	File determineTargetFile(File rootDir, File sourceFile, File targetDir) {
		String rootPath = rootDir.getAbsolutePath();
		String sourcePath = sourceFile.getAbsolutePath();
		File targetFile = new File(targetDir, sourcePath.substring(rootPath.length()).replace(fromEnding, toEnding));
		return targetFile;
	}
}
