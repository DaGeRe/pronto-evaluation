package de.peass.evaluation.base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.dagere.peass.config.ExecutionConfig;
import de.dagere.peass.vcs.CommitUtil;
import de.dagere.peass.vcs.GitCommit;
import de.dagere.peass.vcs.GitUtils;
import de.dagere.peass.vcs.VersionIterator;
import de.dagere.peass.vcs.VersionIteratorGit;

/**
 * Base class for those classes who evaluate a method against DePeC. Therefore, the evaluation itself for the specific method needs to be added.
 * 
 * @author reichelt
 *
 */
public abstract class Evaluator {
	private static final Logger LOG = LogManager.getLogger(Evaluator.class);

	public static final ObjectMapper OBJECTMAPPER = new ObjectMapper();

	static {
		OBJECTMAPPER.enable(SerializationFeature.INDENT_OUTPUT);
	}

	protected final EvaluationFolders folders;
	protected final VersionIterator iterator;
//	protected final File debugFolder;
//	protected final File resultFolder;
	protected final EvaluationProject evaluation;
	protected final SysoutTestExecutor executor;

	public Evaluator(final String type, final ExecutionConfig config, final File projectFolder)  {

		File outputFile = projectFolder.getParentFile();
		if (outputFile.isDirectory()) {
			outputFile = new File(projectFolder.getParentFile(), "ausgabe.txt");
		}

		LOG.debug("Lese {}", projectFolder.getAbsolutePath());
		this.folders = new EvaluationFolders(projectFolder);

		final String url = GitUtils.getURL(projectFolder);
		final List<GitCommit> commits = CommitUtil.getGitCommits(config.getStartversion(), config.getEndversion(), projectFolder);

		iterator = new VersionIteratorGit(projectFolder, commits, null);

		executor = new SysoutTestExecutor(projectFolder);

		evaluation = new EvaluationProject();
		evaluation.setUrl(url);
	}

	public abstract void evaluate() throws IOException, InterruptedException, XmlPullParserException;

	/**
	 * Reads tests from a file container a maven test output.
	 * 
	 * @param currentFile
	 *            Logfile of maven test
	 * @return Count of test methods of every testfile in the current logfile
	 */
	protected EvaluationVersion getTestsFromFile(final File currentFile) {
		final EvaluationVersion currentVersion = new EvaluationVersion();
		try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
			String line;
			boolean inTests = false;
			// Map<String, Integer> tests = new HashMap<>();
			while ((line = reader.readLine()) != null) {
				if (line.contains("-------------------------------------------------------")) {
					final String test = reader.readLine();
					final String nextLine = reader.readLine();
					if ( test != null && 
					      test.contains("T E S T S")
							&& nextLine.contains("-------------------------------------------------------")) {
						inTests = true;
					}
				}

				if (inTests) {
					LOG.trace("Line: {}", line);
					final String runningString = "Running ";
					if (line.contains(runningString)) {
						final String testname = line.substring(runningString.length());
						final String testsRun = reader.readLine();
						LOG.trace("Line: {}", testsRun);
						final String testsRunString = "Tests run: ";
						if (testsRun != null && testsRun.contains(testsRunString)) {
							final String[] splitted = testsRun.split(",");
							final String runCount = splitted[0].substring(splitted[0].lastIndexOf(' ')+1);
							final int count = Integer.parseInt(runCount);
							LOG.info("Test: " + testname + " " + count);
							currentVersion.getTestcaseExecutions().put(testname, count);
						} else {
							LOG.error("Unexpected line: " + testsRun);
						}

						// tests.put(arg0, arg1)
					}
				}
			}

		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return currentVersion;
	}
}
