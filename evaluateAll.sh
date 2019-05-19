project=$1
echo "Evaluating $project.."
java -cp target/pronto-evaluation-0.1-SNAPSHOT.jar de.peass.evaluation.infinitest.InfinitestEvaluator -folder ../../projekte/$project/ &> "$project"_infinitest.txt
java -cp target/pronto-evaluation-0.1-SNAPSHOT.jar de.peass.evaluation.ekstazi.EkstaziEvaluator -folder ../../projekte/$project/ &> "$project"_ekstazi.txt
java -cp target/pronto-evaluation-0.1-SNAPSHOT.jar de.peass.evaluation.empty.EmptyEvaluator -folder ../../projekte/$project/ &> "$project"_empty.txt

