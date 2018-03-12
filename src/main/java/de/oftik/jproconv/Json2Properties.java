package de.oftik.jproconv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * JSON to property converter.
 *
 * @author onkobu
 *
 */
public class Json2Properties {
	private static final Logger logger = Logger.getLogger(Json2Properties.class.getName());

	static final String DEFAULT_FILE_NAME_START = "locale-";

	final List<String> errors;
	private final File sourceDir;
	private final File targetDir;
	private final Collection<ProcessStep> processedFiles;
	private final TargetFileGenerator targetFileGenerator = new TargetFileGenerator(".json", ".properties");
	private String fileNameStart;

	public Json2Properties(File sourceDir, File targetDir, String fileNameStart) {
		errors = new ArrayList<>();
		if (!sourceDir.exists()) {
			errors.add(sourceDir + " does not exist");
		} else if (!sourceDir.isDirectory()) {
			errors.add(sourceDir + " is not a directory");
		}

		if (!targetDir.exists()) {
			errors.add(targetDir + "does not exist");
		} else if (!targetDir.isDirectory()) {
			errors.add(targetDir + " is not a directory");
		}
		this.sourceDir = sourceDir;
		this.targetDir = targetDir;
		this.fileNameStart = fileNameStart;
		processedFiles = new ArrayList<>();
	}

	public Collection<ProcessStep> getProcessedFiles() {
		return processedFiles;
	}

	public static void main(String[] args) {
		if (args == null || args.length != 2) {
			printUsage();
			return;
		}

		final Json2Properties engine = new Json2Properties(new File(args[0]), new File(args[1]),
				args.length > 2 ? args[2] : DEFAULT_FILE_NAME_START);
		engine.run();
		logger.info("Success, have a look at " + args[1]);
	}

	void run() {
		if (!errors.isEmpty()) {
			printErrors(errors);
			return;
		}
		Gson gson = new GsonBuilder().create();
		recurseInto(sourceDir, sourceDir, targetDir, gson);
	}

	private void recurseInto(File rootDir, File sourceFile, File targetDir, Gson gson) {
		if (sourceFile.isDirectory()) {
			for (File f : sourceFile.listFiles()) {
				recurseInto(rootDir, f, targetDir, gson);
			}
			return;
		}
		if (!sourceFile.isFile()) {
			return;
		}
		if (!sourceFile.getName().toLowerCase().endsWith(".json")) {
			return;
		}
		if (!sourceFile.getName().toLowerCase().startsWith(fileNameStart)) {
			return;
		}
		try (FileReader fr = new FileReader(sourceFile); BufferedReader br = new BufferedReader(fr);) {
			logger.info("transforming " + sourceFile + " to .properties");
			Type type = new TypeToken<Map<String, Object>>() {
			}.getType();
			Map<String, Object> propertyMap = gson.fromJson(br, type);
			if (propertyMap == null || propertyMap.isEmpty()) {
				logger.warning("skipping empty file " + sourceFile);
				return;
			}
			processedFiles.add(new ProcessStep(sourceFile, storeInto(rootDir, sourceFile, targetDir, propertyMap)));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private File storeInto(File rootDir, File sourceFile, File targetDir, Map<String, Object> propertyMap) {
		final File targetFile = targetFileGenerator.determineTargetFile(rootDir, sourceFile, targetDir);
		File parentDir = targetFile.getParentFile();
		if (!parentDir.exists()) {
			parentDir.mkdirs();
		}
		try (FileWriter fw = new FileWriter(targetFile); BufferedWriter bw = new BufferedWriter(fw);) {
			List<String> props = new ArrayList<>();
			for (Entry<String, Object> entry : propertyMap.entrySet()) {
				props.addAll(renderEntry(null, entry));
			}
			Collections.sort(props);
			for (String prop : props) {
				bw.write(prop);
				bw.write("\n");
			}
			return targetFile;
		} catch (IOException ex) {
			logger.throwing(Json2Properties.class.getSimpleName(), "storeInto", ex);
		}
		return null;
	}

	private static List<String> renderEntry(String parent, Entry<String, Object> entry) {
		if (entry.getValue() instanceof Map) {
			List<String> res = new ArrayList<>();
			for (Entry<String, Object> subEntry : ((Map<String, Object>) entry.getValue()).entrySet()) {
				res.addAll(renderEntry(parent == null ? entry.getKey() : parent + "." + entry.getKey(), subEntry));
			}
			return res;
		}
		/*
		 * Dafür gibt es keine triviale Entsprechung, da die Indizes wichtig
		 * sein könnten. Eine .properties-Datei könnte Zahlen an die Schlüssel
		 * anhängen. Das Original könnte alternativ eine mit Komma getrennte
		 * Zeichenkette verwenden.
		 */
		if (entry.getValue() instanceof Collection) {
			throw new IllegalArgumentException(
					parent + "." + entry.getKey() + " is of type Collection: " + entry.getValue());
		}
		return Arrays.asList(parent + "." + entry.getKey() + " = " + entry.getValue());
	}

	private static void printErrors(List<String> errors) {
		for (String err : errors) {
			logger.severe("ERROR " + err);
		}
	}

	private static void printUsage() {
		System.out.println("\nJson2Properties\n");
		System.out.println("\t $> Json2Properties <source-dir> <target-dir> [file-name-start]\n");
		System.out.println("Reads all .json-Files in <source-dir> and converts them to Java .properties");
		System.out.println("files in <target-dir>. Sub directories from <source-dir> are created as necessary");
		System.out.println("to resemble <source-dir>'s structure.\n");
		System.out.println("If [file-name-start] is not given, files in <source-dir> must start with");
		System.out.println(DEFAULT_FILE_NAME_START + " and end with .json (ignore case).");
	}
}
