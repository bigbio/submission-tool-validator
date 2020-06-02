package uk.ac.ebi.pride.toolsuite.px_validator.utils;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * This class provides general utitilies for the PGConverter tool, such as:
 * arguments, supported file types, Redis messaging, and handling exiting the application.
 *
 * @author ypriverol
 */
public class Utility {
  private static final Logger log = LoggerFactory.getLogger(Utility.class);

  public static final String ARG_VALIDATION = "v";
  public static final String ARG_CONVERSION = "c";
  public static final String ARG_MZID = "mzid";
  public static final String ARG_PXFILE = "px";
  public static final String ARG_PEAK = "peak";
  public static final String ARG_PEAKS = "peaks";
  public static final String ARG_MZTAB = "mztab";
  public static final String ARG_OUTPUTFILE = "outputfile";
  public static final String ARG_INPUTFILE = "inputfile";
  public static final String ARG_OUTPUTTFORMAT = "outputformat";
  public static final String ARG_CHROMSIZES = "chromsizes";
  public static final String ARG_REPORTFILE = "reportfile";
  public static final String ARG_SCHEMA_VALIDATION = "schema";
  public static final String ARG_SCHEMA_ONLY_VALIDATION = "schemaonly";
  public static final String ARG_FAST_VALIDATION = "fastvalidation";
  public static final String STRING_SEPARATOR = "##";

  /**
   * The supported file types.
   */
  public enum FileType {
    MZID("mzid"),
    MZTAB("mztab");

    private final String format;

    FileType(String format) {
        this.format = format;
    }

    public String toString() {
        return format;
    }
  }

  public enum ErrorCode {
      ERROR("Major structure error"),
      WARNING("Small error in the structure of the file");

      final String message;

      ErrorCode(String message ) {
        this.message = message;
      }

      public String toString() {
          return message;
      }

  }

  /**
   * Handles exiting unexpectedly from the tool.
   * @param e Caught exception during processing
   */
  public static void exitedUnexpectedly(Exception e) {
    log.error("Exception while processing files: ", e);
    System.exit(-1);
  }

  /**
   * Creates a new temporary file, as a copy of an input file. DeleteOnExit() is set.
   * @param file the source input file to copy from.
   * @return the new temporary file. This may be null if it was not created successfully.
   */
  public static File createNewTempFile(File file) {
    if(file != null) {
      log.info("Trying to creating File: " +  file.getAbsolutePath());
    }else{
      log.info("Creating File: NULL");
    }
    File tempFile = null;
    try {
      assert file != null;
      tempFile = new File (Files.createTempDir(), file.getName());
      File tempParentFile = tempFile.getParentFile();
      tempFile.deleteOnExit();
      tempParentFile.deleteOnExit();
      FileUtils.copyFile(file, tempFile);
    } catch (IOException e) {
      log.error("Problem creating temp fle for: " + file.getPath());
      log.error("Deleting temp file " + tempFile.getName() + ": " + tempFile.delete());
      tempFile = null;
    }
    return tempFile;
  }
}
