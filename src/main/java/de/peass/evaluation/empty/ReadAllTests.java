package de.peass.evaluation.empty;

import java.io.File;

import de.dagere.peass.config.ExecutionConfig;
import de.peass.evaluation.base.Evaluator;

/**
 * Generates a) Trace-Method-Diff and b) Trace-Method-Source-Diff from a project by loading every version, executing it with instrumentation and afterwards closing it.
 * 
 * @author reichelt
 *
 */
public abstract class ReadAllTests extends Evaluator {

   public ReadAllTests(final String type, final ExecutionConfig config, final File projectFolder) {
      super(type, config, projectFolder);
      // TODO Auto-generated constructor stub
   }

   //TODO Currently disabled; enable later again
   
//   private static final Logger LOG = LogManager.getLogger(ReadAllTests.class);
//
//   private static final ObjectMapper MAPPER = new ObjectMapper();
//   static {
//      MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
//   }
//
//   private final File executeFile;
//   private final ExecutionData changedTraceMethods = new ExecutionData();
//
//   public ReadAllTests(final String args[]) throws ParseException {
//      super("readall", args);
//      final File resultFolder = new File("results");
//      final String projectName = folders.getProjectFolder().getName();
//      final String url = GitUtils.getURL(folders.getProjectFolder());
//      changedTraceMethods.setUrl(url);
//      executeFile = new File(resultFolder, "execute_full_" + projectName + ".json");
//   }
//
//   @Override
//   public void evaluate() {
//      analyzeVersion();
//      while (iterator.hasNextCommit()) {
//         iterator.goToNextCommit();
//         analyzeVersion();
//
//         try (FileWriter fw = new FileWriter(executeFile)) {
//            fw.write(MAPPER.writeValueAsString(changedTraceMethods));
//            fw.flush();
//         } catch (final IOException e) {
//            e.printStackTrace();
//         }
//
//      }
//   }
//
//   private void analyzeVersion() {
//      final String version = iterator.getTag();
//
//      final File srcFolder = new File(folders.getProjectFolder(), "src");
//      final List<String> clazzes = ClazzFinder.getTestClazzes(srcFolder);
//      final File testSrcFolder = ClazzFinder.getTestFolder(srcFolder);
//      for (final String clazzName : clazzes) {
//         final String filename = clazzName.replace(".", File.separator) + ".java";
//         final File clazzFile = new File(testSrcFolder, filename);
//         if (clazzFile.exists()) {
//            try {
//               LOG.trace("Clazz: {}", clazzName);
//               final CompilationUnit unit = FileComparisonUtil.parse(clazzFile);
//               final ClassOrInterfaceDeclaration clazz = ParseUtil.getClass(unit);
//               final JUnitTestTransformer transformer = new JUnitTestTransformer(folders.getProjectFolder());
//               if (clazz != null) { // File could also define @interface instead of class
//                  final List<String> methods = transformer.getTests(folders.getProjectFolder(), new ChangedEntity(clazz.getNameAsString(), ""));
//                  for (final String method : methods) {
//                     final TestCase testcase = new TestCase(clazzName, method);
//                     changedTraceMethods.addCall(version, version + "~1", testcase);
//                  }
//               }
//
//            } catch (final FileNotFoundException e) {
//               e.printStackTrace();
//            }
//         }
//      }
//   }
//
//   public static void main(final String[] args) throws ParseException, JAXBException, JsonParseException, JsonMappingException, IOException {
//      final Evaluator evaluator = new ReadAllTests(args);
//      evaluator.evaluate();
//   }

}
