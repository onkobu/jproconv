package de.oftik.jproconv;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class JProConv {
	// create Options object
	private static final Options options = new Options();

	static {
		options.addOption(Option.builder("r").desc("Turn .properties into JSON = reversed operation.").build());
		options.addOption(
				Option.builder("p").longOpt("prefix").hasArg().desc("File prefix, default is locale-").build());
	}

	public static void main(String[] args) throws Exception {
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);

		if (args == null || args.length < 2) {
			printUsage();
			return;
		}

		if (cmd.hasOption("r")) {
			new Properties2Json(new File(args[1]), new File(args[2]), cmd.getOptionValue("p")).run();
			return;
		} else if (args.length == 3 && "-r".equals(args[0])) {
			new Json2Properties(new File(args[0]), new File(args[1]), cmd.getOptionValue("p")).run();
			return;
		}
		printUsage();
		System.out.println("Arguments not understood\n");
	}

	private static void printUsage() {
		System.out.println("\nJSON <-> Properties Converter\n");
		System.out.println("\t $> java -jar jproconv [-r] <source-dir> <target-dir>\n");
		System.out.println("Reads all locale-*.json-files in <source-dir> and converts them to Java");
		System.out.println(".properties files in <target-dir>. Sub directories from <source-dir>");
		System.out.println("are created as necessary to resemble <source-dir>'s structure.\n");
		System.out.println("The conversion is reversed by specifing -r. Then all locale-*.properties");
		System.out.println("files in <source-dir> are converted to JSON-format in <target-dir>\n");

		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("jproconv [-r] [-p|--prefix prefix] <source-dir> <target-dir>", options);
	}
}