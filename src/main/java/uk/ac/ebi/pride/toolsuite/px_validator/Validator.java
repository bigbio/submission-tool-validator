package uk.ac.ebi.pride.toolsuite.px_validator;

import org.apache.commons.cli.CommandLine;

import org.apache.log4j.Logger;
import uk.ac.ebi.pride.toolsuite.px_validator.utils.*;
import uk.ac.ebi.pride.toolsuite.px_validator.validators.MzIdValidator;
import uk.ac.ebi.pride.toolsuite.px_validator.validators.MzTabValidator;
import uk.ac.ebi.pride.toolsuite.px_validator.validators.PXFileValidator;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import static uk.ac.ebi.pride.toolsuite.px_validator.utils.Utility.*;

/**
 * This class validates an input file and produces a plain text report file,
 * and potentially a serialized version of ResultReport as well.
 *
 * @author ypriverol
 */
public class Validator {

  private static final Logger log = Logger.getLogger(Validator.class);


  /**
   * This class parses the command line arguments and beings the file validation.
   *
   * @param cmd command line arguments.
   */
  public static IReport startValidation(CommandLine cmd) {
    IReport report = null;
    if (cmd.hasOption(ARG_PXFILE)) {
      try {
        uk.ac.ebi.pride.toolsuite.px_validator.validators.Validator validator = PXFileValidator.getInstance(cmd);
        report = validator.validate();
      } catch (Exception e) {
        e.printStackTrace();
      }

    } if(cmd.hasOption(ARG_MZTAB)){
      try {
        uk.ac.ebi.pride.toolsuite.px_validator.validators.Validator validator = MzTabValidator.getInstance(cmd);
        report = validator.validate();
      } catch (Exception e) {
        e.printStackTrace();
      }
    } if(cmd.hasOption(ARG_MZID)){
      try {
        uk.ac.ebi.pride.toolsuite.px_validator.validators.Validator validator = MzIdValidator.getInstance(cmd);
        report = validator.validate();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }else {
      log.error("Unable to validate unknown input file type");
    }
    return report;
  }

  /**
   * This method gets all the input file ready for validation, if it is extracted.
   *
   * @param file the input file for validation.
   * @return List of extracted files for validation.
   */
  private static List<File> getFilesToValidate(File file) {
    List<File> filesToValidate = new ArrayList<>();
    if (file.isDirectory()) {
      log.error("Unable to validate against directory of mzid files.");
    } else {
      filesToValidate.add(file);
    }
    filesToValidate = extractZipFiles(filesToValidate);
    return filesToValidate;
  }

  /**
   * This method gets a list of providede peak files.
   *
   * @param cmd the command line arguments.
   * @return List of peak files.
   */
  private static List<File> getPeakFiles(CommandLine cmd) {
    List<File> peakFiles = new ArrayList<>();
    if (cmd.hasOption(ARG_PEAK) || cmd.hasOption(ARG_PEAKS)) {
      String[] peakFilesString = cmd.hasOption(ARG_PEAK) ? cmd.getOptionValues(ARG_PEAK)
          : cmd.hasOption(ARG_PEAKS) ?  cmd.getOptionValue(ARG_PEAKS).split(STRING_SEPARATOR) : new String[0];
      for (String aPeakFilesString : peakFilesString) {
        File peakFile = new File(aPeakFilesString);
        if (peakFile.isDirectory()) {
          File[] listFiles = peakFile.listFiles(File::isFile);
          if (listFiles!=null) {
            peakFiles.addAll(Arrays.asList(listFiles));
          }
        } else {
          peakFiles.add(peakFile);
          log.info("Added peak file: " + peakFile.getPath());
        }
      }
      peakFiles = extractZipFiles(peakFiles);
    } else {
      log.error("Peak file not supplied with mzIdentML file.");
    }
    return peakFiles;
  }

  /**
   * This method extracts an input list of files.
   *
   * @param files a list of input zip files to extract.
   * @return a list of extracted files.
   */
  public static List<File> extractZipFiles(List<File> files) {
    List<File> zippedFiles = findZippedFiles(files);
    if (zippedFiles.size()>0) {
      files.removeAll(zippedFiles);
      files.addAll(unzipFiles(zippedFiles, zippedFiles.get(0).getParentFile().getAbsoluteFile()));
    }
    return files.stream().distinct().collect(Collectors.toList());
  }

  /**
   * ~This method identifies any gzipped files.
   * @param files a list if input files.
   * @return a list of files that are gzipped.
   */
  private static List<File> findZippedFiles(List<File> files) {
    return files.stream().filter(file -> file.getName().endsWith(".gz")).collect(Collectors.toList());
  }

  /**
   * This method extracts a list of iniput gzipped files to an output directory.
   * @param zippedFiles a list of input files to extract.
   * @param outputFolder the output directory.
   * @return a list of files that have been extracted.
   */
  private static List<File> unzipFiles(List<File> zippedFiles, File outputFolder) {
    List<File> unzippedFiles = new ArrayList<>();
    zippedFiles.parallelStream().forEach(inputFile -> {
      try {
        log.info("Unzipping file: " + inputFile.getAbsolutePath());
        FileInputStream fis = null;
        GZIPInputStream gs = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
          fis = new FileInputStream(inputFile);
          gs = new GZIPInputStream(fis);
          String outputFile = outputFolder + File.separator + inputFile.getName().replace(".gz", "");
          fos = new FileOutputStream(outputFile);
          bos = new BufferedOutputStream(fos, 2048);
          byte[] data = new byte[2048];
          int count;
          while ((count = gs.read(data, 0, 2048)) != -1) {
            bos.write(data, 0, count);
          }
          bos.flush();
          bos.close();
          unzippedFiles.add(new File(outputFile));
          log.info("Unzipped file: " + outputFile);
        } finally {
          if (fis != null) {
            fis.close();
          }
          if (gs != null) {
            gs.close();
          }
          if (fos != null) {
            fos.close();
          }
          if (bos != null) {
            bos.close();
          }
        }
      } catch (IOException ioe) {
        log.error("IOException when unzipping files.", ioe);
      }
    });
    return unzippedFiles;
  }
}





