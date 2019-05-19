package de.peass.evaluation.ekstazi;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.peass.evaluation.base.EvaluationVersion;
import de.peass.evaluation.base.Evaluator;

/**
 * Runs the tests of all versions with Ekstazi in order to determine the count of tests ekstazi would have run.
 * @author reichelt
 *
 */
public class EkstaziEvaluator extends Evaluator {

	private static final Logger LOG = LogManager.getLogger(EkstaziEvaluator.class);

	public EkstaziEvaluator(final String[] args) throws ParseException {
		super("ekstazi", args);
	}

	@Override
	public void evaluate() {
		final File resultFile = folders.getResultFolder("ekstazi");
		int i = 0;
		while (iterator.hasNextCommit()) {
			iterator.goToNextCommitSoft();
			
			final File pomFile = new File(folders.getProjectFolder(), "pom.xml");
			if (pomFile.exists()) {
			   final File currentFile = folders.getResultFile(i, iterator.getTag());
	         executor.preparePom();
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
	         if (iterator.getTag().equals("160178688b42e26262b6aa9e277646b8865960b7")) {
               System.out.println("Test");
            }
			}

			i++;
		}
	}
	
	public static void main(final String[] args) throws ParseException {
		final Evaluator evaluator = new EkstaziEvaluator(args);
		evaluator.evaluate();
	}

}