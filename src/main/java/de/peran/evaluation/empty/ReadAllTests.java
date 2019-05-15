package de.peran.evaluation.empty;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import de.peass.DependencyReadingStarter;
import de.peass.dependency.ClazzFinder;
import de.peass.dependency.analysis.data.ChangedEntity;
import de.peass.dependency.analysis.data.TestCase;
import de.peass.dependency.changesreading.FileComparisonUtil;
import de.peass.dependency.persistence.ExecutionData;
import de.peass.testtransformation.JUnitTestTransformer;
import de.peass.testtransformation.ParseUtil;
import de.peass.vcs.GitUtils;
import de.peran.evaluation.base.Evaluator;

/**
 * Generates a) Trace-Method-Diff and b) Trace-Method-Source-Diff from a project by loading every version, executing it with instrumentation and afterwards closing it.
 * 
 * @author reichelt
 *
 */
public class ReadAllTests extends Evaluator {

   private static final Logger LOG = LogManager.getLogger(ReadAllTests.class);

   private static final ObjectMapper MAPPER = new ObjectMapper();
   static {
      MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
   }

   private final File executeFile;
   private final ExecutionData changedTraceMethods = new ExecutionData();

   public ReadAllTests(final String args[]) throws ParseException {
      super("readall", args);
      final File resultFolder = DependencyReadingStarter.getResultFolder();
      final String projectName = folders.getProjectFolder().getName();
      final String url = GitUtils.getURL(folders.getProjectFolder());
      changedTraceMethods.setUrl(url);
      executeFile = new File(resultFolder, "execute_full_" + projectName + ".json");
   }

   @Override
   public void evaluate() {
      analyzeVersion();
      while (iterator.hasNextCommit()) {
         iterator.goToNextCommit();
         analyzeVersion();

         try (FileWriter fw = new FileWriter(executeFile)) {
            fw.write(MAPPER.writeValueAsString(changedTraceMethods));
            fw.flush();
         } catch (final IOException e) {
            e.printStackTrace();
         }

      }
   }

   private void analyzeVersion() {
      final String version = iterator.getTag();

      final File srcFolder = new File(folders.getProjectFolder(), "src");
      final List<String> clazzes = ClazzFinder.getTestClazzes(srcFolder);
      final File testSrcFolder = ClazzFinder.getTestFolder(srcFolder);
      for (final String clazzName : clazzes) {
         final String filename = clazzName.replace(".", File.separator) + ".java";
         final File clazzFile = new File(testSrcFolder, filename);
         if (clazzFile.exists()) {
            try {
               LOG.trace("Clazz: {}", clazzName);
               final CompilationUnit unit = FileComparisonUtil.parse(clazzFile);
               final ClassOrInterfaceDeclaration clazz = ParseUtil.getClass(unit);
               final JUnitTestTransformer transformer = new JUnitTestTransformer(folders.getProjectFolder());
               if (clazz != null) { // File could also define @interface instead of class
                  final List<String> methods = transformer.getTests(folders.getProjectFolder(), new ChangedEntity(clazz.getNameAsString(), ""));
                  for (final String method : methods) {
                     final TestCase testcase = new TestCase(clazzName, method);
                     changedTraceMethods.addCall(version, version + "~1", testcase);
                  }
               }

            } catch (final FileNotFoundException e) {
               e.printStackTrace();
            }
         }
      }
   }

   public static void main(final String[] args) throws ParseException, JAXBException, JsonParseException, JsonMappingException, IOException {
      final Evaluator evaluator = new ReadAllTests(args);
      evaluator.evaluate();
   }

}
