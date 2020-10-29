package de.oftik.jproconv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class Properties2JsonTest {
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

	@Test
	public void emptyProperties() throws Exception {
		final File tmpFile = File.createTempFile("locale-01", ".json");
		File sourceFile = new File(
				Thread.currentThread().getContextClassLoader().getResource("locale-01.properties").toURI());
		Properties2Json p2j = new Properties2Json(sourceFile.getParentFile(), tmpFile.getParentFile(), "locale-01");
		p2j.run();
		tmpFile.delete();
		assertThat(p2j.getProcessedFiles(), empty());
	}

	@Test
	public void singleProperty() throws Exception {
		final File tmpFile = File.createTempFile("locale-02", ".json");
		File sourceFile = new File(
				Thread.currentThread().getContextClassLoader().getResource("locale-02.properties").toURI());
		Properties2Json p2j = new Properties2Json(sourceFile.getParentFile(), tmpFile.getParentFile(), "locale-02");
		p2j.run();
		tmpFile.delete();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Type type = new TypeToken<Map<String, Object>>() {
		}.getType();
		Map<String, Object> map = (Map<String, Object>) gson
				.fromJson(new FileReader(p2j.getProcessedFiles().iterator().next().getOutFile()), type);
		p2j.getProcessedFiles().iterator().next().getOutFile().delete();
		assertThat(p2j.getProcessedFiles(), Matchers.hasSize(1));
		assertThat(map, hasEntry("param", "value"));
	}

	@Test
	public void structuredProperties() throws Exception {
		final File tmpFile = File.createTempFile("locale-03", ".json");
		File sourceFile = new File(
				Thread.currentThread().getContextClassLoader().getResource("locale-03.properties").toURI());
		Properties2Json p2j = new Properties2Json(sourceFile.getParentFile(), tmpFile.getParentFile(), "locale-03");
		p2j.run();
		tmpFile.delete();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Type type = new TypeToken<Map<String, Object>>() {
		}.getType();
		Map<String, Object> map = (Map<String, Object>) gson
				.fromJson(new FileReader(p2j.getProcessedFiles().iterator().next().getOutFile()), type);
		p2j.getProcessedFiles().iterator().next().getOutFile().delete();
		assertThat(p2j.getProcessedFiles(), Matchers.hasSize(1));
		assertThat(map, allOf(hasKey("param"), hasKey("other")));
	}
}
