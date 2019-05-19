package de.peass.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.peass.DependencyReadingStarter;
import de.peass.dependency.analysis.data.ChangedEntity;
import de.peass.dependency.analysis.data.TestSet;
import de.peass.dependency.persistence.ExecutionData;

public class GetNonSelectedTests {

	private static final Logger LOG = LogManager.getLogger(GetNonSelectedTests.class);

	private static final ObjectMapper MAPPER = new ObjectMapper();
	static {
		MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
	}

	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		final File allTestsFile = new File(args[0]);
		final File selectedTestsFile = new File(args[1]);

		final ExecutionData all = MAPPER.readValue(allTestsFile, ExecutionData.class);
		final ExecutionData selected = MAPPER.readValue(selectedTestsFile, ExecutionData.class);

		final ExecutionData nonSelected = new ExecutionData();
		nonSelected.setUrl(all.getUrl());
		for (final Map.Entry<String, TestSet> allVersion : all.getVersions().entrySet()) {
			LOG.debug("Version: {}", allVersion.getKey());
			final TestSet allTestSet = allVersion.getValue();
			if (selected.getVersions().containsKey(allVersion.getKey())) {
				final TestSet selectedVersion = selected.getVersions().get(allVersion.getKey());
				for (final Entry<ChangedEntity, Set<String>> selectedTests : selectedVersion.getTestcases().entrySet()) {
					final String testclazz = selectedTests.getKey().getJavaClazzName();
					final Set<String> remainingTests = allTestSet.getTestcases().get(selectedTests.getKey());
					System.out.println("Searching: " + testclazz + " " + remainingTests);
					if (remainingTests != null) { //TODO Workaround: Test-suite and extended tests are also selected, but shouldn't
						if (!remainingTests.containsAll(selectedTests.getValue())) {
							LOG.error("Testclazz " + testclazz);
							LOG.error("Selected contain more than all: " + remainingTests + " " + selectedTests.getValue());
						}

						remainingTests.removeAll(selectedTests.getValue());
						if (remainingTests.size() == 0) {
							allTestSet.getTestcases().remove(selectedTests.getKey());
						}
					}
				}
			}
			LOG.debug("Size: " + allTestSet.classCount());
			nonSelected.getVersions().put(allVersion.getKey(), allTestSet);
		}
		MAPPER.writeValue(new File(DependencyReadingStarter.getResultFolder(), "out.json"), nonSelected);
	}
}
