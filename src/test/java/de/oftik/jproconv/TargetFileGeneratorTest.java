package de.oftik.jproconv;

import java.io.File;
import java.util.Collection;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.oftik.onkotest.util.TestParameters;

/**
 * Tests target file generation and sub-path structuring.
 *
 * @author onkobu
 *
 */
@RunWith(Parameterized.class)
public class TargetFileGeneratorTest {
	private final File rootDir;
	private final File sourceFile;
	private final File targetDir;
	private final String targetFile;
	private final TargetFileGenerator targetFileGenerator = new TargetFileGenerator(".left", ".right");

	public interface P extends TestParameters.Builder {
		P add(String rootDir, String sourceFile, String targetDir, String targetFile);
	}

	public TargetFileGeneratorTest(String rootDir, String sourceFile, String targetDir, String targetFile) {
		super();
		this.rootDir = new File(rootDir);
		this.sourceFile = new File(sourceFile);
		this.targetDir = new File(targetDir);
		this.targetFile = targetFile;
	}

	@Parameters(name = "{index} {0} {1} {2} {3}")
	public static Collection<Object[]> parameters() {
		return TestParameters.begin(P.class)//
				.add(".", "./a.left", ".", "./a.right")//
				.add("./source", "./source/deep/a.left", "./target/", "./target/deep/a.right")//
				.add("./source/", "./source/deep/a.left", "./target", "./target/deep/a.right")//
				.add("./source/flat", "./source/flat/a.left", "./target/", "./target/a.right")//
				.add("./source/flat/", "./source/flat/a.left", "./target/", "./target/a.right")//
				.end();
	}

	@Test
	public void determineFile() {
		Assert.assertThat(targetFileGenerator.determineTargetFile(rootDir, sourceFile, targetDir).getPath(),
				Matchers.equalTo(targetFile));
	}
}
