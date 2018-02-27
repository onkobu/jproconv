package de.oftik.jproconv;

import java.io.File;

public class JProConv {
	public static void main(String[] args) {
		if (args == null || args.length < 2 || args.length > 3) {
			printUsage();
			return;
		}
		if (args.length == 2) {
			new Json2Properties(new File(args[0]), new File(args[1])).run();
			return;
		} else if (args.length == 3 && "-r".equals(args[0])) {
			new Properties2Json(new File(args[1]), new File(args[2])).run();
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
	}
}