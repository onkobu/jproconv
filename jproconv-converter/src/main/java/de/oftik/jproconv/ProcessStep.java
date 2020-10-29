package de.oftik.jproconv;

import java.io.File;

/**
 * A processing step turned the input file into the output file. If the output
 * file is <code>null</code> the step was not successful.
 *
 * @author onkobu
 *
 */
public class ProcessStep {
	private final File inFile;
	private final File outFile;

	public ProcessStep(File in, File out) {
		inFile = in;
		outFile = out;
	}

	public File getInFile() {
		return inFile;
	}

	public File getOutFile() {
		return outFile;
	}

	@Override
	public String toString() {
		if (outFile == null) {
			return inFile + " no output";
		}
		return inFile + " to " + outFile;
	}
}