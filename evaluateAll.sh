java -cp target/evaluation-0.1-SNAPSHOT.jar de.peran.evaluation.infinitest.InfinitestEvaluator -folder ../../projekte/commons-fileupload/ &> fileupload_infinitest.txt
java -cp target/evaluation-0.1-SNAPSHOT.jar de.peran.evaluation.ekstazi.EkstaziEvaluator -folder ../../projekte/commons-fileupload/ &> fileupload_ekstazi.txt
java -cp target/evaluation-0.1-SNAPSHOT.jar de.peran.evaluation.empty.EmptyEvaluator -folder ../../projekte/commons-fileupload/ &> fileupload_empty.txt
