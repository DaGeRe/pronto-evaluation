package de.peass.evaluation.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import de.dagere.peass.dependency.analysis.data.TestCase;
import de.dagere.peass.execution.maven.pom.MavenTestExecutor;
import de.dagere.peass.execution.utils.EnvironmentVariables;
import de.dagere.peass.folders.PeassFolders;

/**
 * Executes the tests of a maven project in order to read the amount of executed tests in the process sysout
 * 
 * @author reichelt
 *
 */
public class SysoutTestExecutor extends MavenTestExecutor {

	private static final int SECOND = 1000;

	public static final int DEFAULT_TIMEOUT = 3;

	private static final Logger LOG = LogManager.getLogger(SysoutTestExecutor.class);

	public static final String ORG_APACHE_MAVEN_PLUGINS = "org.apache.maven.plugins";
	public static final String SUREFIRE_ARTIFACTID = "maven-surefire-plugin";
	public static final String COMPILER_ARTIFACTID = "maven-compiler-plugin";

	public SysoutTestExecutor(final File projectFolder) {
		super(new PeassFolders(projectFolder), null, new EnvironmentVariables());
	}
	
	@Override
   public Process buildMavenProcess(final File logFile, final String... commandLineAddition) throws IOException {
	   // Attention: No clean for infinitest possible!
		final String[] originals = new String[] { "mvn", "-B", "test", "-fn", 
		      "-Dcheckstyle.skip=true",
				"-Dmaven.compiler.source=1.8", 
				"-Dmaven.compiler.target=1.8", 
				"-Dmaven.javadoc.skip=true",
				"-Denforcer.skip=true",
            "-DfailIfNoTests=false",
            "-Drat.skip=true",
            "-Djacoco.skip=true",};
		final String[] vars = new String[commandLineAddition.length + originals.length];
		for (int i = 0; i < originals.length; i++) {
			vars[i] = originals[i];
		}
		for (int i = 0; i < commandLineAddition.length; i++) {
			vars[originals.length + i] = commandLineAddition[i];
		}

		final ProcessBuilder pb = new ProcessBuilder(vars);

		pb.directory(folders.getProjectFolder());
		if (logFile != null) {
			pb.redirectOutput(Redirect.appendTo(logFile));
			pb.redirectError(Redirect.appendTo(logFile));
		}

		final Process process = pb.start();
		return process;
	}

	public void executeTests(final File logFile, final String testname) {
		try {
			final Process process = buildMavenProcess(logFile, "-Dtest=" + testname);
			waitForProcess(process);
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void waitForProcess(final Process process) throws InterruptedException {
		LOG.info("Starting Process");
		process.waitFor(DEFAULT_TIMEOUT, TimeUnit.MINUTES);
		LOG.info("Process finished..");
		Thread killerThread = new Thread(new Runnable() {
         
         @Override
         public void run() {
            LOG.info("Checking isAlive-state");
            if (process.isAlive()) {
               LOG.debug("Destroy...");
               process.destroy();
               destoryhard(process);
            }
         }

         
      });
		if (process.isAlive()) {
		   killerThread.start();
	      killerThread.join(1000);
	      LOG.info("Destroying took too long..");
	      process.destroyForcibly();
	      LOG.info("Destroy-message sent");
	      destoryhard(process);
		}
	}
	
	public static void destoryhard(final Process process) {
      while (process.isAlive()) {
         LOG.debug("Kill...");
         process.destroyForcibly();
         try {
            Thread.sleep(SECOND);
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
      }
   }
	
	@Override
	public void prepareKoPeMeExecution(final File logFile) throws IOException, InterruptedException, XmlPullParserException {
	   final MavenXpp3Reader reader = new MavenXpp3Reader();
      try {
         final File pomFile = new File(folders.getProjectFolder(), "pom.xml");
         final Model model = reader.read(new FileInputStream(pomFile));
         if (model.getBuild() == null) {
            model.setBuild(new Build());
         }

         final Plugin ekstazi = new Plugin();
         ekstazi.setGroupId("org.ekstazi");
         ekstazi.setArtifactId("ekstazi-maven-plugin");
         ekstazi.setVersion("5.2.0");
         final PluginExecution ekstaziExecution = new PluginExecution();
         ekstaziExecution.setGoals(Arrays.asList(new String[] { "select" }));
         final List<PluginExecution> ekstaziExecutions = new LinkedList<>();
         ekstaziExecutions.add(ekstaziExecution);
         ekstazi.setExecutions(ekstaziExecutions);

         model.getBuild().getPlugins().add(ekstazi);

         final MavenXpp3Writer writer = new MavenXpp3Writer();
         writer.write(new FileWriter(pomFile), model);
      } catch (IOException | XmlPullParserException e) {
         e.printStackTrace();
      }
	}


	@Override
	public boolean isVersionRunning(final String version) {
//		return MavenPomUtil.testRunning(folders.getProjectFolder(), new File(folders.getLogFolder(), version), timeout);
	   return true; // TODO Wieder echt pr??fen
	}


   @Override
   public void executeTest(final TestCase tests, final File logFolder, final long timeout) {
      throw new RuntimeException("Not implemented yet");
   }

   @Override
   public boolean doesBuildfileExist() {
      throw new RuntimeException("Not implemented yet");
   }

}
