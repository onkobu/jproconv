package de.oftik.jproconv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests target file generation and sub-path structuring.
 *
 * @author onkobu
 *
 */
public class TargetFileGeneratorTest {
	private final TargetFileGenerator targetFileGenerator = new TargetFileGenerator(".left", ".right");

	@ParameterizedTest
	@MethodSource("mapSourceToTarget")
	public void determineFile(String rootDirName, String sourceFileName, String targetDirName, String targetFile) {
		assertThat(targetFileGenerator.determineTargetFile(new File(rootDirName), new File(sourceFileName), new File(targetDirName)).getPath(),
				equalTo(targetFile));
	}

	static Stream<Arguments> mapSourceToTarget() {
		return Stream.of(Arguments.of(".", "./a.left", ".", "./a.right"),
				Arguments.of("./source", "./source/deep/a.left", "./target/", "./target/deep/a.right"),
				Arguments.of("./source/", "./source/deep/a.left", "./target", "./target/deep/a.right"),
				Arguments.of("./source/flat", "./source/flat/a.left", "./target/", "./target/a.right"),
				Arguments.of("./source/flat/", "./source/flat/a.left", "./target/", "./target/a.right"));
	}
}
