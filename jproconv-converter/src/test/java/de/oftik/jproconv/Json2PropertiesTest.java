package de.oftik.jproconv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.Properties;

import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class Json2PropertiesTest {
	@AfterClass
	@BeforeClass
	public static void cleanTemp() throws Exception {
		final File tmpFile = File.createTempFile("locale-00", ".json");
		File[] tmpFiles = tmpFile.getParentFile().listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().startsWith("locale-")
						&& (pathname.getName().endsWith(".json") || pathname.getName().endsWith(".properties"));
			}
		});
		for (File f : tmpFiles) {
			// It is not necessarily so, that writeable files can be deleted.
			// But this code deals with temporary files it created.
			if (f.canRead() && f.canWrite()) {
				f.delete();
			}
		}
	}

	// empty file, not a single byte
	@Test
	public void readEmptyFile() throws Exception {
		final File tmpFile = File.createTempFile("locale-01", ".properties");
		File sourceFile = new File(
				Thread.currentThread().getContextClassLoader().getResource("locale-01.json").toURI());
		Json2Properties j2p = new Json2Properties(sourceFile.getParentFile(), tmpFile.getParentFile(), "locale-01");
		j2p.run();
		tmpFile.delete();
		assertThat(j2p.getProcessedFiles(), empty());
	}

	// empty JSON, {}
	@Test
	public void readEmptyJson() throws Exception {
		final File tmpFile = File.createTempFile("locale-02", ".properties");
		File sourceFile = new File(
				Thread.currentThread().getContextClassLoader().getResource("locale-02.json").toURI());
		Json2Properties j2p = new Json2Properties(sourceFile.getParentFile(), tmpFile.getParentFile(), "locale-02");
		j2p.run();
		tmpFile.delete();
		assertThat(j2p.getProcessedFiles(), empty());
	}

	// non-mpty JSON
	@Test
	public void readJson() throws Exception {
		final File tmpFile = File.createTempFile("locale-03", ".properties");
		File sourceFile = new File(
				Thread.currentThread().getContextClassLoader().getResource("locale-03.json").toURI());
		Json2Properties j2p = new Json2Properties(sourceFile.getParentFile(), tmpFile.getParentFile(), "locale-03");
		j2p.run();
		tmpFile.delete();
		j2p.getProcessedFiles().stream().map(ProcessStep::getOutFile).forEach(File::delete);
		assertThat(j2p.getProcessedFiles(), hasSize(1));
	}

	// non-mpty JSON
	@Test
	public void readJsonContent() throws Exception {
		final File tmpFile = File.createTempFile("locale-03", ".properties");
		File sourceFile = new File(
				Thread.currentThread().getContextClassLoader().getResource("locale-03.json").toURI());
		Json2Properties j2p = new Json2Properties(sourceFile.getParentFile(), tmpFile.getParentFile(), "locale-03");
		j2p.run();
		tmpFile.delete();
		ProcessStep step = j2p.getProcessedFiles().iterator().next();
		Properties props = new Properties();
		props.load(new FileReader(step.getOutFile()));
		step.getOutFile().delete();
		assertThat(props.getProperty("key.subKeyA"), Matchers.equalTo("valueA"));
		assertThat(props.getProperty("key.subKeyB"), Matchers.equalTo("valueB"));
		assertThat(props.getProperty("key2.subKey2A"), Matchers.equalTo("value2A"));
		assertThat(props.getProperty("key2.subKey2B"), Matchers.equalTo("value2B"));
	}
}
