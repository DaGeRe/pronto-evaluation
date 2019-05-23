package de.peass.evaluation.infinitest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.infinitest.changedetect.FileChangeDetector;
import org.infinitest.parser.ClassFileIndex;
import org.infinitest.parser.JavaClass;

import de.peass.dependency.execution.MavenPomUtil;
import de.peass.evaluation.base.EvaluationVersion;
import de.peass.evaluation.base.Evaluator;
import de.peass.evaluation.base.SysoutTestExecutor;

/**
 * Runs all the tests of all versions with infinitest in order to determine the count of tests infinitest would run
 * @author reichelt
 *
 */
public class InfinitestEvaluator extends Evaluator {

	private static final Logger LOG = LogManager.getLogger(InfinitestEvaluator.class);

	public InfinitestEvaluator(final String[] args) throws ParseException {
		super("infinitest", args);
	}

	@Override
	public void evaluate() {
		final List<File> classPath = Arrays.asList(new File(folders.getProjectFolder(), "target/classes"), new File(folders.getProjectFolder(), "target/test-classes"));
		final File pomFile = new File(folders.getProjectFolder(), "pom.xml");
		final StandaloneClasspath classpath = new StandaloneClasspath(classPath, classPath);
		final FileChangeDetector changeDetector = new FileChangeDetector();
		final File resultFile = folders.getResultFolder("infinitest");

		changeDetector.setClasspathProvider(classpath);

		final ClassFileIndex index = new ClassFileIndex(classpath);
		final MavenXpp3Reader reader = new MavenXpp3Reader();
		int i = 0;
		while (iterator.hasNextCommit()) {
			iterator.goToNextCommitSoft();

			if (pomFile.exists()) {
				try {
					enableIncrementalBuilding(pomFile, reader);

					compileInfinitest();

					final Set<JavaClass> changedClasses = getChangedClasses(changeDetector, index);

					System.out.println("All changes: " + changedClasses);

					final EvaluationVersion currentVersion = new EvaluationVersion();

					if (currentVersion.getTestcaseExecutions().size() > 0) {
					   String testname = buildTestString(changedClasses, currentVersion);
						final File currentFile =  folders.getResultFile(i, iterator.getTag());

						executor.executeTests(currentFile, testname);

						final EvaluationVersion adjustedVersion = getTestsFromFile(currentFile);
						evaluation.getVersions().put(iterator.getTag(), adjustedVersion);
						OBJECTMAPPER.writeValue(resultFile, evaluation);
					}

					System.out.println("Tests 2: " + changedClasses);
				} catch (final Exception e) {
					e.printStackTrace();
				}

			}
			i++;
		}

	}

   public void compileInfinitest() throws IOException, InterruptedException {
      final ProcessBuilder pb = new ProcessBuilder(new String[] { "mvn", "-B", "compile", "test-compile" });
      pb.directory(folders.getProjectFolder());

      Process process = pb.start();
      process.waitFor(SysoutTestExecutor.DEFAULT_TIMEOUT, TimeUnit.MINUTES);
      SysoutTestExecutor.waitForProcess(process);
   }

   public String buildTestString(final Set<JavaClass> changedClasses, final EvaluationVersion currentVersion) {
      String testname = "";
      for (final Iterator<JavaClass> clazzIterator = changedClasses.iterator(); clazzIterator.hasNext();) {
      	final JavaClass clazz = clazzIterator.next();
      	if (!clazz.isATest()) {
      		clazzIterator.remove();
      	} else {
      		currentVersion.getTestcaseExecutions().put(clazz.getName(), 0);
      		testname += clazz.getName() + ",";
      	}
      }
      testname = testname.substring(0, testname.length() - 1);
      return testname;
   }

   public void enableIncrementalBuilding(final File pomFile, final MavenXpp3Reader reader) throws IOException, XmlPullParserException, FileNotFoundException {
      final Model model = reader.read(new FileInputStream(pomFile));
      if (model.getBuild() == null) {
      	model.setBuild(new Build());
      }
      final Plugin compiler = MavenPomUtil.findPlugin(model, MavenPomUtil.COMPILER_ARTIFACTID, MavenPomUtil.ORG_APACHE_MAVEN_PLUGINS);
      MavenPomUtil.setIncrementalBuild(compiler, false);

      final MavenXpp3Writer writer = new MavenXpp3Writer();
      writer.write(new FileWriter(pomFile), model);
   }

	private Set<JavaClass> getChangedClasses(final FileChangeDetector changeDetector, final ClassFileIndex index) throws IOException {
		final Set<File> changedFiles = changeDetector.findChangedFiles();
		System.out.println("Changes: " + changedFiles);
		final Set<JavaClass> changedClasses = index.findClasses(changedFiles);
		final Set<JavaClass> changedParents = index.findChangedParents(changedClasses);

		// combine two sets
		changedClasses.addAll(changedParents);
		return changedClasses;
	}
	
	public static void main(final String[] args) throws IOException, XmlPullParserException, InterruptedException, ParseException {
		final InfinitestEvaluator evaluator = new InfinitestEvaluator(args);
		evaluator.evaluate();
	}

}
