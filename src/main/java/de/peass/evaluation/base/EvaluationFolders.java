package de.peass.evaluation.base;

import java.io.File;

import de.dagere.peass.folders.PeassFolders;


public class EvaluationFolders extends PeassFolders {

//   private final File ekstaziFolder;
//   private final File pureFolder;
//   private final File infinitestFolder;
   private File typeFolder;
   
   public EvaluationFolders(final File projectFolder) {
      super(projectFolder);
      
//      ekstaziFolder = new File(peassFolder, "ekstazi");
//      ekstaziFolder.mkdirs();
//      pureFolder = new File(peassFolder, "pure");
//      pureFolder.mkdirs();
//      infinitestFolder = new File(peassFolder, "infinitest");
//      infinitestFolder.mkdirs();
   }
   
//   public File getEkstaziResultFile(int i, String tag) {
//      return new File(ekstaziFolder, "log_" + i + "_" + tag + ".txt");
//   }

   public File getResultFolder(final String type) {
      typeFolder = new File(peassFolder, type);
      typeFolder.mkdirs();
      return new File(typeFolder, projectFolder.getName() + "_" + type + ".json");
   }

   public File getResultFile(final int i, final String tag) {
      return new File(typeFolder, "log_" + i + "_" + tag + ".txt");
   }

}
