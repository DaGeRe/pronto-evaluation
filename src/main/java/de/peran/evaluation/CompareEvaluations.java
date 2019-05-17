package de.peran.evaluation;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import de.peass.dependency.persistence.ExecutionData;
import de.peran.evaluation.base.CompareUtil;
import de.peran.evaluation.base.EvaluationProject;

/**
 * Prints tests which would have been executed by EKSTAZI, Infinitest and PRONTO.
 * 
 * @author reichelt
 *
 */
public final class CompareEvaluations {

	private static final double PERCENT = 100d;

	private static final Logger LOG = LogManager.getLogger(CompareEvaluations.class);

	private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("##.##");

	public static void main(final String[] args) {
		final File peassFolder = new File(args[0]);
		final File evaluationFolder = new File(args[1]);

		double ekstaziSum = 0;
		double infiniSum = 0;
		double ticSum = 0;
		int count = 0;
		for (final File dependencies : peassFolder.listFiles()) {
			if (dependencies.getName().startsWith("deps_")) {
				final String project = dependencies.getName().substring(5, dependencies.getName().indexOf('.'));
				final File executefile = new File(peassFolder, "execute_" + project + ".json");
				if (executefile.exists()) {
					count++;
					try {
						final ObjectMapper objectMapper = new ObjectMapper();
						final SimpleModule module = new SimpleModule();
//						module.addDeserializer(ChangedTraceTests.class, new ChangedTraceTests.OldVersionDeserializer());
						objectMapper.registerModule(module);

						final ExecutionData changedTraceTests = objectMapper.readValue(executefile, ExecutionData.class);

						final EvaluationProject prontoData = CompareUtil.createEvaluationData(changedTraceTests);

						final File emptyFile = new File(evaluationFolder, project + "_empty.json");
						final File ekstaziFile = new File(evaluationFolder, project + "_ekstazi.json");
						final File infinitestFile = new File(evaluationFolder, project + "_infinitest.json");
						
						if (ekstaziFile.exists() && infinitestFile.exists()) {
						   int infinitestCount = 1;
	                  int emptyCount = 1;
	                  int size = 1;
	                  if (emptyFile.exists()) {
	                     final EvaluationProject emptyData = objectMapper.readValue(emptyFile, EvaluationProject.class);
	                     emptyCount = emptyData.getOverallTestCount();
	                     size = emptyData.getVersions().size();
	                  }

	                  final EvaluationProject ekstaziData = objectMapper.readValue(ekstaziFile, EvaluationProject.class);
	                  if (infinitestFile.exists()) {
	                     final EvaluationProject infinitestData = objectMapper.readValue(infinitestFile, EvaluationProject.class);
	                     infinitestCount = infinitestData.getOverallTestCount();
	                  }

	                  System.out.println(project + ";" + size + ";" + emptyCount + ";" + ekstaziData.getOverallTestCount() + ";"
	                        + PERCENT_FORMAT.format(PERCENT * ekstaziData.getOverallTestCount() / emptyCount) + "%;" + infinitestCount + ";"
	                        + PERCENT_FORMAT.format(PERCENT * infinitestCount / emptyCount) + "%;" + prontoData.getOverallTestCount() + ";"
	                        + PERCENT_FORMAT.format(PERCENT * prontoData.getOverallTestCount() / emptyCount) + "%");

	                  ekstaziSum += (PERCENT * ekstaziData.getOverallTestCount() / emptyCount);
	                  infiniSum += (PERCENT * infinitestCount / emptyCount);
	                  ticSum += (PERCENT * prontoData.getOverallTestCount() / emptyCount);
						}

						
					} catch (final IOException e) {
						e.printStackTrace();
					}

				}
			}
		}

		System.out.println(PERCENT_FORMAT.format(ekstaziSum / count) + ";" + PERCENT_FORMAT.format(infiniSum / count) + ";" + PERCENT_FORMAT.format(ticSum / count));
	}

	private CompareEvaluations() {

	}
}
