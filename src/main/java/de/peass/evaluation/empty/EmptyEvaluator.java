package de.peass.evaluation.empty;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.ParseException;

import de.dagere.peass.config.ExecutionConfig;
import de.peass.evaluation.base.EvaluationVersion;
import de.peass.evaluation.base.Evaluator;

/**
 * Determines how much tests are executed without test selection.
 * 
 * @author reichelt
 *
 */
public class EmptyEvaluator extends Evaluator {


	public EmptyEvaluator(final ExecutionConfig executionConfig, final File projectFolder) throws ParseException {
		super("empty", executionConfig, projectFolder);
	}

	@Override
	public void evaluate() {
		final File resultFile = folders.getResultFolder("empty");
		int i = 0;
		while (iterator.hasNextCommit()) {
			iterator.goToNextCommit();

			final File currentFile = folders.getResultFile(i, iterator.getTag());
			executor.executeTests(new File("results/ekstazi-empty.txt"), "*"); // Try to run all tests by running *

			final EvaluationVersion currentVersion = getTestsFromFile(currentFile);
			if (currentVersion.getTestcaseExecutions().size() > 0) {
				evaluation.getVersions().put(iterator.getTag(), currentVersion);
				try {
					OBJECTMAPPER.writeValue(resultFile, evaluation);
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}

			i++;
		}

	}
}
