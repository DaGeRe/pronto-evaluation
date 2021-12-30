package de.peass.evaluation.ekstazi;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import de.dagere.peass.config.ExecutionConfig;
import de.peass.evaluation.base.EvaluationVersion;
import de.peass.evaluation.base.Evaluator;

/**
 * Runs the tests of all versions with Ekstazi in order to determine the count of tests ekstazi would have run.
 * @author reichelt
 *
 */
public class EkstaziEvaluator extends Evaluator {

	private static final Logger LOG = LogManager.getLogger(EkstaziEvaluator.class);

	public EkstaziEvaluator(final ExecutionConfig config, final File projectFolder) throws ParseException {
		super("ekstazi", config, projectFolder);
	}

	@Override
	public void evaluate() throws IOException, InterruptedException, XmlPullParserException {
	   cleanup();
	   
		final File resultFile = folders.getResultFolder("ekstazi");
		int i = 0;
		while (iterator.hasNextCommit()) {
			iterator.goToNextCommitSoft();
			
			final File pomFile = new File(folders.getProjectFolder(), "pom.xml");
			if (pomFile.exists()) {
			   analyzeVersion(resultFile, i);
			} 

			i++;
		}
	}

   public void analyzeVersion(final File resultFile, final int i) throws IOException, InterruptedException, XmlPullParserException {
      final File currentFile = folders.getResultFile(i, iterator.getTag());
      executor.prepareKoPeMeExecution(new File(currentFile.getParentFile(), currentFile.getName() + "_prepare"));
//      TestSet tests = executor.getTestTransformer().findModuleTests(mapping, executor.getModules(), executor.getModules());
      executor.executeTests(currentFile, "*"); // Try to run all tests by running *

      final EvaluationVersion currentVersion = getTestsFromFile(currentFile);
      if (currentVersion.getTestcaseExecutions().size() > 0) {
         evaluation.getVersions().put(iterator.getTag(), currentVersion);
         try {
            OBJECTMAPPER.writeValue(resultFile, evaluation);
         } catch (final IOException e) {
            e.printStackTrace();
         }
      }
   }

   public void cleanup() {
      File oldEkstaziFolder = new File(folders.getProjectFolder(), ".ekstazi");
	   try {
         FileUtils.deleteDirectory(oldEkstaziFolder);
      } catch (IOException e1) {
         e1.printStackTrace();
      }
   }
	

}
