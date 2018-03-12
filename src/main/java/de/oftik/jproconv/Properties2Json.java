package de.oftik.jproconv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Properties2Json {
	private static final Logger logger = Logger.getLogger(Properties2Json.class.getName());

	final List<String> errors;
	private final File sourceDir;
	private final File targetDir;
	private final String fileNameStart;
	private final Collection<ProcessStep> processedFiles;
	private final TargetFileGenerator targetFileGenerator = new TargetFileGenerator(".properties", ".json");

	public Properties2Json(File sourceDir, File targetDir, String fileNameStart) {
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

		final Properties2Json engine = new Properties2Json(new File(args[0]), new File(args[1]),
				args.length > 2 ? args[2] : Json2Properties.DEFAULT_FILE_NAME_START);
		engine.run();
		logger.info("Success, have a look at " + args[1]);
	}

	void run() {
		if (!errors.isEmpty()) {
			printErrors(errors);
			return;
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
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
		if (!sourceFile.getName().toLowerCase().endsWith(".properties")) {
			return;
		}
		if (!sourceFile.getName().toLowerCase().startsWith(fileNameStart)) {
			return;
		}
		try (FileReader fr = new FileReader(sourceFile); BufferedReader br = new BufferedReader(fr);) {
			logger.info("transforming " + sourceFile + " to JSON");
			Properties props = new Properties();
			props.load(br);
			if (props.isEmpty()) {
				logger.info("Skipping empty properties " + sourceFile);
				return;
			}
			SortedMap<String, Object> propertyMap = new TreeMap<>();
			storeInto(props, propertyMap);

			File targetFile = targetFileGenerator.determineTargetFile(rootDir, sourceFile, targetDir);
			File parentDir = targetFile.getParentFile();
			if (!parentDir.exists()) {
				parentDir.mkdirs();
			}
			try (FileWriter jfw = new FileWriter(targetFile); BufferedWriter bjw = new BufferedWriter(jfw);) {
				gson.toJson(propertyMap, bjw);
				processedFiles.add(new ProcessStep(sourceFile, targetFile));
			}
		} catch (IOException ex) {
			logger.throwing(Properties2Json.class.getSimpleName(), "recurseInto", ex);
			processedFiles.add(new ProcessStep(sourceFile, null));
		}
	}

	private static void storeInto(Properties props, Map<String, Object> propertyMap) {
		for (Object key : props.keySet()) {
			String strKey = key.toString();
			if (strKey.indexOf('.') == -1) {
				propertyMap.put(strKey, props.get(strKey));
			} else {
				List<String> keys = Arrays.stream(strKey.split("\\.")).collect(Collectors.toList());
				findTarget(propertyMap, keys.subList(0, keys.size() - 1)).put(keys.get(keys.size() - 1),
						props.getProperty(strKey));
			}
		}
	}

	private static Map<String, Object> findTarget(Map<String, Object> propertyMap, List<String> keys) {
		Map<String, Object> nextMap = (Map<String, Object>) propertyMap.get(keys.get(0));
		if (nextMap == null) {
			nextMap = new TreeMap<String, Object>();
			propertyMap.put(keys.get(0), nextMap);
		}
		if (keys.size() > 1) {
			return findTarget(nextMap, keys.subList(1, keys.size()));
		}
		return nextMap;
	}

	private static void printErrors(List<String> errors) {
		for (String err : errors) {
			logger.severe("ERROR " + err);
		}
	}

	private static void printUsage() {
		System.out.println("\nProperties2Json\n");
		System.out.println("\t $> Properties2Json <source-dir> <target-dir>\n");
		System.out.println("Reads all locale-*.properties-Files in <source-dir> and converts them to JSON");
		System.out.println("files in <target-dir>. Sub directories from <source-dir> are created as necessary");
		System.out.println("to resemble <source-dir>'s structure.\n");
	}
}
