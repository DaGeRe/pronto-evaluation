package de.peass.evaluation.empty;

import java.io.File;
import java.util.concurrent.Callable;

import org.apache.commons.cli.ParseException;

import de.dagere.peass.config.ExecutionConfig;
import de.dagere.peass.config.parameters.ExecutionConfigMixin;
import picocli.CommandLine;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

public class EmptyEvaluationStarter implements Callable<Void> {

   @Option(names = { "-folder", "--folder" }, description = "Folder that should be analyzed", required = true)
   private File projectFolder;
   
   @Mixin
   private ExecutionConfigMixin executionConfigMixin;
   
   public static void main(final String[] args) throws ParseException {
      try {
         final CommandLine commandLine = new CommandLine(new EmptyEvaluationStarter());
         commandLine.execute(args);
      } catch (final Throwable t) {
         t.printStackTrace();
      }
   }
   
   @Override
   public Void call() throws Exception {
      EmptyEvaluator evaluator = new EmptyEvaluator(new ExecutionConfig(executionConfigMixin), projectFolder);
      evaluator.evaluate();
      return null;
   }


}
