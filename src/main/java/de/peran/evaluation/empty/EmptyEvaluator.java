package de.peran.evaluation.empty;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.ParseException;

import de.peran.evaluation.base.EvaluationVersion;
import de.peran.evaluation.base.Evaluator;

/**
 * Determines how much tests are executed without test selection.
 * 
 * @author reichelt
 *
 */
public class EmptyEvaluator extends Evaluator {

	public static void main(final String[] args) throws ParseException {
		final Evaluator evaluator = new EmptyEvaluator(args);
		evaluator.evaluate();
	}

	public EmptyEvaluator(final String[] args) throws ParseException {
		super("empty", args);
	}

	@Override
	public void evaluate() {
		final File resultFile = folders.getResultFolder("empty");
		int i = 0;
		while (iterator.hasNextCommit()) {
			iterator.goToNextCommit();

			final File currentFile = folders.getResultFile(i, iterator.getTag());
			executor.executeAllKoPeMeTests(currentFile);

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
